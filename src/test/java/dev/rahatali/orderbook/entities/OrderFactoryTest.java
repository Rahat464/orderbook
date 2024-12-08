package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderFactoryTest {

    @Test
    void ensureAskIsInstantiated() {
        Order order = OrderFactory.createOrder(100.0f, 10, Type.ASK, Strategy.LIMIT);
        assertInstanceOf(Ask.class, order);
    }

    @Test
    void ensureBidIsInstantiated() {
        Order order = OrderFactory.createOrder(100.0f, 10, Type.BID, Strategy.LIMIT);
        assertInstanceOf(Bid.class, order);
    }

    @Test
    void ensurePriceIsGenerated() {
        final int MAX_PRICE = 130;
        final int MIN_PRICE = 70;

        for (int i = 0; i < 100; i++) {
            float price = OrderFactory.createOrder().price;
            assertTrue(price >= MIN_PRICE && price <= MAX_PRICE);
        }
    }
}