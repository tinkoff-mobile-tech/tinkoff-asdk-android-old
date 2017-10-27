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

package ru.tinkoff.acquiring.sdk.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;

import ru.tinkoff.acquiring.sdk.R;
import ru.tinkoff.acquiring.sdk.utils.CardValidator;

/**
 * @author a.shishkin1
 */
public class EditCardView extends ViewGroup {

    private static final int FLAG_CARD_SYSTEM_LOGO = 1;
    private static final int FLAG_FULL_CARD_NUMBER = 1 << 1;
    private static final int FLAG_SCAN_CARD_BUTTON = 1 << 2;
    private static final int FLAG_CHANGE_MODE_BUTTON = 1 << 3;
    private static final int FLAG_ONLY_NUMBER_STATE = 1 << 4;
    private static final int FLAG_SAVED_CARD_STATE = 1 << 5;
    private static final int FLAG_RECURRENT_MODE = 1 << 6;

    private static final int MIN_CARD_NUMBER_LENGTH = 4;

    private Runnable update;
    private int flags;

    private static final String DATE_MASK = "\u2022\u2022/\u2022\u2022";

    private int textColor;

    private String cardHint = null;

    private CardValidator cardValidator;

    private CardNumberEditText etCardNumber;
    private EditText etDate;
    private EditText etCvc;
    private Bitmap cardSystemLogo;
    private Paint cardSystemLogoPaint;
    private Paint paint;

    private int cardLogoMargin;
    private int cardTextMargin;

    private SimpleButton btnChangeMode;
    private SimpleButton btnScanCard;

    private float cardSystemLogoAnimationFactor = 1f;

    private CardFormatter cardFormatter;

    private Animator pendingAnimation;

    private boolean buttonsAvailable = true;

    private boolean forbidTextWatcher = false;

    private int scanResId = 0;
    private int nextResId = 0;


    public EditCardView(Context context) {
        super(context);
        init(null);
    }

    public EditCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EditCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public String getCardNumber() {
        return cardFormatter.getNormalizedNumber(etCardNumber.getText().toString(), " ");
    }

    public String getCvc() {
        return etCvc.getText().toString();
    }

    public String getExpireDate() {
        return etDate.getText().toString();
    }

    public boolean isFilledAndCorrect() {
        if (check(FLAG_RECURRENT_MODE)) {
            return true;
        }
        if (check(FLAG_SAVED_CARD_STATE)) {
            return cardValidator.validateSecurityCode(etCvc.getText().toString());
        }

        boolean cardNumberReady = cardValidator.validateNumber(getCardNumber());
        if (!cardNumberReady)
            return false;
        return check(FLAG_ONLY_NUMBER_STATE) ||
                (cardValidator.validateExpirationDate(etDate.getText().toString()) && cardValidator.validateSecurityCode(etCvc.getText().toString()));
    }

    private void init(AttributeSet attributeSet) {

        setAddStatesFromChildren(true);

        btnChangeMode = new SimpleButton(null);
        btnScanCard = new SimpleButton(null);

        flags = 0;
        Context context = getContext();
        cardValidator = new CardValidator();

        if (attributeSet == null) {
            etCardNumber = new CardNumberEditText(context);
            etDate = new EditText(context);
            etCvc = new EditText(context);
        } else {
            etCardNumber = new CardNumberEditText(context, attributeSet);
            etDate = new EditText(context, attributeSet);
            etCvc = new EditText(context, attributeSet);
        }

        textColor = etCardNumber.getCurrentTextColor();

        applyBehaviour(etCardNumber, etCvc, etDate);

        etCvc.setInputType(etCvc.getInputType() | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        update = new Runnable() {
            @Override
            public void run() {
                String number = etCardNumber.getText().toString();
                boolean isCorrect = cardValidator.validateNumber(cardFormatter.getNormalizedNumber(number, " ")) || check(FLAG_SAVED_CARD_STATE) || check(FLAG_RECURRENT_MODE);
                if (!isCorrect && check(FLAG_CHANGE_MODE_BUTTON)) {
                    hideChangeModeButton();
                }
                if (isCorrect && !cardFormatter.isLimited() && !check(FLAG_SAVED_CARD_STATE) && !check(FLAG_RECURRENT_MODE)) {
                    showChangeModeButton();
                }
                etCardNumber.setTextColor(cardFormatter.isNeedToCheck(etCardNumber.length()) && !isCorrect ? Color.RED : textColor);
                etDate.setTextColor(etDate.length() == 5 && !cardValidator.validateExpirationDate(etDate.getText().toString()) && !check(FLAG_SAVED_CARD_STATE) && !check(FLAG_RECURRENT_MODE) ? Color.RED : textColor);
                etCvc.setTextColor(etCvc.length() == 3 && !cardValidator.validateSecurityCode(etCvc.getText().toString()) ? Color.RED : textColor);
                actions.onUpdate(EditCardView.this);
            }
        };

        etDate.setGravity(Gravity.CENTER);
        etCvc.setGravity(Gravity.CENTER);
        etCardNumber.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

        addView(etCardNumber);
        addView(etCvc);
        addView(etDate);
        etCardNumber.addTextChangedListener(new TextWatcher() {

            int[] sel = new int[2];

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (forbidTextWatcher) {
                    return;
                }

                String number = etCardNumber.getText().toString();

                if (!TextUtils.isEmpty(number)) {
                    etCardNumber.removeTextChangedListener(this);
                    char firstChar = number.charAt(0);
                    if (firstChar == '2' || firstChar == '4' || firstChar == '5') {
                        cardFormatter.setType(CardFormatter.DEFAULT); // master card and visa xxxx xxxx xxxx xxxx
                    } else if (firstChar == '6') {
                        cardFormatter.setType(CardFormatter.MAESTRO); // maestro xxxxxxxx xxxx...x
                    } else {
                        cardFormatter.setType(CardFormatter.UNKNOWN);
                    }

                    etCardNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(cardFormatter.getMaxLength())});

                    String formatted = cardFormatter.format(number, " ");
                    populateCardNumber(formatted, before > count);
                    etCardNumber.addTextChangedListener(this);
                    String normalizedNumber = cardFormatter.getNormalizedNumber(number, " ");
                    if (pendingAnimation == null) {
                        boolean isFullCardMode = check(FLAG_FULL_CARD_NUMBER);
                        boolean isLimited = cardFormatter.isLimited();

                        if (isFullCardMode) {
                            boolean isInReadyDataMode = check(FLAG_SAVED_CARD_STATE);
                            if (((isLimited && cardValidator.validateNumber(normalizedNumber)) || isInReadyDataMode) && !check(FLAG_ONLY_NUMBER_STATE)) {
                                showCvcAndDate();
                            }

                            if (check(FLAG_SCAN_CARD_BUTTON) && normalizedNumber.length() > 15) {
                                hideScanButton();
                            }

                            if (!check(FLAG_SCAN_CARD_BUTTON) && normalizedNumber.length() <= 15) {
                                showScanButton();
                            }
                        }
                    }

                }

                boolean noCardLogoCondition = number == null || number.length() < MIN_CARD_NUMBER_LENGTH;

                if (noCardLogoCondition && check(FLAG_CARD_SYSTEM_LOGO)) {
                    hideCardSystemLogo();
                    return;
                }

                if (!noCardLogoCondition && !check(FLAG_CARD_SYSTEM_LOGO)) {
                    showCardSystemLogo();
                    return;
                }
                update.run();
            }

            private void populateCardNumber(String formattedCardNumber, boolean delete) {
                sel[0] = etCardNumber.getSelectionStart();
                sel[1] = etCardNumber.getSelectionEnd();
                int correction = 0;
                String text = etCardNumber.getText().toString();

                correction += countMatchesBeforeIndex(formattedCardNumber, " ", sel[0]) - countMatchesBeforeIndex(text, " ", sel[0]);

                etCardNumber.setText(formattedCardNumber);

                int pos = Math.max(sel[0] + correction, 0);

                etCardNumber.setSelection(Math.min(formattedCardNumber.length(), pos));
            }

            public int countMatchesBeforeIndex(String str, String sub, int border) {
                if (TextUtils.isEmpty(str) || TextUtils.isEmpty(sub)) {
                    return 0;
                }
                int count = 0;
                int idx = 0;
                while ((idx = str.indexOf(sub, idx)) != -1 && idx < border) {
                    count++;
                    idx += sub.length();
                }
                return count;
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etCardNumber.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !check(FLAG_FULL_CARD_NUMBER))
                    return true;
                if (event.getAction() == MotionEvent.ACTION_UP && pendingAnimation == null && !check(FLAG_FULL_CARD_NUMBER) && !check(FLAG_SAVED_CARD_STATE)) {
                    hideCvcAndDate();
                }
                return false;
            }
        });
        etCardNumber.setCustomOnFocusChangedListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && pendingAnimation == null && !check(FLAG_FULL_CARD_NUMBER) && !check(FLAG_SAVED_CARD_STATE)) {
                    hideCvcAndDate();
                }
            }
        });
        etDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence string, int start, int before, int count) {
                if (forbidTextWatcher) {
                    return;
                }

                if (check(FLAG_SAVED_CARD_STATE)) {
                    return;
                }
                etDate.removeTextChangedListener(this);

                String result = string.toString().replace("/", "");
                if (count != 0 && string.length() > 1) {      // set delimiter before next sign
                    result = result.substring(0, 2) + "/" + result.substring(2);
                    etDate.setText(result);
                    etDate.setSelection((start + count + 1 > result.length()) ? result.length() : (start + count + 1));
                } else {
                    if (start == 2) {
                        etDate.setText(result.substring(0, 1));
                        etDate.setSelection(etDate.length());
                    } else {
                        etDate.setSelection(start + count);
                    }
                }
                update.run();
                if (cardValidator.validateExpirationDate(etDate.getText().toString())) {
                    dispatchFocus();
                }
                etDate.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etCvc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (forbidTextWatcher) {
                    return;
                }
                update.run();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        showScanButton();
        setMode(true);
        etCardNumber.requestFocus();
        etCardNumber.setHint("");
        etDate.setHint("");
        etCvc.setHint("");

        etDate.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        etCvc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        cardSystemLogoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardFormatter = new CardFormatter();

        TypedArray a = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.EditCardView, 0, 0);
        cardLogoMargin = a.getDimensionPixelSize(R.styleable.EditCardView_cardLogoMargin, getResources().getDimensionPixelSize(R.dimen.acq_default_card_logo_margin));
        cardTextMargin = a.getDimensionPixelSize(R.styleable.EditCardView_cardTextMargin, getResources().getDimensionPixelSize(R.dimen.acq_default_card_text_margin));
        etCardNumber.setHint(a.getString(R.styleable.EditCardView_numberHint));
        etDate.setHint(a.getString(R.styleable.EditCardView_dateHint));
        etCvc.setHint(a.getString(R.styleable.EditCardView_cvcHint));
        setBtnScanIcon(a.getResourceId(R.styleable.EditCardView_scanIcon, R.drawable.acq_scan_grey));
        setChangeModeIcon(a.getResourceId(R.styleable.EditCardView_changeModeIcon, R.drawable.acq_next_grey));
        a.recycle();
    }

    @Override
    public void setEnabled(boolean enabled) {
        setAddStatesFromChildren(enabled);
        super.setEnabled(enabled);
        etCardNumber.setEnabled(enabled);
        etDate.setEnabled(enabled);
        etCvc.setEnabled(enabled);

        if (enabled) {
            showScanButton();
        } else {
            hideScanButton();
            hideChangeModeButton();
        }
    }

    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        setFocusableInTouchMode(focusable);
        etCardNumber.setFocusable(focusable);
        etCardNumber.setFocusableInTouchMode(focusable);
        etDate.setFocusable(focusable);
        etDate.setFocusableInTouchMode(focusable);
        etCvc.setFocusable(focusable);
        etCvc.setFocusableInTouchMode(focusable);
    }

    @Override
    public void clearFocus() {
        super.clearFocus();
        etCardNumber.clearFocus();
        etDate.clearFocus();
        etCvc.clearFocus();
    }

    public void disableCopyPaste() {
        DisableCopyPasteActionModeCallback callback = new DisableCopyPasteActionModeCallback();
        etCardNumber.setCustomSelectionActionModeCallback(callback);
        etDate.setCustomSelectionActionModeCallback(callback);
        etCvc.setCustomSelectionActionModeCallback(callback);
    }

    public void dispatchFocus() {
        if (check(FLAG_RECURRENT_MODE)) {
            return;
        }
        if (check(FLAG_SAVED_CARD_STATE)) {
            activate(etCvc);
        } else {
            if (check(FLAG_FULL_CARD_NUMBER)) {
                activate(etCardNumber);
            } else if (etDate.length() == 5) {
                activate(etCvc);
            } else {
                activate(etDate);
            }
        }
    }

    protected EditText onCreateField(Context context, AttributeSet attributeSet) {
        return (attributeSet == null) ? new EditText(context) : new EditText(context, attributeSet);
    }

    public void setFullCardNumberModeEnable(boolean enable) {
        if (enable) {
            flags &= ~FLAG_ONLY_NUMBER_STATE;
        } else {
            flags |= FLAG_ONLY_NUMBER_STATE;
        }
        requestLayout();
        invalidate();
    }


    public void setSavedCardState(boolean savedCardState) {

        if (check(FLAG_SAVED_CARD_STATE) == savedCardState) {
            normalizeMode();
            return;
        }

        if (savedCardState) {
            setMode(false);
            hideChangeModeButton();
            hideScanButton();
            flags |= FLAG_SAVED_CARD_STATE;
            etDate.setText(DATE_MASK);
            etCvc.setText("");
            etDate.setEnabled(false);
            etCardNumber.setEnabled(false);
        } else {
            setMode(true);
            showScanButton();
            flags &= ~FLAG_SAVED_CARD_STATE;
            etDate.setEnabled(true);
            etCardNumber.setEnabled(true);
            etCardNumber.setMode(CardNumberEditText.FULL_MODE);
            etCardNumber.setText("");
            etCvc.setText("");
            etDate.setText("");
        }

        requestLayout();
    }

    public void setRecurrentPaymentMode(boolean recurrentMode) {
        if (recurrentMode) {
            setMode(false);
            hideChangeModeButton();
            hideScanButton();
            flags |= FLAG_RECURRENT_MODE;
            etCardNumber.setEnabled(false);
            etDate.setText(DATE_MASK);
            etDate.setEnabled(false);
            etCvc.setText("");
            etCvc.setEnabled(false);
            etCvc.setHint("");
            requestLayout();
        } else {
            flags &= ~FLAG_RECURRENT_MODE;
            setSavedCardState(true);
        }
    }

    @SuppressWarnings("ResourceType")
    protected void applyBehaviour(EditText... fields) {
        int id = 1;
        for (EditText et : fields) {
            et.setId(id); // dirty avoid id duplication
            id++;
            et.setSingleLine(true);
            et.setPadding(0, 0, 0, 0);
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                et.setBackgroundDrawable(null);
            } else {
                et.setBackground(null);
            }
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }


    public void setHints(String cardHint, String dateHint, String cvcHint) {
        etCardNumber.setHint(cardHint);
        etDate.setHint(dateHint);
        etCvc.setHint(cvcHint);
    }

    public void setCardNumber(String number) {
        if (number == null || number.length() == 0) {
            flags &= ~FLAG_CARD_SYSTEM_LOGO;
        } else {
            flags |= FLAG_CARD_SYSTEM_LOGO;
            cardSystemLogo = cardSystemIconsHolder == null ? null : cardSystemIconsHolder.getCardSystemBitmap(number);
        }
        String old = getCardNumber();
        if (old != null && old.length() > 0 && !old.equals(number)) {
            etCvc.setText("");
        }

        etCardNumber.setText(number);
        if (check(FLAG_SAVED_CARD_STATE) || check(FLAG_RECURRENT_MODE)) {
            etCardNumber.setMode(CardNumberEditText.SHORT_MODE);
        }
        requestLayout();
        dispatchFocus();
    }

    public void setExpireDate(String date) {
        etDate.setText(date);
        dispatchFocus();
    }

    public void clear() {
        if (pendingAnimation != null) {
            clearPendingAnimations();
        }
        if (!check(FLAG_SCAN_CARD_BUTTON)) {
            flags |= FLAG_SCAN_CARD_BUTTON;
        }
        setMode(true);
        etCardNumber.setMode(CardNumberEditText.FULL_MODE);
        etCardNumber.setText("");
        etDate.setText("");
        etCvc.setText("");
        dispatchFocus();
    }

    private void clearPendingAnimations() {
        pendingAnimation.removeAllListeners();
        pendingAnimation.end();
        pendingAnimation.cancel();
        pendingAnimation = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (cardSystemIconsHolder != null && cardSystemLogo == null && check(FLAG_CARD_SYSTEM_LOGO)) {
            cardSystemLogo = cardSystemIconsHolder.getCardSystemBitmap(etCardNumber.getText().toString());
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        boolean isFullCardNumberMode = check(FLAG_FULL_CARD_NUMBER);

        int logoWidth = check(FLAG_CARD_SYSTEM_LOGO) ? calculateCardLogoWidth() : 0;

        int additionalRightSpace = 0;

        if (check(FLAG_SCAN_CARD_BUTTON)) {
            additionalRightSpace += calculateScanButtonWidth();
        }
        if (check(FLAG_CHANGE_MODE_BUTTON)) {
            additionalRightSpace += calculateChangeModeWidth();
        }

        int accessWidth = widthSize - logoWidth - additionalRightSpace - (check(FLAG_CARD_SYSTEM_LOGO) ? cardTextMargin : 0) - (getPaddingRight() + getPaddingLeft());

        int contentsWidth = accessWidth / 3;

        int contentWidthSpec = MeasureSpec.makeMeasureSpec(contentsWidth, MeasureSpec.EXACTLY);
        int contentHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        etCardNumber.measure(isFullCardNumberMode ? MeasureSpec.makeMeasureSpec(accessWidth, MeasureSpec.EXACTLY) : contentWidthSpec, contentHeightSpec);
        etDate.measure(contentWidthSpec, contentHeightSpec);
        etCvc.measure(contentWidthSpec, contentHeightSpec);
        int btnsHeight = Math.max(calculateChangeModeHeight(), calculateScanButtonHeight());
        int iconsHeight = Math.max(cardSystemLogo == null ? 0 : cardSystemLogo.getHeight(), btnsHeight);
        int fieldHeight = Math.max(etCardNumber.getMeasuredHeight(), Math.max(etCvc.getMeasuredHeight(), etDate.getMeasuredHeight()));
        int height = Math.max(iconsHeight, fieldHeight);
        height += (getPaddingTop() + getPaddingBottom());
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(heightSize, height);
        }

        setMeasuredDimension(widthSize, height);

    }

    private int calculateCardLogoWidth() {
        return cardSystemLogo == null ? 0 : (int) (cardSystemLogo.getWidth() * cardSystemLogoAnimationFactor);
    }

    private int calculateScanButtonWidth() {
        return btnScanCard.getWidth();
    }

    private int calculateChangeModeWidth() {
        return btnChangeMode.getWidth();
    }

    private int calculateScanButtonHeight() {
        return btnScanCard.getHeight();
    }

    private int calculateChangeModeHeight() {
        return btnChangeMode.getHeight();
    }

    private boolean check(int flags) {
        return (this.flags & flags) == flags;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int logoWidth = check(FLAG_CARD_SYSTEM_LOGO) ? calculateCardLogoWidth() : 0;
        int t;
        int l = getPaddingLeft();
        int startLabels = logoWidth + l + (check(FLAG_CARD_SYSTEM_LOGO) ? cardTextMargin : 0);
        int w = getWidth() - getPaddingRight() - getPaddingLeft();
        int hh = (getHeight() - getPaddingTop() - getPaddingBottom()) >> 1;

        int additionalRightSpace = 0;
        if (check(FLAG_SCAN_CARD_BUTTON)) {
            additionalRightSpace += calculateScanButtonWidth();
            btnScanCard.layoutIn(w - additionalRightSpace + (btnScanCard.getWidth() >> 1) + getPaddingLeft(), hh + getPaddingTop());
        }
        if (check(FLAG_CHANGE_MODE_BUTTON)) {
            additionalRightSpace += calculateChangeModeWidth();
            btnChangeMode.layoutIn(w - additionalRightSpace + (btnChangeMode.getWidth() >> 1) + getPaddingLeft(), hh + getPaddingTop());
        }
        int startLabel;
        int endLabel;
        t = (getHeight() - etCardNumber.getMeasuredHeight()) >> 1;
        startLabel = startLabels;
        endLabel = startLabel + etCardNumber.getMeasuredWidth();
        etCardNumber.layout(startLabel, t, endLabel, t + etCardNumber.getMeasuredHeight());

        t = (getHeight() - etDate.getMeasuredHeight()) >> 1;
        startLabel = endLabel;
        endLabel = startLabel + etDate.getMeasuredWidth();
        etDate.layout(startLabel, t, endLabel, t + etDate.getMeasuredHeight());
        t = (getHeight() - etCvc.getMeasuredHeight()) >> 1;
        startLabel = endLabel;
        endLabel = startLabel + etCvc.getMeasuredWidth();
        etCvc.layout(startLabel, t, endLabel, t + etCvc.getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (check(FLAG_CARD_SYSTEM_LOGO) && cardSystemLogo != null) {
            int yOffset = (getHeight() - cardSystemLogo.getHeight()) >> 1;
            canvas.drawBitmap(cardSystemLogo, cardLogoMargin, yOffset, cardSystemLogoPaint);
        }
        if (check(FLAG_CHANGE_MODE_BUTTON)) {
            btnChangeMode.drawWithPaint(canvas, paint);
        }
        if (check(FLAG_SCAN_CARD_BUTTON)) {
            btnScanCard.drawWithPaint(canvas, paint);
        }

        super.dispatchDraw(canvas);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (check(FLAG_CHANGE_MODE_BUTTON) && btnChangeMode.handleAction(event) && buttonsAvailable) {
                showCvcAndDate();
                return true;
            }
            if (check(FLAG_SCAN_CARD_BUTTON) && btnScanCard.handleAction(event) && buttonsAvailable) {
                actions.onPressScanCard(this);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }


    public void setMode(boolean isFullNumber) {
        if (isFullNumber) {
            flags |= FLAG_FULL_CARD_NUMBER;
        } else {
            flags &= ~FLAG_FULL_CARD_NUMBER;
        }
        normalizeMode();
    }

    private void normalizeMode() {
        if (check(FLAG_FULL_CARD_NUMBER)) {
            etCvc.setVisibility(GONE);
            etDate.setVisibility(GONE);
        } else {
            etCvc.setVisibility(VISIBLE);
            etDate.setVisibility(VISIBLE);
        }
        requestLayout();
    }


    private void showCardSystemLogo() {
        cardSystemLogo = cardSystemIconsHolder.getCardSystemBitmap(etCardNumber.getText().toString());
        flags |= FLAG_CARD_SYSTEM_LOGO;
        cardSystemLogoPaint.setAlpha(0);

        ObjectAnimator animatorAlpha = ObjectAnimator.ofInt(cardSystemLogoPaint, "alpha", 0, 255);
        animatorAlpha.setDuration(150);
        animatorAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        ObjectAnimator animatorEditField = ObjectAnimator.ofFloat(this, "cardSystemLogoAnimationFactor", 0f, 1f);
        animatorEditField.setDuration(150);
        animatorEditField.setInterpolator(new OvershootInterpolator());
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animatorEditField, animatorAlpha);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                requestLayout();
                invalidate();
            }
        });
        set.start();
    }

    private void hideCardSystemLogo() {
        ObjectAnimator animatorAlpha = ObjectAnimator.ofInt(cardSystemLogoPaint, "alpha", 255, 0);
        animatorAlpha.setDuration(150);
        animatorAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        ObjectAnimator animatorEditField = ObjectAnimator.ofFloat(this, "cardSystemLogoAnimationFactor", 1f, 0f);
        animatorEditField.setDuration(150);
        animatorEditField.setInterpolator(new OvershootInterpolator());
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animatorAlpha, animatorEditField);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                flags &= ~FLAG_CARD_SYSTEM_LOGO;
                requestLayout();
                invalidate();
            }
        });
        set.start();
    }


    private void showCvcAndDate() {
        hideChangeModeButton();
        final MutableColorSpan span = new MutableColorSpan(etCardNumber.getPaint().getColor());

        etCardNumber.getText().setSpan(span, 0, Math.max(etCardNumber.length() - 4, 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ObjectAnimator animatorText = ObjectAnimator.ofInt(span, "alpha", 255, 0);
        animatorText.setDuration(200);
        animatorText.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (etCardNumber.getMode() == CardNumberEditText.FULL_MODE) {
                    etCardNumber.getText().setSpan(span, 0, Math.max(etCardNumber.length() - 4, 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        });
        ObjectAnimator toLeftAnimator = ObjectAnimator.ofFloat(etCardNumber, "animationFactor", 0f, 1f);
        toLeftAnimator.setStartDelay(140);
        toLeftAnimator.setDuration(210);
        toLeftAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                etCardNumber.setMode(CardNumberEditText.SHORT_MODE);
            }
        });

        etDate.setVisibility(VISIBLE);
        etDate.setAlpha(0f);
        ObjectAnimator animatorDate = ObjectAnimator.ofFloat(etDate, "alpha", 0f, 1f);
        animatorDate.setDuration(200);
        animatorDate.setStartDelay(200);

        etCvc.setVisibility(VISIBLE);
        etCvc.setAlpha(0f);
        ObjectAnimator animatorCvc = ObjectAnimator.ofFloat(etCvc, "alpha", 0f, 1f);
        animatorCvc.setDuration(200);
        animatorCvc.setStartDelay(280);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorText, toLeftAnimator, animatorCvc, animatorDate);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setMode(false);
                pendingAnimation = null;
                dispatchFocus();
            }
        });
        pendingAnimation = set;
        set.start();

    }

    private void hideCvcAndDate() {
        final MutableColorSpan span = new MutableColorSpan(etCardNumber.getPaint().getColor());
        span.setAlpha(0);
        ObjectAnimator animatorText = ObjectAnimator.ofInt(span, "alpha", 0, 255);
        animatorText.setDuration(250);
        animatorText.setInterpolator(new AccelerateInterpolator());
        animatorText.setStartDelay(200);
        animatorText.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (etCardNumber.getMode() == CardNumberEditText.FULL_MODE) {
                    etCardNumber.getText().setSpan(span, 0, Math.max(etCardNumber.length() - 4, 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            }
        });
        ObjectAnimator toRightAnimator = ObjectAnimator.ofFloat(etCardNumber, "animationFactor", 1f, 0f);
        toRightAnimator.setDuration(200);

        ObjectAnimator animatorDate = ObjectAnimator.ofFloat(etDate, "alpha", 1f, 0f);
        animatorDate.setDuration(150);
        animatorDate.setStartDelay(80);
        animatorDate.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                etCardNumber.setMode(CardNumberEditText.FULL_MODE);
                etCardNumber.setSelection(etCardNumber.length());
                etCardNumber.getText().setSpan(span, 0, Math.max(etCardNumber.length() - 4, 0), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                setMode(true);
            }
        });

        ObjectAnimator animatorCvc = ObjectAnimator.ofFloat(etCvc, "alpha", 1f, 0f);
        animatorCvc.setDuration(150);

        AnimatorSet set = new AnimatorSet();
        set.play(animatorCvc).with(animatorDate).before(toRightAnimator).before(animatorText);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                pendingAnimation = null;
                activate(etCardNumber);
                showChangeModeButton();
            }
        });
        pendingAnimation = set;
        set.start();
    }

    private void showChangeModeButton() {
        flags |= FLAG_CHANGE_MODE_BUTTON;
        requestLayout();
    }


    private void hideChangeModeButton() {
        flags &= ~FLAG_CHANGE_MODE_BUTTON;
        requestLayout();
    }

    private void showScanButton() {
        flags |= FLAG_SCAN_CARD_BUTTON;
        requestLayout();
    }


    private void hideScanButton() {
        flags &= ~FLAG_SCAN_CARD_BUTTON;
        requestLayout();
    }

    private void activate(final EditText et) {
        et.requestFocus();
        et.post(new Runnable() {
            @Override
            public void run() {
                et.setSelection(et.length());
            }
        });
    }

    public void setCardHint(String cardHint) {
        this.cardHint = cardHint;
        etCardNumber.setHint(cardHint);
    }

    public void setCardSystemLogoAnimationFactor(float cardSystemLogoAnimationFactor) {
        this.cardSystemLogoAnimationFactor = cardSystemLogoAnimationFactor;
        requestLayout();
        invalidate();
    }

    public float getCardSystemLogoAnimationFactor() {
        return cardSystemLogoAnimationFactor;
    }

    private CardSystemIconsHolder cardSystemIconsHolder;

    public void setCardSystemIconsHolder(CardSystemIconsHolder cardSystemIconsHolder) {
        this.cardSystemIconsHolder = cardSystemIconsHolder;
        requestLayout();
    }

    public void setBtnScanIcon(int resId) {
        scanResId = resId;
        btnScanCard.setBitmap(BitmapFactory.decodeResource(getResources(), resId));
        requestLayout();
    }

    public void setChangeModeIcon(int resId) {
        nextResId = resId;
        btnChangeMode.setBitmap(BitmapFactory.decodeResource(getResources(), resId));
        requestLayout();
    }

    public interface CardSystemIconsHolder {
        Bitmap getCardSystemBitmap(String cardNumber);
    }


    public class CardFormatter {

        public static final int UNKNOWN = 0;
        public static final int DEFAULT = 1;
        public static final int MAESTRO = 2;

        private int type;
        private int maxLength;
        private int[] defaultRangers = new int[]{19};
        private int[] maestroRangers = new int[]{14, 15, 16, 17, 18, 19, 20};
        private int[] unknownRangers = new int[]{Integer.MAX_VALUE - 3};

        public String format(String input, CharSequence delimiter) {
            return doFormat(getNormalizedNumber(input, delimiter), delimiter);
        }

        public void setType(int type) {
            this.type = type;
            maxLength = 0;
            for (int i : getValidationRanges()) {
                if (i > maxLength) {
                    maxLength = i;
                }
            }
        }

        public boolean isLimited() {
            return type == DEFAULT;
        }

        public boolean isNeedToCheck(int digits) {
            int[] ranges = getValidationRanges();
            for (int range : ranges) {
                if (range == digits) {
                    return true;
                }
            }

            return false;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public int[] getValidationRanges() {
            if (type == DEFAULT)
                return defaultRangers;
            if (type == MAESTRO)
                return maestroRangers;
            return unknownRangers;
        }

        private String getNormalizedNumber(String string, CharSequence delimiter) {
            return string.replace(delimiter, "");
        }

        protected String doFormat(String cardNumber, CharSequence delimiter) {
            if (type == DEFAULT) {
                char[] chars = cardNumber.toCharArray();
                StringBuilder cardNumberBuilder = new StringBuilder(cardNumber);

                for (int i = 1, index = 0; i < chars.length; i++) {
                    if (i % 4 == 0) {
                        cardNumberBuilder.insert(i + index, delimiter);
                        index++;
                    }
                }

                return cardNumberBuilder.toString().trim();
            }
            if (type == MAESTRO) {
                int length = cardNumber.length();

                if (length < 8)
                    return cardNumber;

                StringBuilder cardNumberBuilder = new StringBuilder(cardNumber);
                cardNumberBuilder.insert(8, delimiter);
                return cardNumberBuilder.toString().trim();
            }
            if (type == UNKNOWN) {
                return cardNumber;
            }

            return null;
        }
    }

    private Actions actions = NO_ACTIONS;


    private static Actions NO_ACTIONS = new Actions() {

        @Override
        public void onUpdate(EditCardView ecv) {

        }

        @Override
        public void onPressScanCard(EditCardView ecv) {

        }
    };

    public void setActions(Actions actions) {
        if (actions != null) {
            this.actions = actions;
        } else {
            this.actions = NO_ACTIONS;
        }
    }

    public interface Actions {
        void onUpdate(EditCardView editCardView);

        void onPressScanCard(EditCardView editCardView);
    }

    private class SimpleButton {
        private Rect rect;
        private Bitmap bitmap;
        private boolean isVisible;

        public SimpleButton(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.rect = new Rect();
            this.isVisible = true;
            if (bitmap != null) {
                layoutIn(bitmap.getWidth() >> 1, bitmap.getHeight() >> 1);
            }
        }

        private boolean handleAction(MotionEvent ev) {
            int x = (int) ev.getX();
            return x > rect.left && x < rect.right;
        }

        protected void layoutIn(int centerX, int centerY) {
            if (bitmap != null) {
                int hx = bitmap.getWidth() >> 1;
                int hy = bitmap.getHeight() >> 1;
                rect.set(centerX - hx, centerY - hy, centerX + hx, centerY + hy);
            } else {
                rect.set(centerX, centerY, centerX, centerY);
            }

        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            layoutIn(rect.centerX(), rect.centerY());
        }

        protected void drawWithPaint(Canvas canvas, Paint paint) {
            if (bitmap == null || !isVisible)
                return;
            canvas.drawBitmap(bitmap, null, rect, paint);
        }

        public int getWidth() {
            return rect.width();
        }

        public int getHeight() {
            return rect.height();
        }

        public boolean isVisible() {
            return isVisible;
        }

        public void setVisibility(boolean visible) {
            this.isVisible = visible;
        }
    }

    @SuppressLint("AppCompatCustomView")
    public static class CardNumberEditText extends EditText {

        public static final int FULL_MODE = 0;
        public static final int SHORT_MODE = 1;

        private int charsCount = 4;
        private int mode;
        private float animationFactor = 0f;


        private OnFocusChangeListener customOnFocusChangedListener;

        public CardNumberEditText(Context context) {
            super(context);
        }

        public CardNumberEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void setMode(int mode) {
            this.mode = mode;
            setAnimationFactor(mode == FULL_MODE ? 0f : 1f);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            String text = getText().toString();
            int l = text.length();
            Paint p = getPaint();
            float dist = p.measureText(text.substring(0, Math.max(0, l - charsCount)));
            if (mode == FULL_MODE) {
                canvas.save();
                canvas.translate(-dist * animationFactor, 0);
                super.onDraw(canvas);
                canvas.restore();
            } else if (mode == SHORT_MODE) {
                canvas.drawText(text, Math.max(0, l - charsCount), l, 0, getBaseline(), p);
            }
        }

        @Override
        public boolean bringPointIntoView(int offset) {
            return false;
        }

        public void setAnimationFactor(float animationFactor) {
            this.animationFactor = animationFactor;
            invalidate();
        }

        public int getMode() {
            return mode;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (mode == SHORT_MODE)
                return false;
            return super.onTouchEvent(event);
        }

        @Override
        protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            if (customOnFocusChangedListener != null) {
                customOnFocusChangedListener.onFocusChange(this, focused);
            }
        }

        public OnFocusChangeListener getCustomOnFocusChangedListener() {
            return customOnFocusChangedListener;
        }

        public void setCustomOnFocusChangedListener(OnFocusChangeListener customOnFocusChangedListener) {
            this.customOnFocusChangedListener = customOnFocusChangedListener;
        }
    }

    private static class MutableColorSpan extends CharacterStyle implements UpdateAppearance {


        private int color;

        public MutableColorSpan(int color) {
            super();
            this.color = color;
        }


        public void setAlpha(int alpha) {
            color = (color & 0x00FFFFFF) | (alpha << 24);
        }


        @Override
        public void updateDrawState(TextPaint tp) {
            tp.setColor(color);
        }
    }


    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.flags = flags;
        ss.pan = getCardNumber();
        ss.date = getExpireDate();
        ss.scanResId = scanResId;
        ss.nextResId = nextResId;
        ss.animationFactor = etCardNumber.animationFactor;
        ss.cardNumberMode = etCardNumber.mode;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        flags = ss.flags;
        setCardNumber(ss.pan);
        setExpireDate(ss.date);
        setChangeModeIcon(ss.nextResId);
        setBtnScanIcon(ss.scanResId);
        etCardNumber.animationFactor = ss.animationFactor;
        etCardNumber.setMode(ss.cardNumberMode);
        boolean enableFields = !(check(FLAG_SAVED_CARD_STATE) || check(FLAG_RECURRENT_MODE));
        etDate.setEnabled(enableFields);
        etCardNumber.setEnabled(enableFields);
        normalizeMode();
    }

    static class SavedState extends BaseSavedState {
        int flags;
        String pan;
        String date;
        int nextResId;
        int scanResId;
        float animationFactor;
        int cardNumberMode;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            flags = in.readInt();
            pan = in.readString();
            date = in.readString();
            nextResId = in.readInt();
            scanResId = in.readInt();
            animationFactor = in.readFloat();
            cardNumberMode = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.flags);
            out.writeString(pan);
            out.writeString(date);
            out.writeInt(nextResId);
            out.writeInt(scanResId);
            out.writeFloat(animationFactor);
            out.writeInt(cardNumberMode);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    @Override
    public boolean onCheckIsTextEditor() {
        return super.onCheckIsTextEditor();
    }

    private static class DisableCopyPasteActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }
}

