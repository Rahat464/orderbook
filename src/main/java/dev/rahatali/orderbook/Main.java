package dev.rahatali.orderbook;

import dev.rahatali.orderbook.entities.OrderBook;
import dev.rahatali.orderbook.entities.OrderFactory;
import dev.rahatali.orderbook.enums.Strategy;
import dev.rahatali.orderbook.enums.Type;

import java.util.Scanner;

public class Main {
    private static final int PRICE_SCALE = 2;
    private static final long INITIAL_PRICE = (long) (150.00 * Math.pow(10, PRICE_SCALE));

    private static final OrderBook orderBook = new OrderBook();
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final OrderFactory orderFactory = new OrderFactory(
            INITIAL_PRICE,
            0.0005,
            0.01,
            PRICE_SCALE
    );

    public static void main(String[] args) {
        try {
            menu();
        } finally {
            SCANNER.close();
        }
    }


    public static void menu() {
        out(""" 
                :'#######::'########::'########::'########:'########::'########:::'#######:::'#######::'##:::'##:
                '##.... ##: ##.... ##: ##.... ##: ##.....:: ##.... ##: ##.... ##:'##.... ##:'##.... ##: ##::'##::
                 ##:::: ##: ##:::: ##: ##:::: ##: ##::::::: ##:::: ##: ##:::: ##: ##:::: ##: ##:::: ##: ##:'##:::
                 ##:::: ##: ########:: ##:::: ##: ######::: ########:: ########:: ##:::: ##: ##:::: ##: #####::::
                 ##:::: ##: ##.. ##::: ##:::: ##: ##...:::: ##.. ##::: ##.... ##: ##:::: ##: ##:::: ##: ##. ##:::
                 ##:::: ##: ##::. ##:: ##:::: ##: ##::::::: ##::. ##:: ##:::: ##: ##:::: ##: ##:::: ##: ##:. ##::
                . #######:: ##:::. ##: ########:: ########: ##:::. ##: ########::. #######::. #######:: ##::. ##:
                :.......:::..:::::..::........:::........::..:::::..::........::::.......::::.......:::..::::..::\s
                """);

        while (true) { // NOSONAR - Infinite loop required for user input
            out(orderBook.toString());
            out("1. Automated \n2. Manual \n3. Benchmark \n4. Quit");
            int input = SCANNER.nextInt();

            switch (input) {
                case 1 -> auto();
                case 2 -> manual();
                case 3 -> benchmark();
                case 4 -> {
                    out("Goodbye!");
                    System.exit(0);
                }
                default -> out("Invalid input. Please try again.");
            }
        }
    }

    private static void manual() {
        out("Would you like to prefill the OrderBook (1) or create your own order (2)?");
        int input = SCANNER.nextInt();
        if (input == 1) {
            out("Prefilling with 1,000 orders...");
            for (int i = 0; i < 1000; i++) {
                OrderFactory.OrderParams params = orderFactory.createOrder();
                orderBook.add(params.id(), params.price(), params.quantity(), params.type(), params.strategy());
            }
        } else {
            createOrder();
        }
        out(orderBook.toString());

        int initialSize = orderBook.getSize(null);
        long startTime = System.nanoTime();
        orderBook.match();
        long endTime = System.nanoTime();
        int finalSize = orderBook.getSize(null);

        boolean matchOccurred = finalSize < initialSize;
        out("Order matching " + (matchOccurred ? "completed" : "failed"));
        long timeTakenNs = endTime - startTime;
        double timeTakenMs = timeTakenNs / 1_000_000.0;
        out("Time taken to run matching engine: " + timeTakenNs + "ns (" + timeTakenMs + "ms)");
    }

    private static void createOrder() {
        out("Enter the price: ");
        float price = SCANNER.nextFloat();
        out("Enter the quantity: ");
        int quantity = SCANNER.nextInt();
        out("Enter the type (1. BID, 2. ASK): ");
        int typeInput = SCANNER.nextInt();
        out("Enter the strategy (1. MARKET, 2. LIMIT): ");
        int strategyInput = SCANNER.nextInt();

        long longPrice = (long) (price * Math.pow(10, PRICE_SCALE));
        Type type = typeInput == 1 ? Type.BID : Type.ASK;
        Strategy strategy = strategyInput == 1 ? Strategy.MARKET : Strategy.LIMIT;

        OrderFactory.OrderParams params = orderFactory.createOrder(longPrice, quantity, type, strategy);
        orderBook.add(params.id(), params.price(), params.quantity(), params.type(), params.strategy());
        out("Order " + params.id() + " created.");
    }

    private static void auto() {
        out("Enter the number of orders to be created: ");
        int numOfOrders = SCANNER.nextInt();

        for (int i = 0; i < numOfOrders; i++) {
            OrderFactory.OrderParams params = orderFactory.createOrder();
            orderBook.add(params.id(), params.price(), params.quantity(), params.type(), params.strategy());
        }

        out("Orders added. Matching until the book is clear...");
        int initialSize;
        do {
            initialSize = orderBook.getSize(null);
            orderBook.match();
        } while (orderBook.getSize(null) < initialSize);

        out("Auto matching complete.");
    }

    private static void benchmark() {
        long startTime;
        long endTime;
        final int numOrders = 100_000;

        // Time taken to create 100K orders
        startTime = System.nanoTime();
        for (int i = 0; i < numOrders; i++) {
            OrderFactory.OrderParams params = orderFactory.createOrder();
            orderBook.add(params.id(), params.price(), params.quantity(), params.type(), params.strategy());
        }
        endTime = System.nanoTime();
        String result = generateBenchmarkMessage(startTime, endTime, "create", numOrders);

        // Time taken to match all possible orders
        startTime = System.nanoTime();
        int initialSize = orderBook.getSize(null);
        int currentSize;
        do {
            currentSize = orderBook.getSize(null);
            orderBook.match();
        } while (orderBook.getSize(null) < currentSize);
        endTime = System.nanoTime();
        int ordersProcessed = initialSize - orderBook.getSize(null);
        result += generateBenchmarkMessage(startTime, endTime, "match", ordersProcessed);

        out(result);
    }

    private static String generateBenchmarkMessage(long startTime, long endTime, String message, int amount) {
        double timeTaken = (endTime - startTime) / 1_000_000.0;
        return ("Time taken to " + message + " " + String.format("%,d", amount) + " orders: " + timeTaken + "ms.\n");
    }

    private static void out(String message) {
        System.out.println(message); // NOSONAR
    }
}