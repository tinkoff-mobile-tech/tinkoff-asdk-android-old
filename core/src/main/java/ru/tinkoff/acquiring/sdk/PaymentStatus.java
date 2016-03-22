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

/**
 * @author Mikhail Artemyev
 */
public enum PaymentStatus {
    NEW,
    CANCELLED,
    PREAUTHORIZING,
    FORMSHOWED,
    AUTHORIZING,
    THREE_DS_CHECKING,
    THREE_DS_CHECKED,
    AUTHORIZED,
    REVERSING,
    REVERSED,
    CONFIRMING,
    CONFIRMED,
    REFUNDING,
    REFUNDED,
    REJECTED,
    UNKNOWN;

    private static final String TDS_CHECKING_STRING = "3DS_CHECKING";
    private static final String TDS_CHECKED_STRING = "3DS_CHECKED";

    public static PaymentStatus fromString(final String stringValue) {
        switch (stringValue) {
            case TDS_CHECKING_STRING:
                return THREE_DS_CHECKING;
            case TDS_CHECKED_STRING:
                return THREE_DS_CHECKED;
            default:
                return PaymentStatus.valueOf(stringValue);
        }
    }

    @Override
    public String toString() {
        if (this == THREE_DS_CHECKING) {
            return TDS_CHECKING_STRING;
        }

        if (this == THREE_DS_CHECKED) {
            return TDS_CHECKED_STRING;
        }

        return super.toString();
    }
}
