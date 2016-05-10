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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import ru.tinkoff.acquiring.sdk.nfc.NfcCardScanActivity;
import ru.tinkoff.acquiring.sdk.views.BankKeyboard;
import ru.tinkoff.acquiring.sdk.views.EditCardView;

/**
 * @author a.shishkin1
 */
public class EnterCardFragment extends Fragment implements EditCardView.Actions,
        ICardInterest,
        OnBackPressedListener {

    public static final int REQUEST_CARD_IO = 1;
    public static final int REQUEST_CARD_NFC = 2;

    static final String EXTRA_PAYMENT_ID = "payment_id";

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

        ecvCard.setCardSystemIconsHolder(new CardSystemIconsHolderImpl(getActivity()));
        ecvCard.setActions(this);

        if (((PayFormActivity) getActivity()).shouldUseCustomKeyboard()) {
            customKeyboard = (BankKeyboard) view.findViewById(R.id.acq_keyboard);
        }

        tvChooseCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Card[] cards = ((PayFormActivity) getActivity()).getCards();
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
                CardData cardData = null;

                if (srcCard == null) {
                    cardData = new CardData(ecvCard.getCardNumber(), ecvCard.getExpireDate(), ecvCard.getCvc());
                } else {
                    String cardId = srcCard.getCardId();
                    String cvc = ecvCard.getCvc();
                    cardData = new CardData(cardId, cvc);
                }

                activity.showProgressDialog();
                initPayment(sdk, orderId, customerKey, title, amount, cardData, enteredEmail, reccurentPayment);
            }
        });
    }

    private boolean validateInput(String enteredEmail) {
        int errorMessage = 0;
        if (!ecvCard.isFilledAndCorrect()) {
            errorMessage = R.string.acq_invalid_card;
        } else if (!TextUtils.isEmpty(enteredEmail) && !emailPattern.matcher(enteredEmail).matches()) {
            errorMessage = R.string.acq_invalid_email;
        }

        if (errorMessage != 0) {
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (customKeyboard != null) {
            customKeyboard.attachToView(ecvCard);
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
            ecvCard.setExpireDate(String.format("%02d%02d", scanResult.expiryMonth, (scanResult.expiryYear % 100)));
            return;
        }

        if (requestCode == REQUEST_CARD_NFC && resultCode == Activity.RESULT_OK) {
            ru.tinkoff.core.nfc.model.Card card = (ru.tinkoff.core.nfc.model.Card) data.getSerializableExtra(NfcCardScanActivity.EXTRA_CARD);
            ecvCard.setCardNumber(card.getNumber());
            ecvCard.setExpireDate(card.getExpirationDate());
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private static void initPayment(final AcquiringSdk sdk,
                                    final String orderId,
                                    final String customerKey,
                                    final String payFormTitle,
                                    final Money amount,
                                    final CardData cardData,
                                    final String email,
                                    final boolean reccurentPayment) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Long paymentId = sdk.init(amount, orderId, customerKey, null, payFormTitle, reccurentPayment);
                    PayFormActivity.handler.obtainMessage(SdkHandler.PAYMENT_INIT_COMPLETED, paymentId).sendToTarget();

                    final ThreeDsData threeDsData = sdk.finishAuthorize(paymentId, cardData, email);
                    if (threeDsData.isThreeDsNeed()) {
                        PayFormActivity.handler.obtainMessage(SdkHandler.START_3DS, threeDsData).sendToTarget();
                    } else {
                        PayFormActivity.handler.obtainMessage(SdkHandler.SUCCESS).sendToTarget();
                    }
                } catch (Exception e) {
                    PayFormActivity.handler.obtainMessage(SdkHandler.EXCEPTION, e).sendToTarget();
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
                Card[] cards = activity.getCards();
                Card sourceCard = activity.getSourceCard();
                boolean hasCard = sourceCard != null;
                srcCardChooser.setVisibility(cards != null && cards.length > 0 ? View.VISIBLE : View.GONE);
                tvSrcCardLabel.setText(hasCard ? R.string.acq_saved_card_label : R.string.acq_new_card_label);
                ecvCard.setSavedCardState(hasCard);
                if (hasCard) {
                    ecvCard.setCardNumber(sourceCard.getPan());
                } else {
                    Bundle bundle = ((FragmentsCommunicator.IFragmentManagerExtender) getActivity()).getFragmentsCommunicator().getResult(PayFormActivity.RESULT_CODE_CLEAR_CARD);
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
        return customKeyboard.hide();
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
