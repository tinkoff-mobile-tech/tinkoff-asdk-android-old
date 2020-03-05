package ru.tinkoff.acquiring.payment

import ru.tinkoff.acquiring.sdk.Card
import ru.tinkoff.acquiring.sdk.DeviceDataStorage
import ru.tinkoff.acquiring.sdk.PaymentInfo
import ru.tinkoff.acquiring.sdk.ThreeDsData
import ru.tinkoff.acquiring.sdk.responses.Check3dsVersionResponse

/**
 * @author Stanislav Mukhametshin
 */
class PaymentDataUi internal constructor() {

    internal var paymentInfo: PaymentInfo? = null
    internal var recurrentPayment: Boolean = false
    internal var card: Card? = null
    internal var threeDsData: ThreeDsData? = null
    internal var check3dsVersionResponse: Check3dsVersionResponse? = null
    internal val deviceDataStorage: DeviceDataStorage = DeviceDataStorage()
    var status: Status? = null

    enum class Status {
        REJECTED,
        COLLECT_3DS_DATA,
        THREE_DS_NEEDED
    }
}
