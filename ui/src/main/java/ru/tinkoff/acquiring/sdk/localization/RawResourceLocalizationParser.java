package ru.tinkoff.acquiring.sdk.localization;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author a.shishkin1
 */
public class RawResourceLocalizationParser implements LocalizationParser<Integer>{

    private LocalizationParser<String> inner;
    private Context context;

    public RawResourceLocalizationParser(Context context, LocalizationParser<String> inner) {
        this.context = context;
        this.inner = inner;
    }

    @Override
    public AsdkLocalization parse(Integer resourceId) throws LocalizationParseException {
        InputStream is = context.getResources().openRawResource(resourceId);
        try {
            return inner.parse(Utility.read(is));
        } catch (IOException e) {
            throw new LocalizationParser.LocalizationParseException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
