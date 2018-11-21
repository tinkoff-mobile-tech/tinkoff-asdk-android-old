package ru.tinkoff.acquiring.payment

interface PaymentListener {

    fun onCompleted(paymentId: Long)

    fun onUiNeeded(paymentDataUi: PaymentDataUi)

    fun onError(exception: Exception)
}