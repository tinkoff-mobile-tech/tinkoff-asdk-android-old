package ru.tinkoff.acquiring.payment

interface PaymentListener {

    fun onCompleted()

    fun onUiNeeded(paymentDataUi: PaymentDataUi)

    fun onError(exception: Exception)
}