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
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import ru.tinkoff.acquiring.sdk.requests.AcquiringRequest;
import ru.tinkoff.acquiring.sdk.requests.AddCardRequest;
import ru.tinkoff.acquiring.sdk.requests.AttachCardRequest;
import ru.tinkoff.acquiring.sdk.requests.ChargeRequest;
import ru.tinkoff.acquiring.sdk.requests.Check3dsVersionRequest;
import ru.tinkoff.acquiring.sdk.requests.FinishAuthorizeRequest;
import ru.tinkoff.acquiring.sdk.requests.GetAddCardStateRequest;
import ru.tinkoff.acquiring.sdk.requests.GetCardListRequest;
import ru.tinkoff.acquiring.sdk.requests.GetStateRequest;
import ru.tinkoff.acquiring.sdk.requests.InitRequest;
import ru.tinkoff.acquiring.sdk.requests.RemoveCardRequest;
import ru.tinkoff.acquiring.sdk.requests.SubmitRandomAmountRequest;
import ru.tinkoff.acquiring.sdk.responses.AcquiringResponse;
import ru.tinkoff.acquiring.sdk.responses.AddCardResponse;
import ru.tinkoff.acquiring.sdk.responses.AttachCardResponse;
import ru.tinkoff.acquiring.sdk.responses.ChargeResponse;
import ru.tinkoff.acquiring.sdk.responses.Check3dsVersionResponse;
import ru.tinkoff.acquiring.sdk.responses.FinishAuthorizeResponse;
import ru.tinkoff.acquiring.sdk.responses.GetAddCardStateResponse;
import ru.tinkoff.acquiring.sdk.responses.GetCardListResponse;
import ru.tinkoff.acquiring.sdk.responses.GetStateResponse;
import ru.tinkoff.acquiring.sdk.responses.InitResponse;
import ru.tinkoff.acquiring.sdk.responses.RemoveCardResponse;
import ru.tinkoff.acquiring.sdk.responses.SubmitRandomAmountResponse;

/**
 * @author Mikhail Artemyev
 */
public class AcquiringApi {

    public static final String API_ERROR_CODE_3DSV2_NOT_SUPPORTED = "106";

    private static final String API_URL_RELEASE = "https://securepay.tinkoff.ru/rest";
    private static final String API_URL_DEBUG = "https://rest-api-test.tcsbank.ru/rest";
    private static final String API_URL_RELEASE_V2 = "https://securepay.tinkoff.ru/v2";
    private static final String API_URL_DEBUG_V2 = "https://rest-api-test.tcsbank.ru/v2";
    private static final int STREAM_BUFFER_SIZE = 4096;
    private static final String API_REQUEST_METHOD = "POST";

    private static final String JSON = "application/json";
    private static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    private static final int TIMEOUT = 60000;

    private static final String[] oldMethods = {"Submit3DSAuthorization"};
    private static final List<String> oldMethodsList = Arrays.asList(oldMethods);

    private static final String[] performedErrorCodes = {"0", "104"};
    private static final List<String> performedErrorCodesList = Arrays.asList(performedErrorCodes);

    static String getUrl(String apiMethod) {
        if (useV1Api(apiMethod)) {
            return Journal.isDeveloperMode() ? API_URL_DEBUG : API_URL_RELEASE;
        } else {
            return Journal.isDeveloperMode() ? API_URL_DEBUG_V2 : API_URL_RELEASE_V2;
        }
    }

    static boolean useV1Api(String apiMethod) {
        return oldMethodsList.contains(apiMethod);
    }

    private final Gson gson;

    AcquiringApi() {
        this.gson = createGson();
    }

    InitResponse init(final InitRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, InitResponse.class);
    }

    Check3dsVersionResponse check3DsVersion(Check3dsVersionRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, Check3dsVersionResponse.class);
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

    AddCardResponse addCard(final AddCardRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, AddCardResponse.class);
    }

    AttachCardResponse attachCard(final AttachCardRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, AttachCardResponse.class);
    }

    GetAddCardStateResponse getAddCardState(final GetAddCardStateRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, GetAddCardStateResponse.class);
    }

    SubmitRandomAmountResponse submitRandomAmount(final SubmitRandomAmountRequest request) throws AcquiringApiException, NetworkException {
        return performRequest(request, SubmitRandomAmountResponse.class);
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
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            Journal.log(String.format("=== Sending %s request to %s", API_REQUEST_METHOD, targetUrl.toString()));

            if (!requestBody.isEmpty()) {
                Journal.log(String.format("===== Parameters: %s", requestBody));
                byte[] requestBodyBytes = requestBody.getBytes();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-type", useV1Api(request.getApiMethod()) ? FORM_URL_ENCODED : JSON);
                connection.setRequestProperty("Content-length", String.valueOf(requestBodyBytes.length));

                if (request instanceof FinishAuthorizeRequest && ((FinishAuthorizeRequest) request).is3DsVersionV2()) {
                    connection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                    connection.setRequestProperty("Accept", JSON);
                }

                requestContentStream = connection.getOutputStream();
                requestContentStream.write(requestBodyBytes);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                responseReader = new InputStreamReader(connection.getInputStream());
                final String response = read(responseReader);
                Journal.log(String.format("=== Got server success response: %s", response));
                result = gson.fromJson(response, responseClass);
            } else {
                responseReader = new InputStreamReader(connection.getErrorStream());
                final String response = read(responseReader);
                Journal.log(String.format("=== Got server error response: %s", response));
                throw new NetworkException("Unable to execute request " + request.getApiMethod());
            }

        } catch (IOException e) {
            throw new NetworkException("Unable to execute request " + request.getApiMethod(), e);
        } finally {
            closeQuietly(requestContentStream);
            closeQuietly(responseReader);
        }

        if (performedErrorCodesList.contains(result.getErrorCode())) {
            return result;
        }

        if (!result.isSuccess()) {
            String message = result.getMessage();
            String details = result.getDetails();
            if (message != null && details != null) {
                throw new AcquiringApiException(result, String.format("%s: %s", message, details));
            } else if (message != null) {
                throw new AcquiringApiException(result, message);
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
        if (useV1Api(apiMethod)) {
            return encodeRequestBody(params);
        } else {
            return jsonRequestBody(params);
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
                .excludeFieldsWithModifiers(Modifier.TRANSIENT)
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
