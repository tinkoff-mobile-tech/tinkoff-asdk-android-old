package ru.tinkoff.acquiring.sdk;

import android.os.Bundle;

/**
 * @author Stanislav Mukhametshin
 */
public class PaymentInfoBundlePacker implements IBundlePacker<PaymentInfo> {

    private static final String EXTRA_ORDER_ID = "extra_order_id";
    private static final String EXTRA_PAYMENT_ID = "extra_payment_id";
    private static final String EXTRA_AMOUNT = "extra_amount";
    private static final String EXTRA_CARD_ID = "extra_card_id";
    private static final String EXTRA_ERROR_CODE = "extra_error_code";

    @Override
    public PaymentInfo unpack(Bundle bundle) {
        String orderId = bundle.getString(EXTRA_ORDER_ID);
        long paymentId = bundle.getLong(EXTRA_PAYMENT_ID);
        long amount = bundle.getLong(EXTRA_AMOUNT);
        String cardId = bundle.getString(EXTRA_CARD_ID);
        String errorCode = bundle.getString(EXTRA_ERROR_CODE);
        return new PaymentInfo(orderId, paymentId, amount, cardId, errorCode);
    }

    @Override
    public Bundle pack(PaymentInfo entity) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ORDER_ID, entity.getOrderId());
        bundle.putLong(EXTRA_PAYMENT_ID, entity.getPaymentId());
        bundle.putLong(EXTRA_AMOUNT, entity.getAmount());
        bundle.putString(EXTRA_CARD_ID, entity.getCardId());
        bundle.putString(EXTRA_ERROR_CODE, entity.getErrorCode());
        return bundle;
    }
}
