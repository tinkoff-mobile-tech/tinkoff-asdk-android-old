package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 *         Система налогообложения.
 */
public enum Taxation {

    /**
     * Общая СН.
     */
    OSN("osn"),

    /**
     * Упрощенная СН (доходы).
     */
    USN_INCOME("usn_income"),

    /**
     * Упрощенная СН (доходы минус расходы).
     */
    USN_INCOME_OUTCOME("usn_income_outcome"),

    /**
     * Единый налог на вмененный доход.
     */
    ENVD("envd"),

    /**
     * Единый сельскохозяйственный налог.
     */
    ESN("esn"),

    /**
     * Патентная СН.
     */
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
