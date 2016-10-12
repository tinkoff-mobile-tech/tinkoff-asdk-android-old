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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import ru.tinkoff.acquiring.sample.MerchantParams;
import ru.tinkoff.acquiring.sample.R;
import ru.tinkoff.acquiring.sample.SessionInfo;
import ru.tinkoff.acquiring.sdk.Money;
import ru.tinkoff.acquiring.sdk.OnPaymentListener;
import ru.tinkoff.acquiring.sdk.PayFormActivity;

import static ru.tinkoff.acquiring.sdk.PayFormActivity.init;

/**
 * @author Mikhail Artemyev
 */
public abstract class PayableActivity extends AppCompatActivity implements OnPaymentListener {

    private static final int REQUEST_CODE_PAY = 1;

    private Money paymentAmount;
    private String paymentDescription;
    private String paymentTitle;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
        boolean isCustomKeyboardEnabled = isCustomKeyboardEnabled();
        String terminalId = getTerminalId();
        PayFormActivity
                .init(terminalId, MerchantParams.PASSWORD, MerchantParams.PUBLIC_KEY)
                .prepare(orderId,
                        amount,
                        paymentTitle,
                        paymentDescription,
                        null,
                        SessionInfo.CUSTOMER_EMAIL,
                        false,
                        isCustomKeyboardEnabled
                )
                .setCustomerKey(SessionInfo.CUSTOMER_KEY)
                .startActivityForResult(this, REQUEST_CODE_PAY);
    }

    private boolean isCustomKeyboardEnabled() {
        String key = getString(R.string.acq_sp_use_system_keyboard);
        return !sharedPreferences.getBoolean(key, false);
    }

    private String getTerminalId() {
        String key = getString(R.string.acq_sp_terminal_id);
        String fallback = getString(R.string.acq_sp_default_value_terminal_id);
        return sharedPreferences.getString(key, fallback);
    }

}
