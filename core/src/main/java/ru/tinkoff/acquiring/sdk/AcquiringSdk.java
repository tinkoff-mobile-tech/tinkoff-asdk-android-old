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

import java.security.PublicKey;

import ru.tinkoff.acquiring.sdk.requests.ChargeRequest;
import ru.tinkoff.acquiring.sdk.requests.ChargeRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.FinishAuthorizeRequest;
import ru.tinkoff.acquiring.sdk.requests.FinishAuthorizeRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.GetCardListRequest;
import ru.tinkoff.acquiring.sdk.requests.GetCardListRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.GetStateRequest;
import ru.tinkoff.acquiring.sdk.requests.GetstateRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.InitRequest;
import ru.tinkoff.acquiring.sdk.requests.InitRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.RemoveCardRequest;
import ru.tinkoff.acquiring.sdk.requests.RemoveCardRequestBuilder;
import ru.tinkoff.acquiring.sdk.responses.GetCardListResponse;

/**
 * <p>
 * Класс позволяет конфигурировать SDK и осуществлять взаимодействие с Тинькофф Эквайринг API.
 * Методы, осуществляющие обращение к API, возвращают результат в случае успешного выполнения
 * запроса или бросают исключение (ошибку для iOS) AcquringSdkException (AcquiringSdkError).
 *
 * @author Mikhail Artemyev
 */
public class AcquiringSdk extends Journal {

    private final AcquiringApi api;
    private final String terminalKey;
    private final String password;
    private final PublicKey publicKey;

    /**
     * Создает новый экземпляр SDK
     *
     * @param terminalKey ключ терминала. Выдается после подключения к Тинькофф Эквайринг
     * @param password    пароль от терминала. Выдается вместе с terminalKey
     * @param publicKey   экземпляр PublicKey созданный из публичного ключа, выдаваемого вместе с
     *                    terminalKey
     */
    public AcquiringSdk(final String terminalKey,
                        final String password,
                        final PublicKey publicKey) {

        this.terminalKey = terminalKey;
        this.password = password;
        this.publicKey = publicKey;

        this.api = new AcquiringApi();
    }

    /**
     * Создает новый экземпляр SDK
     *
     * @param terminalKey ключ терминала. Выдается после подключения к Тинькофф Эквайринг
     * @param password    пароль от терминала. Выдается вместе с terminalKey
     * @param publicKey   публичный ключ. Выдается вместе с terminalKey
     */
    public AcquiringSdk(final String terminalKey,
                        final String password,
                        final String publicKey) {
        this(terminalKey, password, new StringKeyCreator(publicKey));
    }


    public AcquiringSdk(final String terminalKey,
                        final String password,
                        final KeyCreator keyCreator) {
        this(terminalKey, password, keyCreator.create());
    }

    public String getTerminalKey() {
        return terminalKey;
    }

    public String getPassword() {
        return password;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Инициирует платежную сессию
     *
     * @param amount       сумма в копейках
     * @param orderId      номер заказа в системе Продавца
     * @param customerKey  идентификатор покупателя в системе Продавца. Если передается и Банком
     *                     разрешена автоматическая привязка карт к терминалу, то для данного
     *                     покупателя будет осуществлена привязка карты
     * @param description  краткое описание
     * @param payFormTitle название шаблона формы оплаты продавца
     * @param recurrent    регистрирует платеж как рекуррентный
     * @param language    язык для локализации
     * @return уникальный идентификатор транзакции в системе Банка
     */
    public Long init(final Money amount,
                     final String orderId,
                     final String customerKey,
                     final String description,
                     final String payFormTitle,
                     final boolean recurrent,
                     final Language language) {


        final InitRequest request = new InitRequestBuilder(password, terminalKey)
                .setAmount(amount.getCoins())
                .setOrderId(orderId)
                .setCustomerKey(customerKey)
                .setDescription(description)
                .setPayForm(payFormTitle)
                .setReccurent(recurrent)
                .setLanguage(language.toString())
                .build();

        try {
            return api.init(request).getPaymentId();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    public Long init(final Money amount,
                     final String orderId,
                     final String customerKey,
                     final String description,
                     final String payFormTitle,
                     final boolean recurrent) {
        return init(amount, orderId, customerKey, description, payFormTitle, recurrent, Language.RUSSIAN);
    }

    /**
     * Подтверждает инициированный платеж передачей карточных данных
     *
     * @param paymentId уникальный идентификатор транзакции в системе Банка
     * @param cardData  данные карты
     * @param infoEmail email, на который будет отправлена квитанция об оплате
     * @return Объект ThreeDsData если терминал требует прохождения 3DS, иначе null
     */
    public ThreeDsData finishAuthorize(final long paymentId,
                                       final CardData cardData,
                                       final String infoEmail) {
        final FinishAuthorizeRequest request = new FinishAuthorizeRequestBuilder(password, terminalKey)
                .setPaymentId(paymentId)
                .setSendEmail(infoEmail != null)
                .setCardData(cardData.encode(publicKey))
                .setEmail(infoEmail)
                .build();

        try {
            return api.finishAuthorize(request).getThreeDsData();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * Возвращает список привязанных карт
     *
     * @param customerKey идентификатор покупателя в системе Продавца
     * @return список сохраненных карт
     */
    public Card[] getCardList(final String customerKey) {
        final GetCardListRequest request = new GetCardListRequestBuilder(password, terminalKey)
                .setCustomerKey(customerKey)
                .build();

        try {
            GetCardListResponse response = api.getCardList(request);
            Journal.log("GetCardListResponse " + response);
            return response.getCard();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * Осуществляет рекуррентный (повторный) платеж — безакцептное списание денежных средств со
     * счета банковской карты Покупателя. Для возможности его использования Покупатель должен
     * совершить хотя бы один платеж в пользу Продавца, который должен быть указан как рекуррентный
     * (см. параметр recurrent в методе Init), фактически являющийся первичным.
     * <p>
     * Другими словами, для использования рекуррентных платежей необходима следующая
     * последовательность действий: <ol> <li>Совершить родительский платеж путем вызова Init с
     * указанием дополнительного параметра Recurrent=Y.</li> <li>Получить RebillId, предварительно
     * вызвав метод GetCardList</li> <li>Спустя некоторое время для совершения рекуррентного платежа
     * необходимо вызвать метод Init со стандартным набором параметров (параметр Recurrent здесь не
     * нужен).</li> <li>Получить в ответ на Init параметр PaymentId.</li> <li>Вызвать метод Charge с
     * параметром RebillId полученным в п.2 и параметром PaymentId полученным в п.4.</li> </ol>
     *
     * @param paymentId
     * @param rebillId
     * @return
     */
    public PaymentInfo charge(final long paymentId, final long rebillId) {
        final ChargeRequest request = new ChargeRequestBuilder(password, terminalKey)
                .setPaymentId(paymentId)
                .setRebillId(rebillId)
                .build();

        try {
            return api.charge(request).getPaymentInfo();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * Возвращает статус платежа
     *
     * @param paymentId уникальный идентификатор транзакции в системе Банка
     * @return статус платежа
     */
    public PaymentStatus getState(final long paymentId) {
        final GetStateRequest request = new GetstateRequestBuilder(password, terminalKey)
                .setPaymentId(paymentId)
                .build();

        try {
            return api.getState(request).getStatus();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * Удаляет привязанную карту
     *
     * @param customerKey идентификатор покупателя в системе Продавца
     * @param cardId      идентификатор карты в системе Банка
     * @return признак, удалена ли карта
     */
    public boolean removeCard(final String customerKey, final String cardId) {
        final RemoveCardRequest request = new RemoveCardRequestBuilder(password, terminalKey)
                .setCustomerKey(customerKey)
                .setCardId(cardId)
                .build();

        try {
            return api.removeCard(request).isSuccess();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * @return Возвращает URL адрес API
     */
    public String getUrl() {
        return AcquiringApi.getUrl();
    }

}
