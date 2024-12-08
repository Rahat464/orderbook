package dev.rahatali.orderbook.enums;

public enum Strategy {
    LIMIT,
    MARKET;

    public boolean isMarket() {
        return this == MARKET;
    }

    public boolean isLimit() {
        return this == LIMIT;
    }
}
