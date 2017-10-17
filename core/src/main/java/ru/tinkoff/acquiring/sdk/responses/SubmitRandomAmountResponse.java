package ru.tinkoff.acquiring.sdk.responses;

import com.google.gson.annotations.SerializedName;

/**
 * @author Vitaliy Markus
 */
public class SubmitRandomAmountResponse extends AcquiringResponse {

    @SerializedName("RequestKey")
    private String requestKey;

    @SerializedName("CustomerKey")
    private String customerKey;

    @SerializedName("CardId")
    private String cardId;

    @SerializedName("RebillId")
    private String rebillId;

    public String getRequestKey() {
        return requestKey;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public String getCardId() {
        return cardId;
    }

    public String getRebillId() {
        return rebillId;
    }
}
