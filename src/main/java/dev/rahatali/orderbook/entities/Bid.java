package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;

import java.math.BigDecimal;

public class Bid extends Order {
    // Market
    public Bid(int id, BigDecimal price, int quantity, Strategy strategyType) {
        super(id, price, quantity, Type.BID, strategyType);
    }

    @Override
    public boolean matchLimitOrder(Order order) {
        return order.price.compareTo(price) <= 0;
    }
}
