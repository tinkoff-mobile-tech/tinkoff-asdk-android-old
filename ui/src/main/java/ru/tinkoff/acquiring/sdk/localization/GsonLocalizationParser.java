package ru.tinkoff.acquiring.sdk.localization;

import com.google.gson.Gson;

import java.util.Map;

/**
 * @author a.shishkin1
 */
public class GsonLocalizationParser implements LocalizationParser<String> {

    @Override
    public AsdkLocalization parse(String s) throws LocalizationParseException {
        try {
            return new Gson().fromJson(s, AsdkLocalization.class);
        } catch (Exception e) {
            throw new LocalizationParseException(e);
        }
    }
}
