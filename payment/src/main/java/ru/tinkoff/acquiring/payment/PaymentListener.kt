package ru.tinkoff.acquiring.payment

interface PaymentListener {

    fun onSuccess(paymentId: Long)

    fun onUiNeeded(paymentDataUi: PaymentDataUi)

    fun onError(exception: Exception)
}