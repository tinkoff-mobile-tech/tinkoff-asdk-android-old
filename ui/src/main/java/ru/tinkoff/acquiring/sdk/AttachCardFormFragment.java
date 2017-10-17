package ru.tinkoff.acquiring.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

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

    private LinearLayout container;
    private TextView titleLabel;
    private TextView descriptionLabel;
    private EditCardView editCardView;
    private EditText emailView;
    private Button attachButtton;
    private ImageView secureIcons;
    private BankKeyboard customKeyboard;

    private FullCardScanner cardScanner;

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
        View root = inflater.inflate(R.layout.acq_fragment_attach_card, container, false);
        initViews(root);

        cardScanner = new FullCardScanner(this, null);
        editCardView.setCardSystemIconsHolder(new ThemeCardLogoCache(getActivity()));
        editCardView.setActions(cardScanner);

        boolean isUsingCustomKeyboard = ((AttachCardFormActivity) getActivity()).shouldUseCustomKeyboard();
        if (isUsingCustomKeyboard) {
            Window window = getActivity().getWindow();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            editCardView.disableCopyPaste();
        } else {
            customKeyboard.hide();
        }

        resolveButtonAndIconsPosition(root);

        attachButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customKeyboard != null) {
                    customKeyboard.hide();
                }

//                final String enteredEmail = getEmail();
//                if (!validateInput(enteredEmail)) {
//                    return;
//                }

                AttachCardFormActivity activity = (AttachCardFormActivity) getActivity();
                activity.showProgressDialog();
                CardData cardData = new CardData(editCardView.getCardNumber(), editCardView.getExpireDate(), editCardView.getCvc());
                attachCard(activity.getSdk(), cardData, null);
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

    private void attachCard(final AcquiringSdk sdk, final CardData cardData, final String email) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AttachCardFormActivity activity = (AttachCardFormActivity) getActivity();
                    Intent intent = activity.getIntent();

                    String customerKey = intent.getStringExtra(AttachCardFormActivity.EXTRA_CUSTOMER_KEY);
                    CheckType checkType = (CheckType) intent.getSerializableExtra(AttachCardFormActivity.EXTRA_CHECK_TYPE);
                    String requestKey = sdk.addCard(customerKey, checkType);

                    Map<String, String> data = (Map<String, String>) intent.getSerializableExtra(AttachCardFormActivity.EXTRA_DATA);
                    AttachCardResponse response = sdk.attachCard(requestKey, cardData, email, data);

                    PaymentStatus status = response.getStatus();
                    if (status == null) {
                        AttachCardFormHandler.INSTANCE.obtainMessage(AttachCardFormHandler.CARD_ID, response.getCardId()).sendToTarget();
                        CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.SUCCESS).sendToTarget();
                    } else if (status == PaymentStatus.THREE_DS_CHECKING) {
                        ThreeDsData threeDsData = response.getThreeDsData();
                        CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.START_3DS, threeDsData).sendToTarget();
                    } else if (status == PaymentStatus.LOOP_CHECKING) {

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
        container = (LinearLayout) root.findViewById(R.id.ll_container_layout);
        titleLabel = (TextView) root.findViewById(R.id.tv_title);
        descriptionLabel = (TextView) root.findViewById(R.id.tv_description);
        editCardView = (EditCardView) root.findViewById(R.id.ecv_card);
        emailView = (EditText) root.findViewById(R.id.et_email);
        attachButtton = (Button) root.findViewById(R.id.btn_attach);
        secureIcons = (ImageView) root.findViewById(R.id.iv_secure_icons);
        customKeyboard = (BankKeyboard) root.findViewById(R.id.acq_keyboard);

        editCardView.setCardNumber("5136 9149 2034 4072");
        editCardView.setExpireDate("11/17");
    }

    private void setCreditCardData(ICreditCard card) {
        editCardView.setCardNumber(card.getCardNumber());
        editCardView.setExpireDate(card.getExpireDate());
    }

    private void resolveButtonAndIconsPosition(View root) {
        LinearLayout containerLayout = (LinearLayout) root.findViewById(R.id.ll_container_layout);
        View space = root.findViewById(R.id.space);
        View secureIcons = root.findViewById(R.id.iv_secure_icons);
        switch (buttonAndIconsPositionMode) {
            case BUTTON_UNDER_FIELDS_ICONS_ON_BOTTOM:
                break;
            case ICONS_ON_BOTTOM_BUTTON_UNDER_ICONS:
                containerLayout.removeView(attachButtton);
                containerLayout.addView(attachButtton);
                break;
            case ICONS_UNDER_FIELDS_BUTTON_ON_BOTTOM:
                containerLayout.removeView(secureIcons);
                containerLayout.removeView(space);
                containerLayout.removeView(attachButtton);
                containerLayout.addView(secureIcons);
                containerLayout.addView(space);
                containerLayout.addView(attachButtton);
                break;
            case ICONS_UNDER_FIELDS_BUTTON_UNDER_ICONS:
                containerLayout.removeView(secureIcons);
                containerLayout.removeView(space);
                containerLayout.removeView(attachButtton);
                containerLayout.addView(secureIcons);
                containerLayout.addView(attachButtton);
                break;
            case BUTTON_UNDER_FIELDS_ICONS_UNDER_BOTTOM:
                containerLayout.removeView(space);
                break;
        }
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
        boolean isUsingCustomKeyboard = ((AttachCardFormActivity) getActivity()).shouldUseCustomKeyboard();
        if (customKeyboard != null && isUsingCustomKeyboard) {
            return customKeyboard.hide();
        } else {
            return false;
        }
    }
}
