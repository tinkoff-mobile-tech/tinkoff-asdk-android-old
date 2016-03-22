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
public enum CardStatus {
    ACTIVE('A'), INACTIVE('I'), DELETED('D');

    public static CardStatus fromChar(final char literal) {
        switch (literal) {
            case 'A':
                return ACTIVE;
            case 'I':
                return INACTIVE;
            case 'D':
                return DELETED;
        }

        throw new IllegalArgumentException(
                String.format("Unknown literal '%c'. Cannot construct CardStatus", literal)
        );
    }

    private char literal;

    CardStatus(final char literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return String.valueOf(literal);
    }
}
