package ru.tinkoff.acquiring.sdk.inflate.pay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.tinkoff.acquiring.sdk.R;
import ru.tinkoff.acquiring.sdk.localization.AsdkLocalization;
import ru.tinkoff.acquiring.sdk.localization.AsdkLocalizations;

import static ru.tinkoff.acquiring.sdk.inflate.pay.PayCellType.PAYMENT_CARD_REQUISITES;
import static ru.tinkoff.acquiring.sdk.inflate.pay.PayCellType.PAY_BUTTON;
import static ru.tinkoff.acquiring.sdk.inflate.pay.PayCellType.SECURE_LOGOS;

/**
 * @author Vitaliy Markus
 */
public class PayCellInflater {

    public static final PayCellType[] DEFAULT_CELL_TYPES = {
            PayCellType.PRODUCT_TITLE,
            PayCellType.PRODUCT_DESCRIPTION,
            PayCellType.AMOUNT,
            PayCellType.PAYMENT_CARD_REQUISITES,
            PayCellType.EMAIL,
            PayCellType.PAY_BUTTON,
            PayCellType.EMPTY_FLEXIBLE_SPACE,
            PayCellType.SECURE_LOGOS
    };

    private final LayoutInflater inflater;
    private final PayCellType[] cellTypes;

    private PayCellInflater(LayoutInflater inflater, PayCellType[] cellTypes) {
        this.inflater = inflater;
        this.cellTypes = cellTypes;
    }

    public static PayCellInflater from(LayoutInflater inflater, PayCellType[] cellTypes) {
        return new PayCellInflater(inflater, cellTypes);
    }

    public static PayCellInflater from(Context context, PayCellType[] cellTypes) {
        return new PayCellInflater(LayoutInflater.from(context), cellTypes);
    }

    public View inflate(ViewGroup container) {
        validate(PAYMENT_CARD_REQUISITES, PAY_BUTTON, SECURE_LOGOS);
        AsdkLocalization localization = AsdkLocalizations.require(container.getContext());
        View root = inflater.inflate(R.layout.acq_fragment_enter_card_base, container, false);
        container = root.findViewById(R.id.ll_container_layout);
        for (PayCellType cellType : cellTypes) {
            switch (cellType) {
                case PRODUCT_TITLE:
                    inflater.inflate(R.layout.acq_cell_product_title, container, true);
                    break;
                case PRODUCT_DESCRIPTION:
                    inflater.inflate(R.layout.acq_cell_product_description, container, true);
                    break;
                case AMOUNT: {
                    View view = inflater.inflate(R.layout.acq_cell_amount, container, true);
                    ((TextView)view.findViewById(R.id.tv_amount_label)).setText(localization.payMoneyAmount);
                    break;
                }
                case PAYMENT_CARD_REQUISITES: {
                    View view = inflater.inflate(R.layout.acq_cell_payment_card_requisites, container, true);
                    ((TextView)view.findViewById(R.id.tv_src_card_choose_btn)).setText(localization.payCardChangeCard);
                    break;
                }
                case EMAIL:
                    inflater.inflate(R.layout.acq_cell_email, container, true);
                    break;
                case PAY_BUTTON:
                    inflater.inflate(R.layout.acq_cell_pay_button, container, true);
                    break;
                case SECURE_LOGOS:
                    inflater.inflate(R.layout.acq_cell_secure_logs, container, true);
                    break;
                case EMPTY_FLEXIBLE_SPACE:
                    inflater.inflate(R.layout.acq_cell_flexible_space, container, true);
                    break;
                case EMPTY_16DP:
                    inflater.inflate(R.layout.acq_cell_empty_16, container, true);
                    break;
                case EMPTY_8DP:
                    inflater.inflate(R.layout.acq_cell_empty_8, container, true);
                    break;
            }
        }
        return root;
    }

    private void validate(PayCellType... types) {
        for (PayCellType type : types) {
            boolean result = containsOnce(type);
            if (!result) {
                throw new IllegalArgumentException("PayCellType: " + type + " must contains only once");
            }
        }

    }

    private boolean containsOnce(PayCellType value) {
        int count = 0;
        for (PayCellType cellType : cellTypes) {
            if (cellType == value) {
                count++;
            }
        }
        return count == 1;
    }
}
