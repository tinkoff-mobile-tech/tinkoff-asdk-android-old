package ru.tinkoff.acquiring.sdk.responses;

import com.google.gson.annotations.SerializedName;

/**
 * @author Vitaliy Markus
 */
public class AddCardResponse extends AcquiringResponse {

    @SerializedName("CustomerKey")
    private String customerKey;

    @SerializedName("PaymentURL")
    private String requestKey;

    public String getCustomerKey() {
        return customerKey;
    }

    public String getRequestKey() {
        return requestKey.replace("https://rest-api-test.tinkoff.ru/AddCard/","");
    }
}
