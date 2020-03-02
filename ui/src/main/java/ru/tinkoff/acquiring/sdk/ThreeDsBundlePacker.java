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

import android.os.Bundle;

/**
 * @author a.shishkin1
 */
public class ThreeDsBundlePacker implements IBundlePacker<ThreeDsData> {

    private static final String PAYMENT_ID = "paymentId";
    private static final String REQUEST_KEY = "requestKey";
    private static final String ASC_URL = "ascUrl";
    private static final String MD = "md";
    private static final String PA_REQ = "paReq";
    private static final String TDS_SERVER_TRANS_ID = "tdsServerTransId";
    private static final String ACS_TRANS_ID = "acsTransId";
    private static final String VERSION = "version";
    private static final String VERSION_NAME = "versionName";
    private static final String IS_NEED = "isThreeDsNeed";

    @Override
    public ThreeDsData unpack(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        boolean isThreeDsNeed = bundle.getBoolean(IS_NEED);
        if (isThreeDsNeed) {

            String acsUrl = bundle.getString(ASC_URL);

            if (bundle.containsKey(PAYMENT_ID)) {
                long paymentId = bundle.getLong(PAYMENT_ID);
                ThreeDsVersion version = ThreeDsVersion.fromValue(bundle.getString(VERSION));

                ThreeDsData threeDsData = new ThreeDsData(paymentId, acsUrl, version);

                if (version == ThreeDsVersion.TWO) {
                    String tdsServerTransId = bundle.getString(TDS_SERVER_TRANS_ID);
                    String acsTransId = bundle.getString(ACS_TRANS_ID);

                    threeDsData.setTdsServerTransId(tdsServerTransId);
                    threeDsData.setAcsTransId(acsTransId);

                } else if (version == ThreeDsVersion.ONE) {
                    String md = bundle.getString(MD);
                    String paReq = bundle.getString(PA_REQ);

                    threeDsData.setMd(md);
                    threeDsData.setPaReq(paReq);
                }

                if (bundle.containsKey(VERSION_NAME)) {
                    String versionName = bundle.getString(VERSION_NAME);
                    threeDsData.setVersionName(versionName);
                }

                return threeDsData;

            } else {
                String requestKey = bundle.getString(REQUEST_KEY);
                String md = bundle.getString(MD);
                String paReq = bundle.getString(PA_REQ);

                ThreeDsData threeDsData = new ThreeDsData(requestKey, acsUrl);
                threeDsData.setMd(md);
                threeDsData.setPaReq(paReq);
                return threeDsData;
            }
        }
        return ThreeDsData.EMPTY_THREE_DS_DATA;
    }

    @Override
    public Bundle pack(ThreeDsData entity) {
        if (entity == null) {
            return null;
        }
        Bundle result = new Bundle();
        Long paymentId = entity.getPaymentId();

        if (paymentId != null) {
            result.putLong(PAYMENT_ID, paymentId);
            result.putString(VERSION, entity.getVersion().toString());

            if (entity.getVersionName() != null) {
                result.putString(VERSION_NAME, entity.getVersionName());
            }

            if (entity.getVersion() == ThreeDsVersion.TWO) {
                result.putString(TDS_SERVER_TRANS_ID, entity.getTdsServerTransId());
                result.putString(ACS_TRANS_ID, entity.getAcsTransId());
            } else {
                result.putString(MD, entity.getMd());
                result.putString(PA_REQ, entity.getPaReq());
            }

        } else {
            result.putString(REQUEST_KEY, entity.getRequestKey());
        }

        result.putString(ASC_URL, entity.getAcsUrl());
        result.putBoolean(IS_NEED, entity.isThreeDsNeed());
        return result;
    }
}
