package ru.tinkoff.acquiring.sdk;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.Locale;

import io.card.payment.CardIOActivity;

/**
 * @author Vitaliy Markus
 */
public class CameraCardIOScanner implements ICameraCardScanner {

    public void startActivityForScanning(Fragment fragment, int requestCode) {
        Intent scanIntent = createIntent(fragment.getActivity());
        fragment.startActivityForResult(scanIntent, requestCode);
    }

    public boolean hasResult(Intent data) {
        return data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT);
    }

    public ICreditCard parseIntentData(Intent data) {
        String cardNumber;
        String expireDate = "";
        String cvc = "";

        io.card.payment.CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
        cardNumber = scanResult.getFormattedCardNumber();
        if (scanResult.expiryMonth != 0 && scanResult.expiryYear != 0) {
            Locale locale = Locale.getDefault();
            int expiryYear = scanResult.expiryYear % 100;
            expireDate = String.format(locale, "%02d%02d", scanResult.expiryMonth, expiryYear);
        }

        return new CreditCard(cardNumber, expireDate, cvc);
    }

    @NonNull
    private Intent createIntent(Activity activity) {
        Intent scanIntent = new Intent(activity, CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true);
        return scanIntent;
    }
}

