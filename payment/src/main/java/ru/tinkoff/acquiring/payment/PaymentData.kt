package ru.tinkoff.acquiring.payment

import ru.tinkoff.acquiring.sdk.Receipt
import ru.tinkoff.acquiring.sdk.Shop

/**
 * @author Stanislav Mukhametshin
 */
data class PaymentData @JvmOverloads constructor(
        val customerKey: String,
        val orderId: String,
        val coins: Long,
        val recurrentPayment: Boolean = false,
        val chargeMode: Boolean,
        val marketPlaceData: MarketPlaceData? = null,
        val language: String? = null,
        val email: String? = null,
        val title: String? = null,
        val description: String? = null
)

data class MarketPlaceData(
        val shops: ArrayList<Shop>,
        val receipts: ArrayList<Receipt>?
)