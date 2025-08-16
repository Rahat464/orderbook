package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BidTest {

    @Test
    void ensureLimitOrderIsNotMatchedAgainstHigherPrice() {
        Bid bid = new Bid(1, BigDecimal.ONE, 1, Strategy.LIMIT);
        assertFalse(bid.match(new Ask(1, BigDecimal.valueOf(1.5), 1, Strategy.LIMIT)));
    }

    @Test
    void ensureLimitOrderIsMatchedAgainstEqualPrice() {
        Bid bid = new Bid(1, BigDecimal.ONE, 1, Strategy.LIMIT);
        assertTrue(bid.match(new Ask(1, BigDecimal.ONE, 1, Strategy.LIMIT)));
    }

    @Test
    void ensureOrderIsNotMatchedAgainstAnotherBid() {
        Bid bid = new Bid(1, BigDecimal.ONE, 1, Strategy.LIMIT);
        assertFalse(bid.match(new Bid(1, BigDecimal.ONE, 1, Strategy.LIMIT)));
    }

    @Test
    void ensureOrderIsMatchedAgainstMarketOrder() {
        Bid bid = new Bid(1, BigDecimal.ONE, 1, Strategy.LIMIT);
        assertTrue(bid.match(new Ask(1, BigDecimal.ONE, 1, Strategy.MARKET)));
    }
}