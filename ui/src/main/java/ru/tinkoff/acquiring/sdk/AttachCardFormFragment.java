package ru.tinkoff.acquiring.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import android.widget.Toast;

import java.util.Map;

import ru.tinkoff.acquiring.sdk.inflate.attach.AttachCellInflater;
import ru.tinkoff.acquiring.sdk.inflate.attach.AttachCellType;
import ru.tinkoff.acquiring.sdk.responses.AttachCardResponse;
import ru.tinkoff.acquiring.sdk.views.BankKeyboard;
import ru.tinkoff.acquiring.sdk.views.EditCardView;

/**
 * @author Vitaliy Markus
 */
public class AttachCardFormFragment extends Fragment implements OnBackPressedListener {

    private static final int BUTTON_UNDER_FIELDS_ICONS_ON_BOTTOM = 0;
    private static final int ICONS_ON_BOTTOM_BUTTON_UNDER_ICONS = 1;
    private static final int ICONS_UNDER_FIELDS_BUTTON_ON_BOTTOM = 2;
    private static final int ICONS_UNDER_FIELDS_BUTTON_UNDER_ICONS = 3;
    private static final int BUTTON_UNDER_FIELDS_ICONS_UNDER_BOTTOM = 4;

    @NonNull
    private EditCardView editCardView;
    @NonNull
    private Button attachButton;
    @Nullable
    private EditText emailView;

    private BankKeyboard customKeyboard;

    private FullCardScanner cardScanner;
    private CardManager cardManager;

    private int buttonAndIconsPositionMode;

    public static AttachCardFormFragment newInstance() {
        Bundle args = new Bundle();
        AttachCardFormFragment fragment = new AttachCardFormFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{R.attr.acqPayButtonAndIconPosition});
        buttonAndIconsPositionMode = typedArray.getInt(0, BUTTON_UNDER_FIELDS_ICONS_ON_BOTTOM);
        typedArray.recycle();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int[] intTypes = getActivity().getIntent().getIntArrayExtra(AttachCardFormActivity.EXTRA_DESIGN_CONFIGURATION);
        AttachCellType[] cellTypes = AttachCellType.toPayCellTypeArray(intTypes);
        View root = AttachCellInflater.from(inflater, cellTypes).inflate(container);
        initViews(root);

        AttachCardFormActivity activity = (AttachCardFormActivity) getActivity();
        cardManager = new CardManager(activity.getSdk());

        cardScanner = new FullCardScanner(this, (ICameraCardScanner) getActivity().getIntent().getSerializableExtra(AttachCardFormActivity.EXTRA_CAMERA_CARD_SCANNER));
        editCardView.setCardSystemIconsHolder(new ThemeCardLogoCache(getActivity()));
        editCardView.setActions(cardScanner);
        if (!cardScanner.isScanEnable()) {
            editCardView.setBtnScanIcon(View.NO_ID);
        }

        boolean isUsingCustomKeyboard = ((AttachCardFormActivity) getActivity()).shouldUseCustomKeyboard();
        if (isUsingCustomKeyboard) {
            Window window = getActivity().getWindow();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            editCardView.disableCopyPaste();
        } else {
            customKeyboard.hide();
        }

        resolveButtonAndIconsPosition(root);

        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customKeyboard != null) {
                    customKeyboard.hide();
                }

                AttachCardFormActivity activity = (AttachCardFormActivity) getActivity();
                final String email = getEmail();
                if (!validateInput(editCardView, email)) {
                    return;
                }

                activity.showProgressDialog();
                CardData cardData = new CardData(editCardView.getCardNumber(), editCardView.getExpireDate(), editCardView.getCvc());
                attachCard(cardManager, activity.getIntent(), cardData, email);
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (cardScanner.hasCameraResult(requestCode, data)) {
            ICreditCard card = cardScanner.parseCameraData(data);
            setCreditCardData(card);
        } else if (cardScanner.hasNfcResult(requestCode, resultCode)) {
            ICreditCard card = cardScanner.parseNfcData(data);
            setCreditCardData(card);
        } else if (cardScanner.isNfcError(requestCode, resultCode)) {
            Toast.makeText(getContext(), R.string.acq_nfc_scan_failed, Toast.LENGTH_SHORT).show();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isUsingCustomKeyboard = ((AttachCardFormActivity) getActivity()).shouldUseCustomKeyboard();
        if (customKeyboard != null && isUsingCustomKeyboard) {
            customKeyboard.attachToView(editCardView);

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
    public boolean onBackPressed() {
        boolean isUsingCustomKeyboard = ((AttachCardFormActivity) getActivity()).shouldUseCustomKeyboard();
        if (customKeyboard != null && isUsingCustomKeyboard) {
            return customKeyboard.hide();
        } else {
            return false;
        }
    }

    private static void attachCard(final CardManager cardManager, final Intent intent, final CardData cardData, final String email) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String customerKey = intent.getStringExtra(AttachCardFormActivity.EXTRA_CUSTOMER_KEY);
                    String checkType = intent.getStringExtra(AttachCardFormActivity.EXTRA_CHECK_TYPE);
                    Map<String, String> data = (Map<String, String>) intent.getSerializableExtra(AttachCardFormActivity.EXTRA_DATA);

                    AttachCardResponse response = cardManager.attachCard(customerKey, checkType, cardData, email, data);

                    PaymentStatus status = response.getStatus();
                    if (status == null) {
                        AttachCardFormHandler.INSTANCE.obtainMessage(AttachCardFormHandler.CARD_ID, response.getCardId()).sendToTarget();
                        CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.SUCCESS).sendToTarget();
                    } else if (status == PaymentStatus.THREE_DS_CHECKING) {
                        ThreeDsData threeDsData = response.getThreeDsData();
                        CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.START_3DS, threeDsData).sendToTarget();
                    } else if (status == PaymentStatus.LOOP_CHECKING) {
                        AttachCardFormHandler.INSTANCE.obtainMessage(AttachCardFormHandler.SHOW_LOOP_CONFIRMATIONS, response.getRequestKey()).sendToTarget();
                    } else {
                        CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.EXCEPTION, new AcquiringSdkException(new IllegalStateException("PaymentState = " + status))).sendToTarget();
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    if (cause != null && cause instanceof NetworkException) {
                        CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.NO_NETWORK).sendToTarget();
                    } else {
                        CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.EXCEPTION, e).sendToTarget();
                    }
                }
            }
        }).start();
    }

    private void initViews(View root) {
        editCardView = root.findViewById(R.id.ecv_card);
        emailView = root.findViewById(R.id.et_email);
        attachButton = root.findViewById(R.id.btn_attach);
        customKeyboard = root.findViewById(R.id.acq_keyboard);

        final String email = getActivity().getIntent().getStringExtra(AttachCardFormActivity.EXTRA_EMAIL);
        if (email != null && emailView != null) {
            emailView.setText(email);
        }
    }

    private void setCreditCardData(ICreditCard card) {
        editCardView.setCardNumber(card.getCardNumber());
        editCardView.setExpireDate(card.getExpireDate());
    }

    private void resolveButtonAndIconsPosition(View root) {
        LinearLayout containerLayout = root.findViewById(R.id.ll_container_layout);
        View space = root.findViewById(R.id.space);
        View secureIcons = root.findViewById(R.id.iv_secure_icons);
        switch (buttonAndIconsPositionMode) {
            case BUTTON_UNDER_FIELDS_ICONS_ON_BOTTOM:
                break;
            case ICONS_ON_BOTTOM_BUTTON_UNDER_ICONS:
                containerLayout.removeView(attachButton);
                containerLayout.addView(attachButton);
                break;
            case ICONS_UNDER_FIELDS_BUTTON_ON_BOTTOM:
                containerLayout.removeView(secureIcons);
                removeSpace(containerLayout, space);
                containerLayout.removeView(attachButton);
                containerLayout.addView(secureIcons);
                addSpace(containerLayout, space);
                containerLayout.addView(attachButton);
                break;
            case ICONS_UNDER_FIELDS_BUTTON_UNDER_ICONS:
                containerLayout.removeView(secureIcons);
                removeSpace(containerLayout, space);
                containerLayout.removeView(attachButton);
                containerLayout.addView(secureIcons);
                containerLayout.addView(attachButton);
                break;
            case BUTTON_UNDER_FIELDS_ICONS_UNDER_BOTTOM:
                removeSpace(containerLayout, space);
                break;
        }
    }

    private void addSpace(ViewGroup container, View space) {
        if (space != null) {
            container.addView(space);
        }
    }

    private void removeSpace(ViewGroup container, View space) {
        if (space != null) {
            container.removeView(space);
        }
    }

    private void hideSoftKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private String getEmail() {
        if (emailView == null) {
            return null;
        }
        String input = emailView.getText().toString().trim();
        return input.isEmpty() ? null : input;
    }

    private boolean validateInput(EditCardView cardView, String email) {
        if (!cardView.isFilledAndCorrect()) {
            Toast.makeText(getActivity(), R.string.acq_invalid_card, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getActivity(), R.string.acq_invalid_email, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
