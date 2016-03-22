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
class ThreeDsBundlePacker implements IBundlePacker<ThreeDsData> {

    private static final String PAYMENT_ID = "paymentId";
    private static final String ORDER_ID = "orderId";
    private static final String AMOUNT = "amount";
    private static final String ASC_URL = "ascUrl";
    private static final String MD = "md";
    private static final String PAREQ = "paReq";
    private static final String IS_NEED = "isThreeDsNeed";


    @Override
    public ThreeDsData unpack(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        boolean isThreeDsNeed = bundle.getBoolean(IS_NEED);
        if (isThreeDsNeed) {
            Long paymentId = bundle.getLong(PAYMENT_ID);
            String orderId = bundle.getString(ORDER_ID);
            Long amount = bundle.getLong(AMOUNT);
            String ascUrl = bundle.getString(ASC_URL);
            String md = bundle.getString(MD);
            String paReq = bundle.getString(PAREQ);

            return new ThreeDsData(paymentId, orderId, amount, ascUrl, md, paReq);
        }
        return ThreeDsData.EMPTY_THREE_DS_DATA;
    }

    @Override
    public Bundle pack(ThreeDsData entity) {
        if (entity == null) {
            return null;
        }
        Bundle result = new Bundle();
        result.putLong(PAYMENT_ID, entity.getPaymentId());
        result.putString(ORDER_ID, entity.getOrderId());
        result.putLong(AMOUNT, entity.getAmount());
        result.putString(ASC_URL, entity.getAcsUrl());
        result.putString(MD, entity.getMd());
        result.putString(PAREQ, entity.getPaReq());
        result.putBoolean(IS_NEED, entity.isThreeDsNeed());
        return result;
    }


}
