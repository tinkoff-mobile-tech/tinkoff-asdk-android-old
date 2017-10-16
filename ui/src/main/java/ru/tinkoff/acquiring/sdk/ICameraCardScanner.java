package ru.tinkoff.acquiring.sdk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.io.Serializable;

/**
 * @author Vitaliy Markus
 */
public interface ICameraCardScanner extends Serializable {

    int REQUEST_CAMERA_CARD_SCAN = 4123;

    void startActivityForScanning(Fragment fragment, int requestCode);

    boolean hasResult(@NonNull Intent data);

    @NonNull
    ICreditCard parseIntentData(Intent data);

}
