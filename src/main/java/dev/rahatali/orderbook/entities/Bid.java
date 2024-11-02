package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;

public class Bid extends Order {
    // Market
    public Bid(int id, float price, int quantity, Strategy strategyType) {
        super(id, price, quantity, Type.BID, strategyType);
    }

    @Override
    public boolean matchLimitOrder(Order order) {
        return order.price <= price;
    }
}
