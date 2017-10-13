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

    public AttachCardFormStarter prepare(String customerKey, boolean customKeyboard) {
        intent = new Intent();
        intent.putExtra(AttachCardFormActivity.EXTRA_CUSTOMER_KEY, customerKey);
        intent.putExtra(AttachCardFormActivity.EXTRA_CUSTOM_KEYBOARD, customKeyboard);

        intent.putExtra(AttachCardFormActivity.EXTRA_TERMINAL_KEY, terminalKey);
        intent.putExtra(AttachCardFormActivity.EXTRA_PASSWORD, password);
        intent.putExtra(AttachCardFormActivity.EXTRA_PUBLIC_KEY, publicKey);
        return this;
    }

    public AttachCardFormStarter setData(HashMap<String, String> data) {
        if (intent == null) {
            throw new IllegalStateException("Use prepare() method for initialization");
        }
        intent.putExtra(AttachCardFormActivity.EXTRA_DATA, data);
        return this;
    }

    public AttachCardFormStarter setTheme(@StyleRes int theme) {
        if (intent == null) {
            throw new IllegalStateException("Use prepare() method for initialization");
        }
        intent.putExtra(AttachCardFormActivity.EXTRA_THEME, theme);
        return this;
    }

    public void startActivityForResult(Activity context, int requestCode) {
        if (intent == null) {
            throw new IllegalStateException("Use prepare() method for initialization");
        }
        intent.setClass(context, AttachCardFormActivity.class);
        context.startActivityForResult(intent, requestCode);
    }
}
