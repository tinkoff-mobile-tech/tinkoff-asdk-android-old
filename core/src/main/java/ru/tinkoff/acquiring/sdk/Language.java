package ru.tinkoff.acquiring.sdk;


public enum Language {

    RUSSIAN("ru"), ENGLISH("en");

    private String locale;

    Language(final String locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        return locale;
    }
}
