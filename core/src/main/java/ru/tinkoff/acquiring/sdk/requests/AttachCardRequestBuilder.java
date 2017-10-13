package ru.tinkoff.acquiring.sdk.requests;

import java.security.PublicKey;
import java.util.Map;

import ru.tinkoff.acquiring.sdk.AcquiringSdk;

/**
 * @author Vitaliy Markus
 */

final public class AttachCardRequestBuilder extends AcquiringRequestBuilder<AttachCardRequest> {

    private final AttachCardRequest request = new AttachCardRequest();

    /**
     * Билдер для запроса AttachCardRequest
     *
     * @param password    Пароль. Выдается банком на каждый магазин.
     * @param terminalKey Уникальный идентификатор терминала. Выдается банком на каждый магазин.
     */
    public AttachCardRequestBuilder(String password, String terminalKey) {
        super(password, terminalKey);
    }

    /**
     * @param cardData Данные карты, преобразованные методом {@link ru.tinkoff.acquiring.sdk.CardData#encode(PublicKey)},
     *                 где ключ получается {@link AcquiringSdk#getPublicKey()}
     */
    public AttachCardRequestBuilder setCardData(final String cardData) {
        request.setCardData(cardData);
        return this;
    }

    /**
     * @param requestKey RequestKey, полученный при запросе AddCard {@link AddCardRequestBuilder}
     */
    public AttachCardRequestBuilder setRequestKey(final String requestKey) {
        request.setRequestKey(requestKey);
        return this;
    }

    /**
     * @param email Email для отправки
     */
    public AttachCardRequestBuilder setEmail(final String email) {
        request.setEmail(email);
        return this;
    }

    /**
     * @param data Объект содержащий дополнительные параметры в виде “ключ”:”значение”. Данные параметры будут переданы на страницу оплаты (в случае ее кастомизации). Максимальная длина для каждого передаваемого параметра:
     *             Ключ – 20 знаков,
     *             Значение – 100 знаков.
     *             Максимальное количество пар «ключ-значение» не может превышать 20.
     */
    public AttachCardRequestBuilder setData(final Map<String, String> data) {
        request.setData(data);
        return this;
    }

    @Override
    protected AttachCardRequest getRequest() {
        return request;
    }

    @Override
    protected void validate() {
        validateNonEmpty(request.getCardData(), AcquiringRequest.CARD_DATA);
        validateNonEmpty(request.getRequestKey(), AcquiringRequest.REQUEST_KEY);
    }
}
