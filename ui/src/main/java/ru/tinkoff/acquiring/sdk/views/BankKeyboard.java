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
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import ru.tinkoff.acquiring.sdk.R;

/**
 * @author Mikhail Artemyev
 */
public class BankKeyboard extends FrameLayout implements
        View.OnClickListener,
        View.OnLongClickListener,
        View.OnFocusChangeListener {

    public static final int ASCII_CODE_OF_ZERO = 48;
    public static final int KEYBOARD_SHOW_DELAY_MILLIS = 500;
    public static final int KEYBOARD_ANIMATION_MILLIS = 50;
    public static final int SYSTEM_KEYBOARD_HIDE_DELAY_MILLIS = 100;
    public static final int CUSTOM_KEYBOARD_HIDE_DELAY_MILLIS = 50;

    private boolean shouldHide;

    private Runnable hidePerformer = new Runnable() {
        @Override
        public void run() {
            if (shouldHide) {
                shouldHide = false;
                setEnabled(false);
                createVisibilityAnimator(false).start();
            }
        }
    };

    public BankKeyboard(Context context) {
        super(context);
        init();
    }

    public BankKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BankKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BankKeyboard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setEnabled(false);

        LayoutInflater.from(getContext())
                .inflate(R.layout.acq_widget_keyboard, this, true);

        final ViewGroup keyboardLayout = (ViewGroup) findViewById(R.id.acq_gl_keys);
        for (int index = 0; index < keyboardLayout.getChildCount(); index++) {
            final View child = keyboardLayout.getChildAt(index);
            if (child instanceof KeyView) {
                child.setOnClickListener(this);
            }
        }
    }

    public void attachToView(final View view) {
        if (view instanceof EditText) {
            registerEditText((EditText) view);
        } else if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;

            for (int index = 0; index < viewGroup.getChildCount(); index++) {
                final View child = viewGroup.getChildAt(index);
                attachToView(child);
            }
        }

    }

    public void showFor(final EditText v) {
        shouldHide = false;
        hideSystemKeyboardFor(v);
        if (isEnabled()) {
            return;
        }

        setEnabled(true);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                createVisibilityAnimator(true).start();
            }
        }, KEYBOARD_SHOW_DELAY_MILLIS);
    }

    public boolean hide() {
        if (isEnabled() && !shouldHide) {
            shouldHide = true;
            postDelayed(hidePerformer, CUSTOM_KEYBOARD_HIDE_DELAY_MILLIS);
            return true;
        }

        return false;
    }

    private void registerEditText(final EditText edittext) {
        edittext.setOnFocusChangeListener(this);
        edittext.setOnLongClickListener(this);
        edittext.setTextIsSelectable(false);
        edittext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFor((EditText) v);
            }
        });

        if (edittext.hasFocus()) {
            showFor(edittext);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) showFor((EditText) v);
        else hide();
    }

    @Override
    public boolean onLongClick(View v) {
        showFor((EditText) v);
        return true;
    }

    @Override
    public void onClick(final View view) {
        if (!(view instanceof KeyView) || !(getContext() instanceof Activity)) {
            return;
        }

        final View focusCurrent = ((Activity) getContext()).getWindow().getCurrentFocus();
        if (focusCurrent == null || !(focusCurrent instanceof EditText)) {
            return;
        }

        final EditText edittext = (EditText) focusCurrent;
        final KeyView key = (KeyView) view;

        processKeyPress(key, edittext);
    }

    private void processKeyPress(KeyView key, EditText edittext) {
        final Editable editable = edittext.getText();
        final int start = edittext.getSelectionStart();

        // delete the selection, if chars are selected:
        final int end = edittext.getSelectionEnd();
        if (end > start) {
            editable.delete(start, end);
        }
        final int keyCode = key.getKeyCode();

        if (keyCode <= 9) {
            // number pressed
            final int charCode = keyCode + ASCII_CODE_OF_ZERO;
            editable.insert(start, String.valueOf((char) charCode));
        } else if (start > 0) {
            // delete pressed and cursor not on start
            editable.delete(start - 1, start);
        }
    }

    private void hideSystemKeyboardFor(final View v) {
        if (v == null || !v.hasFocus()) {
            return;
        }

        final Runnable hideKeyboardTask = new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        };

        hideKeyboardTask.run();

        for (int delay = 0; delay <= SYSTEM_KEYBOARD_HIDE_DELAY_MILLIS; delay += 10) {
            v.postDelayed(hideKeyboardTask, delay);
        }
    }

    private Animator createVisibilityAnimator(final boolean show) {
        final Point screenSize = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(screenSize);

        float startY;
        float endY;
        if (show) {
            startY = getHeight();
            endY = 0F;
        } else {
            startY = 0F;
            endY = getHeight();
        }

        final ObjectAnimator animator = ObjectAnimator.ofFloat(
                BankKeyboard.this,
                View.TRANSLATION_Y,
                startY,
                endY
        );

        animator.setDuration(KEYBOARD_ANIMATION_MILLIS);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (show) {
                    setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        return animator;
    }

}
