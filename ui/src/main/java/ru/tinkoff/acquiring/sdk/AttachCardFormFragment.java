package ru.tinkoff.acquiring.sdk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.tinkoff.acquiring.sdk.views.EditCardView;

/**
 * @author Vitaliy Markus
 */
public class AttachCardFormFragment extends Fragment {

    private LinearLayout container;
    private TextView titleLabel;
    private TextView descriptionLabel;
    private EditCardView editCardView;
    private EditText emailView;
    private Button attachButtton;
    private ImageView secureIcons;

    public static AttachCardFormFragment newInstance() {
        Bundle args = new Bundle();
        AttachCardFormFragment fragment = new AttachCardFormFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.acq_fragment_attach_card, container, false);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        container = (LinearLayout) root.findViewById(R.id.ll_container_layout);
        titleLabel = (TextView) root.findViewById(R.id.tv_title);
        descriptionLabel = (TextView) root.findViewById(R.id.tv_description);
        editCardView = (EditCardView) root.findViewById(R.id.ecv_card);
        emailView = (EditText) root.findViewById(R.id.et_email);
        attachButtton = (Button) root.findViewById(R.id.btn_attach);
        secureIcons = (ImageView) root.findViewById(R.id.iv_secure_icons);
    }
}
