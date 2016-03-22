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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.widget.TextView;

import ru.tinkoff.acquiring.sample.R;
import ru.tinkoff.acquiring.sdk.Money;

/**
 * @author Mikhail Artemyev
 */
public class PaymentResultActivity extends AppCompatActivity {

    private static final String EXTRA_PRICE = "price";

    public static void start(final Context context, final Money price) {
        final Intent intent = new Intent(context, PaymentResultActivity.class);
        intent.putExtra(EXTRA_PRICE, price);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Money price = (Money) getIntent().getSerializableExtra(EXTRA_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Use start() method to start AboutActivity");
        }

        final SpannableString coloredPrice = new SpannableString(price.toString());
        coloredPrice.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)),
                0,
                coloredPrice.length(),
                SpannedString.SPAN_INCLUSIVE_INCLUSIVE
        );

        final String text = getString(R.string.payment_result_success, coloredPrice);
        final TextView textView = (TextView) findViewById(R.id.tv_confirm);
        textView.setText(text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
