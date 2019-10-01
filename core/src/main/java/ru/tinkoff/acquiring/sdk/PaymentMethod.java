package ru.tinkoff.acquiring.sdk;

import com.google.gson.annotations.SerializedName;

public enum PaymentMethod {

    /**
     * Предоплата 100%. Полная
     * предварительная оплата до момента передачи
     * предмета расчета
     */
    @SerializedName("full_prepayment")
    FULL_PREPAYMENT,

    /**
     * Предоплата. Частичная
     * предварительная оплата до момента передачи
     * предмета расчета.
     */
    @SerializedName("prepayment")
    PREPAYMENT,

    /**
     * Аванс.
     */
    @SerializedName("advance")
    ADVANCE,

    /**
     * Полный расчет. Полная
     * оплата, в том числе с учетом аванса (предварительной оплаты)
     */
    @SerializedName("full_payment")
    FULL_PAYMENT,

    /**
     * Частичный расчет и кредит.
     * Частичная оплата предмета расчета в момент
     * его передачи с последующей оплатой в кредит.
     */
    @SerializedName("partial_payment")
    PARTIAL_PAYMENT,

    /**
     * Передача в кредит. Передача
     * предмета расчета без его оплаты в момент его
     * передачи с последующей оплатой в кредит
     */
    @SerializedName("credit")
    CREDIT,

    /**
     * Оплата кредита. Оплата
     * предмета расчета после его передачи с оплатой
     * в кредит.
     */
    @SerializedName("credit_payment")
    CREDIT_PAYMENT

}
