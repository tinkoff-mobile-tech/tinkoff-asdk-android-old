package ru.tinkoff.acquiring.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StyleRes;

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
        String fallback = context.getString(R.string.acq_sp_default_value_terminal_id);
        return preferences.getString(key, fallback);
    }

    public String resolveCustomerKey(String terminalId) {
        String testSdkTerminalId = context.getString(R.string.acq_sp_test_sdk_terminal_id);
        if (testSdkTerminalId.equals(terminalId)) {
            return SessionInfo.TEST_SDK_CUSTOMER_KEY;
        }
        return SessionInfo.DEFAULT_CUSTOMER_KEY;
    }

    public String resolveCustomerEmail(String terminalId) {
        String testSdkTerminalId = context.getString(R.string.acq_sp_test_sdk_terminal_id);
        if (testSdkTerminalId.equals(terminalId)) {
            return SessionInfo.TEST_SDK_CUSTOMER_EMAIL;
        }
        return SessionInfo.DEFAULT_CUSTOMER_EMAIL;
    }

    public boolean isRecurrentPayment(){
        return preferences.getBoolean(context.getString(R.string.acq_sp_recurrent_payment), false);
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
}
