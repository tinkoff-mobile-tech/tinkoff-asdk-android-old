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

package ru.tinkoff.acquiring.sdk;

/**
 * @author Mikhail Artemyev
 */
public class PaymentInfo {

    public static final String CHARGE_SUCCESS = "0";
    public static final String CHARGE_REJECTED_ERROR = "104";

    private final String orderId;
    private final Long paymentId;
    private final Long amount;
    private final String cardId;
    private final String errorCode;

    public PaymentInfo(String orderId, Long paymentId, Long amount, String cardId, String errorCode) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.cardId = cardId;
        this.errorCode = errorCode;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCardId() {
        return cardId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isSuccess() {
        return CHARGE_SUCCESS.equals(errorCode);
    }

    public boolean isRejected() {
        return CHARGE_REJECTED_ERROR.equals(errorCode);
    }
}
