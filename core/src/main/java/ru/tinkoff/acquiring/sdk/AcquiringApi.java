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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ru.tinkoff.acquiring.sdk.requests.AcquiringRequest;
import ru.tinkoff.acquiring.sdk.requests.ChargeRequest;
import ru.tinkoff.acquiring.sdk.requests.FinishAuthorizeRequest;
import ru.tinkoff.acquiring.sdk.requests.GetCardListRequest;
import ru.tinkoff.acquiring.sdk.requests.GetStateRequest;
import ru.tinkoff.acquiring.sdk.requests.InitRequest;
import ru.tinkoff.acquiring.sdk.requests.RemoveCardRequest;
import ru.tinkoff.acquiring.sdk.responses.AcquiringResponse;
import ru.tinkoff.acquiring.sdk.responses.ChargeResponse;
import ru.tinkoff.acquiring.sdk.responses.FinishAuthorizeResponse;
import ru.tinkoff.acquiring.sdk.responses.GetCardListResponse;
import ru.tinkoff.acquiring.sdk.responses.GetStateResponse;
import ru.tinkoff.acquiring.sdk.responses.InitResponse;
import ru.tinkoff.acquiring.sdk.responses.RemoveCardResponse;

/**
 * @author Mikhail Artemyev
 */
public class AcquiringApi {

    private static final String API_URL_RELEASE = "https://securepay.tinkoff.ru/rest";
    private static final String API_URL_DEBUG = "https://rest-api-test.tcsbank.ru/rest";
    private static final String API_URL_RELEASE_V2 = "https://securepay.tinkoff.ru/v2";
    private static final String API_URL_DEBUG_V2 = "https://rest-api-test.tcsbank.ru/v2";
    private static final int STREAM_BUFFER_SIZE = 4096;
    private static final String API_REQUEST_METHOD = "POST";

    private static final String JSON = "application/json";
    private static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";

    private static final String[] newMethods = {"Charge", "FinishAuthorize", "GetCardList", "GetState", "Init", "RemoveCard"};
    private static final List<String> newMethodsList = Arrays.asList(newMethods);

    static String getUrl(String apiMethod) {
        if (useV2Api(apiMethod)) {
            return Journal.isDebug() ? API_URL_DEBUG_V2 : API_URL_RELEASE_V2;
        } else {
            return Journal.isDebug() ? API_URL_DEBUG : API_URL_RELEASE;
        }
    }

    static boolean useV2Api(String apiMethod) {
        return newMethodsList.contains(apiMethod);
    }

    private final Gson gson;

    AcquiringApi() {
        this.gson = createGson();
    }

    InitResponse init(final InitRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, InitResponse.class);
    }

    FinishAuthorizeResponse finishAuthorize(final FinishAuthorizeRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, FinishAuthorizeResponse.class);
    }

    ChargeResponse charge(final ChargeRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, ChargeResponse.class);
    }

    GetStateResponse getState(final GetStateRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, GetStateResponse.class);
    }

    GetCardListResponse getCardList(final GetCardListRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, GetCardListResponse.class);
    }

    RemoveCardResponse removeCard(final RemoveCardRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, RemoveCardResponse.class);
    }

    private <R extends AcquiringResponse> R performRequest(final AcquiringRequest request,
                                                           final Class<R> responseClass) throws NetworkException, AcquiringApiException {

        final R result;
        InputStreamReader responseReader = null;
        OutputStream requestContentStream = null;
        try {
            final URL targetUrl = prepareURI(request.getApiMethod());
            final String requestBody = formatRequestBody(request.asMap(), request.getApiMethod());
            final HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod(API_REQUEST_METHOD);
            Journal.log(String.format("=== Sending %s request to %s", API_REQUEST_METHOD, targetUrl.toString()));

            if (!requestBody.isEmpty()) {
                Journal.log(String.format("===== Parameters: %s", requestBody));
                byte[] requestBodyBytes = requestBody.getBytes();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-type", useV2Api(request.getApiMethod()) ? JSON : FORM_URL_ENCODED);
                connection.setRequestProperty("Content-length", String.valueOf(requestBodyBytes.length));
                requestContentStream = connection.getOutputStream();
                requestContentStream.write(requestBodyBytes);
            }

            responseReader = new InputStreamReader(connection.getInputStream());
            final String response = read(responseReader);
            Journal.log(String.format("=== Got server response: %s", response));

            result = gson.fromJson(response, responseClass);

        } catch (IOException e) {
            throw new NetworkException("Unable to execute request " + request.getApiMethod(), e);
        } finally {
            closeQuietly(requestContentStream);
            closeQuietly(responseReader);
        }

        if (!result.isSuccess()) {
            String message = result.getMessage();
            String details = result.getDetails();
            if (message != null && details != null) {
                throw new AcquiringApiException(result, String.format("%s: %s", message, details));
            } else {
                throw new AcquiringApiException(result);
            }

        }

        return result;
    }

    private URL prepareURI(final String apiMethod) throws MalformedURLException {

        if (apiMethod == null || apiMethod.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot prepare URL for request api method is empty or null!"
            );
        }

        final StringBuilder builder = new StringBuilder(getUrl(apiMethod));
        builder.append("/");
        builder.append(apiMethod);

        return new URL(builder.toString());
    }

    private String formatRequestBody(final Map<String, Object> params, String apiMethod) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        if (useV2Api(apiMethod)) {
            return jsonRequestBody(params);
        } else {
            return encodeRequestBody(params);
        }
    }

    private String jsonRequestBody(final Map<String, Object> params) {
        String json = gson.toJson(params);
        return json;
    }

    private String encodeRequestBody(final Map<String, Object> params) {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            try {
                final String value = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
                builder.append(entry.getKey());
                builder.append('=');
                builder.append(value);
                builder.append('&');
            } catch (UnsupportedEncodingException e) {
                Journal.log(e);
            }
        }

        builder.setLength(builder.length() - 1);

        return builder.toString();
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(CardStatus.class, new CardStatusSerializer())
                .registerTypeAdapter(PaymentStatus.class, new PaymentStatusSerializer())
                .registerTypeAdapter(GetCardListResponse.class, new CardsListDeserializer())
                .registerTypeAdapter(Tax.class, new TaxSerializer())
                .registerTypeAdapter(Taxation.class, new TaxationSerializer())
                .create();
    }

    private String read(final InputStreamReader reader) throws IOException {
        final char[] buffer = new char[STREAM_BUFFER_SIZE];
        int read;
        final StringBuilder result = new StringBuilder();

        while ((read = reader.read(buffer, 0, STREAM_BUFFER_SIZE)) != -1) {
            result.append(buffer, 0, read);
        }

        return result.toString();
    }

    private void closeQuietly(final Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            Journal.log(e);
        }
    }

}
