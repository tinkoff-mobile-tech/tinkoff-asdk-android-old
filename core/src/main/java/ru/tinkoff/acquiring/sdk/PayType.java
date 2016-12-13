package ru.tinkoff.acquiring.sdk;

/**
 * @author Alex Maksakov
 */
public enum PayType {
    ONE_STEP("O"), TWO_STEP("T");

    private final String value;

    PayType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
