package ru.tinkoff.acquiring.payment

import android.app.Activity
import ru.tinkoff.acquiring.sdk.*
import ru.tinkoff.acquiring.sdk.requests.InitRequestBuilder

/**
 * @author Stanislav Mukhametshin
 */
class TinkoffPay constructor(
        terminalKey: String,
        password: String,
        private val publicKey: String
) {

    private val sdk: AcquiringSdk = AcquiringSdk(terminalKey, password, publicKey)
    private var cardId: String? = null

    var paymentData: PaymentData? = null

    @JvmOverloads
    fun pay(card: CardData, paymentData: PaymentData, modifyRequest: InitRequestBuilder.() -> Unit = {}): PaymentProcess {
        cardId = card.cardId
        return payInternal(CardDataPaySource(card), paymentData, modifyRequest)
    }

    @JvmOverloads
    fun pay(gpayToken: String, paymentData: PaymentData, modifyRequest: InitRequestBuilder.() -> Unit = {}): PaymentProcess {
        return payInternal(GPayTokenPaySource(gpayToken), paymentData, modifyRequest)
    }

    private fun payInternal(paySource: PaySource, paymentData: PaymentData, modifyRequest: InitRequestBuilder.() -> Unit): PaymentProcess {
        val initRequestBuilder = InitRequestBuilder(sdk.password, sdk.terminalKey)
        this.paymentData = paymentData

        return PaymentProcess()
                .initPaymentRequest(initRequestBuilder, paymentData, modifyRequest)
                .initPaymentThread(sdk, paySource, paymentData.email, paymentData.chargeMode)
    }

    @JvmOverloads
    fun launchUi(activity: Activity,
                 paymentData: PaymentData,
                 paymentDataUi: PaymentDataUi,
                 requestCode: Int,
                 additionalParams: PayFormStarter.() -> PayFormStarter = { this }) {
        PayFormActivity
                .init(sdk.terminalKey, sdk.password, publicKey)
                .prepare(paymentData.orderId,
                        Money.ofCoins(paymentData.coins),
                        paymentData.title,
                        paymentData.description,
                        paymentDataUi.paymentInfo?.cardId ?: cardId,
                        paymentData.email,
                        paymentDataUi.recurrentPayment,
                        true)
                .setCustomerKey(paymentData.customerKey)
                .setChargeMode(paymentDataUi.recurrentPayment)
                .useFirstAttachedCard(true)
                .addPaymentUiData(paymentDataUi)
                .setTheme(R.style.AcquiringTheme)
                .apply {
                    paymentData.marketPlaceData?.apply {
                        setShops(shops, receipts)
                    }
                }
                .additionalParams()
                .startActivityForResult(activity, requestCode)
    }

    private fun PayFormStarter.addPaymentUiData(paymentDataUi: PaymentDataUi): PayFormStarter {
        paymentDataUi.apply {
            if (card != null && paymentInfo != null) {
                intent.putExtra(TAcqIntentExtra.EXTRA_CARD_DATA, CardsArrayBundlePacker().pack(arrayOf(paymentDataUi.card)))
                intent.putExtra(TAcqIntentExtra.EXTRA_PAYMENT_INFO, PaymentInfoBundlePacker().pack(paymentDataUi.paymentInfo))
            } else if (threeDsData != null) {
                intent.putExtra(TAcqIntentExtra.EXTRA_THREE_DS, ThreeDsBundlePacker().pack(threeDsData))
            }
        }
        return this
    }
}
