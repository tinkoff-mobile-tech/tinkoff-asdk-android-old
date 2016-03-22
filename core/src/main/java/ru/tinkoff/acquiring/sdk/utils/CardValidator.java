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

package ru.tinkoff.acquiring.sdk.utils;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author a.shishkin1
 */
public class CardValidator {
    private static final int[] allowedLengths = {16, 18, 19, 22};

    private static final String ZERO_NUMBERS_CARD_NUMBER_REGEXP = "[0]{1,}";

    public static boolean validateNumber(String cardNumber) {
        if (isEmpty(cardNumber)) {
            return false;
        }

        if (RegexpValidator.validate(cardNumber, ZERO_NUMBERS_CARD_NUMBER_REGEXP)){
            return false;
        }

        boolean lengthAllowed = false;

        for (int allowedLength : allowedLengths) {
            if (cardNumber.length() == allowedLength) {
                lengthAllowed = true;
            }
        }

        return lengthAllowed && validateWithLuhnAlgorithm(cardNumber);
    }



    public static boolean validateSecurityCode(String cvc) {
        if (isEmpty(cvc)) {
            return false;
        }

        return RegexpValidator.validate(cvc, "^[0-9]{3}$");
    }

    public static boolean validateExpirationDate(String expiryDate) {
        if (isEmpty(expiryDate) || expiryDate.length() != 5) {
            return false;
        }

        int month;
        int year;

        try {
            month = Integer.parseInt(expiryDate.substring(0, 2));
            year = Integer.parseInt(expiryDate.substring(3, 5));
        } catch (NumberFormatException e) {
            return false;
        }

        if (month >= 1 && month <= 12) {
            Calendar c = Calendar.getInstance();
            String currentYearStr = Integer.toString(c.get(Calendar.YEAR)).substring(2);
            int currentMonth = c.get(Calendar.MONTH) + 1;
            int currentYear = Integer.parseInt(currentYearStr);
            if (year == currentYear && month >= currentMonth){
                return true;
            }
            if (year > currentYear && year <= currentYear + 20) {
                return true;
            }
        }

        return false;
    }

    // http://en.wikipedia.org/wiki/Luhn_algorithm
    private static boolean validateWithLuhnAlgorithm(String cardNumber) {
        int sum = 0;
        int value = 0;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            try {
                value = Integer.parseInt(cardNumber.substring(i, i + 1));
            } catch (NumberFormatException ex) {
                return false;
            }
            boolean shouldBeDoubled = (cardNumber.length() - i) % 2 == 0;

            if (shouldBeDoubled) {
                value *= 2;
                sum += value > 9 ? 1 + value % 10 : value;
            } else {
                sum += value;
            }
        }

        return sum % 10 == 0;
    }

    public static class RegexpValidator {
        public RegexpValidator() {
        }

        public static boolean validate(CharSequence string, String regexp) {
            Pattern pattern = Pattern.compile(regexp);
            Matcher matcher = pattern.matcher(string);
            return matcher.matches();
        }
    }

    private static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
    }

}
