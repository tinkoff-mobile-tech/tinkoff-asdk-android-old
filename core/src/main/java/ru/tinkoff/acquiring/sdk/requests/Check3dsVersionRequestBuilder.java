package ru.tinkoff.acquiring.sdk.requests;

/** TODO doc
 * @author Mariya Chernyadieva
 */
public class Check3dsVersionRequestBuilder extends AcquiringRequestBuilder<Check3dsVersionRequest> {

    private Check3dsVersionRequest request = new Check3dsVersionRequest();

    public Check3dsVersionRequestBuilder(final String password, final String terminalKey) {
        super(password, terminalKey);
    }

    public Check3dsVersionRequestBuilder setPaymentId(final Long value) {
        request.setPaymentId(value);
        return this;
    }

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
