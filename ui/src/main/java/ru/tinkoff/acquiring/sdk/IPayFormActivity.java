package ru.tinkoff.acquiring.sdk;

import androidx.annotation.Nullable;

import ru.tinkoff.acquiring.sdk.responses.Check3dsVersionResponse;

/**
 * @author Vitaliy Markus
 */
public interface IPayFormActivity extends IBaseSdkActivity {

    void onCardsReady(Card[] cards);

    void onDeleteCard(Card card);

    void onPaymentInitCompleted(Long paymentId);

    void onChargeRequestRejected(PaymentInfo paymentInfo);

    void onThreeDsV2Rejected();

    void onGooglePayError();

    void collect3dsData(@Nullable Check3dsVersionResponse response);
}
