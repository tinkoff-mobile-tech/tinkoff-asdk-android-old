package ru.tinkoff.acquiring.sdk;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Shop implements Serializable {

    @SerializedName("ShopCode")
    private String shopCode;
    @SerializedName("Name")
    private String name;
    @SerializedName("Amount")
    private long amount;

    public Shop(String shopCode, String name, long amount) {
        this.shopCode = shopCode;
        this.name = name;
        this.amount = amount;
    }

    public String getShopCode() {
        return shopCode;
    }

    public void setShopCode(String shopCode) {
        this.shopCode = shopCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
