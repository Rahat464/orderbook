# Order Matching System

## Project Overview
This project is an order matching system that simulates a basic stock exchange. It supports both manual and automated order creation and matching.
The system uses a custom linked list implementation to manage orders and a `TreeMap` to store and match bids and asks efficiently.
It supports both limit and market orders, and provides basic logging and performance measurement features.

This has been developed using Java 22.0.1, and although not tested, should work with any recent version of Java.

## Setup Instructions
1. **Clone the Repository**:
    ```sh
    git clone https://github.com/Rahat464/orderbook.git
    cd orderbook
    ```

2. **Build the Project**:
    Ensure you have Java installed. Then, run:
    ```sh
    javac -d out src/**/*.java
    ```

3. **Run the Program**:
    ```sh
    java -cp out main.Main
    ```

## Project Structure
- **`src/main/Main.java`**: The entry point of the application. It provides options for manual and automated order creation and matching.
- **`src/entities/OrderBook.java`**: Manages the order book, including adding orders and matching them.
- **`src/entities/Order.java`**: Abstract class representing an order. Contains common properties and methods for orders.
- **`src/entities/Bid.java`**: Extends `Order` to represent a bid order.
- **`src/entities/Ask.java`**: Extends `Order` to represent an ask order.
- **`src/entities/OrderFactory.java`**: Factory class for creating orders, either randomly or based on user input.
- **`src/structures/LinkedList.java`**: Custom linked list implementation used to store orders.
- **`src/structures/Node.java`**: Represents a node in the custom linked list.
- **`src/enums/Type.java`**: Enum representing the type of order (BID or ASK).
- **`src/enums/Strategy.java`**: Enum representing the strategy of the order (LIMIT or MARKET).
- **`src/enums/Status.java`**: Enum representing the status of the order (ACTIVE, COMPLETED, CANCELLED).

## Usage Instructions
1. **Manual Mode**:
    - Run the program and select the manual mode.
    - Choose to prefill the order book or create your own orders.
    - Follow the prompts to enter order details.
    - The system will attempt to match orders and display the results.

2. **Automated Mode**:
    - Run the program and select the automated mode.
    - Enter the number of orders to be created.
    - The system will create random orders and match them automatically.

## Additional Information
- **Logging**: The system logs order matching activities and garbage collection events.
- **Performance**: The system includes a basic performance measurement for order matching.
- **Garbage Collection**: Inactive orders are periodically cleaned up to maintain performance.

Feel free to explore the code and modify it to suit your needs. Contributions are welcome!

Code in this repository is licensed under MIT License. See `LICENSE` for more information.
