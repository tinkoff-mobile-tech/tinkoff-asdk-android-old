package ru.tinkoff.acquiring.sdk.requests;

import java.util.Map;

/**
 * @author Vitaliy Markus
 */
final public class SubmitRandomAmountRequest extends AcquiringRequest {

    private String requestKey;
    private Long amount;

    public SubmitRandomAmountRequest() {
        super("SubmitRandomAmount");
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = super.asMap();

        putIfNotNull(REQUEST_KEY, requestKey, map);
        putIfNotNull(AMOUNT, amount, map);
        
        return map;
    }

    public String getRequestKey() {
        return requestKey;
    }

    void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public Long getAmount() {
        return amount;
    }

    void setAmount(Long amount) {
        this.amount = amount;
    }
}
