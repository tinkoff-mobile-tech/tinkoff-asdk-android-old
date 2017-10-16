package ru.tinkoff.acquiring.sdk.responses;

import com.google.gson.annotations.SerializedName;

import ru.tinkoff.acquiring.sdk.PaymentStatus;

/**
 * @author Vitaliy Markus
 */

public class GetAddCardStateResponse extends AcquiringResponse {

    @SerializedName("RequestKey")
    private String requestKey;

    @SerializedName("Status")
    private PaymentStatus status;

    @SerializedName("CardId")
    private String cardId;

    @SerializedName("RebillId")
    private String rebillId;

    public String getRequestKey() {
        return requestKey;
    }

    public PaymentStatus getStatus() {
        return status;
    }

}
