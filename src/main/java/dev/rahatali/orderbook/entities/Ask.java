package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;

import java.math.BigDecimal;

public class Ask extends Order {
    // Market
    public Ask(int id, BigDecimal price, int quantity, Strategy strategyType) {
        super(id, price, quantity, Type.ASK, strategyType);
    }

    @Override
    public boolean matchLimitOrder(Order order) {
        return order.price.compareTo(price) >= 0;
    }
}
