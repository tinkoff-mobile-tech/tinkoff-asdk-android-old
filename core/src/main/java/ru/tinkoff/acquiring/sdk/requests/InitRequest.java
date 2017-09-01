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

import java.util.HashMap;
import java.util.Map;

import ru.tinkoff.acquiring.sdk.Receipt;

/**
 * @author Mikhail Artemyev
 */
final public class InitRequest extends AcquiringRequest {

    private Long amount;
    private String orderId;
    private String customerKey;
    private String description;
    private String payForm;
    private String recurrent;
    private String language;
    private String payType;
    private Receipt receipt;
    private Map<String, String> data;
    private boolean chargeFlag;

    public InitRequest() {
        super("Init");
    }

    @Override
    public Map<String, Object> asMap() {
        final Map<String, Object> map = super.asMap();

        putIfNotNull(AMOUNT, amount.toString(), map);
        putIfNotNull(ORDER_ID, orderId, map);
        putIfNotNull(CUSTOMER_KEY, customerKey, map);
        putIfNotNull(DESCRIPTION, description, map);
        putIfNotNull(PAY_FORM, payForm, map);
        putIfNotNull(RECURRENT, recurrent, map);
        putIfNotNull(LANGUAGE, language, map);
        putIfNotNull(PAY_TYPE, payType, map);
        putIfNotNull(RECEIPT, receipt, map);
        putDataIfNonNull(map);

        return map;
    }

    public Long getAmount() {
        return amount;
    }

    void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public String getPayForm() {
        return payForm;
    }

    void setPayForm(String payForm) {
        this.payForm = payForm;
    }

    public boolean getRecurrent() {
        return "Y".equals(recurrent);
    }

    void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent ? "Y" : null;
    }

    public String getLanguage() {
        return language;
    }

    void setLanguage(String language) {
        this.language = language;
    }

    public String getPayType() {
        return payType;
    }

    void setPayType(String payType) {
        this.payType = payType;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    void setReceipt(Receipt receiptValue) {
        this.receipt = receiptValue;
    }

    public Map<String, String> getData() {
        return data;
    }

    void setData(Map<String, String> dataValue) {
        this.data = dataValue;
    }

    public boolean isChargeFlag() {
        return chargeFlag;
    }

    void setChargeFlag(boolean chargeFlag) {
        this.chargeFlag = chargeFlag;
    }

    private void putDataIfNonNull(Map<String, Object> map) {
        HashMap<String, String> dataMap = new HashMap<>();
        if (data != null) {
            dataMap.putAll(data);
        }
        dataMap.put(CHARGE_FLAG, Boolean.toString(chargeFlag));
        map.put(DATA, dataMap);
    }
}
