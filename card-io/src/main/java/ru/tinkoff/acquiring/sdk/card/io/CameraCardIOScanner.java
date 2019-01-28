package ru.tinkoff.acquiring.sdk.card.io;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.Locale;

import io.card.payment.CardIOActivity;
import ru.tinkoff.acquiring.sdk.CreditCard;
import ru.tinkoff.acquiring.sdk.ICameraCardScanner;
import ru.tinkoff.acquiring.sdk.ICreditCard;
import ru.tinkoff.acquiring.sdk.Language;
import ru.tinkoff.acquiring.sdk.TAcqIntentExtra;

/**
 * @author Vitaliy Markus
 */
public class CameraCardIOScanner implements ICameraCardScanner {

    @Override
    public void startActivityForScanning(Fragment fragment, int requestCode) {
        Intent scanIntent = createIntent(fragment.getActivity());
        fragment.startActivityForResult(scanIntent, requestCode);
    }

    @Override
    public boolean hasResult(@NonNull Intent data) {
        return data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT);
    }

    @NonNull
    @Override
    public ICreditCard parseIntentData(Intent data) {
        String cardNumber;
        String expireDate = "";
        String cardholderName = "";

        io.card.payment.CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
        cardNumber = scanResult.getFormattedCardNumber();
        if (scanResult.expiryMonth != 0 && scanResult.expiryYear != 0) {
            Locale locale = Locale.getDefault();
            int expiryYear = scanResult.expiryYear % 100;
            expireDate = String.format(locale, "%02d%02d", scanResult.expiryMonth, expiryYear);
        }

        return new CreditCard(cardNumber, expireDate, cardholderName);
    }

    @NonNull
    private Intent createIntent(Activity activity) {
        Intent scanIntent = new Intent(activity, CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true);
        setLanguageOrLocale(activity, scanIntent);
        return scanIntent;
    }

    private void setLanguageOrLocale(Activity activity, Intent scanIntent) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return;
        }
        int languageExtra = intent.getIntExtra(TAcqIntentExtra.EXTRA_LANGUAGE, -1);
        if (languageExtra != -1) {
            Language language = Language.values()[languageExtra];
            scanIntent.putExtra(CardIOActivity.EXTRA_LANGUAGE_OR_LOCALE, language.toString());
        }
    }
}

