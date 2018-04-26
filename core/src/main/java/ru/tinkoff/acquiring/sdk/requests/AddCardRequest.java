package ru.tinkoff.acquiring.sdk.requests;

import java.util.Map;

/**
 * @author Vitaliy Markus
 */
final public class AddCardRequest extends AcquiringRequest {

    private String customerKey;
    private String checkType;

    public AddCardRequest() {
        super("AddCard");
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = super.asMap();

        putIfNotNull(CUSTOMER_KEY, customerKey, map);
        putIfNotNull(CHECK_TYPE, checkType, map);

        return map;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public String getCheckType() {
        return checkType;
    }

    void setCheckType(String checkType) {
        this.checkType = checkType;
    }
}
