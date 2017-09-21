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
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import ru.tinkoff.acquiring.sdk.R;

/**
 * @author Mikhail Artemyev
 */
public class KeyView extends View {

    private static final float KEY_SCALE_X = 0.333F;
    private static final float KEY_SCALE_Y = 0.25F;
    private static final float KEY_DEFAULT_TEXT_SIZE_DP = 34;
    private static final int CIRCLE_ANIMATION_DURATION_MILLIS = 200;
    private static final String DEFAULT_FONT_FAMILY = "sans-serif-light";

    private int keyCode;
    private String contentText;
    private Bitmap contentImage;

    private boolean drawingPressAnimation = false;
    private ValueAnimator circleAnimator;

    private float textWidth;

    private Paint contentPaint;

    private PointF circleCenter;
    private float circleRadius;
    private Paint circlePaint;

    private static float dpToPx(float px) {
        return px * Resources.getSystem().getDisplayMetrics().density;
    }

    public KeyView(Context context) {
        super(context);
        init(null);
    }

    public KeyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public KeyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public KeyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(final AttributeSet attrs) {
        setClickable(true);

        contentPaint = new Paint();
        contentPaint.setTextSize(dpToPx(KEY_DEFAULT_TEXT_SIZE_DP));
        contentPaint.setAntiAlias(true);
        contentPaint.setColor(ContextCompat.getColor(getContext(), R.color.acq_colorKeyText));
        contentPaint.setTypeface(Typeface.create(DEFAULT_FONT_FAMILY, Typeface.NORMAL));

        circlePaint = new Paint();
        circlePaint.setColor(ContextCompat.getColor(getContext(), R.color.acq_colorKeyCircle));

        applyAttrs(attrs);
    }

    private void applyAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        final TypedArray a = getContext()
                .getTheme()
                .obtainStyledAttributes(attrs, R.styleable.KeyView, 0, 0);

        try {
            keyCode = a.getInt(R.styleable.KeyView_keyCode, -1);

            final float textSize = a.getDimension(R.styleable.KeyView_keyTextSize, -1.F);
            if (textSize != -1.F) {
                contentPaint.setTextSize(dpToPx(textSize));
            }

            final String fontFamily = a.getString(R.styleable.KeyView_keyTextFontFamily);
            if (fontFamily != null) {
                contentPaint.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
            }

            contentText = a.getString(R.styleable.KeyView_keyText);
            if (contentText != null) {
                textWidth = contentPaint.measureText(contentText);
            }

            final int imageDrawableId = a.getResourceId(R.styleable.KeyView_keyImage, -1);
            if (imageDrawableId != -1) {
                contentImage = BitmapFactory.decodeResource(getResources(), imageDrawableId);
            }

            final int textColor = a.getColor(R.styleable.KeyView_keyTextColor, contentPaint.getColor());
            contentPaint.setColor(textColor);

            final int circleColor = a.getColor(R.styleable.KeyView_keyCircleColor, circlePaint.getColor());
            circlePaint.setColor(circleColor);
        } finally {
            a.recycle();
        }
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public Bitmap getContentImage() {
        return contentImage;
    }

    public void setContentImage(Bitmap contentImage) {
        this.contentImage = contentImage;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        if (drawingPressAnimation) {
            circleAnimator.cancel();
        }

        drawingPressAnimation = true;
        circleCenter = new PointF(event.getX(), event.getY());

        circleAnimator = createCircleAnimator();
        circleAnimator.start();

        return super.onTouchEvent(event);
    }

    private ValueAnimator createCircleAnimator() {
        final float maxDimen = Math.max(getWidth(), getHeight()) * 0.8F;
        final ValueAnimator animator = ValueAnimator.ofFloat(0F, maxDimen);
        animator.setDuration(CIRCLE_ANIMATION_DURATION_MILLIS);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleRadius = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                drawingPressAnimation = false;
                invalidate();
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final float width = MeasureSpec.getSize(widthMeasureSpec) * KEY_SCALE_X;
        final float height = MeasureSpec.getSize(heightMeasureSpec) * KEY_SCALE_Y;

        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (contentText != null) {
            final float x = (canvas.getWidth() / 2) - (textWidth / 2);
            final float y = (canvas.getHeight() / 2) - ((contentPaint.descent() + contentPaint.ascent()) / 2);
            canvas.drawText(contentText, x, y, contentPaint);
        }

        if (contentImage != null) {
            canvas.drawBitmap(contentImage, getWidth() / 2 - contentImage.getWidth() / 2,
                    getHeight() / 2 - contentImage.getHeight() / 2, contentPaint);
        }

        if (drawingPressAnimation) {
            canvas.drawCircle(circleCenter.x, circleCenter.y, circleRadius, circlePaint);
        }
    }
}
