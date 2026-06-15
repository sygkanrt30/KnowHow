package ru.tbank.shared.events;

public enum ReviewResult {
    APPROVE("approved"),
    REJECT("rejected");

    private final String value;

    ReviewResult(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
