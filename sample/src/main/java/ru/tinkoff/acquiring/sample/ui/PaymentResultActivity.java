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
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
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
    private static final String EXTRA_CARD_ID = "card_id";

    public static void start(final Context context, final Money price) {
        final Intent intent = new Intent(context, PaymentResultActivity.class);
        intent.putExtra(EXTRA_PRICE, price);
        context.startActivity(intent);
    }

    public static void start(final Context context, final String cardId) {
        final Intent intent = new Intent(context, PaymentResultActivity.class);
        intent.putExtra(EXTRA_CARD_ID, cardId);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final TextView textView = (TextView) findViewById(R.id.tv_confirm);
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_PRICE)) {
            final Money price = (Money) intent.getSerializableExtra(EXTRA_PRICE);

            final SpannableString coloredPrice = new SpannableString(price.toString());
            coloredPrice.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)),
                    0,
                    coloredPrice.length(),
                    SpannedString.SPAN_INCLUSIVE_INCLUSIVE
            );

            final String text = getString(R.string.payment_result_success, coloredPrice);
            textView.setText(text);
        } else {
            String cardId = intent.getStringExtra(EXTRA_CARD_ID);
            final String text = getString(R.string.attachment_result_success, cardId);
            textView.setText(text);
        }
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
