package ru.tinkoff.acquiring.sdk.inflate.attach;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.tinkoff.acquiring.sdk.R;
import ru.tinkoff.acquiring.sdk.localization.AsdkLocalization;
import ru.tinkoff.acquiring.sdk.localization.AsdkLocalizations;

import static ru.tinkoff.acquiring.sdk.inflate.attach.AttachCellType.ATTACH_BUTTON;
import static ru.tinkoff.acquiring.sdk.inflate.attach.AttachCellType.PAYMENT_CARD_REQUISITES;
import static ru.tinkoff.acquiring.sdk.inflate.attach.AttachCellType.SECURE_LOGOS;

/**
 * @author Vitaliy Markus
 */
public class AttachCellInflater {

    public static final AttachCellType[] DEFAULT_CELL_TYPES = {
            AttachCellType.TITLE,
            AttachCellType.DESCRIPTION,
            AttachCellType.PAYMENT_CARD_REQUISITES,
            AttachCellType.EMAIL,
            AttachCellType.ATTACH_BUTTON,
            AttachCellType.EMPTY_FLEXIBLE_SPACE,
            AttachCellType.SECURE_LOGOS
    };

    private final LayoutInflater inflater;
    private final AttachCellType[] cellTypes;

    private AttachCellInflater(LayoutInflater inflater, AttachCellType[] cellTypes) {
        this.inflater = inflater;
        this.cellTypes = cellTypes;
    }

    public static AttachCellInflater from(LayoutInflater inflater, AttachCellType[] cellTypes) {
        return new AttachCellInflater(inflater, cellTypes);
    }

    public static AttachCellInflater from(Context context, AttachCellType[] cellTypes) {
        return new AttachCellInflater(LayoutInflater.from(context), cellTypes);
    }

    public View inflate(ViewGroup container) {
        validate(PAYMENT_CARD_REQUISITES, ATTACH_BUTTON, SECURE_LOGOS);
        AsdkLocalization localization = AsdkLocalizations.require(container.getContext());
        View root = inflater.inflate(R.layout.acq_fragment_attach_card_base, container, false);
        container = root.findViewById(R.id.ll_container_layout);
        for (AttachCellType cellType : cellTypes) {
            switch (cellType) {
                case TITLE:
                    inflater.inflate(R.layout.acq_cell_product_title, container, true);
                    break;
                case DESCRIPTION:
                    inflater.inflate(R.layout.acq_cell_product_description, container, true);
                    break;
                case PAYMENT_CARD_REQUISITES:
                    inflater.inflate(R.layout.acq_cell_payment_card_requisites_attach, container, true);
                    break;
                case EMAIL: {
                    View view = inflater.inflate(R.layout.acq_cell_email, container, true);
                    ((TextView) view.findViewById(R.id.et_email)).setHint(localization.payEmail);
                    break;
                }
                case ATTACH_BUTTON:
                    inflater.inflate(R.layout.acq_cell_attach_button, container, true);
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

    private void validate(AttachCellType... types) {
        for (AttachCellType type : types) {
            boolean result = containsOnce(type);
            if (!result) {
                throw new IllegalArgumentException("AttachCellType: " + type + " must contains only once");
            }
        }

    }

    private boolean containsOnce(AttachCellType value) {
        int count = 0;
        for (AttachCellType cellType : cellTypes) {
            if (cellType == value) {
                count++;
            }
        }
        return count == 1;
    }
}
