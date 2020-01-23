package ru.tinkoff.acquiring.sample.camera;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ru.tinkoff.acquiring.sdk.CreditCard;
import ru.tinkoff.acquiring.sdk.ICameraCardScanner;
import ru.tinkoff.acquiring.sdk.ICreditCard;

/**
 * @author Vitaliy Markus
 */
public class DemoCameraScanner implements ICameraCardScanner {

    @Override
    public void startActivityForScanning(Fragment fragment, int requestCode) {
        Intent scanIntent = createIntent(fragment.getActivity());
        fragment.startActivityForResult(scanIntent, requestCode);
    }

    @Override
    public boolean hasResult(@NonNull Intent data) {
        return data.hasExtra(DemoCameraScanActivity.EXTRA_CARD_NUMBER) ||
                data.hasExtra(DemoCameraScanActivity.EXTRA_EXPIRE_DATE);
    }

    @NonNull
    @Override
    public ICreditCard parseIntentData(Intent data) {
        String cardNumber = data.getStringExtra(DemoCameraScanActivity.EXTRA_CARD_NUMBER);
        String expireDate = data.getStringExtra(DemoCameraScanActivity.EXTRA_EXPIRE_DATE);
        return new CreditCard(cardNumber, expireDate, "");
    }

    @NonNull
    private Intent createIntent(Activity activity) {
        return new Intent(activity, DemoCameraScanActivity.class);
    }
}
