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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.tinkoff.acquiring.sdk.CryptoUtils;
import ru.tinkoff.acquiring.sdk.Journal;

/**
 * @author Mikhail Artemyev
 */
abstract class AcquiringRequestBuilder<R extends AcquiringRequest> {

    private final static String PASSWORD_KEY = "Password";

    private final String password;
    private final String terminalKey;

    AcquiringRequestBuilder(final String password, final String terminalKey) {
        this.password = password;
        this.terminalKey = terminalKey;
    }

    protected abstract R getRequest();

    protected abstract void validate();

    public R build() {
        validate();

        final R request = getRequest();
        request.setTerminalKey(terminalKey);
        request.setToken(makeToken());

        return getRequest();
    }

    protected void validateNonNull(final Object value, final String name) {
        if (value == null) {
            throw new IllegalStateException(String.format("Unable to build request: field '%s' is null", name));
        }
    }

    protected void validateNonEmpty(final String value, final String name) {
        validateNonNull(value, name);
        if (value.trim().isEmpty()) {
            throw new IllegalStateException(String.format("Unable to build request: field '%s' is empty", name));
        }
    }

    protected void validateZeroOrPositive(final Long value, final String name) {
        validateNonNull(value, name);
        if (value < 0) {
            throw new IllegalStateException(String.format("Unable to build request: field '%s' is negative"));
        }
    }


    private String makeToken() {
        AcquiringRequest request = getRequest();
        Map<String, Object> parameters = request.asMap();

        parameters.remove(AcquiringRequest.TOKEN);
        parameters.put(PASSWORD_KEY, password);

        List<String> sortedKeys = new ArrayList<>(parameters.keySet());
        Collections.sort(sortedKeys);

        StringBuilder builder = new StringBuilder();
        Set<String> ignoredKes = request.getTokenIgnoreFields();
        for (final String key : sortedKeys) {
            if (!ignoredKes.contains(key)) {
                Journal.log(key);
                builder.append(parameters.get(key));
            }
        }

        return CryptoUtils.sha256(builder.toString());
    }
}
