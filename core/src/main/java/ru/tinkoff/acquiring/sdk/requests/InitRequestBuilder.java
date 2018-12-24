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

import java.util.List;
import java.util.Map;

import ru.tinkoff.acquiring.sdk.PayType;
import ru.tinkoff.acquiring.sdk.Receipt;
import ru.tinkoff.acquiring.sdk.Shop;

/**
 * @author Mikhail Artemyev
 */
final public class InitRequestBuilder extends AcquiringRequestBuilder<InitRequest> {

    private InitRequest request = new InitRequest();

    /**
     * Билдер для запроса Init
     *
     * @param password    Пароль. Выдается банком на каждый магазин.
     * @param terminalKey Уникальный идентификатор терминала. Выдается банком на каждый магазин.
     */
    public InitRequestBuilder(final String password, final String terminalKey) {
        super(password, terminalKey);
    }

    /**
     * @param value Сумма в копейках
     */
    public InitRequestBuilder setAmount(final long value) {
        request.setAmount(value);
        return this;
    }

    /**
     * @param value Номер заказа в системе Продавца
     */
    public InitRequestBuilder setOrderId(final String value) {
        request.setOrderId(value);
        return this;
    }

    /**
     * @param value Идентификатор покупателя в системе Продавца.
     *              Если передается, то для данного покупателя будет осуществлена привязка карты к данному идентификатору клиента CustomerKey.
     *              В нотификации на AUTHORIZED будет передан параметр CardId, подробнее см. метод GetGardList {@link GetCardListRequestBuilder}.
     *              Параметр обязателен, если Recurrent = Y
     */
    public InitRequestBuilder setCustomerKey(final String value) {
        request.setCustomerKey(value);
        return this;
    }

    /**
     * @param value Краткое описание
     */
    public InitRequestBuilder setDescription(final String value) {
        request.setDescription(value);
        return this;
    }

    /**
     * @param value Заголовок формы, не более 20 символов
     */
    public InitRequestBuilder setPayForm(final String value) {
        request.setPayForm(value);
        return this;
    }

    /**
     * @param value Если передается и установлен в Y, то регистрирует платёж как рекуррентный. В этом случае после оплаты в нотификации на AUTHORIZED будет передан параметр RebillId для использования в методе Charge
     */
    public InitRequestBuilder setRecurrent(final boolean value) {
        request.setRecurrent(value);
        return this;
    }

    /**
     * @param language Язык платёжной формы.
     *                 ru - форма оплаты на русском языке;
     *                 en - форма оплаты на англифском языке.
     *                 По умолчанию (если параметр не передан) - форма оплаты на русском языке.
     */
    public InitRequestBuilder setLanguage(final String language) {
        request.setLanguage(language);
        return this;
    }

    /**
     * @param payType Тип оплаты
     */
    public InitRequestBuilder setPayType(PayType payType) {
        request.setPayType(payType.toString());
        return this;
    }

    /**
     * @param receipt Объект с данными чека
     */
    public InitRequestBuilder setReceipt(Receipt receipt) {
        request.setReceipt(receipt);
        return this;
    }

    /**
     * @param shops    - Объекты с данными магазинов
     * @param receipts - Объекты с данными чеков
     */
    public InitRequestBuilder setShops(List<Shop> shops, List<Receipt> receipts) {
        request.setShops(shops, receipts);
        return this;
    }

    /**
     * @param data Объект содержащий дополнительные параметры в виде “ключ”:”значение”. Данные параметры будут переданы на страницу оплаты (в случае ее кастомизации). Максимальная длина для каждого передаваемого параметра:
     *             Ключ – 20 знаков,
     *             Значение – 100 знаков.
     *             Максимальное количество пар «ключ-значение» не может превышать 20.
     */
    public InitRequestBuilder setData(Map<String, String> data) {
        request.setData(data);
        return this;
    }

    /**
     * @param data Объект содержащий дополнительные параметры в виде “ключ”:”значение”. Данные параметры будут переданы на страницу оплаты (в случае ее кастомизации). Максимальная длина для каждого передаваемого параметра:
     *             Ключ – 20 знаков,
     *             Значение – 100 знаков.
     *             Максимальное количество пар «ключ-значение» не может превышать 20.
     */
    public InitRequestBuilder addData(Map<String, String> data) {
        request.addData(data);
        return this;
    }

    /**
     * @param chargeFlag Флаг, о том, что происходит оплата в рекуретном режиме, и вместо вызова FinishAuthorize необходимо вызвать Charge
     */
    public InitRequestBuilder setChargeFlag(boolean chargeFlag) {
        request.setChargeFlag(chargeFlag);
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
