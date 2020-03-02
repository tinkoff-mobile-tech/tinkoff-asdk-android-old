package ru.tinkoff.acquiring.sdk;

/**
 * @author Mariya Chernyadieva
 */
public enum ThreeDsVersion {

    ONE("1"),
    TWO("2");

    private final String version;

    ThreeDsVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return version;
    }

    public static ThreeDsVersion fromValue(String value) {
        for (ThreeDsVersion version : values()) {
            if (value.startsWith(version.toString())) {
                return version;
            }
        }
        throw new IllegalStateException("Unknown 3SD version");
    }
}
