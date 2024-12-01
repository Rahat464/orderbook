package dev.rahatali.orderbook.entities;

import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class OrderFactory {
    static Random r = new Random();
    private static int orderId = 0;

    // Geometric Brownian Motion parameters
    private static final float BASE_PRICE = 100.0f; // S0
    private static final float MIN_PRICE = 70.0f;
    private static final float MAX_PRICE = 130.0f;
    private static final float DRIFT = 0.01f;     // μ
    private static final float VOLATILITY = 0.02f; // σ
    private static final float DELTA_T = 1.0f;    // Time step
    private static float lastPrice = BASE_PRICE;

    private OrderFactory() {}

    // Uses random values to create an order
    // For automated order creation
    public static Order createOrder(){
        orderId++;

        float price = generatePrice();
        int quantity = r.nextInt(100) + 1;
        Type type = (r.nextInt(2) == 0) ? Type.BID : Type.ASK;
        Strategy strategyType = (r.nextInt(2) == 0) ? Strategy.MARKET : Strategy.LIMIT;

        if(type.isAsk()) return new Ask(orderId, price, quantity, strategyType);
        else return new Bid(orderId, price, quantity, strategyType);
    }

    // For user input
    public static Order createOrder(float price, int quantity, Type type, Strategy strategyType) {
        orderId++;
        if(type.isAsk()) return new Ask(orderId, price, quantity, strategyType);
        else return new Bid(orderId, price, quantity, strategyType);
    }

    /**
     * Generates a new price using Geometric Brownian Motion
     * S(t) = S(0) * e^((μ - 0.5 * σ^2)*t + σ * √t * Z)
     * @return new price
     */
    private static float generatePrice() {
        float randomComponent = (float) r.nextGaussian();

        float driftComponent = (DRIFT - 0.5f * VOLATILITY * VOLATILITY) * DELTA_T; // (μ - 0.5 * σ^2) * t
        float randomWalk = VOLATILITY * (float) Math.sqrt(DELTA_T) * randomComponent; // σ * √t * Z
        float newPrice = lastPrice * (float) Math.exp(driftComponent + randomWalk);

        newPrice = BigDecimal. valueOf(newPrice).setScale(2, RoundingMode.HALF_UP).floatValue();
        lastPrice = Math.clamp(newPrice, MIN_PRICE, MAX_PRICE);

        return newPrice;
    }

}
