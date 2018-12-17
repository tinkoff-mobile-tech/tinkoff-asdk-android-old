package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 *         Ставка налога.
 */
public enum Tax {

    /**
     * Без НДС
     */
    NONE("none"),

    /**
     * НДС по ставке 0%/
     */
    VAT_0("vat0"),

    /**
     * НДС чека по ставке 10%/
     */
    VAT_10("vat10"),

    /**
     * НДС чека по ставке 18%/
     */
    VAT_18("vat18"),

    /**
     * НДС чека по расчетной ставке 10/110.
     */
    VAT_110("vat110"),

    /**
     * НДС чека по расчетной ставке 18/118.
     */
    VAT_118("vat118"),

    /**
     * НДС чека по ставке 20%/
     */
    VAT_20("vat20"),

    /**
     * НДС чека по расчетной ставке 20/120
     */
    VAT_120("vat120");

    private final String tax;

    Tax(String tax) {
        this.tax = tax;
    }

    @Override
    public String toString() {
        return tax;
    }
}
