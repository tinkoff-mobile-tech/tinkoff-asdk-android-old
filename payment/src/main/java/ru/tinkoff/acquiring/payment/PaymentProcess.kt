package ru.tinkoff.acquiring.payment

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import ru.tinkoff.acquiring.sdk.*
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
    private var state : Int = CREATED

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
    internal fun initPaymentRequest(initRequestBuilder: InitRequestBuilder,
                                    customerKey: String,
                                    paymentData: PaymentData): PaymentProcess {
        requestBuilder = paymentData.run {
            initRequestBuilder.setOrderId(orderId)
                    .setAmount(coins)
                    .setChargeFlag(chargeMode)
                    .setCustomerKey(customerKey)
                    .setRecurrent(recurrentPayment)
        }
        paymentDataUi.recurrentPayment = paymentData.recurrentPayment
        return this
    }

    @JvmSynthetic
    internal fun initPaymentThread(sdk: AcquiringSdk,
                                   cardData: CardData,
                                   email: String?,
                                   chargeMode: Boolean): PaymentProcess {
        paymentDataUi.card = cardData.map()

        thread = Thread(Runnable {
            try {
                val paymentId = sdk.init(requestBuilder)

                if (Thread.interrupted()) throw InterruptedException()

                handler.run {
                    if (!chargeMode) {
                        val threeDsData = sdk.finishAuthorize(paymentId, cardData, email)
                        if (Thread.interrupted()) throw InterruptedException()

                        if (threeDsData.isThreeDsNeed) {
                            obtainMessage(START_3DS, threeDsData)
                        } else {
                            obtainMessage(SUCCESS)
                        }
                    } else {
                        val paymentInfo = sdk.charge(paymentId, cardData.rebillId)
                        if (paymentInfo.isSuccess) {
                            obtainMessage(SUCCESS)
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
        sendToListener(lastKnownAction ?: return, paymentListener)
    }

    fun unsubscribe(paymentListener: PaymentListener) {
        this.paymentListeners -= paymentListener
    }

    fun start(): PaymentProcess {
        if (state != CREATED) {
            throw IllegalStateException("already in use create another PaymentProcess")
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
                SUCCESS -> onCompleted()
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
                CHARGE_REQUEST_REJECTED -> {
                    paymentDataUi.paymentInfo = msg.obj as PaymentInfo
                    paymentDataUi.status = PaymentDataUi.Status.REJECTED
                }
                START_3DS -> {
                    paymentDataUi.threeDsData = msg.obj as ThreeDsData
                    paymentDataUi.status = PaymentDataUi.Status.THREE_DS_NEEDED
                }
                EXCEPTION -> lastException = (msg.obj as Exception)
            }
            sendToListeners(msg.what)
            state = FINISHED
        }
    }

    interface PaymentListener {

        fun onCompleted()

        fun onUiNeeded(paymentDataUi: PaymentDataUi)

        fun onError(exception: Exception)
    }
}
