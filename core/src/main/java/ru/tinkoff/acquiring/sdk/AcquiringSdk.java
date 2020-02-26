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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.Map;

import ru.tinkoff.acquiring.sdk.requests.AddCardRequest;
import ru.tinkoff.acquiring.sdk.requests.AddCardRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.AttachCardRequest;
import ru.tinkoff.acquiring.sdk.requests.AttachCardRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.ChargeRequest;
import ru.tinkoff.acquiring.sdk.requests.ChargeRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.Check3dsVersionRequest;
import ru.tinkoff.acquiring.sdk.requests.Check3dsVersionRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.FinishAuthorizeRequest;
import ru.tinkoff.acquiring.sdk.requests.FinishAuthorizeRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.GetAddCardStateRequest;
import ru.tinkoff.acquiring.sdk.requests.GetAddCardStateRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.GetCardListRequest;
import ru.tinkoff.acquiring.sdk.requests.GetCardListRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.GetStateRequest;
import ru.tinkoff.acquiring.sdk.requests.GetStateRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.InitRequest;
import ru.tinkoff.acquiring.sdk.requests.InitRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.RemoveCardRequest;
import ru.tinkoff.acquiring.sdk.requests.RemoveCardRequestBuilder;
import ru.tinkoff.acquiring.sdk.requests.SubmitRandomAmountRequest;
import ru.tinkoff.acquiring.sdk.requests.SubmitRandomAmountRequestBuilder;
import ru.tinkoff.acquiring.sdk.responses.AddCardResponse;
import ru.tinkoff.acquiring.sdk.responses.AttachCardResponse;
import ru.tinkoff.acquiring.sdk.responses.Check3dsVersionResponse;
import ru.tinkoff.acquiring.sdk.responses.GetAddCardStateResponse;
import ru.tinkoff.acquiring.sdk.responses.GetCardListResponse;
import ru.tinkoff.acquiring.sdk.responses.SubmitRandomAmountResponse;

/**
 * <p>
 * Класс позволяет конфигурировать SDK и осуществлять взаимодействие с Тинькофф Эквайринг API.
 * Методы, осуществляющие обращение к API, возвращают результат в случае успешного выполнения
 * запроса или бросают исключение (ошибку для iOS) AcquringSdkException (AcquiringSdkError).
 *
 * @author Mikhail Artemyev
 */
public class AcquiringSdk extends Journal {

    private static final int PAY_FORM_MAX_LENGTH = 20;

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
     * @param builder Билдер связанный с запросом Init
     * @return уникальный идентификатор транзакции в системе Банка
     */
    public Long init(InitRequestBuilder builder) {
        InitRequest request = builder.build();
        return executeInitRequest(request);
    }

    //TODO doc
    public Check3dsVersionResponse check3DsVersion(final long paymentId, final CardData cardData) {
        final Check3dsVersionRequest request = new Check3dsVersionRequestBuilder(password, terminalKey)
                .setPaymentId(paymentId)
                .setCardData(cardData.encode(publicKey))
                .build();

        try {
            return api.check3DsVersion(request);
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * Подтверждает инициированный платеж передачей карточных данных
     *
     * @param paymentId уникальный идентификатор транзакции в системе Банка
     * @param cardData  данные карты
     * @param infoEmail email, на который будет отправлена квитанция об оплате
     * @param deviceData TODO
     * @return Объект ThreeDsData если терминал требует прохождения 3DS, иначе null
     */
    public ThreeDsData finishAuthorize(final long paymentId,
                                       final CardData cardData,
                                       final String infoEmail,
                                       final Map<String, String> deviceData) {
        final FinishAuthorizeRequestBuilder requestBuilder = new FinishAuthorizeRequestBuilder(password, terminalKey)
                .setPaymentId(paymentId)
                .setSendEmail(infoEmail != null)
                .setCardData(cardData.encode(publicKey))
                .setEmail(infoEmail)
                .setData(deviceData);

        if (deviceData != null) {
            requestBuilder.setIp(getIpAddress());
        }

        try {
            return api.finishAuthorize(requestBuilder.build()).getThreeDsData();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * Подтверждает инициированный платеж передачей токена Google Pay
     *
     * @param paymentId уникальный идентификатор транзакции в системе Банка
     * @param token  токен полученный от Google Pay
     * @param infoEmail email, на который будет отправлена квитанция об оплате
     * @return Объект ThreeDsData если терминал требует прохождения 3DS, иначе null
     */
    public ThreeDsData finishAuthorize(final long paymentId,
                                       final String token,
                                       final String infoEmail) {
        final FinishAuthorizeRequest request = new FinishAuthorizeRequestBuilder(password, terminalKey)
                .setPaymentId(paymentId)
                .setGooglePayToken(token)
                .setSendEmail(infoEmail != null)
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
    public PaymentInfo charge(final long paymentId, final String rebillId) {
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
        final GetStateRequest request = new GetStateRequestBuilder(password, terminalKey)
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
     * Метод подготовки для привязки карты, необходимо вызвать {@link AcquiringSdk#addCard(String, CheckType)} перед методом {@link AcquiringSdk#attachCard(String, CardData, String, Map)}
     *
     * @param customerKey идентификатор покупателя в системе Продавца
     * @param checkType   тип привязки {@link CheckType}
     * @return возвращет ключ запроса (RequestKey)
     */
    public String addCard(final String customerKey, final CheckType checkType) {
        return addCard(customerKey, checkType.toString());
    }

    /**
     * Метод подготовки для привязки карты, необходимо вызвать {@link AcquiringSdk#addCard(String, CheckType)} перед методом {@link AcquiringSdk#attachCard(String, CardData, String, Map)}
     *
     * @param customerKey идентификатор покупателя в системе Продавца
     * @param checkType   тип привязки {@link CheckType}
     * @return возвращет ключ запроса (RequestKey)
     */
    public String addCard(final String customerKey, final String checkType) {
        final AddCardRequest request = new AddCardRequestBuilder(password, terminalKey)
                .setCustomerKey(customerKey)
                .setCheckType(checkType)
                .build();

        try {
            AddCardResponse response = api.addCard(request);
            return response.getRequestKey();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * Метод привязки карты, вызывается после {@link AcquiringSdk#addCard(String, CheckType)}
     *
     * @param requestKey ключ запроса, полученный в качестве ответа на {@link AcquiringSdk#addCard(String, CheckType)}
     * @param cardData   данные привязанной карты
     * @param email      email
     * @param data       дополнительные параметры в виде ключ, значение
     * @return возвращает результат запроса
     */
    public AttachCardResponse attachCard(final String requestKey, final CardData cardData, final String email, final Map<String, String> data) {
        final AttachCardRequest request = new AttachCardRequestBuilder(password, terminalKey)
                .setRequestKey(requestKey)
                .setCardData(cardData.encode(publicKey))
                .setEmail(email)
                .setData(data)
                .build();

        try {
            AttachCardResponse response = api.attachCard(request);
            return response;
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * Метод проверки состояния привязки карты после 3DS
     *
     * @param requestKey ключ запроса, полученный в качестве ответа на {@link AcquiringSdk#addCard(String, CheckType)}
     * @return возвращает результат запроса
     */
    public GetAddCardStateResponse getAddCardState(final String requestKey) {
        final GetAddCardStateRequest request = new GetAddCardStateRequestBuilder(password, terminalKey)
                .setRequestKey(requestKey)
                .build();

        try {
            GetAddCardStateResponse response = api.getAddCardState(request);
            return response;
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * Метод подтверждения при {@link CheckType#THREE_DS_HOLD} привязки
     *
     * @param requestKey ключ запроса, полученный в качестве ответа на {@link AcquiringSdk#addCard(String, CheckType)}
     * @param amount     забронированная сумма в копейках
     * @return возвращает идентификатор привязанной карты (CardId)
     */
    public String submitRandomAmount(final String requestKey, final Long amount) {
        final SubmitRandomAmountRequest request = new SubmitRandomAmountRequestBuilder(password, terminalKey)
                .setRequestKey(requestKey)
                .setAmount(amount)
                .build();

        try {
            SubmitRandomAmountResponse response = api.submitRandomAmount(request);
            return response.getCardId();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    /**
     * @return Возвращает URL адрес API, в зависимости от названия метода
     */
    public String getUrl(String apiMethod) {
        return AcquiringApi.getUrl(apiMethod);
    }

    private Long executeInitRequest(InitRequest request) {
        try {
            return api.init(request).getPaymentId();
        } catch (AcquiringApiException | NetworkException e) {
            throw new AcquiringSdkException(e);
        }
    }

    private String getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            //ignore
        }
        return "";
    }
}
