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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * @author Mikhail Artemyev
 */
public class Money implements Serializable, Comparable<Money> {

    private static final byte COINS_IN_RUBLE = 100;
    private static final byte PRECISION = 2;
    public static final String DEFAULT_INT_DIVIDER = " ";
    public static final String DEFAULT_INT_FRACT_DIVIDER = ",";

    private final long valueCoins;
    private String integralDivider = DEFAULT_INT_DIVIDER;
    private String integralFractionDivider = DEFAULT_INT_FRACT_DIVIDER;

    public static Money ofRubles(final long rubles) {
        return new Money(toCoins(rubles));
    }

    public static Money ofRubles(final BigDecimal value) {
        final BigDecimal precised = value.setScale(PRECISION, RoundingMode.HALF_EVEN);
        final BigDecimal coins = precised.multiply(new BigDecimal(COINS_IN_RUBLE, new MathContext(0)));
        return new Money(coins.longValue());
    }

    public static Money ofRubles(final double rubles) {
        return ofRubles(new BigDecimal(rubles));
    }

    public static Money ofCoins(final long coins) {
        return new Money(coins);
    }

    private static long toCoins(final long rubles) {
        return rubles * COINS_IN_RUBLE;
    }

    public Money() {
        this(0L);
    }

    private Money(long valueCoins) {
        this.valueCoins = valueCoins;
    }

    public long getCoins() {
        return valueCoins;
    }

    @Override
    public String toString() {
        final long fractional = valueCoins % COINS_IN_RUBLE;

        if (fractional == 0) {
            return formatIntPart(valueCoins);
        }

        return String.format("%s%s%s",
                formatIntPart(valueCoins),
                getIntegralFractionDivider(),
                formatFractionalPart(fractional));
    }

    private String formatFractionalPart(long fractional) {
        return String.format("%02d", fractional);
    }

    public String toHumanReadableString() {
        return toString() + " P";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Money money = (Money) o;

        return valueCoins == money.valueCoins;

    }

    @Override
    public int hashCode() {
        return (int) (valueCoins ^ (valueCoins >>> 32));
    }

    public String getIntegralDivider() {
        return integralDivider;
    }

    public void setIntegralDivider(String integralDivider) {
        this.integralDivider = integralDivider;
    }

    public String getIntegralFractionDivider() {
        return integralFractionDivider;
    }

    public void setIntegralFractionDivider(String integralFractionDivider) {
        this.integralFractionDivider = integralFractionDivider;
    }

    private String formatIntPart(final long decimal) {
        if (decimal < 100) {
            return "0";
        }

        String unformatted = String.valueOf(decimal);

        // trim last digits as they are fractional part
        unformatted = unformatted.substring(0, unformatted.length() - PRECISION);

        // separate first < 3 characters as 'head'
        final int headLength = unformatted.length() % 3;
        final StringBuilder result;
        if (headLength > 0) {
            result = new StringBuilder(unformatted.substring(0, headLength));
        } else {
            result = new StringBuilder();
        }

        for (int i = headLength; i < unformatted.length(); i++) {
            final int headlessOffset = i - headLength;

            // separate each 3 characters
            if (headlessOffset % 3 == 0 && i != unformatted.length() - 1) {
                result.append(getIntegralDivider());
            }

            result.append(unformatted.charAt(i));
        }

        return result.toString();
    }

    @Override
    public int compareTo(Money o) {
        return Long.valueOf(valueCoins).compareTo(o.getCoins());
    }


}
