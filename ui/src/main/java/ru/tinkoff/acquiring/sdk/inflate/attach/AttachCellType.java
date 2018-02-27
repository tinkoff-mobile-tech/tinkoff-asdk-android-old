package ru.tinkoff.acquiring.sdk.inflate.attach;

/**
 * @author Vitaliy Markus
 */
public enum AttachCellType {
    TITLE,
    DESCRIPTION,
    PAYMENT_CARD_REQUISITES,
    EMAIL,
    ATTACH_BUTTON,
    SECURE_LOGOS,
    EMPTY_FLEXIBLE_SPACE,
    EMPTY_16DP,
    EMPTY_8DP;

    public static int[] toIntArray(AttachCellType... types) {
        int[] array = new int[types.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = types[i].ordinal();
        }
        return array;
    }

    public static AttachCellType[] toPayCellTypeArray(int[] types) {
        AttachCellType[] values = AttachCellType.values();
        AttachCellType[] array = new AttachCellType[types.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = values[types[i]];
        }
        return array;
    }
}
