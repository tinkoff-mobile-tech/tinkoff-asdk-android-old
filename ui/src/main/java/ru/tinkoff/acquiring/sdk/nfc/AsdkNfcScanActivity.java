package ru.tinkoff.acquiring.sdk.nfc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ru.tinkoff.acquiring.sdk.CreditCard;
import ru.tinkoff.acquiring.sdk.R;
import ru.tinkoff.acquiring.sdk.localization.AsdkLocalization;
import ru.tinkoff.acquiring.sdk.localization.HasAsdkLocalization;
import ru.tinkoff.core.nfc2.BaseNfcActivity;
import ru.tinkoff.core.nfc2.ImperfectAlgorithmException;
import ru.tinkoff.core.nfc2.MalformedDataException;

/**
 * @author Vitaliy Markus
 */
public class AsdkNfcScanActivity extends BaseNfcActivity implements HasAsdkLocalization {

    public static final String EXTRA_CARD = "card_extra";
    public static final int RESULT_ERROR = 256;

    private static final int ALPHA_MASK = 0xCCFFFFFF;

    private AsdkLocalizationProperty asdkLocalizationProperty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        asdkLocalizationProperty = new AsdkLocalizationProperty(this);
        setContentView(R.layout.acq_activity_nfc);

        ((TextView) findViewById(R.id.tv_description)).setText(getAsdkLocalization().nfcDescription);

        Button closeBtn = findViewById(R.id.acq_btn_close);
        closeBtn.setText(getAsdkLocalization().nfcCloseButton);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        applyBackgroundColor();
    }

    @Override
    public void onResult(String cardNumber, String expireDate) {
        CreditCard card = new CreditCard(cardNumber, expireDate, "");
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CARD, card);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public String getNfcDisabledDialogTitle() {
        return getAsdkLocalization().nfcDialogDisableTitle;
    }

    @Override
    public String getNfcDisabledDialogMessage() {
        return getAsdkLocalization().nfcDialogDisableMessage;
    }

    @Override
    public void onException(Exception exception) {
        onException();
    }

    @Override
    public void onClarifiedException(MalformedDataException ex) {
        onException();
    }

    @Override
    public void onClarifiedException(ImperfectAlgorithmException ex) {
        onException();
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

    private void onException() {
        setResult(RESULT_ERROR);
        finish();
    }

    @Override
    public AsdkLocalization getAsdkLocalization() {
        return asdkLocalizationProperty.getAsdkLocalization();
    }
}
