package entities;

import enums.Strategy;
import enums.Type;

import java.util.Random;

public class OrderFactory {
    static Random r = new Random();
    private static int orderId = 0;

    private OrderFactory() {}

    // Uses random values to create an order
    // For automated order creation
    public static Order createOrder(){
        increment();

        float price = generatePrice();
        int quantity = r.nextInt(100) + 1;
        Type type = (r.nextInt(2) == 0) ? Type.BID : Type.ASK;
        Strategy strategyType = (r.nextInt(2) == 0) ? Strategy.MARKET : Strategy.LIMIT;

        if(type.isAsk()) return new Ask(orderId, price, quantity, strategyType);
        else return new Bid(orderId, price, quantity, strategyType);
    }

    // For user input
    public static Order createOrder(float price, int quantity, Type type, Strategy strategyType) {
        increment();
        if(type.isAsk()) return new Ask(orderId, price, quantity, strategyType);
        else return new Bid(orderId, price, quantity, strategyType);
    }

    public static int getOrderId() {return orderId;}
    public static void increment() {orderId++;}

    // Generate random price using Gaussian distribution
    private static float generatePrice() {
        float price = (float) r.nextGaussian() * 100 + 100;
        price = (float) Math.round(price * 100) / 100;
        return price < 0 ? -price : price;
    }

}
