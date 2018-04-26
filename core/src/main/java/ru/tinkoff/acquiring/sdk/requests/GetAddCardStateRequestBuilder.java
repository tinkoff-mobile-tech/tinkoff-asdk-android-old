package ru.tinkoff.acquiring.sdk.requests;

/**
 * @author Vitaliy Markus
 */
public class GetAddCardStateRequestBuilder extends AcquiringRequestBuilder<GetAddCardStateRequest> {

    private final GetAddCardStateRequest request = new GetAddCardStateRequest();

    /**
     * Билдер для запроса GetAddCardStateRequest
     *
     * @param password    Пароль. Выдается банком на каждый магазин.
     * @param terminalKey Уникальный идентификатор терминала. Выдается банком на каждый магазин.
     */
    public GetAddCardStateRequestBuilder(String password, String terminalKey) {
        super(password, terminalKey);
    }

    /**
     * @param requestKey RequestKey, полученный при запросе AddCard {@link AddCardRequestBuilder}
     */
    public GetAddCardStateRequestBuilder setRequestKey(final String requestKey) {
        request.setRequestKey(requestKey);
        return this;
    }

    @Override
    protected GetAddCardStateRequest getRequest() {
        return request;
    }

    @Override
    protected void validate() {
        validateNonEmpty(request.getRequestKey(), AcquiringRequest.REQUEST_KEY);
    }
}
