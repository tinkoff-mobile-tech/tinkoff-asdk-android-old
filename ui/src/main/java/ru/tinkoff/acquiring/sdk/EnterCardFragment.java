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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import ru.tinkoff.acquiring.sdk.nfc.NfcCardScanActivity;
import ru.tinkoff.acquiring.sdk.requests.InitRequestBuilder;
import ru.tinkoff.acquiring.sdk.views.BankKeyboard;
import ru.tinkoff.acquiring.sdk.views.EditCardView;

import static android.widget.Toast.makeText;

/**
 * @author a.shishkin1
 */
public class EnterCardFragment extends Fragment implements EditCardView.Actions,
        ICardInterest,
        OnBackPressedListener {

    public static final int REQUEST_CARD_IO = 1;
    public static final int REQUEST_CARD_NFC = 2;

    private static final int PAY_FORM_MAX_LENGTH = 20;

    private static final int AMOUNT_POSITION_INDEX = 0;
    private static final int BUTTON_POSITION_INDEX = 1;
    private static final int PAY_WITH_AMOUNT_FORMAT_INDEX = 2;

    private static final int AMOUNT_POSITION_OVER_FIELDS = 0;

    private static final int BUTTON_UNDER_FIELDS_ICONS_ON_BOTTOM = 0;
    private static final int ICONS_ON_BOTTOM_BUTTON_UNDER_ICONS = 1;
    private static final int ICONS_UNDER_FIELDS_BUTTON_ON_BOTTOM = 2;
    private static final int ICONS_UNDER_FIELDS_BUTTON_UNDER_ICONS = 3;
    private static final int BUTTON_UNDER_FIELDS_ICONS_UNDER_BOTTOM = 4;


    private EditCardView ecvCard;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvAmount;
    private TextView tvSrcCardLabel;
    private TextView tvChooseCardButton;
    private EditText etEmail;
    private Button btnPay;
    private View srcCardChooser;

    private AcquiringSdk sdk;

    private Pattern emailPattern = Patterns.EMAIL_ADDRESS;

    private BankKeyboard customKeyboard;

    private boolean chargeMode;
    private int amountPositionMode;
    private int buttonAndIconsPositionMode;
    private String payAmountFormat;

    public static EnterCardFragment newInstance(boolean chargeMode) {
        Bundle args = new Bundle();
        args.putBoolean(PayFormActivity.EXTRA_CHARGE_MODE, chargeMode);
        EnterCardFragment fragment = new EnterCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{R.attr.acqPayAmountPosition, R.attr.acqPayButtonAndIconPosition, R.attr.acqPayWithAmountFormat});
        amountPositionMode = typedArray.getInt(AMOUNT_POSITION_INDEX, AMOUNT_POSITION_OVER_FIELDS);
        buttonAndIconsPositionMode = typedArray.getInt(BUTTON_POSITION_INDEX, BUTTON_UNDER_FIELDS_ICONS_ON_BOTTOM);
        payAmountFormat = typedArray.getString(PAY_WITH_AMOUNT_FORMAT_INDEX);
        typedArray.recycle();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.acq_fragment_enter_card, container, false);
        ecvCard = (EditCardView) view.findViewById(R.id.ecv_card);

        tvSrcCardLabel = (TextView) view.findViewById(R.id.tv_src_card_label);
        tvDescription = (TextView) view.findViewById(R.id.tv_description);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvAmount = (TextView) view.findViewById(R.id.tv_amount);
        tvChooseCardButton = (TextView) view.findViewById(R.id.tv_src_card_choose_btn);
        btnPay = (Button) view.findViewById(R.id.btn_pay);
        srcCardChooser = view.findViewById(R.id.ll_src_card_chooser);

        etEmail = (EditText) view.findViewById(R.id.et_email);

        final FragmentActivity activity = getActivity();
        ecvCard.setCardSystemIconsHolder(new CardSystemIconsHolderImpl(activity));
        ecvCard.setActions(this);

        customKeyboard = (BankKeyboard) view.findViewById(R.id.acq_keyboard);

        boolean isUsingCustomKeyboard = ((PayFormActivity) activity).shouldUseCustomKeyboard();
        if (isUsingCustomKeyboard) {

            // disable soft keyboard while custom keyboard is not attached to edit card view
            Window window = getActivity().getWindow();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            ecvCard.disableCopyPaste();
        } else {
            customKeyboard.hide();
        }

        tvChooseCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Card[] cards = ((PayFormActivity) activity).getCards();
                if (cards != null) {
                    startChooseCard();
                }
            }
        });

        srcCardChooser.setVisibility(View.GONE);

        chargeMode = getArguments().getBoolean(PayFormActivity.EXTRA_CHARGE_MODE);
        if (chargeMode) {
            ecvCard.setEnabled(false);
            ecvCard.setFocusable(false);
            ecvCard.clearFocus();
            ecvCard.setFullCardNumberModeEnable(false);
            ecvCard.setCardHint(getString(R.string.acq_recurrent_mode_card_hint));
        }

        if (amountPositionMode != AMOUNT_POSITION_OVER_FIELDS) {
            view.findViewById(R.id.ll_price_layout).setVisibility(View.GONE);
        }

        resolveButtonAndIconsPosition(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sdk = ((PayFormActivity) getActivity()).getSdk();

        final Intent intent = getActivity().getIntent();

        final String email = intent.getStringExtra(PayFormActivity.EXTRA_E_MAIL);
        if (email != null) {
            etEmail.setText(email);
        }

        final String title = intent.getStringExtra(PayFormActivity.EXTRA_TITLE);
        tvTitle.setText(title);

        String description = intent.getStringExtra(PayFormActivity.EXTRA_DESCRIPTION);
        tvDescription.setText(description);

        final Money amount = (Money) intent.getSerializableExtra(PayFormActivity.EXTRA_AMOUNT);
        String amountText = amount != null ? amount.toHumanReadableString() : "";
        if (amountPositionMode == AMOUNT_POSITION_OVER_FIELDS) {
            tvAmount.setText(amountText);
        } else if (TextUtils.isEmpty(payAmountFormat)) {
            String text = btnPay.getText().toString();
            btnPay.setText(text + " " + amountText);
        } else {
            btnPay.setText(String.format(payAmountFormat, amountText));
        }

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customKeyboard != null) {
                    customKeyboard.hide();
                }

                final String enteredEmail = getEmail();
                if (!validateInput(enteredEmail)) {
                    return;
                }
                final PayFormActivity activity = (PayFormActivity) getActivity();
                final CardData cardData = getCardData(activity);
                activity.showProgressDialog();

                InitRequestBuilder requestBuilder = createInitRequestBuilder(intent);
                initPayment(sdk, requestBuilder, cardData, enteredEmail);
            }
        });
    }

    private void resolveButtonAndIconsPosition(View root) {
        LinearLayout containerLayout = (LinearLayout) root.findViewById(R.id.ll_container_layout);
        View space = root.findViewById(R.id.space);
        View secureIcons = root.findViewById(R.id.iv_secure_icons);
        switch (buttonAndIconsPositionMode) {
            case BUTTON_UNDER_FIELDS_ICONS_ON_BOTTOM:
                break;
            case ICONS_ON_BOTTOM_BUTTON_UNDER_ICONS:
                containerLayout.removeView(btnPay);
                containerLayout.addView(btnPay);
                break;
            case ICONS_UNDER_FIELDS_BUTTON_ON_BOTTOM:
                containerLayout.removeView(secureIcons);
                containerLayout.removeView(space);
                containerLayout.removeView(btnPay);
                containerLayout.addView(secureIcons);
                containerLayout.addView(space);
                containerLayout.addView(btnPay);
                break;
            case ICONS_UNDER_FIELDS_BUTTON_UNDER_ICONS:
                containerLayout.removeView(secureIcons);
                containerLayout.removeView(space);
                containerLayout.removeView(btnPay);
                containerLayout.addView(secureIcons);
                containerLayout.addView(btnPay);
                break;
            case BUTTON_UNDER_FIELDS_ICONS_UNDER_BOTTOM:
                containerLayout.removeView(space);
                break;
        }
    }

    private Language resolveLanguage() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();

        if (language != null && language.toLowerCase().startsWith("ru")) {
            return null;
        } else {
            return Language.ENGLISH;
        }
    }

    private boolean validateInput(String enteredEmail) {
        int errorMessage = 0;
        if (!ecvCard.isFilledAndCorrect()) {
            errorMessage = R.string.acq_invalid_card;
        } else if (!TextUtils.isEmpty(enteredEmail) && !emailPattern.matcher(enteredEmail).matches()) {
            errorMessage = R.string.acq_invalid_email;
        }

        if (errorMessage != 0) {
            makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private InitRequestBuilder createInitRequestBuilder(Intent intent) {
        final String password = intent.getStringExtra(PayFormActivity.EXTRA_PASSWORD);
        final String terminalKey = intent.getStringExtra(PayFormActivity.EXTRA_TERMINAL_KEY);

        final InitRequestBuilder builder = new InitRequestBuilder(password, terminalKey);

        final String orderId = intent.getStringExtra(PayFormActivity.EXTRA_ORDER_ID);
        final String customerKey = intent.getStringExtra(PayFormActivity.EXTRA_CUSTOMER_KEY);
        String title = intent.getStringExtra(PayFormActivity.EXTRA_TITLE);
        if (title != null && title.length() > PAY_FORM_MAX_LENGTH) {
            title = title.substring(0, PAY_FORM_MAX_LENGTH);
        }
        final Money amount = (Money) intent.getSerializableExtra(PayFormActivity.EXTRA_AMOUNT);
        final boolean recurrentPayment = intent.getBooleanExtra(PayFormActivity.EXTRA_RECURRENT_PAYMENT, false);

        builder.setOrderId(orderId)
                .setCustomerKey(customerKey)
                .setPayForm(title)
                .setAmount(amount.getCoins())
                .setRecurrent(recurrentPayment);

        final Language language = resolveLanguage();
        if (language != null) {
            builder.setLanguage(language.toString());
        }

        final Receipt receiptValue = (Receipt) intent.getSerializableExtra(PayFormActivity.EXTRA_RECEIPT_VALUE);
        if (receiptValue != null) {
            builder.setReceipt(receiptValue);
        }

        final Map<String, String> dataValue = (Map<String, String>) intent.getSerializableExtra(PayFormActivity.EXTRA_DATA_VALUE);
        if (dataValue != null) {
            builder.setData(dataValue);
        }

        return builder;
    }

    private CardData getCardData(PayFormActivity activity) {
        final Card card = activity.getSourceCard();

        if (chargeMode) {
            return new CardData(card.getRebillId());
        } else if (card == null) {
            return new CardData(ecvCard.getCardNumber(), ecvCard.getExpireDate(), ecvCard.getCvc());
        } else {
            String cardId = card.getCardId();
            String cvc = ecvCard.getCvc();
            return new CardData(cardId, cvc);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isUsingCustomKeyboard = ((PayFormActivity) getActivity()).shouldUseCustomKeyboard();
        if (customKeyboard != null && isUsingCustomKeyboard) {
            customKeyboard.attachToView(ecvCard);

            Window window = getActivity().getWindow();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
        }
    }

    private String getEmail() {
        String input = etEmail.getText().toString().trim();
        return input.isEmpty() ? null : input;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPressScanCard(EditCardView editCardView) {
        //noinspection ConstantConditions,ConstantConditions
        if (isNfcEnable()) {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            CharSequence items[] = getResources().getStringArray(R.array.acq_scan_types);
            adb.setItems(items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int i) {
                    if (i == 0) {
                        startCardIo();
                    } else if (i == 1) {
                        startNfcScan();
                    }
                    dialog.dismiss();
                }

            });
            adb.show();
        } else {
            startCardIo();
        }
    }

    @Override
    public void onUpdate(EditCardView editCardView) {
    }

    private boolean isNfcEnable() {
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideSoftKeyboard();
    }

    private void startCardIo() {
        Intent scanIntent = new Intent(getActivity(), CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true);
        startActivityForResult(scanIntent, REQUEST_CARD_IO);
    }

    private void startNfcScan() {
        Intent cardFromNfcIntent = new Intent(getActivity(), NfcCardScanActivity.class);
        startActivityForResult(cardFromNfcIntent, REQUEST_CARD_NFC);
    }

    private void startChooseCard() {
        ((PayFormActivity) getActivity()).startChooseCard();
    }

    @Override
    public void onStart() {
        super.onStart();
        onCardReady();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CARD_IO && data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
            ecvCard.setCardNumber(scanResult.getFormattedCardNumber());
            if (scanResult.expiryMonth != 0 && scanResult.expiryYear != 0) {
                Locale locale = Locale.getDefault();
                int expiryYear = scanResult.expiryYear % 100;
                String format = String.format(locale, "%02d%02d", scanResult.expiryMonth, expiryYear);
                ecvCard.setExpireDate(format);
            }
            return;
        }

        if (requestCode == REQUEST_CARD_NFC && resultCode == Activity.RESULT_OK) {
            ru.tinkoff.core.nfc.model.Card card = (ru.tinkoff.core.nfc.model.Card) data.getSerializableExtra(NfcCardScanActivity.EXTRA_CARD);
            ecvCard.setCardNumber(card.getNumber());
            ecvCard.setExpireDate(card.getExpirationDate());
            return;
        } else if (requestCode == REQUEST_CARD_NFC && resultCode == NfcCardScanActivity.RESULT_ERROR) {
            Toast t = Toast.makeText(getContext(), R.string.acq_nfc_scan_failed, Toast.LENGTH_SHORT);
            t.show();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initPayment(final AcquiringSdk sdk,
                             final InitRequestBuilder requestBuilder,
                             final CardData cardData,
                             final String email) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    requestBuilder.setChargeFlag(chargeMode);
                    final Long paymentId = sdk.init(requestBuilder);
                    PayFormActivity.handler.obtainMessage(SdkHandler.PAYMENT_INIT_COMPLETED, paymentId).sendToTarget();

                    if (!chargeMode) {
                        final ThreeDsData threeDsData = sdk.finishAuthorize(paymentId, cardData, email);
                        if (threeDsData.isThreeDsNeed()) {
                            PayFormActivity.handler.obtainMessage(SdkHandler.START_3DS, threeDsData).sendToTarget();
                        } else {
                            PayFormActivity.handler.obtainMessage(SdkHandler.SUCCESS).sendToTarget();
                        }
                    } else {
                        PaymentInfo paymentInfo = sdk.charge(paymentId, cardData.getRebillId());
                        PayFormActivity.handler.obtainMessage(SdkHandler.SUCCESS).sendToTarget();
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    Message msg;
                    if (cause != null && cause instanceof NetworkException) {
                        msg = PayFormActivity.handler.obtainMessage(SdkHandler.NO_NETWORK);
                    } else {
                        msg = PayFormActivity.handler.obtainMessage(SdkHandler.EXCEPTION, e);
                    }
                    msg.sendToTarget();
                }
            }

        }).start();
    }

    @Override
    public void onCardReady() {

        PayFormActivity.handler.post(new Runnable() {
            @Override
            public void run() {
                PayFormActivity activity = (PayFormActivity) getActivity();
                if (activity == null) {
                    return;
                }
                Card[] cards = activity.getCards();
                Card sourceCard = activity.getSourceCard();
                boolean hasCard = sourceCard != null;
                srcCardChooser.setVisibility(cards != null && cards.length > 0 ? View.VISIBLE : View.GONE);
                tvSrcCardLabel.setText(hasCard ? R.string.acq_saved_card_label : R.string.acq_new_card_label);
                if (chargeMode) {
                    if (hasCard) {
                        ecvCard.setRecurrentPaymentMode(true);
                        ecvCard.setCardNumber(sourceCard.getPan());
                    }
                    hideSoftKeyboard();
                    if (customKeyboard != null) {
                        customKeyboard.hide();
                    }
                } else {
                    ecvCard.setSavedCardState(hasCard);
                    if (hasCard) {
                        ecvCard.setCardNumber(sourceCard.getPan());
                    } else {
                        Bundle bundle = activity.getFragmentsCommunicator().getResult(PayFormActivity.RESULT_CODE_CLEAR_CARD);
                        if (bundle != null) {
                            ecvCard.clear();
                        } else {
                            ecvCard.dispatchFocus();
                        }
                    }
                }
            }
        });

    }

    private void hideSoftKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onBackPressed() {
        boolean isUsingCustomKeyboard = ((PayFormActivity) getActivity()).shouldUseCustomKeyboard();
        if (customKeyboard != null && isUsingCustomKeyboard) {
            return customKeyboard.hide();
        } else {
            return false;
        }
    }

    private static class CardSystemIconsHolderImpl extends ThemeCardLogoCache implements EditCardView.CardSystemIconsHolder {

        private Context context;

        public CardSystemIconsHolderImpl(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public Bitmap getCardSystemBitmap(String cardNumber) {
            return getLogoByNumber(context, cardNumber);
        }
    }

}
