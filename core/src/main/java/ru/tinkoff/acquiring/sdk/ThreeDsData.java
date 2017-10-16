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
public class ThreeDsData {

    private final Long paymentId;
    private final String requestKey;
    private final String acsUrl;
    private final String md;
    private final String paReq;
    private final boolean isThreeDsNeed;

    public static final ThreeDsData EMPTY_THREE_DS_DATA = new ThreeDsData();

    private ThreeDsData() {
        this.isThreeDsNeed = false;
        this.paymentId = null;
        this.requestKey = null;
        this.acsUrl = null;
        this.md = null;
        this.paReq = null;
    }

    public ThreeDsData(Long paymentId, String acsUrl, String md, String paReq) {
        this.isThreeDsNeed = true;
        this.paymentId = paymentId;
        this.requestKey = null;
        this.acsUrl = acsUrl;
        this.md = md;
        this.paReq = paReq;
    }

    public ThreeDsData(String requestKey, String acsUrl, String md, String paReq) {
        this.isThreeDsNeed = true;
        this.paymentId = null;
        this.requestKey = requestKey;
        this.acsUrl = acsUrl;
        this.md = md;
        this.paReq = paReq;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getRequestKey() {
        return requestKey;
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

    public boolean isThreeDsNeed() {
        return isThreeDsNeed;
    }

    public boolean isPayment() {
        return paymentId != null && requestKey == null;
    }

    public boolean isAttaching() {
        return paymentId == null && requestKey != null;
    }

    @Override
    public String toString() {
        return "Data: " +
                paymentId + ", " +
                acsUrl + ", " +
                md + ", " +
                paReq + ", " +
                isThreeDsNeed + ";";
    }


}
