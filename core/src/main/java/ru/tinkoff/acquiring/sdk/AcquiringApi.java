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
    private static final int STREAM_BUFFER_SIZE = 4096;
    public static final String API_REQUEST_METHOD = "POST";

    static String getUrl() {
        return Journal.isDebug() ? API_URL_DEBUG : API_URL_RELEASE;
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
            final String requestBody = encodeRequestBody(request.asMap());
            final HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod(API_REQUEST_METHOD);
            Journal.log(String.format("=== Sending %s request to %s", API_REQUEST_METHOD, targetUrl.toString()));

            if (!requestBody.isEmpty()) {
                Journal.log(String.format("===== Parameters: %s", requestBody));
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-length", String.valueOf(requestBody.length()));
                requestContentStream = connection.getOutputStream();
                requestContentStream.write(requestBody.getBytes());
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
            throw new AcquiringApiException(result);
        }

        return result;
    }

    private URL prepareURI(final String apiMethod) throws MalformedURLException {

        if (apiMethod == null || apiMethod.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot prepare URL for request api method is empty or null!"
            );
        }

        final StringBuilder builder = new StringBuilder(getUrl());
        builder.append("/");
        builder.append(apiMethod);

        return new URL(builder.toString());
    }

    private String encodeRequestBody(final Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            try {
                final String value = URLEncoder.encode(entry.getValue(), "UTF-8");
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
