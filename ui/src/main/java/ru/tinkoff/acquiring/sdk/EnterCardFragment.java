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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import ru.tinkoff.acquiring.sdk.nfc.NfcCardScanActivity;
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
    public static final int REQUEST_MASKED_WALLET = 3;
    public static final int REQUEST_FULL_WALLET = 4;

    private static final int PAY_FORM_MAX_LENGTH = 20;

    private EditCardView ecvCard;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvAmount;
    private TextView tvSrcCardLabel;
    private TextView tvChooseCardButton;
    private EditText etEmail;
    private Button btnPay;
    private View srcCardChooser;

    private GoogleApiClient googleApiClient;

    private AcquiringSdk sdk;

    private Pattern emailPattern = Patterns.EMAIL_ADDRESS;

    private BankKeyboard customKeyboard;

    private AndroidPayParams androidPayParams;

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

        androidPayParams = activity.getIntent().getParcelableExtra(PayFormActivity.EXTRA_ANDROID_PAY_PARAMS);
        boolean isUsingCustomKeyboard = ((PayFormActivity) activity).shouldUseCustomKeyboard();
        if (isUsingCustomKeyboard) {

            // disable soft keyboard while custom keyboard is not attached to edit card view
            Window window = activity.getWindow();
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

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sdk = ((PayFormActivity) getActivity()).getSdk();

        Intent intent = getActivity().getIntent();

        final String email = intent.getStringExtra(PayFormActivity.EXTRA_E_MAIL);
        if (email != null) {
            etEmail.setText(email);
        }

        final String title = intent.getStringExtra(PayFormActivity.EXTRA_TITLE);
        tvTitle.setText(title);

        String description = intent.getStringExtra(PayFormActivity.EXTRA_DESCRIPTION);
        tvDescription.setText(description);

        final Money amount = (Money) intent.getSerializableExtra(PayFormActivity.EXTRA_AMOUNT);
        tvAmount.setText(amount != null ? amount.toHumanReadableString() : null);

        final String orderId = intent.getStringExtra(PayFormActivity.EXTRA_ORDER_ID);
        final String customerKey = intent.getStringExtra(PayFormActivity.EXTRA_CUSTOMER_KEY);
        final boolean reccurentPayment = intent.getBooleanExtra(PayFormActivity.EXTRA_RECURENT_PAYMENT, false);

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

                Card srcCard = activity.getSourceCard();
                CardData cardData;

                if (srcCard == null) {
                    cardData = new CardData(ecvCard.getCardNumber(), ecvCard.getExpireDate(), ecvCard.getCvc());
                } else {
                    String cardId = srcCard.getCardId();
                    String cvc = ecvCard.getCvc();
                    cardData = new CardData(cardId, cvc);
                }

                setupProgressDialog(true);

                initPayment(sdk, orderId, null, title, amount, cardData, enteredEmail,
                        reccurentPayment, resolveLanguage(), null);
            }
        });
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

    @Override
    public void onPause() {
        super.onPause();
        hideSoftKeyboard();
    }

    @Override
    public void onStart() {
        super.onStart();
        onCardReady();
        if (androidPayParams != null) {
            initGoogleApiClient();
            initAndroidPay();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private void initGoogleApiClient() {
        Wallet.WalletOptions options = new Wallet.WalletOptions.Builder()
                .setEnvironment(androidPayParams.getEnvironment())
                .build();

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Wallet.API, options)
                .build();
        googleApiClient.connect();
    }

    private void initAndroidPay() {
        IsReadyToPayRequest isReadyTpPayRequest = IsReadyToPayRequest.newBuilder()
                .addAllowedCardNetwork(WalletConstants.CardNetwork.MASTERCARD)
                .addAllowedCardNetwork(WalletConstants.CardNetwork.VISA)
                .build();

        setupProgressDialog(true);
        Wallet.Payments.isReadyToPay(googleApiClient, isReadyTpPayRequest).setResultCallback(
                new ResultCallback<BooleanResult>() {
                    @Override
                    public void onResult(@NonNull BooleanResult booleanResult) {
                        setupProgressDialog(false);
                        if (booleanResult.getStatus().isSuccess() && booleanResult.getValue()) {
                            showAndroidPayButton();
                        } else {
                            hideAndroidPayButton();
                        }
                    }
                });
    }

    private void hideAndroidPayButton() {
        View view = getView();
        if (view != null) {
            View container = view.findViewById(R.id.fl_android_pay_placeholder);
            container.setVisibility(View.GONE);
        }
    }

    private SupportWalletFragment initWalletFragment() {
        WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                .setBuyButtonText(androidPayParams.getBuyButtonText())
                .setBuyButtonAppearance(androidPayParams.getBuyButtonAppearance())
                .setBuyButtonHeight(WalletFragmentStyle.Dimension.MATCH_PARENT)
                .setBuyButtonWidth(WalletFragmentStyle.Dimension.UNIT_PX, btnPay.getWidth());

        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(androidPayParams.getEnvironment())
                .setFragmentStyle(walletFragmentStyle)
                .setTheme(androidPayParams.getTheme())
                .setMode(WalletFragmentMode.BUY_BUTTON)
                .build();

        return SupportWalletFragment.newInstance(walletFragmentOptions);
    }

    private void showAndroidPayButton() {
        MaskedWalletRequest maskedWalletRequest = buildMaskedWalledRequest();

        WalletFragmentInitParams.Builder startParamsBuilder =
                WalletFragmentInitParams.newBuilder()
                        .setMaskedWalletRequest(maskedWalletRequest)
                        .setMaskedWalletRequestCode(REQUEST_MASKED_WALLET);
        SupportWalletFragment walletFragment = initWalletFragment();
        walletFragment.initialize(startParamsBuilder.build());

        getView().findViewById(R.id.fl_android_pay_placeholder).setVisibility(View.VISIBLE);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fl_android_pay_placeholder, walletFragment)
                .commit();
    }

    private MaskedWalletRequest buildMaskedWalledRequest() {
        Intent intent = getActivity().getIntent();

        PaymentMethodTokenizationParameters parameters =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.NETWORK_TOKEN)
                        .addParameter("publicKey", AndroidPayParams.PUBLIC_KEY)
                        .build();

        String description = intent.getStringExtra(PayFormActivity.EXTRA_DESCRIPTION);
        String price = getFormattedPrice();
        return MaskedWalletRequest.newBuilder()
                .setMerchantName(androidPayParams.getMerchantName())
                .setPhoneNumberRequired(androidPayParams.isPhoneRequired())
                .setShippingAddressRequired(androidPayParams.isAddressRequired())
                .setCountryCode(androidPayParams.getCountryCode())
                .setCurrencyCode(AndroidPayParams.CURRENCY_CODE)
                .setEstimatedTotalPrice(price)
                .setCart(getAndroidPayCart(description, price))
                .setPaymentMethodTokenizationParameters(parameters)
                .build();
    }

    private String getFormattedPrice() {
        Intent intent = getActivity().getIntent();
        Money money = (Money) intent.getSerializableExtra(PayFormActivity.EXTRA_AMOUNT);
        BigDecimal bigDecimal = new BigDecimal(money.getCoins());
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return bigDecimal.toString();
    }

    private void setupProgressDialog(boolean isEnabled) {
        PayFormActivity activity = (PayFormActivity) getActivity();
        if (activity == null) {
            return;
        }
        if (isEnabled) {
            activity.showProgressDialog();
        } else {
            activity.hideProgressDialog();
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
        } else if (!isValidEmail(enteredEmail)) {
            errorMessage = R.string.acq_invalid_email;
        }

        if (errorMessage != 0) {
            makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String enteredEmail) {
        return !TextUtils.isEmpty(enteredEmail) && emailPattern.matcher(enteredEmail).matches();
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

        int androidPayErrorCode = -1;
        if (data != null) {
            androidPayErrorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
        }

        if (requestCode == REQUEST_MASKED_WALLET && resultCode == Activity.RESULT_OK) {
            startFullWalletRequest(data);
        } else if (requestCode == REQUEST_MASKED_WALLET) {
            handleAndroidPayError(androidPayErrorCode);
        }

        if (requestCode == REQUEST_FULL_WALLET && resultCode == Activity.RESULT_OK) {
            initAndroidPayPayment(data);
        } else if (requestCode == REQUEST_FULL_WALLET) {
            handleAndroidPayError(androidPayErrorCode);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleAndroidPayError(int errorCode) {
        switch (errorCode) {
            case WalletConstants.ERROR_CODE_INVALID_PARAMETERS:
            case WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE:
            case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:
            case WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR:
            case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
            case WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION:
            case WalletConstants.ERROR_CODE_UNKNOWN:
            default:
                PayFormActivity.handler.obtainMessage(SdkHandler.ANDROID_PAY_ERROR).sendToTarget();
                break;
        }
    }

    private void startFullWalletRequest(Intent data) {
        MaskedWallet maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);

        Intent intent = getActivity().getIntent();
        String description = intent.getStringExtra(PayFormActivity.EXTRA_DESCRIPTION);

        String price = getFormattedPrice();

        FullWalletRequest request = FullWalletRequest.newBuilder()
                .setGoogleTransactionId(maskedWallet.getGoogleTransactionId())
                .setCart(getAndroidPayCart(description, price))
                .build();

        Wallet.Payments.loadFullWallet(googleApiClient, request, REQUEST_FULL_WALLET);
    }

    private void initAndroidPayPayment(Intent data) {
        final String enteredEmail = getEmail();
        if (!isValidEmail(enteredEmail)) {
            makeText(getActivity(), R.string.acq_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        FullWallet fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
        PaymentMethodToken token = fullWallet.getPaymentMethodToken();
        String tokenJson = token.getToken();

        Intent intent = getActivity().getIntent();
        final Money amount = (Money) intent.getSerializableExtra(PayFormActivity.EXTRA_AMOUNT);
        final String orderId = intent.getStringExtra(PayFormActivity.EXTRA_ORDER_ID);
        final String customerKey = intent.getStringExtra(PayFormActivity.EXTRA_CUSTOMER_KEY);
        final boolean recurrentPayment = intent.getBooleanExtra(PayFormActivity.EXTRA_RECURENT_PAYMENT, false);
        String description = intent.getStringExtra(PayFormActivity.EXTRA_DESCRIPTION);


        initPayment(sdk, orderId, customerKey, description, amount, null, enteredEmail,
                recurrentPayment, resolveLanguage(), tokenJson);
    }

    private Cart getAndroidPayCart(String description, String price) {
        return Cart.newBuilder()
                .setTotalPrice(price)
                .setCurrencyCode(AndroidPayParams.CURRENCY_CODE)
                .addLineItem(LineItem.newBuilder()
                        .setDescription(description)
                        .setTotalPrice(price)
                        .setCurrencyCode(AndroidPayParams.CURRENCY_CODE)
                        .build())
                .build();
    }

    private static void initPayment(final AcquiringSdk sdk,
                                    final String orderId,
                                    final String customerKey,
                                    final String payFormTitle,
                                    final Money amount,
                                    final CardData cardData,
                                    final String email,
                                    final boolean recurrentPayment,
                                    final Language language,
                                    final String androidPayToken) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String payForm;
                    if (payFormTitle != null && payFormTitle.length() > PAY_FORM_MAX_LENGTH) {
                        payForm = payFormTitle.substring(0, PAY_FORM_MAX_LENGTH);
                    } else {
                        payForm = payFormTitle;
                    }
                    Long paymentId;
                    if (language == null) {
                        paymentId = sdk.init(amount, orderId, customerKey, null, payForm, recurrentPayment);
                    } else {
                        paymentId = sdk.init(amount, orderId, customerKey, null, payForm, recurrentPayment, language);
                    }

                    PayFormActivity.handler.obtainMessage(SdkHandler.PAYMENT_INIT_COMPLETED, paymentId).sendToTarget();

                    ThreeDsData threeDsData;
                    if (androidPayToken != null) {
                        threeDsData = sdk.finishAuthorize(paymentId, androidPayToken, email);
                    } else {
                        threeDsData = sdk.finishAuthorize(paymentId, cardData, email);
                    }

                    if (threeDsData.isThreeDsNeed()) {
                        PayFormActivity.handler.obtainMessage(SdkHandler.START_3DS, threeDsData).sendToTarget();
                    } else {
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
