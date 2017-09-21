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

import ru.tinkoff.acquiring.sdk.utils.CardValidator;

/**
 * @author Mikhail Artemyev
 */
public class CardData {

    private static final String KEY_CARD_ID = "CardId";
    private static final String KEY_PAN = "PAN";
    private static final String KEY_DATE = "ExpDate";
    private static final String KEY_CVV = "CVV";

    private String pan;
    private String expiryDate;
    private String securityCode;
    private String cardId;
    private String rebillId;

    public CardData(String pan, String expiryDate, String securityCode) {
        this.pan = pan;
        this.expiryDate = expiryDate;
        this.securityCode = securityCode;
    }

    public CardData(String cardId, String securityCode) {
        this.securityCode = securityCode;
        this.cardId = cardId;
    }

    public CardData(String rebillId) {
        this.rebillId = rebillId;
    }

    public String encode(final PublicKey publicKey) {
        validate();
        String mergedData;
        if (cardId != null) {
            mergedData = String.format("%s=%s;%s=%s",
                    KEY_CARD_ID, cardId,
                    KEY_CVV, securityCode);

        } else {
            String date = expiryDate.replaceAll("\\D", "");
            mergedData = String.format("%s=%s;%s=%s;%s=%s",
                    KEY_PAN, pan,
                    KEY_DATE, date,
                    KEY_CVV, securityCode);
        }

        return CryptoUtils.encodeBase64(CryptoUtils.encryptRsa(mergedData, publicKey));
    }


    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public String getRebillId() {
        return rebillId;
    }

    private void validate() {
        String wrongField = null;

        if (cardId == null) {
            if (!CardValidator.validateNumber(pan)) {
                wrongField = "number";
            } else if (!CardValidator.validateExpirationDate(expiryDate)) {
                wrongField = "expiration date";
            }
        }

        if (!CardValidator.validateSecurityCode(securityCode) && wrongField == null) {
            wrongField = "security code";
        }

        if (wrongField != null) {
            throw new IllegalStateException("Cannot encode card data. Wrong " + wrongField);
        }
    }
}
