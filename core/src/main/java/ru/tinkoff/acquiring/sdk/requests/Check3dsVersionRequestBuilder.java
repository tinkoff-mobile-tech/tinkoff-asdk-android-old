package ru.tinkoff.acquiring.sdk.requests;

import java.security.PublicKey;

/**
 * @author Mariya Chernyadieva
 */
public class Check3dsVersionRequestBuilder extends AcquiringRequestBuilder<Check3dsVersionRequest> {

    private Check3dsVersionRequest request = new Check3dsVersionRequest();

    /**
     * Билдер для запроса Check3dsVersion
     *
     * @param password    Пароль. Выдается банком на каждый магазин.
     * @param terminalKey Уникальный идентификатор терминала. Выдается банком на каждый магазин.
     */
    public Check3dsVersionRequestBuilder(final String password, final String terminalKey) {
        super(password, terminalKey);
    }

    /**
     * @param value Уникальный идентификатор транзакции в системе Банка, полученный в ответе на вызов метода Init
     */
    public Check3dsVersionRequestBuilder setPaymentId(final Long value) {
        request.setPaymentId(value);
        return this;
    }

    /**
     * @param value Данные карты, преобразованные методом {@link ru.tinkoff.acquiring.sdk.CardData#encode(PublicKey)},
     *              где ключ получается {@link ru.tinkoff.acquiring.sdk.AcquiringSdk#getPublicKey()}
     */
    public Check3dsVersionRequestBuilder setCardData(final String value) {
        request.setCardData(value);
        return this;
    }

    @Override
    protected Check3dsVersionRequest getRequest() {
        return request;
    }

    @Override
    protected void validate() {
        validateZeroOrPositive(request.getPaymentId(), "Payment ID");
        validateNonEmpty(request.getCardData(), "Card data");
    }
}
