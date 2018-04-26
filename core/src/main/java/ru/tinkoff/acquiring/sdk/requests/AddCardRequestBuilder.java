package ru.tinkoff.acquiring.sdk.requests;

import ru.tinkoff.acquiring.sdk.CheckType;

/**
 * @author Vitaliy Markus
 */

final public class AddCardRequestBuilder extends AcquiringRequestBuilder<AddCardRequest> {

    private final AddCardRequest request = new AddCardRequest();

    /**
     * Билдер для запроса AddCardRequest
     *
     * @param password    Пароль. Выдается банком на каждый магазин.
     * @param terminalKey Уникальный идентификатор терминала. Выдается банком на каждый магазин.
     */
    public AddCardRequestBuilder(final String password, final String terminalKey) {
        super(password, terminalKey);
    }

    /**
     * @param customerKey Идентификатор покупателя в системе Продавца, к которому привязаны карты.
     */
    public AddCardRequestBuilder setCustomerKey(final String customerKey) {
        request.setCustomerKey(customerKey);
        return this;
    }

    /**
     * @param checkType Тип проверки при привязки карты. {@link CheckType}
     */
    public AddCardRequestBuilder setCheckType(final CheckType checkType) {
        return setCheckType(checkType.toString());
    }

    /**
     * @param checkType Тип проверки при привязки карты. {@link CheckType}
     */
    public AddCardRequestBuilder setCheckType(final String checkType) {
        request.setCheckType(checkType);
        return this;
    }

    @Override
    protected AddCardRequest getRequest() {
        return request;
    }

    @Override
    protected void validate() {
        validateNonEmpty(request.getCustomerKey(), AcquiringRequest.CUSTOMER_KEY);
        validateNonNull(request.getCheckType(), AcquiringRequest.CHECK_TYPE);
    }
}
