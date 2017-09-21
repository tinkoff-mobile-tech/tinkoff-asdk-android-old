package ru.tinkoff.acquiring.sdk;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * @author Vitaliy Markus
 */
public class TaxSerializer implements JsonSerializer<Tax> {

    @Override
    public JsonElement serialize(Tax src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
