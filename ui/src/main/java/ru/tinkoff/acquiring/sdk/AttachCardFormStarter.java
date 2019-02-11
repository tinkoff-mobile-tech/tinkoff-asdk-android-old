package ru.tinkoff.acquiring.sdk;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.StyleRes;

import java.io.ObjectOutputStream;
import java.util.HashMap;

import ru.tinkoff.acquiring.sdk.inflate.attach.AttachCellInflater;
import ru.tinkoff.acquiring.sdk.inflate.attach.AttachCellType;

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
        return prepare(customerKey, checkType.toString(), customKeyboard, email);
    }

    public AttachCardFormStarter prepare(String customerKey, String checkType, boolean customKeyboard, String email) {
        intent = new Intent();
        intent.putExtra(TAcqIntentExtra.EXTRA_CUSTOMER_KEY, customerKey);
        intent.putExtra(TAcqIntentExtra.EXTRA_CHECK_TYPE, checkType);
        intent.putExtra(TAcqIntentExtra.EXTRA_CUSTOM_KEYBOARD, customKeyboard);
        intent.putExtra(TAcqIntentExtra.EXTRA_E_MAIL, email);

        intent.putExtra(TAcqIntentExtra.EXTRA_TERMINAL_KEY, terminalKey);
        intent.putExtra(TAcqIntentExtra.EXTRA_PASSWORD, password);
        intent.putExtra(TAcqIntentExtra.EXTRA_PUBLIC_KEY, publicKey);

        intent.putExtra(TAcqIntentExtra.EXTRA_DESIGN_CONFIGURATION, AttachCellType.toIntArray(AttachCellInflater.DEFAULT_CELL_TYPES));
        return this;
    }

    public AttachCardFormStarter setData(HashMap<String, String> data) {
        checkIntent();
        intent.putExtra(TAcqIntentExtra.EXTRA_DATA, data);
        return this;
    }

    public AttachCardFormStarter setTheme(@StyleRes int theme) {
        checkIntent();
        intent.putExtra(TAcqIntentExtra.EXTRA_THEME, theme);
        return this;
    }

    public AttachCardFormStarter setCameraCardScanner(ICameraCardScanner cameraCardScanner) {
        checkIntent();
        intent.putExtra(TAcqIntentExtra.EXTRA_CAMERA_CARD_SCANNER, cameraCardScanner);
        return this;
    }

    public AttachCardFormStarter setDesignConfiguration(AttachCellType... types) {
        checkIntent();
        intent.putExtra(TAcqIntentExtra.EXTRA_DESIGN_CONFIGURATION, AttachCellType.toIntArray(types));
        return this;
    }

    public AttachCardFormStarter setLanguage(Language language) {
        checkIntent();
        intent.putExtra(TAcqIntentExtra.EXTRA_LANGUAGE, language.ordinal());
        return this;
    }

    public AttachCardFormStarter setRawLocalizationResourceId(int rawLocalizationResourceId) {
        checkIntent();
        intent.putExtra(TAcqIntentExtra.EXTRA_LOCALIZATION_RAW_RESOURCE_ID, rawLocalizationResourceId);
        return this;
    }

    public Intent getIntent() {
        checkIntent();
        return intent;
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
