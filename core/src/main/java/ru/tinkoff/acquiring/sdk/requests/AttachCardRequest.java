package ru.tinkoff.acquiring.sdk.requests;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vitaliy Markus
 */
final public class AttachCardRequest extends AcquiringRequest {

    private String cardData;
    private String requestKey;
    private String email;
    private Map<String, String> data;

    public AttachCardRequest() {
        super("AttachCard");
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = super.asMap();

        putIfNotNull(CARD_DATA, cardData, map);
        putIfNotNull(REQUEST_KEY, requestKey, map);
        putDataIfNonNull(map);

        return map;
    }

    public String getCardData() {
        return cardData;
    }

    void setCardData(String cardData) {
        this.cardData = cardData;
    }

    public String getRequestKey() {
        return requestKey;
    }

    void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    public Map<String, String> getData() {
        return data;
    }

    void setData(Map<String, String> data) {
        this.data = data;
    }

    void addData(Map<String, String> data) {
        if (data != null) {
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.putAll(data);
        }
    }

    private void putDataIfNonNull(Map<String, Object> map) {
        if (data == null && (email == null || email.length() == 0)) {
            return;
        }

        HashMap<String, String> dataMap = new HashMap<>();
        if (data != null) {
            dataMap.putAll(data);
        }
        dataMap.put(DATA_KEY_EMAIL, email);
        map.put(DATA, dataMap);
    }
}
