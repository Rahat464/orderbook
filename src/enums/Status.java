package enums;

public enum Status {
    ACTIVE,
    COMPLETED,
    CANCELLED;

    public boolean isActive() {return this == ACTIVE;}
    public boolean isCompleted() {return this == COMPLETED;}
}