package ru.tinkoff.acquiring.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StyleRes;

import ru.tinkoff.acquiring.sample.camera.DemoCameraScanner;
import ru.tinkoff.acquiring.sdk.ICameraCardScanner;
import ru.tinkoff.acquiring.sdk.card.io.CameraCardIOScanner;

/**
 * @author Vitaliy Markus
 */

public class SettingsSdkManager {

    private final Context context;
    private final SharedPreferences preferences;

    public SettingsSdkManager(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isCustomKeyboardEnabled() {
        String key = context.getString(R.string.acq_sp_use_system_keyboard);
        return !preferences.getBoolean(key, false);
    }

    public String getTerminalId() {
        String key = context.getString(R.string.acq_sp_terminal_id);
        String fallback = SessionParams.DEFAULT.terminalId;
        return preferences.getString(key, fallback);
    }

    public boolean isRecurrentPayment() {
        return preferences.getBoolean(context.getString(R.string.acq_sp_recurrent_payment), false);
    }

    public boolean useFirstAttachedCard() {
        return preferences.getBoolean(context.getString(R.string.acq_sp_use_first_saved_card), true);
    }

    public boolean isAndroidPayEnabled() {
        return preferences.getBoolean(context.getString(R.string.acq_sp_android_pay), true);
    }

    @StyleRes
    public int resolveStyle() {
        String defaultStyleName = context.getString(R.string.acq_sp_default_style_id);
        String customStyleName = context.getString(R.string.acq_sp_custom_style_id);
        String styleName = preferences.getString(context.getString(R.string.acq_sp_style_id), defaultStyleName);
        if (customStyleName.equals(styleName)) {
            return R.style.AcquiringTheme_Custom;
        }
        return R.style.AcquiringTheme;
    }

    @StyleRes
    public int resolveAttachCardStyle() {
        String defaultStyleName = context.getString(R.string.acq_sp_default_style_id);
        String customStyleName = context.getString(R.string.acq_sp_custom_style_id);
        String styleName = preferences.getString(context.getString(R.string.acq_sp_style_id), defaultStyleName);
        if (customStyleName.equals(styleName)) {
            return R.style.AcquiringTheme_Custom_AttachCard;
        }
        return R.style.AcquiringTheme_AttachCard;
    }

    public String getCheckType() {
        String defaultCheckType = context.getString(R.string.acq_sp_check_type_no);
        String checkType = preferences.getString(context.getString(R.string.acq_sp_check_type_id), defaultCheckType);
        return checkType;
    }

    public ICameraCardScanner getCameraScanner() {
        String cardIOCameraScan = context.getString(R.string.acq_sp_camera_type_card_io);
        String cameraScan = preferences.getString(context.getString(R.string.acq_sp_camera_type_id), cardIOCameraScan);
        if (cardIOCameraScan.equals(cameraScan)) {
            return new CameraCardIOScanner();
        }
        return new DemoCameraScanner();
    }
}
