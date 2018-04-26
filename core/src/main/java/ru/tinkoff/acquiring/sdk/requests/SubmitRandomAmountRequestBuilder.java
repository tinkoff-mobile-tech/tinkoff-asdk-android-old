package ru.tinkoff.acquiring.sdk.requests;

/**
 * @author Vitaliy Markus
 */
public class SubmitRandomAmountRequestBuilder extends AcquiringRequestBuilder<SubmitRandomAmountRequest> {

    private final SubmitRandomAmountRequest request = new SubmitRandomAmountRequest();

    public SubmitRandomAmountRequestBuilder(String password, String terminalKey) {
        super(password, terminalKey);
    }

    /**
     * @param requestKey RequestKey, полученный при запросе AddCard {@link AddCardRequestBuilder}
     */
    public SubmitRandomAmountRequestBuilder setRequestKey(final String requestKey) {
        request.setRequestKey(requestKey);
        return this;
    }

    /**
     * @param amount Забронированная сумма в копейках
     */
    public SubmitRandomAmountRequestBuilder setAmount(final Long amount) {
        request.setAmount(amount);
        return this;
    }

    @Override
    protected SubmitRandomAmountRequest getRequest() {
        return request;
    }

    @Override
    protected void validate() {
        validateNonNull(request.getRequestKey(), AcquiringRequest.REQUEST_KEY);
        validateZeroOrPositive(request.getAmount(), AcquiringRequest.AMOUNT);
    }
}
