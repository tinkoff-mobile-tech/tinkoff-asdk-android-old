package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 */
public interface IPaymentRejectPerformer {

    void onChargeRequestRejected(PaymentInfo paymentInfo);

    void onThreeDsV2Rejected();
}
