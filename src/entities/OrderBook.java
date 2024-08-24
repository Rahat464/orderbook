package entities;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import enums.Type;
import structures.*;

public class OrderBook {
    private final LinkedList orders;
    private final TreeMap<Float, LinkedList> ask;
    private final TreeMap<Float, LinkedList> bid;

    private ScheduledExecutorService scheduler;
    private static final Logger LOGGER = Logger.getLogger(OrderBook.class.getName());
    Iterator<Order> iterator;

    // Default constructor
    public OrderBook() {
        orders = new LinkedList();
        ask = new TreeMap<>(Comparator.reverseOrder());
        bid = new TreeMap<>();

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanupInactiveOrders, 1, 1, TimeUnit.MINUTES);
    }

    // null - All orders
    // Type.ASK - All ask orders
    // Type.BID - All bid orders
    public boolean isEmpty(Type type) {
        if (type == null) return orders.isEmpty();
        return type.isAsk() ? ask.isEmpty() : bid.isEmpty();
    }

    public void add(Order order) {
        final TreeMap<Float, LinkedList> map = order.isAsk() ? ask : bid;

        orders.add(order);
        if (map.containsKey(order.price)) {
            map.get(order.price).add(order);
        } else {
            LinkedList list = new LinkedList();
            list.add(order);
            map.put(order.price, list);
        }
        log("Order added: " + order);
    }

    public boolean match() {
        Node current = orders.getHead();

        while (current != null) {
            Order order = current.getVal();
            if (order.isActive()) {
                Order matchingOrder = findMatchingOrder(order);
                if (matchingOrder != null) {
                    executeOrder(order, matchingOrder);
                    return true;
                }
            }
            current = current.getNext();
        }
        return false;
    }

    private Order findMatchingOrder(Order order) {
        final TreeMap<Float, LinkedList> map = order.isAsk() ? bid : ask;

        if (order.isMarket()) return findMatchingMarketOrder(map, order);
        else return findMatchingLimitOrder(map, order);
    }

    private Order findMatchingMarketOrder(TreeMap<Float, LinkedList> map, Order order) {
        for (LinkedList list : map.values()) {
            Node current = list.getHead();
            while (current != null) {
                Order matchingOrder = current.getVal();
                if (matchingOrder.isActive() && order.match(matchingOrder)) {
                    return matchingOrder;
                }
                current = current.getNext();
            }
        }
        return null;
    }

    private Order findMatchingLimitOrder(TreeMap<Float, LinkedList> map, Order order) {
        for (Map.Entry<Float, LinkedList> entry : map.entrySet()) {
            if ((order.isAsk() && entry.getKey() <= order.price) || (order.isBid() && entry.getKey() >= order.price)) {
                Node current = entry.getValue().getHead();
                while (current != null) {
                    Order matchingOrder = current.getVal();
                    if (matchingOrder.isActive() && order.match(matchingOrder)) {
                        return matchingOrder;
                    }
                    current = current.getNext();
                }
            }
        }
        return null;
    }

    private void executeOrder(Order order1, Order order2) {
        int quantityTraded = Math.min(order1.getQuantity(), order2.getQuantity());
        order1.setQuantity(order1.getQuantity() - quantityTraded);
        order2.setQuantity(order2.getQuantity() - quantityTraded);

        log(String.format("Order %d traded with Order %d for %d units at price %.2f",
                order1.getId(), order2.getId(), quantityTraded, order2.price)
        );
    }

    // Garbage Collection
    // Orders are not removed immediately as it will take O(n) time
    // Instead, we occasionally scan through the list and remove the completed orders all in one go
    private void cleanupInactiveOrders() {
        int startSize = orders.size();
        log("Garbage Collection Started");

        Node current = orders.getHead();
        while (current != null) {
            Order order = current.getVal();
            if (!order.isActive()) {
                Node tmp = current;
                current = current.getPrev();

                final TreeMap<Float, LinkedList> map = order.isAsk() ? ask : bid;
                orders.remove(tmp);
                LinkedList list = map.get(order.price);
                list.remove(tmp);
            }
            current = current.getNext();
        }

        log("Garbage Collection Finished:" + (startSize - orders.size()) + " removed");
    }

    private void log(String message) {
        if (LOGGER.isLoggable(Level.INFO)) LOGGER.info(message);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Asks:\n");
        ask.entrySet().stream().limit(5).forEach(entry ->
            sb.append(formatOrder(entry.getKey(), entry.getValue().size(), "\u001B[32m")) // Green color
        );

        sb.append("Bids:\n");
        bid.entrySet().stream().limit(5).forEach(entry ->
            sb.append(formatOrder(entry.getKey(), entry.getValue().size(), "\u001B[31m")) // Red color
        );

        // Calculate disparity
        Float highestBid = bid.isEmpty() ? null : bid.firstKey();
        Float lowestAsk = ask.isEmpty() ? null : ask.firstKey();
        if (highestBid != null && lowestAsk != null) {
            float disparity = lowestAsk - highestBid;
            sb.append(String.format("Disparity: %.2f%n", disparity));
        } else {
            sb.append("Disparity: N/A\n");
        }

        return sb.toString();
    }

    private String formatOrder(float price, int quantity, String color) {
        return String.format("%sPrice: %.2f, Quantity: %d\u001B[0m%n", color, price, quantity);
    }
}
