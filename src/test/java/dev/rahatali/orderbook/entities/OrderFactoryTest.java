package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class OrderFactoryTest {

    private static final OrderFactory ORDER_FACTORY = new OrderFactory(
            BigDecimal.valueOf(150.00),
            BigDecimal.valueOf(0.0005),
            BigDecimal.valueOf(0.01),
            2
    );

    @Test
    void ensureAskIsInstantiated() {
        Order order = ORDER_FACTORY.createOrder(BigDecimal.valueOf(100), 10, Type.ASK, Strategy.LIMIT);
        assertInstanceOf(Ask.class, order);
    }

    @Test
    void ensureBidIsInstantiated() {
        Order order = ORDER_FACTORY.createOrder(BigDecimal.valueOf(100), 10, Type.BID, Strategy.LIMIT);
        assertInstanceOf(Bid.class, order);
    }
}