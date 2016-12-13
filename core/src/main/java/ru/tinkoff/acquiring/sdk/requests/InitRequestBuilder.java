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

import ru.tinkoff.acquiring.sdk.PayType;

/**
 * @author Mikhail Artemyev
 */
final public class InitRequestBuilder extends AcquiringRequestBuilder<InitRequest> {

    private InitRequest request = new InitRequest();

    public InitRequestBuilder(final String password, final String terminalKey) {
        super(password, terminalKey);
    }

    public InitRequestBuilder setAmount(final long value) {
        request.setAmount(value);
        return this;
    }

    public InitRequestBuilder setOrderId(final String value) {
        request.setOrderId(value);
        return this;
    }

    public InitRequestBuilder setCustomerKey(final String value) {
        request.setCustomerKey(value);
        return this;
    }

    public InitRequestBuilder setDescription(final String value) {
        request.setDescription(value);
        return this;
    }

    public InitRequestBuilder setPayForm(final String value) {
        request.setPayForm(value);
        return this;
    }

    public InitRequestBuilder setReccurent(final boolean value) {
        request.setReccurent(value);
        return this;
    }

    public InitRequestBuilder setLanguage(final String language) {
        request.setLanguage(language);
        return this;
    }

    public InitRequestBuilder setPayType(PayType payType) {
        request.setPayType(payType.toString());
        return this;
    }

    @Override
    protected void validate() {
        validateNonEmpty(request.getOrderId(), "Order ID");
        validateZeroOrPositive(request.getAmount(), "Amount");
    }

    @Override
    protected InitRequest getRequest() {
        return request;
    }
}
