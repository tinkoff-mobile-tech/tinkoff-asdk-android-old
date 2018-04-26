package ru.tinkoff.acquiring.sdk;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Vitaliy Markus
 */
public class MoneyUtils {

    public static final Locale RUS_LOCALE = new Locale("ru", "RU");

    public static final char DEFAULT_MONEY_DECIMAL_SEPARATOR = ',';
    public static final char DEFAULT_MONEY_GROUPING_SEPARATOR = '\u00a0';

    public static final DecimalFormat MONEY_FORMAT;
    public static final DecimalFormat MONEY_FORMAT_PRECISE;

    private static final char POINT_SEPARATOR = '.';
    private static final String MONEY_FRACTIONAL_PART = ".00";
    private static final String DEFAULT_NORMALIZED = "0.00";

    static {
        final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(RUS_LOCALE);
        decimalFormatSymbols.setDecimalSeparator(DEFAULT_MONEY_DECIMAL_SEPARATOR);
        decimalFormatSymbols.setGroupingSeparator(DEFAULT_MONEY_GROUPING_SEPARATOR);

        MONEY_FORMAT = new DecimalFormat("#,##0.##", decimalFormatSymbols);
        MONEY_FORMAT_PRECISE = new DecimalFormat("#,##0.####", decimalFormatSymbols);
    }

    private MoneyUtils() {
        // Utility classes should not have a public or default constructor.
    }

    public static String replaceArtifacts(String string) {
        return string.replace(String.valueOf(DEFAULT_MONEY_DECIMAL_SEPARATOR), ".").replace(String.valueOf(DEFAULT_MONEY_GROUPING_SEPARATOR), "");
    }

    public static String format(String s) {
        String integral;
        String fraction = "";
        int commaIndex = s.indexOf(MoneyUtils.DEFAULT_MONEY_DECIMAL_SEPARATOR);
        if (commaIndex != -1) {
            integral = s.substring(0, commaIndex);
            fraction = s.substring(commaIndex, s.length());
        } else {
            integral = s;
        }
        if (integral.length() == 0) {
            return fraction;
        } else {
            String formatString = formatMoney(new BigDecimal(replaceArtifacts(integral)));
            return String.format("%s%s", formatString, fraction);
        }
    }

    public static String formatMoney(BigDecimal amount) {
        return MONEY_FORMAT.format(amount);
    }

    public static String normalize(String rawMoney) {
        String normalized;
        if (TextUtils.isEmpty(rawMoney)) {
            normalized = DEFAULT_NORMALIZED;
        } else {
            normalized = MoneyUtils.replaceArtifacts(rawMoney);
            if (!normalized.contains(Character.toString(POINT_SEPARATOR))) {
                normalized += MONEY_FRACTIONAL_PART;
            } else {
                if (normalized.charAt(0) == POINT_SEPARATOR) {
                    normalized = "0" + normalized;
                }
            }
        }
        return normalized;
    }

    public static class MoneyWatcher implements TextWatcher {

        private static final int DEFAULT_LIMIT = 7;
        private static final String FORMAT_PATTERN = "^((\\d%s?){1,%d})?(%s\\d{0,2})?$";

        protected final EditText editText;

        private Pattern pattern;
        private String beforeEditing = "";
        private boolean selfEdit;

        public MoneyWatcher(EditText editText) {
            this.editText = editText;
            setLengthLimit(DEFAULT_LIMIT);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (selfEdit) {
                return;
            }
            beforeEditing = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }


        @Override
        public void afterTextChanged(Editable editable) {
            if (selfEdit) {
                return;
            }

            String resultString = MoneyUtils.replaceArtifacts(editable.toString());
            resultString = resultString.replace('.', MoneyUtils.DEFAULT_MONEY_DECIMAL_SEPARATOR);
            if (!TextUtils.isEmpty(resultString)) {
                boolean isValidCharacters = pattern.matcher(resultString).matches();
                if (!isValidCharacters) {
                    resultString = beforeEditing;
                } else {
                    resultString = MoneyUtils.format(resultString);
                }
            }
            selfEdit = true;
            editable.replace(0, editable.length(), resultString, 0, resultString.length());
            selfEdit = false;
        }

        /**
         * Sets length limit and updates regex pattern for validation.
         * 9 is suggested for RUB, 7 is suggested for other currencies.
         *
         * @param lengthLimit the length limit.
         */
        public void setLengthLimit(int lengthLimit) {
            String patternValue = String.format(Locale.getDefault(), FORMAT_PATTERN, MoneyUtils.DEFAULT_MONEY_GROUPING_SEPARATOR, lengthLimit, MoneyUtils.DEFAULT_MONEY_DECIMAL_SEPARATOR);
            pattern = Pattern.compile(patternValue);
        }
    }
}
