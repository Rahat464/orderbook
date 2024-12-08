package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BidTest {

    @Test
    void ensureLimitOrderIsNotMatchedAgainstHigherPrice() {
        Bid bid = new Bid(1, 1.0f, 1, Strategy.LIMIT);
        assertFalse(bid.match(new Ask(1, 1.5f, 1, Strategy.LIMIT)));
    }

    @Test
    void ensureLimitOrderIsMatchedAgainstEqualPrice() {
        Bid bid = new Bid(1, 1.0f, 1, Strategy.LIMIT);
        assertTrue(bid.match(new Ask(1, 1.0f, 1, Strategy.LIMIT)));
    }

    @Test
    void ensureOrderIsNotMatchedAgainstAnotherBid() {
        Bid bid = new Bid(1, 1.0f, 1, Strategy.LIMIT);
        assertFalse(bid.match(new Bid(1, 1.0f, 1, Strategy.LIMIT)));
    }

    @Test
    void ensureOrderIsMatchedAgainstMarketOrder() {
        Bid bid = new Bid(1, 1.0f, 1, Strategy.LIMIT);
        assertTrue(bid.match(new Ask(1, 1.0f, 1, Strategy.MARKET)));
    }
}