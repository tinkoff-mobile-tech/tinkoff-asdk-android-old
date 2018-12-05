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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.tinkoff.acquiring.sdk.localization.AsdkLocalization;
import ru.tinkoff.acquiring.sdk.responses.AcquiringResponse;


/**
 * Экран оплаты. Позволяет посмотреть данные платежа, ввести данные карты для оплаты, проходить в
 * случае необходимости 3DS, управлять ранее сохраненными картами.
 *
 * @author a.shishkin1
 */
public class PayFormActivity extends AppCompatActivity implements FragmentsCommunicator.IFragmentManagerExtender, IPayFormActivity {

    public static final int RESULT_ERROR = 500;
    public static final String API_ERROR_NO_CUSTOMER = "7";

    static final int RESULT_CODE_CLEAR_CARD = 101;

    private static final String INSTANCE_KEY_CARDS = "cards";
    private static final String INSTANCE_KEY_CARD_INDEX = "card_idx";

    private FragmentsCommunicator fragmentsCommunicator;
    private Long paymentId;

    public static PayFormStarter init(String terminalKey, String password, String publicKey) {
        return new PayFormStarter(terminalKey, password, publicKey);
    }

    private DialogsManager dialogsManager;
    private AcquiringSdk sdk;
    private CardManager cardManager;
    private Card[] cards;
    private Card sourceCard;
    private boolean useCustomKeyboard;
    private boolean isCardsReady;
    private boolean chargeMode;
    private AsdkLocalizationProperty asdkLocalizationProperty;

    @Override
    public AcquiringSdk getSdk() {
        return sdk;
    }

    CardManager getCardManager() {
        return cardManager;
    }

    Card[] getCards() {
        return cards;
    }

    Card getSourceCard() {
        return sourceCard;
    }

    void setSourceCard(Card sourceCard) {
        this.sourceCard = sourceCard;
    }

    boolean shouldUseCustomKeyboard() {
        return useCustomKeyboard;
    }

    public PayFormActivity() {
        fragmentsCommunicator = new FragmentsCommunicator();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        asdkLocalizationProperty = new AsdkLocalizationProperty(this);
        Intent intent = getIntent();
        int theme = intent.getIntExtra(TAcqIntentExtra.EXTRA_THEME, 0);
        if (theme != 0) {
            setTheme(theme);
        }

        fragmentsCommunicator.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.acq_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getAsdkLocalization().payTitle);

        dialogsManager = new DialogsManager(this);

        String terminalKey = intent.getStringExtra(TAcqIntentExtra.EXTRA_TERMINAL_KEY);
        String password = intent.getStringExtra(TAcqIntentExtra.EXTRA_PASSWORD);
        String publicKey = intent.getStringExtra(TAcqIntentExtra.EXTRA_PUBLIC_KEY);
        chargeMode = intent.getBooleanExtra(TAcqIntentExtra.EXTRA_CHARGE_MODE, false);

        sdk = new AcquiringSdk(terminalKey, password, publicKey);
        cardManager = new CardManager(sdk);
        useCustomKeyboard = intent.getBooleanExtra(TAcqIntentExtra.EXTRA_CUSTOM_KEYBOARD, false);

        if (savedInstanceState == null) {
            isCardsReady = false;
            if (intent.hasExtra(TAcqIntentExtra.EXTRA_PAYMENT_INFO)) {
                showRejected();
            } else if (intent.hasExtra(TAcqIntentExtra.EXTRA_THREE_DS)) {
                showThreeDsData();
            } else {
                startFinishAuthorized();
                if (isCardChooseEnable()) {
                    String customerKey = intent.getStringExtra(TAcqIntentExtra.EXTRA_CUSTOMER_KEY);
                    showProgressDialog();
                    requestCards(customerKey, cardManager);
                }
            }
        } else {
            cards = new CardsArrayBundlePacker().unpack(savedInstanceState.getBundle(INSTANCE_KEY_CARDS));
            int idx = savedInstanceState.getInt(INSTANCE_KEY_CARD_INDEX, -1);
            if (idx != -1) {
                sourceCard = cards[idx];
            }
        }
    }

    private void showRejected() {
        Bundle paymentInfoBundle = getIntent().getBundleExtra(TAcqIntentExtra.EXTRA_PAYMENT_INFO);
        Bundle cardInfoBundle = getIntent().getBundleExtra(TAcqIntentExtra.EXTRA_CARD_DATA);
        Card[] cards = new CardsArrayBundlePacker().unpack(cardInfoBundle);
        final PaymentInfo paymentInfo = new PaymentInfoBundlePacker().unpack(paymentInfoBundle);

        startFinishAuthorized();

        EnterCardFragment fragment = createEnterCardFragment(false);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        onCardsReady(cards);
        fragment.setRejectAction(new EnterCardFragment.RejectAction() {
            @Override
            public void onPerformReject() {
                onChargeRequestRejected(paymentInfo);
            }
        });
    }

    private void showThreeDsData() {
        Bundle threeDsBundle = getIntent().getBundleExtra(TAcqIntentExtra.EXTRA_THREE_DS);
        ThreeDsData threeDsData = new ThreeDsBundlePacker().unpack(threeDsBundle);
        start3DS(threeDsData);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fragmentsCommunicator.onSavedInstanceState(outState);
        outState.putBundle(INSTANCE_KEY_CARDS, new CardsArrayBundlePacker().pack(cards));
        if (sourceCard != null && cards != null) {
            for (int i = 0; i < cards.length; i++) {
                if (sourceCard == cards[i]) {
                    outState.putInt(INSTANCE_KEY_CARD_INDEX, i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PayFormHandler.INSTANCE.register(this);
        CommonSdkHandler.INSTANCE.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dialogsManager.dismissDialogs();
        PayFormHandler.INSTANCE.unregister(this);
        CommonSdkHandler.INSTANCE.unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void success() {
        hideProgressDialog();
        final Intent data = new Intent();
        data.putExtra(TAcqIntentExtra.EXTRA_PAYMENT_ID, paymentId);
        setResult(RESULT_OK, data);
        if (isCardChooseEnable()) {
            cardManager.clear(getIntent().getStringExtra(TAcqIntentExtra.EXTRA_CUSTOMER_KEY));
        }
        finish();
    }

    @Override
    public void cancel() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void showProgressDialog() {
        if (!dialogsManager.isProgressShowing()) {
            dialogsManager.showProgressDialog(getAsdkLocalization().payDialogProgressPayMessage);
        }
    }

    @Override
    public void hideProgressDialog() {
        dialogsManager.hideProgressDialog();
    }

    @Override
    public void exception(Throwable throwable) {
        hideProgressDialog();

        if (throwable instanceof AcquiringApiException) {
            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
        }

        Intent data = new Intent();
        data.putExtra(TAcqIntentExtra.EXTRA_ERROR, throwable);
        setResult(RESULT_ERROR, data);
        finish();
    }

    @Override
    public void start3DS(ThreeDsData data) {
        hideProgressDialog();
        Fragment fragment = createThreeDsFragment(data);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    @Override
    public void showErrorDialog(Exception exception) {
        String message = exception.getMessage();
        if (TextUtils.isEmpty(message)) {
            message = getAsdkLocalization().payDialogErrorFallbackMessage;
        }
        dialogsManager.showErrorDialog(getAsdkLocalization().payDialogErrorTitle, message);
    }

    @Override
    public void noNetwork() {
        String title = getAsdkLocalization().payDialogErrorTitle;
        String message = getAsdkLocalization().payDialogErrorNetwork;
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_CANCELED);
                PayFormActivity.this.finish();
            }
        };
        dialogsManager.showErrorDialog(title, message, onClickListener);
        hideProgressDialog();
    }

    @Override
    public void onCardsReady(Card[] cards) {
        hideProgressDialog();
        boolean useFirstAttachedCard = getIntent().getBooleanExtra(TAcqIntentExtra.EXTRA_USE_FIRST_ATTACHED_CARD, true);
        this.cards = filterCards(cards);
        if (!isCardsReady && sourceCard == null && cards != null && cards.length > 0) {
            String cardId = getIntent().getStringExtra(TAcqIntentExtra.EXTRA_CARD_ID);
            if (cardId != null) {
                sourceCard = cardManager.getCardById(cardId);
            }
            if (useFirstAttachedCard && this.cards.length > 0 && sourceCard == null) {
                sourceCard = this.cards[0];
            }
        }
        isCardsReady = true;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null && fragment instanceof ICardInterest) {
            ((ICardInterest) fragment).onCardReady();
        }
    }

    @Override
    public void onDeleteCard(Card card) {
        cardManager.clear(getIntent().getStringExtra(TAcqIntentExtra.EXTRA_CUSTOMER_KEY));
        if (sourceCard == card) {
            sourceCard = null;
        }
        if (cards.length == 1 && cards[0] == card) {
            cards = new Card[0];
        } else {
            List<Card> list = new ArrayList<>(Arrays.asList(cards));
            list.remove(card);
            Card[] array = new Card[list.size()];
            list.toArray(array);
            cards = array;
        }
        onCardsReady(cards);
    }

    @Override
    public void onPaymentInitCompleted(final Long paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public void onChargeRequestRejected(PaymentInfo paymentInfo) {
        hideProgressDialog();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null && fragment instanceof IChargeRejectPerformer) {
            ((IChargeRejectPerformer) fragment).onChargeRequestRejected(paymentInfo);
        }
    }

    @Override
    public void onGooglePayError() {
        hideProgressDialog();
        String title = getAsdkLocalization().payDialogErrorTitle;
        String message = getAsdkLocalization().payDialogErrorFallbackMessage;
        dialogsManager.showErrorDialog(title, message);
    }

    public void selectCardById(String cardId) {
        Card selectedCard = cardManager.getCardById(cardId);
        if (selectedCard != null) {
            sourceCard = selectedCard;
        }
    }

    @Override
    public void onBackPressed() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment instanceof OnBackPressedListener) {
            final OnBackPressedListener listener = ((OnBackPressedListener) fragment);
            if (listener.onBackPressed()) {
                return;
            }
        }

        navigateBack();
    }

    @Override
    public FragmentsCommunicator getFragmentsCommunicator() {
        return fragmentsCommunicator;
    }

    protected ThreeDsFragment createThreeDsFragment(ThreeDsData data) {
        return ThreeDsFragment.newInstance(new ThreeDsBundlePacker().pack(data));
    }

    protected EnterCardFragment createEnterCardFragment(boolean chargeMode) {
        return EnterCardFragment.newInstance(chargeMode);
    }

    protected CardListFragment createCardListFragment(String customerKey, boolean chargeMode) {
        return CardListFragment.newInstance(customerKey, chargeMode);
    }

    void startFinishAuthorized() {
        Fragment fragment = createEnterCardFragment(chargeMode);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    void startChooseCard() {
        Fragment fragment = createCardListFragment(getIntent().getStringExtra(TAcqIntentExtra.EXTRA_CUSTOMER_KEY), chargeMode);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack("choose_card")
                .commit();
    }

    void finishChooseCards() {
        getSupportFragmentManager().popBackStack("choose_card", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    static void requestCards(final String customerKey, final CardManager cardManager) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Card[] cards = cardManager.getActiveCards(customerKey);
                    PayFormHandler.INSTANCE.obtainMessage(PayFormHandler.CARDS_READY, cards).sendToTarget();
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    if (cause == null) {
                        Journal.log(e);
                        PayFormHandler.INSTANCE.obtainMessage(PayFormHandler.CARDS_READY, new Card[0]).sendToTarget();
                    } else if (cause instanceof AcquiringApiException) {
                        AcquiringResponse apiResponse = ((AcquiringApiException) cause).getResponse();
                        if (apiResponse != null && API_ERROR_NO_CUSTOMER.equals(apiResponse.getErrorCode())) {
                            PayFormHandler.INSTANCE.obtainMessage(PayFormHandler.CARDS_READY, new Card[0]).sendToTarget();
                        } else {
                            CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.EXCEPTION, cause).sendToTarget();
                        }
                    } else if (cause instanceof NetworkException) {
                        CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.NO_NETWORK).sendToTarget();
                    } else {
                        Journal.log(cause);
                        PayFormHandler.INSTANCE.obtainMessage(PayFormHandler.CARDS_READY, new Card[0]).sendToTarget();
                    }
                }
            }
        }).start();
    }

    public boolean isCardChooseEnable() {
        return getIntent().getStringExtra(TAcqIntentExtra.EXTRA_CUSTOMER_KEY) != null;
    }

    private void navigateBack() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    public static void dispatchResult(int resultCode, Intent data, OnPaymentListener listener) {
        if (resultCode == RESULT_OK) {
            listener.onSuccess(data.getLongExtra(TAcqIntentExtra.EXTRA_PAYMENT_ID, -1L));
        } else if (resultCode == RESULT_CANCELED) {
            listener.onCancelled();
        } else if (resultCode == RESULT_ERROR) {
            listener.onError((Exception) data.getSerializableExtra(TAcqIntentExtra.EXTRA_ERROR));
        }
    }

    private Card[] filterCards(@NonNull Card[] cards) {
        if (!chargeMode) {
            return cards;
        }

        ArrayList<Card> list = new ArrayList<>();
        for (Card card : cards) {
            if (!TextUtils.isEmpty(card.getRebillId())) {
                list.add(card);
            }
        }
        return list.toArray(new Card[list.size()]);
    }

    @Override
    public AsdkLocalization getAsdkLocalization() {
        return asdkLocalizationProperty.getAsdkLocalization();
    }
}
