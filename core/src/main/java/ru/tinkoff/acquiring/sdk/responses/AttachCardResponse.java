package ru.tinkoff.acquiring.sdk.responses;

import com.google.gson.annotations.SerializedName;

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
    private Status status;

    @SerializedName("ACSUrl")
    private String acsUrl;

    @SerializedName("MD")
    private String md;

    @SerializedName("PaReq")
    private String paReq;

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

    public Status getStatus() {
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

    public enum Status {

        NONE(""),
        THREE_DS_CHECKING("3DS_CHECKING"),
        LOOP_CHECKING("LOOP_CHECKING");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static Status fromString(String status) {
            if (status == null || status.isEmpty()) {
                return NONE;
            }
            if ("3DS_CHECKING".equals(status)) {
                return THREE_DS_CHECKING;
            }
            if ("LOOP_CHECKING".equals(status)) {
                return LOOP_CHECKING;
            }
            return NONE;
        }
    }
}
