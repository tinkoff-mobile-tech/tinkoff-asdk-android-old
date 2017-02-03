/*
 * Copyright © 2016 Tinkoff Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.acquiring.sdk.responses;

import com.google.gson.annotations.SerializedName;

import ru.tinkoff.acquiring.sdk.AcquiringSdkException;
import ru.tinkoff.acquiring.sdk.PaymentStatus;
import ru.tinkoff.acquiring.sdk.ThreeDsData;

/**
 * @author Mikhail Artemyev
 */
final public class FinishAuthorizeResponse extends AcquiringResponse {

    @SerializedName("PaymentId")
    private Long paymentId;

    @SerializedName("OrderId")
    private String orderId;

    @SerializedName("Amount")
    private Long amount;

    @SerializedName("ACSUrl")
    private String acsUrl;

    @SerializedName("MD")
    private String md;

    @SerializedName("PaReq")
    private String paReq;

    @SerializedName("Status")
    private PaymentStatus status;

    private transient ThreeDsData threeDsData;

    public ThreeDsData getThreeDsData() {
        if (threeDsData == null) {
            if (status == PaymentStatus.CONFIRMED || status == PaymentStatus.AUTHORIZED) {
                threeDsData = ThreeDsData.EMPTY_THREE_DS_DATA;
            } else if (status == PaymentStatus.THREE_DS_CHECKING) {
                threeDsData = new ThreeDsData(paymentId, orderId, amount, acsUrl, md, paReq);
            } else {
                throw new AcquiringSdkException(new IllegalStateException("incorrect PaymentStatus " + status));
            }
        }
        return threeDsData;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
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

    public PaymentStatus getStatus() {
        return status;
    }
}
