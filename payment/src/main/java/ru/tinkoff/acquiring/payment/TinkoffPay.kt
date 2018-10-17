package ru.tinkoff.acquiring.payment

import android.app.Activity
import ru.tinkoff.acquiring.sdk.*
import ru.tinkoff.acquiring.sdk.requests.InitRequestBuilder
import java.lang.IllegalArgumentException

/**
 * @author Stanislav Mukhametshin
 */
class TinkoffPay private constructor(private var sdk: AcquiringSdk,
                                     private val customerKey: String,
                                     private val email: String?,
                                     private val publicKey: String) {

    var card: CardData? = null
    var paymentData: PaymentData? = null

    fun pay(card: CardData, paymentData: PaymentData): PaymentProcess {
        val initRequestBuilder = InitRequestBuilder(sdk.password, sdk.terminalKey)
        this.card = card
        this.paymentData = paymentData

        return PaymentProcess()
                .initPaymentRequest(initRequestBuilder, customerKey, paymentData)
                .initPaymentThread(sdk, card, email, paymentData.chargeMode)
    }

    fun launchUi(activity: Activity, paymentData: PaymentData, paymentDataUi: PaymentDataUi, requestCode: Int) {
        PayFormActivity
                .init(sdk.terminalKey, sdk.password, publicKey)
                .prepare(paymentData.orderId,
                        Money.ofCoins(paymentData.coins),
                        "",
                        "",
                        paymentDataUi.paymentInfo?.cardId,
                        email,
                        paymentDataUi.recurrentPayment,
                        true)
                .setCustomerKey(customerKey)
                .setChargeMode(paymentDataUi.recurrentPayment)
                .useFirstAttachedCard(true)
                .addPaymentUiData(paymentDataUi)
                .setTheme(R.style.AcquiringTheme)
                .startActivityForResult(activity, requestCode)
    }

    private fun PayFormStarter.addPaymentUiData(paymentDataUi: PaymentDataUi): PayFormStarter {
        paymentDataUi.apply {
            if (card != null && paymentInfo != null) {
                intent.putExtra(PayFormActivity.EXTRA_CARD_DATA, CardsArrayBundlePacker().pack(arrayOf(paymentDataUi.card)))
                intent.putExtra(PayFormActivity.EXTRA_PAYMENT_INFO, PaymentInfoBundlePacker().pack(paymentDataUi.paymentInfo))
            } else if (threeDsData != null) {
                intent.putExtra(PayFormActivity.EXTRA_THREE_DS, ThreeDsBundlePacker().pack(threeDsData))
            }
        }
        return this
    }

    class Builder(private val sdk: AcquiringSdk,
                  private var publicKey: String) {

        private var customerKey: String? = null
        private var email: String? = null

        companion object {
            @JvmStatic
            fun init(terminalKey: String, password: String, publicKey: String): Builder {
                val sdk = AcquiringSdk(terminalKey, password, publicKey)
                return Builder(sdk, publicKey)
            }
        }

        fun setCustomerKey(customerKey: String): Builder = apply { this.customerKey = customerKey }

        fun setEmail(email: String) = apply { this.email = email }

        fun build(): TinkoffPay {
            val customerKey = customerKey
                    ?: throw IllegalArgumentException("CustomerKey is not set")
            return TinkoffPay(sdk, customerKey, email, publicKey)
        }
    }
}
