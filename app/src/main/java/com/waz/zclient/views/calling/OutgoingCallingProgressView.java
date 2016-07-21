/**
 * Wire
 * Copyright (C) 2016 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.views.calling;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import com.waz.zclient.R;
import com.waz.zclient.ui.utils.ColorUtils;
import com.waz.zclient.ui.views.properties.PositionAnimatable;

public class OutgoingCallingProgressView extends View implements PositionAnimatable {
    public static final String TAG = OutgoingCallingProgressView.class.getName();
    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final int DEFAULT_DURATION = 2800;

    private int centerX;
    private int centerY;

    private Paint color1;
    private Paint color2;
    private Paint color3;

    private RectF rect1;
    private RectF rect2;

    private boolean isAnimationRunning;
    private float animationPosition;
    private ObjectAnimator objectAnimator;
    private int oneRoundDuration;
    private int innerRadius;
    private int myColor;
    private Paint innerPaint;

    public void setAccentColor(int color) {
        innerPaint.setColor(color);
    }

    @Override
    public void setAnimationPosition(float animationPosition) {
        this.animationPosition = animationPosition;
        invalidate();
    }

    @Override
    public float getAnimationPosition() {
        return animationPosition;
    }

    public void setColor(int color) {
        myColor = color;
        setPaint(getWidth());

        color1.setColor(color);
        color2.setColor(color);
        color3.setColor(color);

        invalidate();
    }

    public OutgoingCallingProgressView(Context context) {
        this(context, null, 0);
    }

    public OutgoingCallingProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutgoingCallingProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        isAnimationRunning = false;
        oneRoundDuration = DEFAULT_DURATION;

        // preparing paints
        color1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        color2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        color3 = new Paint(Paint.ANTI_ALIAS_FLAG);

        color1.setStyle(Paint.Style.FILL);
        color2.setStyle(Paint.Style.FILL);
        color3.setStyle(Paint.Style.FILL);
        setColor(DEFAULT_COLOR);

        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setAccentColor(DEFAULT_COLOR);

        if (attrs == null) {
            setColor(DEFAULT_COLOR);
            innerRadius = 0;
        } else {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                                                                     R.styleable.OutgoingCallingProgressView, 0, 0);
            innerRadius = a.getDimensionPixelSize(R.styleable.OutgoingCallingProgressView_ocpvInnerRadius, 0);
            setColor(a.getColor(R.styleable.OngoingCallingProgressView_ocpvCircleColor, DEFAULT_COLOR));

            a.recycle();
        }

        rect1 = new RectF();
        rect2 = new RectF();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int actualWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        final int actualHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();

        final int newCenterX = getPaddingLeft() + actualWidth / 2;
        final int newCenterY = getPaddingTop() + actualHeight / 2;
        if (newCenterX == centerX && newCenterY == centerY) {
            return;
        }

        centerX = newCenterX;
        centerY = newCenterY;

        final int radius = Math.min(actualWidth, actualHeight) / 2;
        rect1.set(centerX - radius + 10, centerY - radius + 10, centerX + radius - 10, centerY + radius - 10);
        rect2.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        setPaint(actualWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getVisibility() == INVISIBLE ||
            getVisibility() == GONE) {
            return;
        }

        int radius = (int) (360 * animationPosition);

        canvas.rotate(radius, centerX, centerY);
        canvas.drawArc(rect2, 180, 180, false, color1);
        canvas.drawArc(rect2, 0, 180, false, color2);

        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, innerRadius, innerPaint);
    }

    public void startViewAnimation() {
        if (isAnimationRunning) {
            return;
        }

        isAnimationRunning = true;

        objectAnimator = ObjectAnimator.ofFloat(this, ANIMATION_POSITION, 0, 1);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        });
        objectAnimator.setDuration(oneRoundDuration);
        objectAnimator.start();

    }

    public void stopViewAnimation() {
        if (!isAnimationRunning) {
            return;
        }

        if (objectAnimator != null) {
            objectAnimator.setRepeatCount(0);
            objectAnimator.cancel();
        }

        isAnimationRunning = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startViewAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopViewAnimation();
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        onVisibilityHasChanged();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        onVisibilityHasChanged();
    }

    private void onVisibilityHasChanged() {
        if (getWindowVisibility() != VISIBLE) {
            stopViewAnimation();
            return;
        }
        int visibility = getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            stopViewAnimation();
        } else {
            startViewAnimation();
        }
    }

    private void setPaint(int actualWidth) {
        color1.setShader(new LinearGradient(getPaddingLeft(),
                                            centerY,
                                            actualWidth + getPaddingLeft(),
                                            centerY,
                                            new int[] {ColorUtils.injectAlpha(0.5f, myColor), myColor},
                                            null,
                                            Shader.TileMode.MIRROR));

        color2.setShader(new LinearGradient(getPaddingLeft(),
                                            centerY,
                                            actualWidth + getPaddingLeft(),
                                            centerY,
                                            new int[] {ColorUtils.injectAlpha(0.5f, myColor), Color.TRANSPARENT},
                                            null,
                                            Shader.TileMode.MIRROR));
    }
}
