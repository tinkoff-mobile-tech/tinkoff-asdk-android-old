package ru.tinkoff.acquiring.sdk.localization;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author a.shishkin1
 */
public class FileLocalizationParser implements LocalizationParser<File> {

    private LocalizationParser<String> inner;

    public FileLocalizationParser(LocalizationParser<String> inner) {
        this.inner = inner;
    }

    @Override
    public AsdkLocalization parse(File file) throws LocalizationParseException {
        InputStream is = null;
        try {
            return inner.parse(Utility.read(is = new FileInputStream(file)));
        } catch (IOException e) {
            throw new LocalizationParser.LocalizationParseException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
