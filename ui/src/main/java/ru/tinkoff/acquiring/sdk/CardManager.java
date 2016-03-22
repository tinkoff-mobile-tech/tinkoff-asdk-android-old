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

import java.util.HashMap;
import java.util.Map;

/**
 * @author a.shishkin1
 */
public class CardManager {

    private static Map<String, Card[]> cards = new HashMap<>();

    private AcquiringSdk sdk;

    public CardManager(AcquiringSdk sdk) {
        this.sdk = sdk;
    }


    public Card[] getCards(String customerKey) {
        Card[] result = cards.get(customerKey);
        if (result == null) {
            result = sdk.getCardList(customerKey);
            cards.put(customerKey, result);
        }
        return result;
    }

    public Card getCardById(String cardId) {
        for (Card[] cardArr : cards.values()) {
            for (Card card : cardArr) {
                if (cardId.equals(card.getCardId())) {
                    return card;
                }
            }
        }
        return null;
    }

    public void clear(String customerKey) {
        cards.remove(customerKey);
    }
}
