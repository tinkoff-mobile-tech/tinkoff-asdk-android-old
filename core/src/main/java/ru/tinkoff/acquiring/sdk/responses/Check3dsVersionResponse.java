package ru.tinkoff.acquiring.sdk.responses;

import com.google.gson.annotations.SerializedName;

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

    public String getVersion() {
        return version;
    }

    public String getServerTransId() {
        return serverTransId;
    }

    public String getThreeDsMethodUrl() {
        return threeDsMethodUrl;
    }
}
