package ru.tinkoff.acquiring.payment

/**
 * @author Stanislav Mukhametshin
 */
data class PaymentData(val orderId: String,
                       val coins: Long,
                       val recurrentPayment: Boolean = false,
                       val chargeMode: Boolean,
                       val language: String? = null)
