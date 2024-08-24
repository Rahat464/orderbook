package enums;

public enum Strategy {
    LIMIT,
    MARKET;

    public boolean isMarket() {return this == MARKET;}
}
