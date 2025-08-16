package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Type;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.math.RoundingMode.HALF_UP;

/**
 * Represents an order book that stores ask and bid orders.
 * The order book is implemented as a TreeMap with the price as the key and a list of orders as the value.
 * The TreeMap is sorted in descending order for ask orders and ascending order for bid orders.
 * The order book also has a scheduled task that periodically cleans up inactive orders.
 */
public class OrderBook {
    public static final int CLEANUP_INTERVAL = 5;
    private static final Logger LOGGER = Logger.getLogger(OrderBook.class.getName());

    private final TreeMap<BigDecimal, LinkedList<Order>> ask;
    private final TreeMap<BigDecimal, LinkedList<Order>> bid;
    private final int[] size = (new int[]{0, 0}); // [0] = ask, [1] = bid
    private boolean preferAsk = true;
    ScheduledExecutorService scheduler;

    public OrderBook() {
        ask = new TreeMap<>(Comparator.reverseOrder());
        bid = new TreeMap<>();

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanupInactiveOrders, CLEANUP_INTERVAL, CLEANUP_INTERVAL, TimeUnit.MINUTES);
    }

    /**
     * Gets the size of the order book.
     *
     * @param type the type of orders to get the size of (null for all orders, Type. ASK for ask orders, Type. BID for bid orders)
     * @return the size of the order book
     */
    private int getSize(Type type) {
        if (type == null) {
            return size[0] + size[1];
        }
        return type.isAsk() ? size[0] : size[1];
    }

    /**
     * Sets the size of the order book.
     *
     * @param type the type of orders to set the size of (null for all orders, Type. ASK for ask orders, Type. BID for bid orders)
     * @param size the size of the order book
     */
    private void setSize(Type type, int size) {
        if (type == null) {
            this.size[0] = size;
            this.size[1] = size;
            return;
        }
        int mapIndex = type.isAsk() ? 0 : 1;
        this.size[mapIndex] = size;
    }

    /**
     * Checks if the order book is empty.
     *
     * @param type the type of orders to check (null for all orders, Type. ASK for ask orders, Type. BID for bid orders)
     * @return true if the specified type of orders is empty, false otherwise
     */
    public boolean isEmpty(Type type) {
        if (type == null) {
            return ask.isEmpty() || bid.isEmpty() ||
                    ask.values().stream().allMatch(List::isEmpty) ||
                    bid.values().stream().allMatch(List::isEmpty);
        }
        TreeMap<BigDecimal, LinkedList<Order>> map = type.isAsk() ? ask : bid;
        return map.isEmpty() || map.values().stream().allMatch(List::isEmpty);
    }

    /**
     * Adds an order to the order book.
     *
     * @param order the order to be added
     */
    public synchronized void add(Order order) {
        final TreeMap<BigDecimal, LinkedList<Order>> map = order.isAsk() ? ask : bid;
        map.computeIfAbsent(order.price, x -> new LinkedList<>()).add(order);
        setSize(order.type, getSize(order.type) + 1);
        LOGGER.log(Level.INFO, "Order added: {0}", order);
    }

    /**
     * Removes an order from the order book.
     *
     * @param order the order to be removed
     */
    private synchronized void remove(BigDecimal price, Order order) {
        final TreeMap<BigDecimal, LinkedList<Order>> map = order.isAsk() ? ask : bid;
        LinkedList<Order> priceLevel = map.get(price);
        priceLevel.remove(order);
        if (priceLevel.isEmpty()) map.remove(price);
        setSize(order.type, getSize(order.type) - 1);
    }

    /**
     * Matches orders in the order book.
     * Edge Cases:
     * - If both the ask and bid order books are empty, it returns false.
     * - If the first order in the price level is not active, it continues to the next order.
     * - If a matching order is found, it executes the trade and returns true.
     * - If no matching order is found after iterating through the price level, it returns false.
     *
     * @return true if a matching order is found and executed, false otherwise
     */
    public synchronized boolean match() {
        if (isEmpty(null)) {
            LOGGER.log(Level.INFO, "Matching failed. Order book is empty");
            return false;
        }
        final TreeMap<BigDecimal, LinkedList<Order>> map = preferAsk ? ask : bid;

        Map.Entry<BigDecimal, LinkedList<Order>> entry = removeEmptyPriceLevels(map);
        if (entry == null) {
            LOGGER.log(Level.INFO, "Matching failed. No active orders found");
            return false;
        }

        boolean matchFound = processPriceLevel(entry.getValue());
        if (matchFound) {
            preferAsk = !preferAsk;
        }
        return matchFound;
    }

    /**
     * Removes empty price levels from the order book.
     *
     * @param map the map of orders to remove empty price levels from
     * @return the first non-empty price level, or null if no non-empty price levels are found
     */
    private Map.Entry<BigDecimal, LinkedList<Order>> removeEmptyPriceLevels(TreeMap<BigDecimal, LinkedList<Order>> map) {
        while (!map.isEmpty()) {
            Map.Entry<BigDecimal, LinkedList<Order>> entry = map.firstEntry();
            if (entry != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                return entry;
            }
            map.pollFirstEntry();
        }
        return null;
    }

    /**
     * Processes a price level by iterating through the orders and finding matching orders.
     *
     * @param priceLevel the price level to process
     * @return true if a matching order is found and executed, false otherwise
     */
    private boolean processPriceLevel(LinkedList<Order> priceLevel) {
        final Iterator<Order> iterator = priceLevel.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (!order.isActive()) {
                remove(order.price, order);
                iterator.remove();
            } else if (isOrderPriceWithinLimit(order)) {
                Order matchingOrder = findMatchingOrder(order);
                if (matchingOrder != null) {
                    LOGGER.log(Level.INFO, "Matching order found: {0}", matchingOrder);
                    executeOrder(order, matchingOrder);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Finds a matching order for the given order.
     * Edge Cases:
     * - If the order book is empty, it returns null.
     * - If no active orders are found, it returns null.
     * - If the order is a market order, it will match with the first active order found.
     * - If the order is a limit order, it will match with the first active order within the price constraints.
     *
     * @param order the order to find a match for
     * @return the matching order, or null if no match is found
     */
    private Order findMatchingOrder(Order order) {
        final TreeMap<BigDecimal, LinkedList<Order>> map = order.isAsk() ? bid : ask;

        LOGGER.log(Level.INFO, "Finding matching order for: {0}", order);
        final Order matchingOrder =
                order.isMarket() ?
                        findMatchingMarketOrder(map, order) :
                        findMatchingLimitOrder(map, order);

        if (matchingOrder == null) {
            LOGGER.info("No matching order found.");
        }
        return matchingOrder;
    }

    /**
     * Finds a matching market order for the given order.
     * Edge Cases:
     * - If the order book is empty, it returns null.
     * - If no active orders are found, it returns null.
     * - If the order is a market order, it will match with the first active order found.
     *
     * @param map   the map of orders to search
     * @param order the order to find a match for
     * @return the matching order, or null if no match is found
     */
    private Order findMatchingMarketOrder(TreeMap<BigDecimal, LinkedList<Order>> map, Order order) {
        for (LinkedList<Order> priceLevel : map.values()) {
            for (Order matchingOrder : priceLevel) {
                if (matchingOrder.isActive() && order.match(matchingOrder)) return matchingOrder;
            }
        }
        return null;
    }

    /**
     * Finds a matching limit order for the given order.
     * Edge Cases:
     * - If the order book is empty, it returns null.
     * - If no active orders are found within the price constraints, it returns null.
     * - If the order is a limit order, it will match with the first active order within the price constraints.
     * - If the first order in the price level does not match, it continues to the next order without removing it.
     *
     * @param map   the map of orders to search
     * @param order the order to find a match for
     * @return the matching order, or null if no match is found
     */
    private Order findMatchingLimitOrder(TreeMap<BigDecimal, LinkedList<Order>> map, Order order) {
        for (Map.Entry<BigDecimal, LinkedList<Order>> key : map.entrySet()) {
            BigDecimal price = key.getKey();
            LinkedList<Order> priceLevel = key.getValue();

            if ((order.isAsk() && price.compareTo(order.price) > 0) || (order.isBid() && price.compareTo(order.price) < 0))
                continue;

            for (Order matchingOrder : priceLevel) {
                if (matchingOrder.isActive() && order.match(matchingOrder)) return matchingOrder;
            }
        }
        return null;
    }

    /**
     * Checks if the price of a limit order is within the valid range.
     *
     * @param order the order to check
     * @return true if the order price is within the valid range, false otherwise
     */
    private boolean isOrderPriceWithinLimit(Order order) {
        final TreeMap<BigDecimal, LinkedList<Order>> map = order.isAsk() ? bid : ask;
        if (map.isEmpty()) return false;
        final BigDecimal bestPrice = map.firstKey();

        if ((order.isAsk() && order.price.compareTo(bestPrice) >= 0 || (order.isBid() && order.price.compareTo(bestPrice) <= 0))) {
            LOGGER.log(Level.INFO, "Limit order within range: {0}", order);
            return true;
        } else {
            LOGGER.log(Level.FINE, "Order {0} price {1} out of range for best price {2}",
                    new Object[]{order, order.price, bestPrice});
            return false;
        }
    }

    /**
     * Executes a trade between two orders.
     * This method updates the quantities of both orders based on the minimum quantity
     * between the two orders. If the quantity of either order reaches 0, it will be marked as completed by the Order.
     *
     * @param order1 the first order involved in the trade
     * @param order2 the second order involved in the trade
     */
    private void executeOrder(Order order1, Order order2) {
        int quantityTraded = Math.min(order1.getQuantity(), order2.getQuantity());
        order1.setQuantity(order1.getQuantity() - quantityTraded);
        order2.setQuantity(order2.getQuantity() - quantityTraded);

        if (order1.getQuantity() == 0) {
            remove(order1.price, order1);
            setSize(order1.type, getSize(order1.type) - 1);
        }
        if (order2.getQuantity() == 0) {
            remove(order2.price, order2);
            setSize(order2.type, getSize(order2.type) - 1);
        }

        LOGGER.log(
                Level.INFO,
                "Order {0} traded with Order {1} for {2} units at price {3}",
                new Object[]{order1.getId(), order2.getId(), quantityTraded, order2.price}
        );
    }

    /**
     * Periodically scans through the order book and removes completed orders.
     * This method is scheduled to run at fixed intervals.
     */
    private void cleanupInactiveOrders() {
        LOGGER.info("Garbage Collection Started");

        int ordersRemoved = 0;
        ordersRemoved += cleanupInactiveOrders(ask);
        ordersRemoved += cleanupInactiveOrders(bid);

        LOGGER.log(Level.INFO, "Garbage Collection Finished: {0} removed", ordersRemoved);
    }

    /**
     * Scans through the given map of orders and removes inactive orders or empty price levels.
     *
     * @param map the map of orders to clean up
     * @return the number of orders removed
     */
    private int cleanupInactiveOrders(TreeMap<BigDecimal, LinkedList<Order>> map) {
        int ordersRemoved = 0;

        Iterator<Map.Entry<BigDecimal, LinkedList<Order>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BigDecimal, LinkedList<Order>> entry = iterator.next();
            LinkedList<Order> priceLevel = entry.getValue();

            // Remove all inactive orders in price level
            for (Order order : priceLevel) {
                if (!order.isActive()) {
                    setSize(order.type, getSize(order.type) - 1);
                    remove(entry.getKey(), order);
                    ordersRemoved++;
                }
            }

            if (priceLevel.isEmpty()) {
                iterator.remove();
            }
        }

        return ordersRemoved;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Order Book Size: ").append(ask.size() + bid.size()).append("\n");

        sb.append("Asks:\n");
        ask.entrySet().stream().limit(5).forEach(entry ->
                sb.append(formatOrder(entry.getKey(), entry.getValue().size(), "\u001B[32m")) // Green color
        );

        BigDecimal highestBid = bid.isEmpty() ? null : bid.firstKey();
        BigDecimal lowestAsk = ask.isEmpty() ? null : ask.firstKey();
        if (highestBid != null && lowestAsk != null) {
            BigDecimal disparity = lowestAsk.subtract(highestBid);
            sb.append("Disparity: ")
                    .append(disparity.setScale(2, HALF_UP).toPlainString())
                    .append('\n');
        } else {
            sb.append("Disparity: N/A\n");
        }

        sb.append("Bids:\n");
        bid.entrySet().stream().limit(5).forEach(entry ->
                sb.append(formatOrder(entry.getKey(), entry.getValue().size(), "\u001B[31m")) // Red color
        );

        return sb.toString();
    }

    private String formatOrder(BigDecimal price, int quantity, String color) {
        return String.format("%sPrice: %s, Quantity: %d\u001B[0m%n", color, price.setScale(2, HALF_UP).toPlainString(), quantity);
    }
}
