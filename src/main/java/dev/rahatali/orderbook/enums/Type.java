package dev.rahatali.orderbook.enums;

public enum Type {
    ASK,
    BID;

    public boolean isBid() {return this == BID;}
    public boolean isAsk() {return this == ASK;}
}
