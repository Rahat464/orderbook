package dev.rahatali.orderbook.structures;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceLevel {
    // A queue of indices into the main Order[] pool
    public final IntArrayQueue orders;
    private final long price;
    private PriceLevel prev;
    private PriceLevel next;

    public PriceLevel(long price) {
        this.price = price;
        this.orders = new IntArrayQueue();
    }

    public final int enqueue(int orderIndex) {
        return orders.add(orderIndex);
    }
}
