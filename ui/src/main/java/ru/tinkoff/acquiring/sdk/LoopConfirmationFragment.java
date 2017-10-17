package ru.tinkoff.acquiring.sdk;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Vitaliy Markus
 */
public class LoopConfirmationFragment extends Fragment {

    public static final String EXTRA_REQUEST_KEY = "request_key";

    private static final int BUTTON_UNDER_FIELDS_ICONS_ON_BOTTOM = 0;
    private static final int ICONS_ON_BOTTOM_BUTTON_UNDER_ICONS = 1;
    private static final int ICONS_UNDER_FIELDS_BUTTON_ON_BOTTOM = 2;
    private static final int ICONS_UNDER_FIELDS_BUTTON_UNDER_ICONS = 3;
    private static final int BUTTON_UNDER_FIELDS_ICONS_UNDER_BOTTOM = 4;

    private Button checkButton;

    private int buttonAndIconsPositionMode;

    public static LoopConfirmationFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(EXTRA_REQUEST_KEY, requestKey);
        LoopConfirmationFragment fragment = new LoopConfirmationFragment();
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
        View root = inflater.inflate(R.layout.acq_fragment_loop_confirmation, container, false);
        initViews(root);
        resolveButtonAndIconsPosition(root);
        return root;
    }

    private void initViews(View root) {
        final TextView amountView = (TextView) root.findViewById(R.id.et_amount);

        checkButton = (Button) root.findViewById(R.id.btn_check);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttachCardFormActivity activity = (AttachCardFormActivity) getActivity();
                Long amount;
                try {
                    amount = Long.parseLong(amountView.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(activity, R.string.acq_attaching_card_loop_parse_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                activity.showProgressDialog();
                submitRandomAmount(activity.getSdk(), getArguments().getString(EXTRA_REQUEST_KEY), amount);
            }
        });
    }

    private void submitRandomAmount(final AcquiringSdk sdk, final String requestKey, final long amount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cardId = sdk.submitRandomAmount(requestKey, amount);
                    AttachCardFormHandler.INSTANCE.obtainMessage(AttachCardFormHandler.CARD_ID, cardId).sendToTarget();
                    CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.SUCCESS).sendToTarget();
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

    private void resolveButtonAndIconsPosition(View root) {
        LinearLayout containerLayout = (LinearLayout) root.findViewById(R.id.ll_container_layout);
        View space = root.findViewById(R.id.space);
        View secureIcons = root.findViewById(R.id.iv_secure_icons);
        switch (buttonAndIconsPositionMode) {
            case BUTTON_UNDER_FIELDS_ICONS_ON_BOTTOM:
                break;
            case ICONS_ON_BOTTOM_BUTTON_UNDER_ICONS:
                containerLayout.removeView(checkButton);
                containerLayout.addView(checkButton);
                break;
            case ICONS_UNDER_FIELDS_BUTTON_ON_BOTTOM:
                containerLayout.removeView(secureIcons);
                containerLayout.removeView(space);
                containerLayout.removeView(checkButton);
                containerLayout.addView(secureIcons);
                containerLayout.addView(space);
                containerLayout.addView(checkButton);
                break;
            case ICONS_UNDER_FIELDS_BUTTON_UNDER_ICONS:
                containerLayout.removeView(secureIcons);
                containerLayout.removeView(space);
                containerLayout.removeView(checkButton);
                containerLayout.addView(secureIcons);
                containerLayout.addView(checkButton);
                break;
            case BUTTON_UNDER_FIELDS_ICONS_UNDER_BOTTOM:
                containerLayout.removeView(space);
                break;
        }
    }
}
