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

    private Long paymentId;
    private String requestKey;
    private String acsUrl;
    private String md;
    private String paReq;
    private boolean isThreeDsNeed;
    private String tdsServerTransId;
    private String acsTransId;

    private ThreeDsVersion version;
    private String versionName;

    public static final ThreeDsData EMPTY_THREE_DS_DATA = new ThreeDsData();

    private ThreeDsData() {
        this.isThreeDsNeed = false;
        this.paymentId = null;
        this.requestKey = null;
        this.acsUrl = null;
        this.md = null;
        this.paReq = null;
    }

    public ThreeDsData(Long paymentId, String acsUrl, ThreeDsVersion version) {
        this.isThreeDsNeed = true;
        this.paymentId = paymentId;
        this.requestKey = null;
        this.acsUrl = acsUrl;
        this.version = version;
    }

    public ThreeDsData(String requestKey, String acsUrl) {
        this.isThreeDsNeed = true;
        this.paymentId = null;
        this.requestKey = requestKey;
        this.acsUrl = acsUrl;
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

    public ThreeDsVersion getVersion() {
        return version;
    }

    public String getTdsServerTransId() {
        return tdsServerTransId;
    }

    public void setTdsServerTransId(String tdsServerTransId) {
        this.tdsServerTransId = tdsServerTransId;
    }

    public String getAcsTransId() {
        return acsTransId;
    }

    public void setAcsTransId(String acsTransId) {
        this.acsTransId = acsTransId;
    }

    public void setMd(String md) {
        this.md = md;
    }

    public void setPaReq(String paReq) {
        this.paReq = paReq;
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


    public void setVersionName(String version) {
        this.versionName = version;
    }

    public String getVersionName() {
        return versionName;
    }
}
