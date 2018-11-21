package ru.tinkoff.acquiring.payment

import ru.tinkoff.acquiring.sdk.CardData

/**
 * @author a.shishkin1
 */

internal interface PaySource

internal class CardDataPaySource(val cardData: CardData) : PaySource

internal class GPayTokenPaySource(val token: String) : PaySource