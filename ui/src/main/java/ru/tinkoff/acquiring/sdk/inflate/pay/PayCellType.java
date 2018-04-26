package ru.tinkoff.acquiring.sdk.inflate.pay;

/**
 * @author Vitaliy Markus
 */
public enum PayCellType {
    PRODUCT_TITLE,
    PRODUCT_DESCRIPTION,
    AMOUNT,
    PAYMENT_CARD_REQUISITES,
    EMAIL,
    PAY_BUTTON,
    SECURE_LOGOS,
    EMPTY_FLEXIBLE_SPACE,
    EMPTY_16DP,
    EMPTY_8DP;

    public static int[] toIntArray(PayCellType... types) {
        int[] array = new int[types.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = types[i].ordinal();
        }
        return array;
    }

    public static PayCellType[] toPayCellTypeArray(int[] types) {
        PayCellType[] values = PayCellType.values();
        PayCellType[] array = new PayCellType[types.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = values[types[i]];
        }
        return array;
    }
}
