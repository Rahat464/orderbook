package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderFactory {
    private static final AtomicInteger ORDER_ID_GENERATOR = new AtomicInteger(1);
    private static final Random RANDOM = new Random();

    private final BigDecimal drift;
    private final BigDecimal volatility;
    private final int priceScale; // Number of decimal places for the price
    private BigDecimal lastPrice;

    public OrderFactory(BigDecimal initialPrice, BigDecimal drift, BigDecimal volatility, int priceScale) {
        this.lastPrice = initialPrice;
        this.drift = drift;
        this.volatility = volatility;
        this.priceScale = priceScale;
    }

    // Random order for simulations
    public Order createOrder() {
        int id = ORDER_ID_GENERATOR.getAndIncrement();
        BigDecimal price = generateNextPrice();
        int quantity = RANDOM.nextInt(100) + 1; // 1..100
        Type type = RANDOM.nextBoolean() ? Type.ASK : Type.BID;
        Strategy strategy = RANDOM.nextBoolean() ? Strategy.LIMIT : Strategy.MARKET;
        return type.isAsk() ? new Ask(id, price, quantity, strategy) : new Bid(id, price, quantity, strategy);
    }

    // Explicit order creation with BigDecimal
    public Order createOrder(BigDecimal price, int quantity, Type type, Strategy strategy) {
        int id = ORDER_ID_GENERATOR.getAndIncrement();
        return type.isAsk() ? new Ask(id, price, quantity, strategy) : new Bid(id, price, quantity, strategy);
    }

    // Backward-compatible overload used by Main (float price)
    public Order createOrder(float price, int quantity, Type type, Strategy strategy) {
        return createOrder(BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP), quantity, type, strategy);
    }

    /**
     * Generates a new price using Geometric Brownian Motion with BigDecimal for precision.
     * Note: This is more complex than with floats, but it is correct.
     * @return The next price in the sequence.
     */
    private BigDecimal generateNextPrice() {
        // S(t) = S(t-1) * exp( (μ - 0.5 * σ^2)Δt + σ * ε * sqrt(Δt) )
        // Using Δt = 1 for simplicity.

        // Standard Gaussian random number
        double gaussian = RANDOM.nextGaussian();
        BigDecimal randomComponent = BigDecimal.valueOf(gaussian);

        // Term 1: (μ - 0.5 * σ^2)
        BigDecimal driftTerm = drift.subtract(volatility.pow(2).divide(BigDecimal.valueOf(2), priceScale + 5, RoundingMode.HALF_UP));

        // Term 2: σ * ε
        BigDecimal randomWalkTerm = volatility.multiply(randomComponent);

        // Exponent: term1 + term2
        BigDecimal exponent = driftTerm.add(randomWalkTerm);

        // newPrice = lastPrice * e^(exponent)
        // Math.exp() takes a double, so we do the final step using doubles but with high precision from BigDecimal
        BigDecimal priceMovement = BigDecimal.valueOf(Math.exp(exponent.doubleValue()));
        BigDecimal newPrice = lastPrice.multiply(priceMovement);

        // Clamp and set scale
        // newPrice = newPrice.max(MIN_PRICE).min(MAX_PRICE); // Assuming MIN/MAX are BigDecimals
        newPrice = newPrice.setScale(priceScale, RoundingMode.HALF_UP);

        this.lastPrice = newPrice;
        return newPrice;
    }
}
