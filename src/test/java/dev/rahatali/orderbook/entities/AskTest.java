package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AskTest {

    // Order tests
    @Test
    void ensureNegativeQuantityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Ask(1, BigDecimal.ONE, -1, Strategy.LIMIT));
    }

    @Test
    void ensureOrderIsCompletedWhenQuantityIsZero() {
        Ask ask = new Ask(1, BigDecimal.ONE, 1, Strategy.LIMIT);
        ask.setQuantity(0);
        assertFalse(ask.isActive());
    }

    @Test
    void ensureLimitOrderIsNotMatchedAgainstLowerPrice() {
        Ask ask = new Ask(1, BigDecimal.ONE, 1, Strategy.LIMIT);
        assertFalse(ask.matchLimitOrder(new Bid(1, BigDecimal.valueOf(0.5), 1, Strategy.LIMIT)));
    }

    @Test
    void ensureLimitOrderIsMatchedAgainstEqualPrice() {
        Ask ask = new Ask(1, BigDecimal.ONE, 1, Strategy.LIMIT);
        assertTrue(ask.matchLimitOrder(new Bid(1, BigDecimal.ONE, 1, Strategy.LIMIT)));
    }

    @Test
    void ensureOrderIsNotMatchedAgainstAnotherAsk() {
        Ask ask = new Ask(1, BigDecimal.ONE, 1, Strategy.LIMIT);
        assertFalse(ask.match(new Ask(1, BigDecimal.ONE, 1, Strategy.LIMIT)));
    }

    @Test
    void ensureOrderIsMatchedAgainstMarketOrder() {
        Ask ask = new Ask(1, BigDecimal.ONE, 1, Strategy.LIMIT);
        assertTrue(ask.match(new Bid(1, BigDecimal.ONE, 1, Strategy.MARKET)));
    }
}