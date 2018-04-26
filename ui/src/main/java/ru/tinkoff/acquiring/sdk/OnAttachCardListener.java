package ru.tinkoff.acquiring.sdk;

/**
 * Коллбек позволяющий получать уведомления о статусе привязки карты
 *
 * @author Vitaliy Markus
 */
public interface OnAttachCardListener {

    /**
     * Вызывается в случае, если карта успешно привязана
     * @param cardId
     */
    void onSuccess(String cardId);

    /**
     * Вызывается, если привязка карьы была отменена пользователем
     */
    void onCancelled();

    /**
     * Вызывается, если не удалось привязать карту
     */
    void onError(Exception e);
}
