package ru.tinkoff.acquiring.sdk;


public enum Language {

    RUSSIAN("ru"), ENGLISH("en");

    private String locale;

    public static Language fromValue(final String value, Language fallback) {
        if (value != null && value.toLowerCase().startsWith("ru")) {
            return RUSSIAN;
        }
        return fallback;
    }

    Language(final String locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        return locale;
    }
}
