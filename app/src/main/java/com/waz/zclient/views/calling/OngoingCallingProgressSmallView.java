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
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import com.waz.zclient.R;
import com.waz.zclient.utils.ViewUtils;
import com.waz.zclient.ui.animation.interpolators.penner.Expo;
import com.waz.zclient.ui.views.properties.PositionAnimatable;

import java.util.ArrayList;
import java.util.List;

public class OngoingCallingProgressSmallView extends View {
    public static final String TAG = OngoingCallingProgressSmallView.class.getName();

    private static final int DEFAULT_COLOR = Color.RED;
    private static final int ANIM_FACTOR = 1;

    private static final int DEFAULT_OPACITY = 100;

    private static final int DEFAULT_TO_DURATION = 350 * ANIM_FACTOR;
    private static final int DEFAULT_BACK_DURATION = 700 * ANIM_FACTOR;

    private static final int DEFAULT_INNER_RADIUS1 = 6;
    private static final int DEFAULT_INNER_RADIUS1_TO = 7;
    private static final int DEFAULT_INNER_RADIUS1_DELAY_TO = 0;
    private static final int DEFAULT_INNER_RADIUS1_DELAY_BACK = 350 * ANIM_FACTOR;

    private static final int DEFAULT_INNER_RADIUS2 = 6;
    private static final int DEFAULT_INNER_RADIUS2_TO = 8;
    private static final int DEFAULT_INNER_RADIUS2_DELAY_TO = 150 * ANIM_FACTOR;
    private static final int DEFAULT_INNER_RADIUS2_DELAY_BACK = 500 * ANIM_FACTOR;

    private static final int DEFAULT_INNER_RADIUS3 = 12;
    private static final int DEFAULT_INNER_RADIUS3_TO = 15;
    private static final int DEFAULT_INNER_RADIUS3_DELAY_TO = 300 * ANIM_FACTOR;
    private static final int DEFAULT_INNER_RADIUS3_DELAY_BACK = 650 * ANIM_FACTOR;

    private int centerX;
    private int centerY;

    private int outerOpacity;
    private int middleOpacity;
    private int innerOpacity;

    private List<Arc> arcs = new ArrayList<>();

    private boolean isAnimationRunning;
    private int accentColor = DEFAULT_COLOR;

    public void setAccentColor(int color) {
        this.accentColor = color;
        for (Arc arc : arcs) {
            arc.setAccentColor();
        }
    }

    public OngoingCallingProgressSmallView(Context context) {
        this(context, null, 0);
    }

    public OngoingCallingProgressSmallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OngoingCallingProgressSmallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CallingIndicator, 0, 0);
        try {
            outerOpacity = a.getInt(R.styleable.CallingIndicator_outerOpacity, DEFAULT_OPACITY);
            middleOpacity = a.getInt(R.styleable.CallingIndicator_middleOpacity, DEFAULT_OPACITY);
            innerOpacity = a.getInt(R.styleable.CallingIndicator_innerOpacity, DEFAULT_OPACITY);
        } finally {
            a.recycle();
        }

        isAnimationRunning = false;

        // preparing paints
        Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorPaint.setStyle(Paint.Style.STROKE);

        Arc arc = new Arc(0, DEFAULT_INNER_RADIUS1, 0, DEFAULT_INNER_RADIUS1_TO, ViewUtils.getAlphaValue(innerOpacity), DEFAULT_INNER_RADIUS1_DELAY_TO, DEFAULT_INNER_RADIUS1_DELAY_BACK);
        arcs.add(arc);
        arc = new Arc(DEFAULT_INNER_RADIUS1, DEFAULT_INNER_RADIUS2, DEFAULT_INNER_RADIUS1_TO, DEFAULT_INNER_RADIUS2_TO, ViewUtils.getAlphaValue(middleOpacity), DEFAULT_INNER_RADIUS2_DELAY_TO, DEFAULT_INNER_RADIUS2_DELAY_BACK);
        arcs.add(arc);
        arc = new Arc(DEFAULT_INNER_RADIUS2, DEFAULT_INNER_RADIUS3, DEFAULT_INNER_RADIUS2_TO, DEFAULT_INNER_RADIUS3_TO, ViewUtils.getAlphaValue(outerOpacity), DEFAULT_INNER_RADIUS3_DELAY_TO, DEFAULT_INNER_RADIUS3_DELAY_BACK);
        arcs.add(arc);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int actualWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int actualHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();

        centerX = getPaddingLeft() + actualWidth / 2;
        centerY = getPaddingTop() + actualHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Arc arc : arcs) {
            arc.draw(canvas);
        }
    }


    public void startViewAnimation() {
        if (isAnimationRunning) {
            return;
        }
        isAnimationRunning = true;

        play();


    }

    public void play() {
        if (isAnimationRunning) {
            for (Arc arc : arcs) {
                arc.play();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    play();
                }
            }, DEFAULT_BACK_DURATION + DEFAULT_TO_DURATION);

        }
    }


    public void stopViewAnimation() {
        if (!isAnimationRunning) {
            return;
        }

        for (Arc arc : arcs) {
            arc.stop();
        }

        isAnimationRunning = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onVisibilityHasChanged();
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
        if (getWindowVisibility() == View.GONE || getWindowVisibility() == View.INVISIBLE) {
            stopViewAnimation();
        }

        int visibility = getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            stopViewAnimation();
        } else {
            startViewAnimation();
        }
    }


    public class Arc implements PositionAnimatable {
        private final int alpha;
        public int radius;
        public int radiusTo;

        public int strokeWidth;
        public int strokeWidthTo;


        private ObjectAnimator objectAnimatorTo;
        private ObjectAnimator objectAnimatorBack;
        private float animationPosition;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override
        public void setAnimationPosition(float animationPosition) {
            this.animationPosition = animationPosition;
            OngoingCallingProgressSmallView.this.invalidate();
        }

        @Override
        public float getAnimationPosition() {
            return animationPosition;
        }

        public void play() {
            animationPosition = 0;
            stop();
            objectAnimatorTo.start();
            objectAnimatorBack.start();
        }

        public void stop() {
            objectAnimatorTo.cancel();
            objectAnimatorBack.cancel();
        }

        public Arc(int innerRadius, int outerRadius, int innerRadiusTo, int outerRadiusTo, int alpha, int startDelayTo, int startDelayBack) {
            this.alpha = alpha;
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(alpha);
            strokeWidth = ViewUtils.toPx(getContext(), outerRadius) - ViewUtils.toPx(getContext(), innerRadius);
            this.radius = ViewUtils.toPx(getContext(), innerRadius) + strokeWidth / 2;

            strokeWidthTo = ViewUtils.toPx(getContext(), outerRadiusTo) - ViewUtils.toPx(getContext(), innerRadiusTo);
            radiusTo = ViewUtils.toPx(getContext(), innerRadiusTo) + strokeWidthTo / 2;

            objectAnimatorTo = ObjectAnimator.ofFloat(this, ANIMATION_POSITION, 0, 1);
            objectAnimatorTo.setDuration(DEFAULT_TO_DURATION);
            objectAnimatorTo.setStartDelay(startDelayTo);
            objectAnimatorTo.setInterpolator(new Expo.EaseIn());

            objectAnimatorBack = ObjectAnimator.ofFloat(this, ANIMATION_POSITION, 0);
            objectAnimatorBack.setDuration(DEFAULT_BACK_DURATION);
            objectAnimatorBack.setStartDelay(startDelayBack);
            objectAnimatorBack.setInterpolator(new Expo.EaseOut());
        }

        public void draw(Canvas canvas) {
            float stroke = strokeWidth * (1 - animationPosition) + animationPosition * strokeWidthTo;
            float r = radius * (1 - animationPosition) + animationPosition * radiusTo;

            paint.setStrokeWidth(stroke);
            canvas.drawCircle(centerX, centerY, r, paint);
        }

        @Override
        public String toString() {
            return "r: " + radius + " - s: " + strokeWidth + " rTo: " + radiusTo + " - sTo: " + strokeWidthTo;
        }

        public void setAccentColor() {
            paint.setColor(accentColor);
            paint.setAlpha(alpha);
        }
    }


}
