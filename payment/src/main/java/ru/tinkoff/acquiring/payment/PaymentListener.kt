package ru.tinkoff.acquiring.payment

import ru.tinkoff.acquiring.sdk.responses.Check3dsVersionResponse

interface PaymentListener {

    fun onSuccess(paymentId: Long)

    fun onUiNeeded(paymentDataUi: PaymentDataUi)

    fun onCollectDeviceData(response: Check3dsVersionResponse?) : Map<String, String>?

    fun onError(exception: Exception)
}