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

package ru.tinkoff.acquiring.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.wallet.WalletConstants;

import java.util.HashMap;

import ru.tinkoff.acquiring.sample.SessionParams;
import ru.tinkoff.acquiring.sample.R;
import ru.tinkoff.acquiring.sample.SettingsSdkManager;
import ru.tinkoff.acquiring.sdk.AndroidPayParams;
import ru.tinkoff.acquiring.sdk.Item;
import ru.tinkoff.acquiring.sdk.Money;
import ru.tinkoff.acquiring.sdk.OnPaymentListener;
import ru.tinkoff.acquiring.sdk.PayFormActivity;
import ru.tinkoff.acquiring.sdk.Receipt;
import ru.tinkoff.acquiring.sdk.Tax;
import ru.tinkoff.acquiring.sdk.Taxation;
import ru.tinkoff.acquiring.sdk.inflate.pay.PayCellType;

/**
 * @author Mikhail Artemyev
 */
public abstract class PayableActivity extends AppCompatActivity implements OnPaymentListener {

    private static final int REQUEST_CODE_PAY = 1;

    private Money paymentAmount;
    private String paymentDescription;
    private String paymentTitle;
    private SettingsSdkManager settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        settings = new SettingsSdkManager(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAY) {
            PayFormActivity.dispatchResult(resultCode, data, this);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("payment_amount", paymentAmount);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        paymentAmount = savedInstanceState == null ? null : (Money) savedInstanceState.getSerializable("payment_amount");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSuccess(long paymentId) {
        PaymentResultActivity.start(this, paymentAmount);
    }

    @Override
    public void onCancelled() {
        Toast.makeText(this, R.string.payment_cancelled, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(this, R.string.payment_failed, Toast.LENGTH_SHORT).show();
        Log.e("SAMPLE", e.getMessage(), e);
    }

    protected void initPayment(final Money amount,
                               final String orderId,
                               final String title,
                               final String description) {
        this.paymentAmount = amount;
        this.paymentTitle = title;
        this.paymentDescription = description;
        boolean isCustomKeyboardEnabled = settings.isCustomKeyboardEnabled();
        String terminalId = settings.getTerminalId();
        AndroidPayParams androidPayParams = new AndroidPayParams.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build();
        SessionParams sessionParams = SessionParams.get(terminalId);
        PayFormActivity
                .init(sessionParams.terminalId, sessionParams.secret, sessionParams.publicKey)
                .prepare(orderId,
                        amount,
                        paymentTitle,
                        paymentDescription,
                        null,
                        sessionParams.customerEmail,
                        true,
                        isCustomKeyboardEnabled
                )
                .setCustomerKey(sessionParams.customerKey)
                .setChargeMode(settings.isRecurrentPayment())
                .useFirstAttachedCard(settings.useFirstAttachedCard())
                .setCameraCardScanner(settings.getCameraScanner())
                //.setReceipt(createReceipt())
                //.setData(createData())
                .setTheme(settings.resolveStyle())
                .setDesignConfiguration(PayCellType.PAYMENT_CARD_REQUISITES, PayCellType.PAY_BUTTON, PayCellType.SECURE_LOGOS)
                .setAndroidPayParams(settings.isAndroidPayEnabled() ? androidPayParams : null)
                .startActivityForResult(this, REQUEST_CODE_PAY);
    }

    private Receipt createReceipt() {
        Item[] items = new Item[]{new Item("name_1", 10L, 20, 30L, Tax.VAT_10), new Item("name_2", 20L, 30, 40L, Tax.VAT_18)};
        return new Receipt(items, "email@email.email", Taxation.USN_INCOME);
    }

    private HashMap<String, String> createData() {
        HashMap<String, String> map = new HashMap<>();
        map.put("key_1", "value_1");
        map.put("key_2", "value_2");
        map.put("key_3", "value_3");
        return map;
    }

}
