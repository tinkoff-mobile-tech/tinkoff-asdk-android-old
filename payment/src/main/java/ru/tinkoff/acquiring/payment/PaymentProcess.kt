package ru.tinkoff.acquiring.payment

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.Card
import ru.tinkoff.acquiring.sdk.CardData
import ru.tinkoff.acquiring.sdk.CardStatus
import ru.tinkoff.acquiring.sdk.PaymentInfo
import ru.tinkoff.acquiring.sdk.ThreeDsData
import ru.tinkoff.acquiring.sdk.requests.InitRequestBuilder

/**
 * @author Stanislav Mukhametshin
 */
class PaymentProcess internal constructor() {

    private val handler = PaymentHandler()
    private lateinit var thread: Thread
    private lateinit var requestBuilder: InitRequestBuilder
    private var paymentListeners: Set<PaymentListener> = HashSet()
    private val paymentDataUi = PaymentDataUi()
    private var lastException: Exception? = null
    private var lastKnownAction: Int? = null
    private var paymentId: Long? = null
    private var state: Int = CREATED

    private companion object {
        private const val SUCCESS = 1
        private const val START_3DS = 2
        private const val CHARGE_REQUEST_REJECTED = 3
        private const val EXCEPTION = 4

        private const val CREATED = 0
        private const val EXECUTING = 1
        private const val FINISHED = 2
    }

    @JvmSynthetic
    internal fun initPaymentRequest(
            initRequestBuilder: InitRequestBuilder,
            paymentData: PaymentData,
            modifyRequest: InitRequestBuilder.() -> Unit
    ): PaymentProcess {
        requestBuilder = paymentData.run {
            initRequestBuilder.setOrderId(orderId)
                    .setAmount(coins)
                    .setChargeFlag(chargeMode)
                    .setCustomerKey(paymentData.customerKey)
                    .setRecurrent(recurrentPayment)
                    .also(modifyRequest)
        }
        paymentDataUi.recurrentPayment = paymentData.recurrentPayment
        return this
    }

    @JvmSynthetic
    internal fun initPaymentThread(sdk: AcquiringSdk,
                                   paySource: PaySource,
                                   email: String?,
                                   chargeMode: Boolean): PaymentProcess {
        paymentDataUi.card = (paySource as? CardDataPaySource)?.cardData?.map()

        thread = Thread(Runnable {
            try {
                val paymentId = sdk.init(requestBuilder)

                if (Thread.interrupted()) throw InterruptedException()

                handler.run {
                    if (!chargeMode || paySource is GPayTokenPaySource) {
                        val threeDsData = when (paySource) {
                            is CardDataPaySource -> sdk.finishAuthorize(paymentId, paySource.cardData, email)
                            is GPayTokenPaySource -> sdk.finishAuthorize(paymentId, paySource.token, email)
                            else -> throw IllegalArgumentException()
                        }

                        if (Thread.interrupted()) throw InterruptedException()

                        if (threeDsData.isThreeDsNeed) {
                            obtainMessage(START_3DS, threeDsData)
                        } else {
                            obtainMessage(SUCCESS, threeDsData.paymentId)
                        }
                    } else {
                        val cardData = (paySource as? CardDataPaySource)?.cardData
                                ?: throw IllegalArgumentException()
                        val paymentInfo = sdk.charge(paymentId, cardData.rebillId)
                        if (paymentInfo.isSuccess) {
                            obtainMessage(SUCCESS, paymentInfo.paymentId)
                        } else {
                            obtainMessage(CHARGE_REQUEST_REJECTED, paymentInfo)
                        }
                    }
                }.sendToTarget()
            } catch (e: Exception) {
                handler.obtainMessage(EXCEPTION, e).sendToTarget()
            }
        })
        return this
    }

    private fun CardData.map(): Card {
        val card = Card()
        card.pan = pan
        card.cardId = cardId
        card.rebillId = rebillId
        card.status = CardStatus.ACTIVE
        return card
    }

    fun subscribe(paymentListener: PaymentListener) {
        this.paymentListeners += paymentListener
        val action = lastKnownAction
        if (action != null) {
            sendToListener(action, paymentListener)
        }
    }

    fun unsubscribe(paymentListener: PaymentListener) {
        this.paymentListeners -= paymentListener
    }

    fun start(): PaymentProcess {
        if (state != CREATED) {
            throw IllegalStateException("Already in use create another PaymentProcess")
        }
        state = EXECUTING
        thread.start()
        return this
    }

    fun stop() {
        thread.interrupt()
        state = FINISHED
    }

    private fun sendToListeners(action: Int) {
        lastKnownAction = action
        paymentListeners.forEach { sendToListener(action, it) }
    }

    private fun sendToListener(action: Int, listener: PaymentListener) {
        listener.apply {
            when (action) {
                SUCCESS -> onCompleted(paymentId ?: return)
                CHARGE_REQUEST_REJECTED, START_3DS -> onUiNeeded(paymentDataUi)
                EXCEPTION -> onError(lastException ?: return)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    inner class PaymentHandler : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                SUCCESS -> paymentId = msg.obj as Long
                EXCEPTION -> lastException = (msg.obj as Exception)
                CHARGE_REQUEST_REJECTED -> {
                    paymentDataUi.paymentInfo = msg.obj as PaymentInfo
                    paymentDataUi.status = PaymentDataUi.Status.REJECTED
                }
                START_3DS -> {
                    paymentDataUi.threeDsData = msg.obj as ThreeDsData
                    paymentDataUi.status = PaymentDataUi.Status.THREE_DS_NEEDED
                }
            }
            sendToListeners(msg.what)
            state = FINISHED
        }
    }
}
