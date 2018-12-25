/*
 * Copyright © 2016 Tinkoff Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.acquiring.sdk;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.tinkoff.acquiring.sdk.inflate.pay.PayCellInflater;
import ru.tinkoff.acquiring.sdk.inflate.pay.PayCellType;

/**
 * Вспомогательный класс для запуска экрана оплаты PayFormActivity
 *
 * @author a.shishkin1
 */
public class PayFormStarter {

    private Intent intent;
    private final String terminalKey;
    private final String password;
    private final String publicKey;

    public PayFormStarter(String terminalKey, String password, String publicKey) {
        this.terminalKey = terminalKey;
        this.password = password;
        this.publicKey = publicKey;
    }

    public PayFormStarter prepare(String orderId, Money amount, String title, String description, String cardId, String email, boolean recurrentPayment, boolean customKeyboard) {
        intent = new Intent();
        intent.putExtra(PayFormActivity.EXTRA_ORDER_ID, orderId);
        intent.putExtra(PayFormActivity.EXTRA_AMOUNT, amount);
        intent.putExtra(PayFormActivity.EXTRA_TITLE, title);
        intent.putExtra(PayFormActivity.EXTRA_DESCRIPTION, description);
        intent.putExtra(PayFormActivity.EXTRA_CARD_ID, cardId);
        intent.putExtra(PayFormActivity.EXTRA_E_MAIL, email);
        intent.putExtra(PayFormActivity.EXTRA_CUSTOM_KEYBOARD, customKeyboard);
        intent.putExtra(PayFormActivity.EXTRA_RECURRENT_PAYMENT, recurrentPayment);
        intent.putExtra(PayFormActivity.EXTRA_TERMINAL_KEY, terminalKey);
        intent.putExtra(PayFormActivity.EXTRA_PASSWORD, password);
        intent.putExtra(PayFormActivity.EXTRA_PUBLIC_KEY, publicKey);
        intent.putExtra(PayFormActivity.EXTRA_DESIGN_CONFIGURATION, PayCellType.toIntArray(PayCellInflater.DEFAULT_CELL_TYPES));
        return this;
    }

    public PayFormStarter prepare(String orderId, Long amount, boolean recurrentPayment) {
        intent = new Intent();
        intent.putExtra(PayFormActivity.EXTRA_ORDER_ID, orderId);
        intent.putExtra(PayFormActivity.EXTRA_AMOUNT, Money.ofRubles(amount));
        intent.putExtra(PayFormActivity.EXTRA_RECURRENT_PAYMENT, recurrentPayment);
        intent.putExtra(PayFormActivity.EXTRA_TERMINAL_KEY, terminalKey);
        intent.putExtra(PayFormActivity.EXTRA_PASSWORD, password);
        intent.putExtra(PayFormActivity.EXTRA_PUBLIC_KEY, publicKey);
        intent.putExtra(PayFormActivity.EXTRA_DESIGN_CONFIGURATION, PayCellType.toIntArray(PayCellInflater.DEFAULT_CELL_TYPES));
        return this;
    }

    public PayFormStarter setGooglePayParams(GooglePayParams googlePayParams) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_ANDROID_PAY_PARAMS, googlePayParams);
        return this;
    }

    public PayFormStarter setCustomerKey(String customerKey) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_CUSTOMER_KEY, customerKey);
        return this;
    }

    public PayFormStarter setReceipt(Receipt receipt) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_RECEIPT_VALUE, receipt);
        return this;
    }

    public PayFormStarter setShops(@NonNull ArrayList<Shop> shops, @Nullable ArrayList<Receipt> receipts) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_SHOPS_VALUE, shops);
        if (receipts != null) {
            intent.putExtra(PayFormActivity.EXTRA_RECEIPTS_VALUE, receipts);
        }
        return this;
    }

    public PayFormStarter setData(HashMap<String, String> data) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_DATA_VALUE, data);
        return this;
    }

    public PayFormStarter setChargeMode(boolean mode) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_CHARGE_MODE, mode);
        return this;
    }

    public PayFormStarter useFirstAttachedCard(boolean use) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_USE_FIRST_ATTACHED_CARD, use);
        return this;
    }

    public PayFormStarter setTheme(@StyleRes int theme) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_THEME, theme);
        return this;
    }

    public PayFormStarter setCameraCardScanner(ICameraCardScanner cameraCardScanner) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_CAMERA_CARD_SCANNER, cameraCardScanner);
        return this;
    }

    public PayFormStarter setDesignConfiguration(PayCellType... types) {
        checkIntent();
        intent.putExtra(PayFormActivity.EXTRA_DESIGN_CONFIGURATION, PayCellType.toIntArray(types));
        return this;
    }

    public Intent getIntent() {
        checkIntent();
        return intent;
    }

    public void startActivityForResult(Activity context, int requestCode) {
        checkIntent();
        intent.setClass(context, PayFormActivity.class);
        context.startActivityForResult(intent, requestCode);
    }

    private void checkIntent() {
        if (intent == null) {
            throw new IllegalStateException("Use prepare() method for initialization");
        }
    }
}
