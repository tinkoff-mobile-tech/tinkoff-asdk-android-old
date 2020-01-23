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
    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5yse9ka3ZQE0feuGtem\n" +
            "Yv3IqOlLck8zHUM7lTr0za6lXTszRSXfUO7jMb+L5C7e2QNFs+7sIX2OQJ6a+HG8\n" +
            "kr+jwJ4tS3cVsWtd9NXpsU40PE4MeNr5RqiNXjcDxA+L4OsEm/BlyFOEOh2epGyY\n" +
            "Ud5/iO3OiQFRNicomT2saQYAeqIwuELPs1XpLk9HLx5qPbm8fRrQhjeUD5TLO8b+\n" +
            "4yCnObe8vy/BMUwBfq+ieWADIjwWCMp2KTpMGLz48qnaD9kdrYJ0iyHqzb2mkDhd\n" +
            "Izkim24A3lWoYitJCBrrB2xM05sm9+OdCI1f7nPNJbl5URHobSwR94IRGT7CJcUj\n" +
            "vwIDAQAB";

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
