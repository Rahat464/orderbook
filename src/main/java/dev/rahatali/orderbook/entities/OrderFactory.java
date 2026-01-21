package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderFactory {
    private static final AtomicInteger ORDER_ID_GENERATOR = new AtomicInteger(1);
    private static final Random RANDOM = new Random();

    private final double drift;
    private final double volatility;
    private long lastPrice;
    private final int priceScale; // Number of decimal places for the price

    public OrderFactory() {
        this.priceScale = 2;
        double scaleFactor = Math.pow(10, this.priceScale);
        this.lastPrice = (long) (150.00 * scaleFactor);
        this.drift = 0.0005;
        this.volatility = 0.01;
    }

    public OrderFactory(long initialPrice, double drift, double volatility, int priceScale) {
        this.lastPrice = initialPrice;
        this.drift = drift;
        this.volatility = volatility;
        this.priceScale = priceScale;
    }

    // Random order for simulations
    public OrderParams createOrder() {
        int id = ORDER_ID_GENERATOR.getAndIncrement();

        long price = generateNextPrice();
        int quantity = RANDOM.nextInt(100) + 1; // 1..100
        Type type = RANDOM.nextBoolean() ? Type.ASK : Type.BID;
        Strategy strategy = RANDOM.nextBoolean() ? Strategy.LIMIT : Strategy.MARKET;
        return new OrderParams(id, price, quantity, type, strategy);
    }

    // Explicit order creation
    public OrderParams createOrder(long price, int quantity, Type type, Strategy strategy) {
        int id = ORDER_ID_GENERATOR.getAndIncrement();
        return new OrderParams(id, price, quantity, type, strategy);
    }

    /**
     * Generates a new price using Geometric Brownian Motion with long for precision.
     * @return The next price in the sequence.
     */
    private long generateNextPrice() {
        // 1. Define the scaling factor based on the number of decimal places.
        double scaleFactor = Math.pow(10, this.priceScale);

        // 2. De-scale the last price from a long to a double for calculation.
        double lastPriceAsDouble = this.lastPrice / scaleFactor;

        // 3. Perform the Geometric Brownian Motion calculation purely with doubles.
        // S(t) = S(t-1) * exp( (μ - 0.5 * σ^2)Δt + σ * ε * sqrt(Δt) )
        // Using Δt = 1 for simplicity.
        double gaussian = RANDOM.nextGaussian();
        double driftTerm = this.drift - (0.5 * this.volatility * this.volatility);
        double randomWalkTerm = this.volatility * gaussian;
        double exponent = driftTerm + randomWalkTerm;
        double priceMovementMultiplier = Math.exp(exponent);

        double newPriceAsDouble = lastPriceAsDouble * priceMovementMultiplier;
        this.lastPrice = Math.round(newPriceAsDouble * scaleFactor);
        return this.lastPrice;
    }

    // Simple record to hold order parameters instead of creating full Order objects.
    public record OrderParams(int id, long price, int quantity, Type type, Strategy strategy) {
    }
}
