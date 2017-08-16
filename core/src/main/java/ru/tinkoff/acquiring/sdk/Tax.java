package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 */
public enum Tax {

    NONE("none"),
    VAT_0("vat0"),
    VAT_10("vat10"),
    VAT_18("vat18"),
    VAT_110("vat110"),
    VAT_118("vat118");

    private final String tax;

    Tax(String tax) {
        this.tax = tax;
    }

    @Override
    public String toString() {
        return tax;
    }
}
