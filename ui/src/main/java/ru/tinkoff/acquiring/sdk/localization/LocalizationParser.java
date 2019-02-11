package ru.tinkoff.acquiring.sdk.localization;

/**
 * @author a.shishkin1
 */
public interface LocalizationParser<Source> {

    AsdkLocalization parse(Source source) throws LocalizationParseException;

    class LocalizationParseException extends Exception {
        public LocalizationParseException(String message, Throwable cause) {
            super(message, cause);
        }

        public LocalizationParseException(Throwable cause) {
            super(cause);
        }
    }
}
