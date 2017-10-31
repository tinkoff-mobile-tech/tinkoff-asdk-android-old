package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 */
public interface IChargeRejectPerformer {

    void onChargeRequestRejected(PaymentInfo paymentInfo);
}
