package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 */
public interface IPayFormActivity extends IBaseSdkActivity {

    void onCardsReady(Card[] cards);

    void onDeleteCard(Card card);

    void onPaymentInitCompleted(Long paymentId);

    void onChargeRequestRejected(PaymentInfo paymentInfo);

    void onAndroidPayError();
}
