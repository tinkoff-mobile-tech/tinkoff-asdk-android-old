package ru.tinkoff.acquiring.sdk.requests;

import java.util.Map;

/**
 * @author Vitaliy Markus
 */
public class GetAddCardStateRequest extends AcquiringRequest {

    private String requestKey;

    public GetAddCardStateRequest() {
        super("GetAddCardState");
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = super.asMap();

        putIfNotNull(REQUEST_KEY, requestKey, map);

        return map;
    }

    public String getRequestKey() {
        return requestKey;
    }

    void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }
}
