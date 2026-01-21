package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Status;
import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {
    private int id;
    private long price;
    private Type type;
    private Strategy strategyType;
    private int quantity;
    private Status status = Status.CANCELLED;
    private long timestamp;
    private int queueIndex;

    /*
    Instantiate empty order object
    * */
    protected Order() {
    }

    protected void modifyOrder(int id, long price, int quantity, Type type, Strategy strategyType) {
        if (id < 0 || price <= 0 || quantity < 0)
            throw new IllegalArgumentException("Invalid arguments");

        this.id = id;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
        this.strategyType = strategyType;
        this.timestamp = System.currentTimeMillis();
        this.status = Status.ACTIVE;
    }

    public final void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantity = quantity;
        if (this.quantity == 0) complete();
    }

    public final boolean isActive() {
        return status.isActive();
    }

    private void complete() {
        status = Status.COMPLETED;
    }

    public void reset() {
        this.id = 0;
        this.price = 0;
        this.quantity = 0;
        this.type = null;
        this.strategyType = null;
        this.status = Status.CANCELLED;
        this.timestamp = 0;
        this.queueIndex = 0;
    }

    @Override
    public final String toString() {
        return String.format(
                "Order [id=%d, price=%d, quantity=%d, type=%s, status=%s, strategyType=%s]",
                id, price, quantity, type, status, strategyType
        );
    }
}
