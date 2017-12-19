package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 */
public enum CheckType {

    /**
     * Привязка без проверки
     */
    NO("NO"),

    /**
     * Привязка с блокировкой в 1 руб
     */
    HOLD("HOLD"),

    /**
     * Привязка с 3DS
     */
    THREE_DS("3DS"),

    /**
     * Привязка с 3DS и блокировкой маленькой суммы до 2 руб
     */
    THREE_DS_HOLD("3DSHOLD");

    private final String checkType;

    CheckType(String checkType) {
        this.checkType = checkType;
    }

    @Override
    public String toString() {
        return checkType;
    }
}
