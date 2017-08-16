package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 */
public enum Taxation {

    OSN("osn"),
    USN_INCOME("usn_income"),
    USN_INCOME_OUTCOME("usn_income_outcome"),
    ENVD("envd"),
    ESN("esn"),
    PATENT("patent");

    private final String taxation;

    Taxation(String taxation) {
        this.taxation = taxation;
    }

    @Override
    public String toString() {
        return taxation;
    }
}
