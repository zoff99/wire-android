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
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import com.waz.zclient.R;
import com.waz.zclient.ui.views.properties.PositionAnimatable;

import java.util.ArrayList;
import java.util.List;

public class OngoingCallingProgressView extends View implements PositionAnimatable {
    public static final String TAG = OngoingCallingProgressView.class.getName();

    private static final int DEFAULT_COLOR = Color.RED;
    private static final int DEFAULT_DURATION = 2000;

    private boolean radiusSpecified;
    private List<Integer> specRadius = new ArrayList<>();

    private int centerX;
    private int centerY;

    private Paint colorPaint;

    private boolean isAnimationRunning;
    private float animationPosition;
    private ObjectAnimator objectAnimator;
    private int oneRoundDuration;
    private List<Arc> arcs;

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
        colorPaint.setColor(color);
        invalidate();
    }

    public OngoingCallingProgressView(Context context) {
        this(context, null, 0);
    }

    public OngoingCallingProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OngoingCallingProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        isAnimationRunning = false;
        oneRoundDuration = DEFAULT_DURATION;

        // preparing paints
        colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorPaint.setStyle(Paint.Style.FILL);


        if (attrs == null) {
            setColor(DEFAULT_COLOR);
            radiusSpecified = false;
            return;
        }

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                                                                 R.styleable.OngoingCallingProgressView, 0, 0);

        int radius;

        radius = a.getDimensionPixelSize(R.styleable.OngoingCallingProgressView_ocpvRadius1, -1);
        if (radius != -1) {
            specRadius.add(radius);
        }
        radius = a.getDimensionPixelSize(R.styleable.OngoingCallingProgressView_ocpvRadius2, -1);
        if (radius != -1) {
            specRadius.add(radius);
        }
        radius = a.getDimensionPixelSize(R.styleable.OngoingCallingProgressView_ocpvRadius3, -1);
        if (radius != -1) {
            specRadius.add(radius);
        }
        radius = a.getDimensionPixelSize(R.styleable.OngoingCallingProgressView_ocpvRadius4, -1);
        if (radius != -1) {
            specRadius.add(radius);
        }

        oneRoundDuration = a.getInteger(R.styleable.OngoingCallingProgressView_ocpvOneRoundDuration, DEFAULT_DURATION);
        radiusSpecified = specRadius.size() > 0;

        setColor(a.getColor(R.styleable.OngoingCallingProgressView_ocpvCircleColor, DEFAULT_COLOR));


        a.recycle();

        arcs = new ArrayList<>();
        arcs.add(new Arc());
        arcs.add(new Arc());
        arcs.add(new Arc());
        arcs.add(new Arc());
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int actualWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int actualHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();

        int radius = Math.min(actualWidth, actualHeight) / 2;

        centerX = getPaddingLeft() + actualWidth / 2;
        centerY = getPaddingTop() + actualHeight / 2;

        if (!radiusSpecified) {
            arcs.get(0).set(0, 0);
            arcs.get(1).set(0, radius / 3);
            arcs.get(2).set(radius / 3, 2 * radius / 3);
            arcs.get(3).set(2 * radius / 3, radius);
            return;
        }

        int formerRad = 0;
        for (int i = 0; i < specRadius.size(); i++) {
            Integer rad = specRadius.get(i);
            switch (i) {
                case 0:
                    arcs.get(i).set(rad);
                    break;
                default:
                    arcs.get(i).set(formerRad, rad);
                    break;

            }
            formerRad = rad;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int pos = 0;

        for (Arc arc : arcs) {
            colorPaint.setAlpha(getCircleAlpha(pos));
            colorPaint.setStrokeWidth(arc.strokeWidth);
            canvas.drawCircle(centerX, centerY, arc.radius, colorPaint);
            pos++;
        }
    }


    private int getCircleAlpha(int i) {
        switch (i) {
            case 0:
                return 255;
            case 1:
                if (animationPosition < 0.5f) {
                    return 0;
                }
                // TODO not specified by design yet, some playful default
                return (int) (180 * (animationPosition - 0.5f) * 2);
            case 2:
                if (animationPosition < 0.75f) {
                    return 0;
                }
                // TODO not specified by design yet, some playful default
                return (int) (120 * (animationPosition - 0.75f) * 4.0f);
            case 3:
                if (animationPosition < 0.85f) {
                    return 0;
                }
                // TODO not specified by design yet, some playful default
                return (int) (80 * (animationPosition - 0.85f) * 4.0f);
            default:
                return 255;
        }
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


    public class Arc {
        public int radius;
        public int strokeWidth;

        private void set(int radius) {
            this.strokeWidth = 2;
            this.radius = radius;
        }

        private void set(int innerRadius, int outerRadius) {
            strokeWidth = outerRadius - innerRadius;
            radius = innerRadius + strokeWidth / 2;
        }

        @Override
        public String toString() {
            return "r: " + radius + " - s: " + strokeWidth;
        }
    }
}
