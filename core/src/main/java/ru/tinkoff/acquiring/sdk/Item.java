package ru.tinkoff.acquiring.sdk;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author Vitaliy Markus
 * Информация о товаре.
 */
public class Item implements Serializable {

    @SerializedName("Name")
    private String name;

    @SerializedName("Price")
    private Long price;

    @SerializedName("Quantity")
    private double quantity;

    @SerializedName("Amount")
    private Long amount;

    @SerializedName("Tax")
    private Tax tax;

    @SerializedName("Ean13")
    private String ean13;

    @SerializedName("ShopCode")
    private String shopCode;

    @SerializedName("AgentData")
    private AgentData agentData;

    @SerializedName("SupplierInfo")
    private SupplierInfo supplierInfo;

    @SerializedName("PaymentObject")
    private PaymentObject paymentObject;

    @SerializedName("PaymentMethod")
    private PaymentMethod paymentMethod;

    /**
     * @param name     Наименование товара. Максимальная длина строки – 128 символов.
     * @param price    Сумма в копейках. Целочисленное значение не более 10 знаков.
     * @param quantity Количество/вес. Целая часть не более 8 знаков.
     * @param amount   Сумма в копейках. Целочисленное значение не более 10 знаков.
     * @param tax      Ставка налога.
     */
    public Item(String name, Long price, double quantity, Long amount, Tax tax) {
        this.name = name;
        this.price = price;
        this.quantity = round(quantity);
        this.amount = amount;
        this.tax = tax;
    }

    public String getName() {
        return name;
    }

    public Long getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public Long getAmount() {
        return amount;
    }

    public Tax getTax() {
        return tax;
    }

    public String getEan13() {
        return ean13;
    }

    public String getShopCode() {
        return shopCode;
    }

    /**
     * @param ean13 Штрих-код.
     */
    public void setEan13(String ean13) {
        this.ean13 = ean13;
    }

    /**
     * @param shopCode Код магазина
     */
    public void setShopCode(String shopCode) {
        this.shopCode = shopCode;
    }

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    public AgentData getAgentData() {
        return agentData;
    }

    public void setAgentData(AgentData agentData) {
        this.agentData = agentData;
    }

    public SupplierInfo getSupplierInfo() {
        return supplierInfo;
    }

    public void setSupplierInfo(SupplierInfo supplierInfo) {
        this.supplierInfo = supplierInfo;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentObject getPaymentObject() {
        return paymentObject;
    }

    public void setPaymentObject(PaymentObject paymentObject) {
        this.paymentObject = paymentObject;
    }
}
