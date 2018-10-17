package ru.tinkoff.acquiring.sample.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Random;

import ru.tinkoff.acquiring.payment.PaymentData;
import ru.tinkoff.acquiring.payment.PaymentDataUi;
import ru.tinkoff.acquiring.payment.PaymentProcess;
import ru.tinkoff.acquiring.payment.TinkoffPay;
import ru.tinkoff.acquiring.payment.TinkoffPay.Builder;
import ru.tinkoff.acquiring.sample.R;
import ru.tinkoff.acquiring.sample.SessionParams;
import ru.tinkoff.acquiring.sample.SettingsSdkManager;
import ru.tinkoff.acquiring.sdk.CardData;
import ru.tinkoff.acquiring.sdk.Money;

/**
 * @author Stanislav Mukhametshin
 */
public class PaymentTestActivity extends AppCompatActivity {

    private static final int RESULT_REQUEST_CODE = 100;

    private SettingsSdkManager settings;
    private TinkoffPay tinkoffPay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_test);
        Button button = findViewById(R.id.test);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay();
            }
        });
        settings = new SettingsSdkManager(this);
        initPayment();
    }

    private void initPayment() {
        SessionParams sessionParams = SessionParams.TEST_SDK;
        tinkoffPay = Builder
                .init(sessionParams.terminalId, sessionParams.secret, sessionParams.publicKey)
                .setEmail(sessionParams.customerEmail)
                .setCustomerKey(sessionParams.customerKey)
                .build();
    }

    private void pay() {
        CardData cardData = randomCard();
        final PaymentData paymentData = randomPaymentInfo();

        tinkoffPay.pay(cardData, paymentData)
                .start()
                .subscribe(new PaymentProcess.PaymentListener() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(PaymentTestActivity.this, "onCompleted ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUiNeeded(@NonNull PaymentDataUi paymentDataUi) {
                        Toast.makeText(PaymentTestActivity.this, "onUiNeeded " + paymentDataUi.getStatus(), Toast.LENGTH_SHORT).show();
                        tinkoffPay.launchUi(PaymentTestActivity.this, paymentData, paymentDataUi, RESULT_REQUEST_CODE);
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        exception.printStackTrace();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(PaymentTestActivity.this, "ui RESULT_OK", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private CardData randomCard() {
        return new CardData("2222222222222224", "08/22", "111");
    }

    private PaymentData randomPaymentInfo() {
        String oderId = String.valueOf(Math.abs(new Random().nextInt()));

        return new PaymentData(oderId,
                Money.ofRubles(10).getCoins(),
                settings.isRecurrentPayment(),
                true,
                "ru");
    }
}
