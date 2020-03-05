package ru.tinkoff.acquiring.sdk.requests;

import java.util.Map;

/**
 * @author Mariya Chernyadieva
 */
public class Check3dsVersionRequest extends AcquiringRequest {

    private Long paymentId;
    private String cardData;

    Check3dsVersionRequest() {
        super("Check3dsVersion");
    }

    @Override
    public Map<String, Object> asMap() {
        final Map<String, Object> map = super.asMap();

        putIfNotNull(PAYMENT_ID, paymentId.toString(), map);
        putIfNotNull(CARD_DATA, cardData, map);

        return map;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getCardData() {
        return cardData;
    }

    public void setCardData(String cardData) {
        this.cardData = cardData;
    }
}
