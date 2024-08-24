package entities;

import enums.*;

public abstract class Order {
    // Public as these are read-only
    public final int id; // Upto ~2.1 billion orders
    public final float price;
    public final Type type;
    public final Strategy strategyType;
    public final long timestamp;
    private int quantity;
    private Status status = Status.ACTIVE;


    protected Order(int id, float price, int quantity, Type type, Strategy strategyType) {
        this.id = id;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
        this.strategyType = strategyType;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public int getId() {return id;}

    public boolean isBid() {return type.isBid();}
    public boolean isAsk() {return type.isAsk();}
    public boolean isMarket() {return strategyType.isMarket();}

    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantity = quantity;
        if(this.quantity == 0) complete();
    }

    public boolean isActive() {return status.isActive();}
    private void complete() {status = Status.COMPLETED;}

    public boolean match(Order order){
        if(order.type == this.type) return false;
        return strategyType == Strategy.MARKET || matchLimitOrder(order);
    }
    protected abstract boolean matchLimitOrder(Order order);
    // We do not need a matchMarketOrder method as it will always return true
    // The only condition that would need to be checked is if the order is a Bid or Ask
    // Though this is already checked in the match method

    @Override
    public String toString() {
        return "Order [id=" + id + ", price=" + price + ", quantity=" + quantity + ", type=" + type + ", status=" + status + ", strategyType=" + strategyType + "]";
    }
}
