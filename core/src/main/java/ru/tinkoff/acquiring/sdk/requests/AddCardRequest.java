package ru.tinkoff.acquiring.sdk.requests;

import java.util.Map;

import ru.tinkoff.acquiring.sdk.CheckType;

/**
 * @author Vitaliy Markus
 */
final public class AddCardRequest extends AcquiringRequest {

    private String customerKey;
    private CheckType checkType;

    public AddCardRequest() {
        super("AddCard");
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = super.asMap();

        putIfNotNull(CUSTOMER_KEY, customerKey, map);
        putIfNotNull(CHECK_TYPE, checkType.toString(), map);

        return map;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public CheckType getCheckType() {
        return checkType;
    }

    void setCheckType(CheckType checkType) {
        this.checkType = checkType;
    }
}
