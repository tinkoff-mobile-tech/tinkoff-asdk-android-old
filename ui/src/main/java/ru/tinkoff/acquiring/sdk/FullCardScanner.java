package ru.tinkoff.acquiring.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import ru.tinkoff.acquiring.sdk.localization.AsdkLocalization;
import ru.tinkoff.acquiring.sdk.localization.AsdkLocalizations;
import ru.tinkoff.acquiring.sdk.localization.HasAsdkLocalization;
import ru.tinkoff.acquiring.sdk.nfc.AsdkNfcScanActivity;
import ru.tinkoff.acquiring.sdk.views.EditCardView;

/**
 * @author Vitaliy Markus
 */
public class FullCardScanner implements EditCardView.Actions {

    public static final int REQUEST_CARD_NFC = 2964;

    @NonNull
    private final Fragment fragment;

    private String noScanProvidersMessage;

    @Nullable
    private final ICameraCardScanner cameraCardScanner;

    public FullCardScanner(@NonNull Fragment fragment, @Nullable ICameraCardScanner cameraCardScanner, String noScanProvidersMessage) {
        this.fragment = fragment;
        this.noScanProvidersMessage = noScanProvidersMessage;
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
            AsdkLocalization localization = ((HasAsdkLocalization) editCardView.getContext()).getAsdkLocalization();
            CharSequence items[] = new CharSequence[] {
                    localization.payDialogCardScanCamera,
                    localization.payDialogCardScanNfc
            };
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
            Toast.makeText(activity, noScanProvidersMessage, Toast.LENGTH_SHORT).show();
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
