package ru.tinkoff.acquiring.sdk;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import ru.tinkoff.acquiring.sdk.responses.AttachCardResponse;

/**
 * @author Vitaliy Markus
 */
public class AttachCardStatusDeserializer implements JsonDeserializer<AttachCardResponse.Status> {

    @Override
    public AttachCardResponse.Status deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String string = json.getAsString();
        return AttachCardResponse.Status.fromString(string);
    }
}
