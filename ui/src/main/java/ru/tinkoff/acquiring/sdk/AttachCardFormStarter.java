package ru.tinkoff.acquiring.sdk;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.StyleRes;

import java.util.HashMap;

/**
 * @author Vitaliy Markus
 */
public class AttachCardFormStarter {

    private Intent intent;

    private final String terminalKey;
    private final String password;
    private final String publicKey;

    public AttachCardFormStarter(String terminalKey, String password, String publicKey) {
        this.terminalKey = terminalKey;
        this.password = password;
        this.publicKey = publicKey;
    }

    public AttachCardFormStarter prepare(String customerKey, CheckType checkType, boolean customKeyboard, String email) {
        intent = new Intent();
        intent.putExtra(AttachCardFormActivity.EXTRA_CUSTOMER_KEY, customerKey);
        intent.putExtra(AttachCardFormActivity.EXTRA_CHECK_TYPE, checkType);
        intent.putExtra(AttachCardFormActivity.EXTRA_CUSTOM_KEYBOARD, customKeyboard);
        intent.putExtra(AttachCardFormActivity.EXTRA_E_MAIL, email);

        intent.putExtra(AttachCardFormActivity.EXTRA_TERMINAL_KEY, terminalKey);
        intent.putExtra(AttachCardFormActivity.EXTRA_PASSWORD, password);
        intent.putExtra(AttachCardFormActivity.EXTRA_PUBLIC_KEY, publicKey);
        return this;
    }

    public AttachCardFormStarter setData(HashMap<String, String> data) {
        checkIntent();
        intent.putExtra(AttachCardFormActivity.EXTRA_DATA, data);
        return this;
    }

    public AttachCardFormStarter setTheme(@StyleRes int theme) {
        checkIntent();
        intent.putExtra(AttachCardFormActivity.EXTRA_THEME, theme);
        return this;
    }

    public AttachCardFormStarter setCameraCardScanner(ICameraCardScanner cameraCardScanner) {
        checkIntent();
        intent.putExtra(AttachCardFormActivity.EXTRA_CAMERA_CARD_SCANNER, cameraCardScanner);
        return this;
    }

    public void startActivityForResult(Activity context, int requestCode) {
        checkIntent();
        intent.setClass(context, AttachCardFormActivity.class);
        context.startActivityForResult(intent, requestCode);
    }

    private void checkIntent() {
        if (intent == null) {
            throw new IllegalStateException("Use prepare() method for initialization");
        }
    }
}
