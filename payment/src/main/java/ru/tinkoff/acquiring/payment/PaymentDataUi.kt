package ru.tinkoff.acquiring.payment

import ru.tinkoff.acquiring.sdk.Card
import ru.tinkoff.acquiring.sdk.PaymentInfo
import ru.tinkoff.acquiring.sdk.ThreeDsData

/**
 * @author Stanislav Mukhametshin
 */
class PaymentDataUi internal constructor() {

    internal var paymentInfo: PaymentInfo? = null
    internal var recurrentPayment: Boolean = false
    internal var card: Card? = null
    internal var threeDsData: ThreeDsData? = null
    var status: Status? = null

    enum class Status {
        REJECTED,
        THREE_DS_NEEDED
    }
}
