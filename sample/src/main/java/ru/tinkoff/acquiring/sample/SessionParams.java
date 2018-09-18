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

package ru.tinkoff.acquiring.sample;

import com.google.android.gms.wallet.WalletConstants;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.tinkoff.acquiring.sdk.AcquiringSdk;

/**
 * @author Mikhail Artemyev
 */
public class SessionParams {

    public static int GPAY_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;
    public static boolean IS_DEVELOPER_MODE = true;

    public static final String DEFAULT_CUSTOMER_KEY = "user-key";
    public static final String DEFAULT_CUSTOMER_EMAIL = "user@example.com";
    public static final String TEST_SDK_CUSTOMER_KEY = "testCustomerKey1@gmail.com";
    public static final String TEST_SDK_CUSTOMER_EMAIL = "testCustomerKey1@gmail.com";

    private static final String DEFAULT_TERMINAL_ID = "dk3DS";
    private static final String SDK_TERMINAL_ID = "TestSDK";
    private static final String NON_3DS_TERMINAL_ID = "sdkNon3DS";

    private static final String PASSWORD = "12345678";
    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqBiorLS9OrFPezixO5lSsF+HiZPFQWDO\n" +
            "7x8gBJp4m86Wwz7ePNE8ZV4sUAZBqphdqSpXkybM4CJwxdj5R5q9+RHsb1dbMjThTXniwPpJdw4W\n" +
            "KqG5/cLDrPGJY9NnPifBhA/MthASzoB+60+jCwkFmf8xEE9rZdoJUc2p9FL4wxKQPOuxCqL2iWOx\n" +
            "AO8pxJBAxFojioVu422RWaQvoOMuZzhqUEpxA9T62lN8t3jj9QfHXaL4Ht8kRaa2JlaURtPJB5iB\n" +
            "M+4pBDnqObNS5NFcXOxloZX4+M8zXaFh70jqWfiCzjyhaFg3rTPE2ClseOdS7DLwfB2kNP3K0GuP\n" +
            "uLzsMwIDAQAB";

    public static final SessionParams TEST_SDK = new SessionParams(
            SDK_TERMINAL_ID, PASSWORD, PUBLIC_KEY, DEFAULT_CUSTOMER_KEY, DEFAULT_CUSTOMER_EMAIL
    );

    public static final SessionParams NON_3DS = new SessionParams(
            NON_3DS_TERMINAL_ID, PASSWORD, PUBLIC_KEY, DEFAULT_CUSTOMER_KEY, DEFAULT_CUSTOMER_EMAIL
    );

    public static final SessionParams DEFAULT = TEST_SDK;

    private static Map<String, SessionParams> terminals = new HashMap<String, SessionParams>() {

        private void addTerminal(SessionParams sessionParams) {
            put(sessionParams.terminalId, sessionParams);
        }

        {
            addTerminal(DEFAULT);
            addTerminal(TEST_SDK);
            addTerminal(NON_3DS);
        }
    };

    public static Collection<SessionParams> terminals() {
        return terminals.values();
    }

    public static SessionParams get(String terminalId) {
        return terminals.get(terminalId);
    }

    public final String terminalId;
    public final String secret;
    public final String publicKey;
    public final String customerKey;
    public final String customerEmail;

    public SessionParams(String terminalId, String secret, String publicKey, String customerKey, String customerEmail) {
        this.terminalId = terminalId;
        this.secret = secret;
        this.publicKey = publicKey;
        this.customerKey = customerKey;
        this.customerEmail = customerEmail;
    }
}
