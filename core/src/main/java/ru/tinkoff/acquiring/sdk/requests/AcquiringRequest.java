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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mikhail Artemyev
 */
public class AcquiringRequest {

    public static final String TERMINAL_KEY = "TerminalKey";
    public static final String PAYMENT_ID = "PaymentId";
    public static final String SEND_EMAIL = "SendEmail";
    public static final String TOKEN = "Token";
    public static final String EMAIL = "InfoEmail";
    public static final String CARD_DATA = "CardData";
    public static final String LANGUAGE = "Language";
    public static final String AMOUNT = "Amount";
    public static final String ORDER_ID = "OrderId";
    public static final String DESCRIPTION = "Description";
    public static final String PAY_FORM = "PayForm";
    public static final String CUSTOMER_KEY = "CustomerKey";
    public static final String RECURRENT = "Recurrent";
    public static final String REBILL_ID = "RebillId";
    public static final String CARD_ID = "CardId";
    public static final String CVV = "CVV";
    public static final String PAY_TYPE = "PayType";
    public static final String RECEIPT = "Receipt";
    public static final String DATA = "DATA";
    public static final String CHARGE_FLAG = "chargeFlag";
    public static final String DATA_KEY_EMAIL = "Email";
    public static final String[] IGNORED_FIELDS_VALUES = new String[]{DATA, RECEIPT};
    public static final Set<String> IGNORED_FIELDS = new HashSet<>(Arrays.asList(IGNORED_FIELDS_VALUES));

    private String terminalKey;
    private String token;
    private final String apiMethod;

    AcquiringRequest(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();

        putIfNotNull(TERMINAL_KEY, terminalKey, map);
        putIfNotNull(TOKEN, token, map);

        return map;
    }

    public Set<String> getTokenIgnoreFields() {
        return IGNORED_FIELDS;
    }

    public String getTerminalKey() {
        return terminalKey;
    }

    void setTerminalKey(String terminalKey) {
        this.terminalKey = terminalKey;
    }

    public String getToken() {
        return token;
    }

    void setToken(String token) {
        this.token = token;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    protected void putIfNotNull(final String key, final Object value, final Map<String, Object> map) {
        if (key == null || value == null || map == null) {
            return;
        }

        map.put(key, value);
    }
}

