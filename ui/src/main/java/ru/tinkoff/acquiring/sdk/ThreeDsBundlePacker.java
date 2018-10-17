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
    private static final String IS_NEED = "isThreeDsNeed";

    @Override
    public ThreeDsData unpack(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        boolean isThreeDsNeed = bundle.getBoolean(IS_NEED);
        if (isThreeDsNeed) {
            String ascUrl = bundle.getString(ASC_URL);
            String md = bundle.getString(MD);
            String paReq = bundle.getString(PA_REQ);
            if (bundle.containsKey(PAYMENT_ID)) {
                Long paymentId = bundle.getLong(PAYMENT_ID);
                return new ThreeDsData(paymentId, ascUrl, md, paReq);
            } else {
                String requestKey = bundle.getString(REQUEST_KEY);
                return new ThreeDsData(requestKey, ascUrl, md, paReq);
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
        } else {
            result.putString(REQUEST_KEY, entity.getRequestKey());
        }
        result.putString(ASC_URL, entity.getAcsUrl());
        result.putString(MD, entity.getMd());
        result.putString(PA_REQ, entity.getPaReq());
        result.putBoolean(IS_NEED, entity.isThreeDsNeed());
        return result;
    }


}
