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

package ru.tinkoff.acquiring.sdk.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import ru.tinkoff.acquiring.sdk.Journal;
import ru.tinkoff.acquiring.sdk.R;
import ru.tinkoff.core.nfc.CardParser;
import ru.tinkoff.core.nfc.model.Card;

/**
 * @author a.shishkin1
 */
public class NfcCardScanActivity extends AppCompatActivity {

    public static final String EXTRA_CARD = "card_extra";
    public static final int RESULT_ERROR = 256;

    private static final int REQUEST_CODE_SETTINGS = 91;
    private static final int ALPHA_MASK = 0xCCFFFFFF;

    private final String[][] nfcTechFilter = new String[][]{new String[]{NfcA.class.getName()}};

    private NfcAdapter nfc;
    private PendingIntent nfcIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acq_activity_nfc);

        final Button closeBtn = (Button) findViewById(R.id.acq_btn_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        applyBackgroundColor();
        initNfc();
    }

    private void applyBackgroundColor() {
        final View rootView = findViewById(R.id.acq_view_root);
        final ColorDrawable currentBackground = (ColorDrawable) rootView.getBackground();
        final int currentColor = currentBackground.getColor();

        // modify only default color
        if (currentColor == ContextCompat.getColor(this, R.color.acq_colorNfcBackground)) {
            final int newColor = ALPHA_MASK & currentColor;
            rootView.setBackgroundColor(newColor);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfc != null && nfcIntent != null) {
            nfc.enableForegroundDispatch(this, nfcIntent, null, nfcTechFilter);
        }
    }

    @Override
    protected void onPause() {
        if (nfc != null && nfcIntent != null) {
            nfc.disableForegroundDispatch(this);
        }

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Card card = new CardParser().parse(tag);

            if (card != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_CARD, card);

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        } catch (Exception e) {
            Journal.log(e);
            setResult(RESULT_ERROR);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SETTINGS && !nfc.isEnabled()) {
            finish();
        }
    }

    private void initNfc() {
        nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc == null) {
            return;
        }

        nfcIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (!nfc.isEnabled()) {
            Toast.makeText(this, R.string.acq_nfc_need_enable, Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), REQUEST_CODE_SETTINGS);
        }
    }


}
