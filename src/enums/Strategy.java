package enums;

public enum Strategy {
    LIMIT,
    MARKET;

    public boolean isLimit() {return this == LIMIT;}
    public boolean isMarket() {return this == MARKET;}
}
