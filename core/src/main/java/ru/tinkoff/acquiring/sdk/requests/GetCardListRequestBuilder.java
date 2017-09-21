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

/**
 * @author Mikhail Artemyev
 */
final public class GetCardListRequestBuilder extends AcquiringRequestBuilder<GetCardListRequest> {

    private GetCardListRequest request = new GetCardListRequest();

    /**
     * Билдер для запроса GetCardList
     *
     * @param password    Пароль. Выдается банком на каждый магазин.
     * @param terminalKey Уникальный идентификатор терминала. Выдается банком на каждый магазин.
     */
    public GetCardListRequestBuilder(final String password, final String terminalKey) {
        super(password, terminalKey);
    }

    /**
     * @param value Идентификатор покупателя в системе Продавца, к которому привязаны карты.
     */
    public GetCardListRequestBuilder setCustomerKey(final String value) {
        request.setCustomerKey(value);
        return this;
    }

    @Override
    protected GetCardListRequest getRequest() {
        return request;
    }

    @Override
    protected void validate() {
        validateNonEmpty(request.getCustomerKey(), "Customer key");
    }
}
