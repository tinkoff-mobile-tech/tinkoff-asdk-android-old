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

import java.util.ArrayList;
import java.util.Random;

import ru.tinkoff.acquiring.payment.MarketPlaceData;
import ru.tinkoff.acquiring.payment.PaymentData;
import ru.tinkoff.acquiring.payment.PaymentDataUi;
import ru.tinkoff.acquiring.payment.PaymentListener;
import ru.tinkoff.acquiring.payment.TinkoffPay;
import ru.tinkoff.acquiring.sample.R;
import ru.tinkoff.acquiring.sample.SessionParams;
import ru.tinkoff.acquiring.sample.SettingsSdkManager;
import ru.tinkoff.acquiring.sdk.AgentData;
import ru.tinkoff.acquiring.sdk.AgentSign;
import ru.tinkoff.acquiring.sdk.CardData;
import ru.tinkoff.acquiring.sdk.Item;
import ru.tinkoff.acquiring.sdk.Money;
import ru.tinkoff.acquiring.sdk.PaymentMethod;
import ru.tinkoff.acquiring.sdk.PaymentObject;
import ru.tinkoff.acquiring.sdk.Receipt;
import ru.tinkoff.acquiring.sdk.Shop;
import ru.tinkoff.acquiring.sdk.SupplierInfo;
import ru.tinkoff.acquiring.sdk.Tax;
import ru.tinkoff.acquiring.sdk.Taxation;

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
        tinkoffPay = new TinkoffPay(sessionParams.terminalId, sessionParams.secret, sessionParams.publicKey);
    }

    private void pay() {
        final CardData cardData = randomCard();
        final PaymentData paymentData = randomPaymentInfo();

        tinkoffPay.pay(cardData, paymentData)
                .start()
                .subscribe(new PaymentListener() {
                    @Override
                    public void onSuccess(long paymentId) {
                        Toast.makeText(PaymentTestActivity.this, "onSuccess ", Toast.LENGTH_SHORT).show();
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
        return new CardData("4300000000000777", "11/22", "111");
    }

    private PaymentData randomPaymentInfo() {
        String oderId = String.valueOf(Math.abs(new Random().nextInt()));
        SessionParams sessionParams = SessionParams.TEST_SDK;
        MarketPlaceData marketPlaceData = randomMarketPlaceData();

        return new PaymentData(
                sessionParams.customerKey,
                oderId,
                Money.ofRubles(10).getCoins(),
                false,
                false,
                marketPlaceData,
                "ru",
                "email@test.ru"
        );
    }

    private MarketPlaceData randomMarketPlaceData() {
        ArrayList<Receipt> receipts = new ArrayList<>();
        Item[] items = new Item[1];
        Item item = new Item("Название товара 1", 2000L, 2D, 4000L, Tax.VAT_10);

        AgentData agentData = new AgentData();
        agentData.setAgentSign(AgentSign.BANK_PAYING_AGENT);
        agentData.setOperationName("Позиция чека 1");
        agentData.setPhones(new String[]{"+823456781012141611"});
        agentData.setReceiverPhones(new String[]{"+923456781012141611", "+133456781012141611"});
        agentData.setTransferPhones(new String[]{"+136456781012141611"});
        agentData.setOperationName("Tinkoff");
        agentData.setOperatorAddress("г. Тольятти");
        agentData.setOperatorInn("7710140679");
        item.setAgentData(agentData);

        SupplierInfo supplierInfo = new SupplierInfo();
        supplierInfo.setPhones(new String[]{"88001007755", "+74959565555"});
        supplierInfo.setName("СПАО \"Ингосстрах\"");
        supplierInfo.setInn("7705042179");
        item.setSupplierInfo(supplierInfo);

        item.setPaymentMethod(PaymentMethod.FULL_PREPAYMENT);
        item.setPaymentObject(PaymentObject.LOTTERY_PRIZE);

        items[0] = item;
        Receipt receipt = new Receipt("100", items, "email@test.ru", Taxation.OSN);
        receipts.add(receipt);

        ArrayList<Shop> shops = new ArrayList<>();
        Shop shop = new Shop("100", "Название товара 1", 5000L);
        shops.add(shop);

        return new MarketPlaceData(shops, receipts);
    }
}
