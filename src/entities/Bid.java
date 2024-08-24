package entities;

import enums.Type;
import enums.Strategy;

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
