package ru.tinkoff.acquiring.payment

/**
 * @author Stanislav Mukhametshin
 */
data class PaymentData @JvmOverloads constructor(
        val customerKey: String,
        val orderId: String,
        val coins: Long,
        val recurrentPayment: Boolean = false,
        val chargeMode: Boolean,
        val language: String? = null,
        val email: String? = null,
        val title: String? = null,
        val description: String? = null
)
