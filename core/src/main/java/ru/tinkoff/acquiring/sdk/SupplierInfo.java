package ru.tinkoff.acquiring.sdk;

import com.google.gson.annotations.SerializedName;

public class SupplierInfo {

    @SerializedName("Phones")
    private String[] phones;

    @SerializedName("Name")
    private String name;

    @SerializedName("Inn")
    private String inn;

    public String[] getPhones() {
        return phones;
    }

    public void setPhones(String[] phones) {
        this.phones = phones;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

}
