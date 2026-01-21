package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;
import dev.rahatali.orderbook.structures.IntArrayQueue;
import dev.rahatali.orderbook.structures.PriceLevel;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.api.stack.primitive.MutableIntStack;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.eclipse.collections.impl.stack.mutable.primitive.IntArrayStack;

import java.util.EmptyStackException;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class OrderBook {
    private static final Logger LOGGER = Logger.getLogger(OrderBook.class.getName());

    private final Order[] orderPool;
    private final MutableIntStack freeOrders;
    private MutableLongObjectMap<PriceLevel> askPriceLevels;
    private MutableLongObjectMap<PriceLevel> bidPriceLevels;
    private TreeMap<Long, PriceLevel> sortedAskPriceLevels;
    private TreeMap<Long, PriceLevel> sortedBidPriceLevels;
    private PriceLevel bestBidPriceLevel = null;
    private PriceLevel bestAskPriceLevel = null;

    private final int[] size = (new int[]{0, 0}); // [0] = ask, [1] = bid


    /**
     * Instantiate 10k objects and create a stack that keeps track of free orders
     */
    public OrderBook() {
        // todo: future improvement, use params from application.properties for pool size
        orderPool = new Order[10_000];
        this.freeOrders = IntArrayStack.newStackWith();

        for (int i = 0; i < orderPool.length; i++) {
            orderPool[i] = new Order();
            freeOrders.push(i);
        }

        this.askPriceLevels = new LongObjectHashMap<>();
        this.bidPriceLevels = new LongObjectHashMap<>();
        this.sortedAskPriceLevels = new TreeMap<>();
        this.sortedBidPriceLevels = new TreeMap<>();
    }

    /**
     * Gets the size of the order book.
     *
     * @param type the type of orders to get the size of (null for all orders, Type. ASK for ask orders, Type. BID for bid orders)
     * @return the size of the order book
     */
    public int getSize(Type type) {
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

    public void add(int id, long price, int quantity, Type type, Strategy strategy) {
        int freeOrderIndex;
        try {
            freeOrderIndex = freeOrders.pop();
        } catch (EmptyStackException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IllegalStateException("No new order objects available.");
        }

        Order order = orderPool[freeOrderIndex];
        order.modifyOrder(
                id,
                price,
                quantity,
                type,
                strategy
        );

        // todo: future improvement, precompute a range of price levels based on known price range
        MutableLongObjectMap<PriceLevel> priceLevelMap = getTypePriceLevel(type);
        PriceLevel priceLevel = priceLevelMap.get(price);

        if (priceLevel == null) {
            priceLevel = new PriceLevel(price);
            priceLevelMap.put(price, priceLevel);
            insertNewPriceLevel(type, priceLevel);
        }

        int queueIndex = priceLevel.enqueue(freeOrderIndex);
        order.setQueueIndex(queueIndex);
    }

    private MutableLongObjectMap<PriceLevel> getTypePriceLevel(Type type) {
        return type.isAsk() ? this.askPriceLevels : this.bidPriceLevels;
    }


    private void insertNewPriceLevel(Type t, PriceLevel newLevel) {
        TreeMap<Long, PriceLevel> sortedMap = t.isBid() ? this.sortedBidPriceLevels : this.sortedAskPriceLevels;
        sortedMap.put(newLevel.getPrice(), newLevel);

        Entry<Long, PriceLevel> prevEntry = sortedMap.lowerEntry(newLevel.getPrice());
        Entry<Long, PriceLevel> nextEntry = sortedMap.higherEntry(newLevel.getPrice());
        PriceLevel prev = (prevEntry != null) ? prevEntry.getValue() : null;
        PriceLevel next = (nextEntry != null) ? nextEntry.getValue() : null;

        newLevel.setPrev(prev);
        newLevel.setNext(next);

        if (prev != null) prev.setNext(newLevel);
        if (next != null) next.setPrev(newLevel);

        if (t.isBid()) {
            if (next == null) this.bestBidPriceLevel = newLevel;
        } else { // ASK
            if (prev == null) this.bestAskPriceLevel = newLevel;
        }
    }

    /**
     * Removes an order from the order book.
     *
     * @param orderIndex the order to be removed
     */
    private void remove(int orderIndex) {
        Order order = orderPool[orderIndex];

        if (order.getStatus().isComplete() || order.getStatus().isCancelled()) {
            setSize(order.getType(), getSize(order.getType()) - 1);
            order.reset();
            freeOrders.push(orderIndex);
        }
    }

    /**
     * Matches orders in the order book.
     * Edge Cases:
     * - If both the ask and bid order books are empty, it returns false.
     * - If the first order in the price level is not active, it continues to the next order.
     * - If a matching order is found, it executes the trade and returns true.
     * - If no matching order is found after iterating through the price level, it returns false.
     */
    public void match() {
        while (canMatch()) {
            tradeAtBestPrice();
            updateBestLevels();
        }
    }

    private void tradeAtBestPrice() {
        IntArrayQueue bidQueue = bestBidPriceLevel.getOrders();
        IntArrayQueue askQueue = bestAskPriceLevel.getOrders();

        while (!bidQueue.isEmpty() && !askQueue.isEmpty()) {
            Order bidOrder = orderPool[bidQueue.peek()];

            if (bidOrder.isActive()) {
                Order askOrder = orderPool[askQueue.peek()];
                if (askOrder.isActive()) {
                    // Both orders are active
                    executeTrade(bidOrder, askOrder);
                } else {
                    // Ask order is inactive, so we remove it
                    remove(askQueue.poll());
                }
            } else {
                // Bid order is inactive, so we remove it
                remove(bidQueue.poll());
            }
        }
    }

    private void updateBestLevels() {
        if (bestBidPriceLevel != null && bestBidPriceLevel.getOrders().isEmpty()) {
            PriceLevel exhaustedLevel = bestBidPriceLevel;
            this.bestBidPriceLevel = exhaustedLevel.getNext();
            removePriceLevel(Type.BID, exhaustedLevel);
        }

        if (bestAskPriceLevel != null && bestAskPriceLevel.getOrders().isEmpty()) {
            PriceLevel exhaustedLevel = bestAskPriceLevel;
            this.bestAskPriceLevel = exhaustedLevel.getNext();
            removePriceLevel(Type.ASK, exhaustedLevel);
        }
    }

    private boolean canMatch() {
        if (bestBidPriceLevel == null || bestAskPriceLevel == null) return false;
        return bestBidPriceLevel.getPrice() >= bestAskPriceLevel.getPrice();
    }

    private void executeTrade(Order bidOrder, Order askOrder) {
        int quantityTraded = Math.min(bidOrder.getQuantity(), askOrder.getQuantity());

        bidOrder.setQuantity(bidOrder.getQuantity() - quantityTraded);
        askOrder.setQuantity(askOrder.getQuantity() - quantityTraded);

        LOGGER.log(
                Level.INFO,
                "Trade Executed: Bid Order {0} and Ask Order {1} for {2} units.",
                new Object[]{bidOrder.getId(), askOrder.getId(), quantityTraded}
        );

        if (bidOrder.getQuantity() == 0) {
            remove(this.bestBidPriceLevel.getOrders().poll());
        }
        if (askOrder.getQuantity() == 0) {
            remove(this.bestAskPriceLevel.getOrders().poll());
        }
    }

    private void removePriceLevel(Type type, PriceLevel levelToRemove) {
        if (type.isBid()) {
            this.bidPriceLevels.remove(levelToRemove.getPrice());
            this.sortedBidPriceLevels.remove(levelToRemove.getPrice());
        } else { // ASK
            this.askPriceLevels.remove(levelToRemove.getPrice());
            this.sortedAskPriceLevels.remove(levelToRemove.getPrice());
        }

        PriceLevel prevLevel = levelToRemove.getPrev();
        PriceLevel nextLevel = levelToRemove.getNext();

        if (prevLevel != null) {
            prevLevel.setNext(nextLevel);
        }
        if (nextLevel != null) {
            nextLevel.setPrev(prevLevel);
        }
    }


    @Override
    public String toString() {
        final int priceScale = 10_000; // Assuming 4 decimal places. TODO: Make this a configurable constant.
        StringBuilder sb = new StringBuilder();

        sb.append("Order Book Size: ")
                .append(size[0] + size[1])
                .append("\n")
                .append("Asks:\n");

        this.sortedAskPriceLevels.values()
                .stream()
                .limit(5)
                .forEach(level -> sb.append(
                        formatOrder(level.getPrice(), level.getOrders().size(), "\u001B[32m", priceScale) // green
                )
        );

        if (bestBidPriceLevel != null && bestAskPriceLevel != null) {
            double spread = (bestAskPriceLevel.getPrice() - bestBidPriceLevel.getPrice()) / (double) priceScale;
            sb.append("Spread: ").append(String.format("%.4f", spread)).append("\n");
        } else {
            sb.append("Spread: N/A\n");
        }

        sb.append("Bids:\n");

        this.sortedBidPriceLevels.descendingMap()
                .values()
                .stream()
                .limit(5)
                .forEach(level -> sb.append(
                        formatOrder(level.getPrice(), level.getOrders().size(), "\u001B[31m", priceScale) // red
                )
        );

        return sb.toString();
    }

    private String formatOrder(long price, int quantity, String color, int priceScale) {
        double displayPrice = price / (double) priceScale;
        return String.format("%sPrice: %.4f, Total Orders: %d\u001B[0m%n", color, displayPrice, quantity);
    }
}
