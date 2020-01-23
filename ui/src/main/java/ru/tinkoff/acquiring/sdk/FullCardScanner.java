package ru.tinkoff.acquiring.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Toast;

import ru.tinkoff.acquiring.sdk.nfc.AsdkNfcScanActivity;
import ru.tinkoff.acquiring.sdk.views.EditCardView;

/**
 * @author Vitaliy Markus
 */
public class FullCardScanner implements EditCardView.Actions {

    public static final int REQUEST_CARD_NFC = 2964;

    @NonNull
    private final Fragment fragment;

    @Nullable
    private final ICameraCardScanner cameraCardScanner;

    public FullCardScanner(@NonNull Fragment fragment, @Nullable ICameraCardScanner cameraCardScanner) {
        this.fragment = fragment;
        if (cameraCardScanner != null) {
            this.cameraCardScanner = cameraCardScanner;
        } else {
            this.cameraCardScanner = null;
        }
    }

    @Override
    public void onPressScanCard(EditCardView editCardView) {
        final Activity activity = fragment.getActivity();
        boolean nfcEnable = isNfcEnable(activity);
        boolean cameraEnabled = isCameraEnable();
        if (nfcEnable && cameraEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            CharSequence items[] = activity.getResources().getStringArray(R.array.acq_scan_types);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    if (i == 0) {
                        startCameraScan();
                    } else if (i == 1) {
                        startNfcScan(activity);
                    }
                    dialog.dismiss();
                }
            });
            builder.show();
        } else if (cameraEnabled) {
            startCameraScan();
        } else if (nfcEnable) {
            startNfcScan(activity);
        } else {
            Toast.makeText(activity, R.string.acq_no_scan_providers, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isScanEnable() {
        return isNfcEnable(fragment.getActivity()) || isCameraEnable();
    }

    public boolean hasCameraResult(int requestCode, Intent data) {
        return requestCode == ICameraCardScanner.REQUEST_CAMERA_CARD_SCAN && data != null && cameraCardScanner.hasResult(data);
    }

    public boolean hasNfcResult(int requestCode, int resultCode) {
        return requestCode == REQUEST_CARD_NFC && resultCode == Activity.RESULT_OK;
    }

    public boolean isNfcError(int requestCode, int resultCode) {
        return requestCode == REQUEST_CARD_NFC && resultCode == AsdkNfcScanActivity.RESULT_ERROR;
    }

    @NonNull
    public ICreditCard parseCameraData(Intent data) {
        return cameraCardScanner.parseIntentData(data);
    }

    @NonNull
    public ICreditCard parseNfcData(Intent data) {
        return (ICreditCard) data.getSerializableExtra(AsdkNfcScanActivity.EXTRA_CARD);
    }

    @Override
    public void onUpdate(EditCardView editCardView) {

    }

    private boolean isNfcEnable(Activity activity) {
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    }

    private boolean isCameraEnable() {
        return cameraCardScanner != null;
    }

    private void startCameraScan() {
        cameraCardScanner.startActivityForScanning(fragment, ICameraCardScanner.REQUEST_CAMERA_CARD_SCAN);
    }

    private void startNfcScan(Activity activity) {
        Intent cardFromNfcIntent = new Intent(activity, AsdkNfcScanActivity.class);
        fragment.startActivityForResult(cardFromNfcIntent, REQUEST_CARD_NFC);
    }
}
