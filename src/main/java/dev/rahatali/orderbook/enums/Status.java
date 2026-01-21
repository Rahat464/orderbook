package dev.rahatali.orderbook.enums;

public enum Status {
    ACTIVE,
    COMPLETED,
    CANCELLED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isComplete() {
        return this == COMPLETED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }
}