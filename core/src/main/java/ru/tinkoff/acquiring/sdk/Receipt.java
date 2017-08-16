package ru.tinkoff.acquiring.sdk;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author Vitaliy Markus
 */
public class Receipt implements Serializable {

    @SerializedName("Items")
    private Item[] items;

    @SerializedName("Email")
    private String email;

    @SerializedName("Taxation")
    private Taxation taxation;

    @SerializedName("Phone")
    private String phone;

    public Receipt(Item[] items, String email, Taxation taxation) {
        this.items = items;
        this.email = email;
        this.taxation = taxation;
    }

    public Item[] getItems() {
        return items;
    }

    public String getEmail() {
        return email;
    }

    public Taxation getTaxation() {
        return taxation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
