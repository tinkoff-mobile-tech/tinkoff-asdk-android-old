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

package ru.tinkoff.acquiring.sdk.requests;

import java.util.Map;

/**
 * @author Mikhail Artemyev
 */
final public class ChargeRequest extends AcquiringRequest {

    private Long paymentId;
    private String rebillId;

    public ChargeRequest() {
        super("Charge");
    }

    @Override
    public Map<String, Object> asMap() {
        final Map<String, Object> map = super.asMap();

        putIfNotNull(PAYMENT_ID, paymentId.toString(), map);
        putIfNotNull(REBILL_ID, rebillId, map);

        return map;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getRebillId() {
        return rebillId;
    }

    void setRebillId(String rebillId) {
        this.rebillId = rebillId;
    }
}
