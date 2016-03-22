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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * @author Mikhail Artemyev
 */
public class CardStatusSerializer implements
        JsonSerializer<CardStatus>,
        JsonDeserializer<CardStatus> {

    @Override
    public CardStatus deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json != null) {
            final String stringRepresentation = json.getAsString();
            if (stringRepresentation.length() != 1) {
                throw new JsonParseException("Card Status has wrong format: " + stringRepresentation);
            }

            return CardStatus.fromChar(stringRepresentation.charAt(0));
        }

        return null;
    }

    @Override
    public JsonElement serialize(CardStatus src, Type typeOfSrc, JsonSerializationContext context) {
        if (src != null) {
            return new JsonPrimitive(src.toString());
        }

        return null;
    }
}
