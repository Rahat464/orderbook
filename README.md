# Order Matching System

## Project Overview
This project is an order matching system that simulates a basic stock exchange. It supports both manual and automated order creation and matching.
The system uses a `LinkedList` to manage orders and a `TreeMap` to store and match bids and asks efficiently.
It supports both limit and market orders, and provides basic logging and performance measurement features.

This has been developed using Java 22.0.1, and although not tested, should work with any recent version of Java.

## Setup Instructions
1. **Clone the Repository**:
    ```sh
    git clone https://github.com/Rahat464/orderbook.git
    cd orderbook
    ```

2. **Build the Project**:
   Ensure you have Java and Maven installed. Then, run:
    ```sh
    mvn clean install
    ```

3. **Run the Program**:
    ```sh
    mvn exec:java
    ```

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