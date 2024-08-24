package main;

import entities.Order;
import entities.OrderBook;
import entities.OrderFactory;
import enums.Strategy;
import enums.Type;

import java.util.Scanner;

public class Main {
    private static final OrderBook orderBook = new OrderBook();
    private static final Scanner SCANNER = new Scanner(System.in);


    public static void main(String[] args) {
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

        int input = 0;
        while (true){ // NOSONAR
            out("1. Automated \n2. Manual \n3. Quit");
            input = SCANNER.nextInt();

            switch (input) {
                case 1 -> auto();
                case 2 -> manual();
                case 3-> {
                    out("Goodbye!");
                    System.exit(0);
                }
                default -> out("Invalid input. Please try again.");
            }
        }
    }

    private static void manual(){
        out("Would you like to prefill the OrderBook (1) or create your own order (2)?");
        int input = SCANNER.nextInt();
        if (input == 1) {
            for (int i = 0; i < 1000; i++) {
                orderBook.add(OrderFactory.createOrder());
            }
        } else createOrder();

        long startTime = System.nanoTime();
        final boolean match = orderBook.match();
        long endTime = System.nanoTime();

        out("Order matching " + (match ? "completed" : "failed"));
        long timeTakenNs = endTime - startTime;
        double timeTakenMs = timeTakenNs / 1_000_000.0;
        out("Time taken to match order: " + timeTakenNs + "ns (" + timeTakenMs + "ms)");
    }

    private static void createOrder(){
        out("Enter the price: ");
        float price = SCANNER.nextFloat();
        out("Enter the quantity: ");
        int quantity = SCANNER.nextInt();
        out("Enter the type (1. BID, 2. ASK): ");
        int type = SCANNER.nextInt();
        out("Enter the strategy (1. MARKET, 2. LIMIT): ");
        int strategy = SCANNER.nextInt();

        Order order = OrderFactory.createOrder(
                price,
                quantity,
                type == 1 ? Type.BID : Type.ASK,
                strategy == 1 ? Strategy.MARKET : Strategy.LIMIT
        );
        orderBook.add(order);
        out("Order " + order.id + " created.");
    }

    private static void auto(){

        out("Enter the number of orders to be created: ");
        int numOfOrders = SCANNER.nextInt();

        for (int i = 0; i < numOfOrders; i++) {
            orderBook.add(OrderFactory.createOrder());
        }

        // Match all orders
        while (!orderBook.isEmpty(null)) {
            orderBook.match();
        }
    }

    private static void out(String message){
        System.out.println(message); // NOSONAR
    }
}