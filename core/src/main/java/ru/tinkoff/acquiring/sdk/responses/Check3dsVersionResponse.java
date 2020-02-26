package ru.tinkoff.acquiring.sdk.responses;

import com.google.gson.annotations.SerializedName;

import ru.tinkoff.acquiring.sdk.ThreeDsVersion;

/**
 * @author Mariya Chernyadieva
 */
public class Check3dsVersionResponse extends AcquiringResponse {

    @SerializedName("Version")
    private String version;

    @SerializedName("TdsServerTransID")
    private String serverTransId;

    @SerializedName("ThreeDSMethodURL")
    private String threeDsMethodUrl;

    public ThreeDsVersion getVersion() {
        if (version == null) {
            throw new IllegalStateException("Incorrect 3DS Version!");
        }
        return ThreeDsVersion.fromValue(version);
    }

    public String getServerTransId() {
        return serverTransId;
    }

    public String getThreeDsMethodUrl() {
        return threeDsMethodUrl;
    }
}
