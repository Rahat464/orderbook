package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookTest {

    @Test
    void testIsEmpty() {
        // Test with empty order book
        OrderBook orderBook = new OrderBook();
        assertTrue(orderBook.isEmpty(null));

        // Test with order book with bids
        orderBook.add(new Bid(1, BigDecimal.valueOf(100), 10, Strategy.LIMIT));
        assertFalse(orderBook.isEmpty(Type.BID));

        // Test with order book with asks
        orderBook.add(new Ask(1, BigDecimal.valueOf(100), 10, Strategy.LIMIT));
        assertFalse(orderBook.isEmpty(Type.ASK));

        // Test with order book with bids and asks
        assertFalse(orderBook.isEmpty(null));
    }

    @Test
    void testMatchWithTwoOrders() {
        OrderBook orderBook = new OrderBook();
        orderBook.add(new Bid(1, BigDecimal.valueOf(100), 10, Strategy.LIMIT));
        orderBook.add(new Ask(2, BigDecimal.valueOf(100), 10, Strategy.LIMIT));
        assertTrue(orderBook.match());
        System.out.println(orderBook);
        assertTrue(orderBook.isEmpty(null));
    }
}