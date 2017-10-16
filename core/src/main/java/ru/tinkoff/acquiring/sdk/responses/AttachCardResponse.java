package ru.tinkoff.acquiring.sdk.responses;

import com.google.gson.annotations.SerializedName;

import ru.tinkoff.acquiring.sdk.AcquiringSdkException;
import ru.tinkoff.acquiring.sdk.PaymentStatus;
import ru.tinkoff.acquiring.sdk.ThreeDsData;

/**
 * @author Vitaliy Markus
 */

public class AttachCardResponse extends AcquiringResponse {

    @SerializedName("RequestKey")
    private String requestKey;

    @SerializedName("CustomerKey")
    private String customerKey;

    @SerializedName("CardId")
    private String cardId;

    @SerializedName("RebillId")
    private String rebillId;

    @SerializedName("Status")
    private PaymentStatus status;

    @SerializedName("ACSUrl")
    private String acsUrl;

    @SerializedName("MD")
    private String md;

    @SerializedName("PaReq")
    private String paReq;

    private transient ThreeDsData threeDsData;

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

    public PaymentStatus getStatus() {
        return status;
    }

    public String getAcsUrl() {
        return acsUrl;
    }

    public String getMd() {
        return md;
    }

    public String getPaReq() {
        return paReq;
    }

    public ThreeDsData getThreeDsData() {
        if (threeDsData == null) {
            if (status == PaymentStatus.THREE_DS_CHECKING) {
                threeDsData = new ThreeDsData(requestKey, acsUrl, md, paReq);
            } else {
                threeDsData = ThreeDsData.EMPTY_THREE_DS_DATA;
            }
        }
        return threeDsData;
    }
}
