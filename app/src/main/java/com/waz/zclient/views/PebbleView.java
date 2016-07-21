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
package com.waz.zclient.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import com.waz.zclient.R;
import com.waz.zclient.ui.utils.TypefaceUtils;
import com.waz.zclient.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class PebbleView extends View {
    private static final int ALPHA_REDUCTION_RATE_RIGHT = 20;
    private static final int ALPHA_REDUCTION_RATE_LEFT = 13;
    private static final float GRAVITY_LEFT = 1.0f;
    private static final float GRAVITY = 1.8f;
    private static final int DEFAULT_PEBBLE_OFFSET = 20;
    private static final int DEFAULT_NUM_OF_PEBBLES_NOT_HOT_KNOCK = 1;
    private static final float INTERNAL_DIVIDER_HOT_KNOCK = 5f;
    private static final float INTERNAL_DIVIDER_NOT_HOT_KNOCK = 2.5f;
    private static final int SEC_IN_MS = 1000;
    private static final double DEFAULT_NUM_OF_PEBBLES_HOT_KNOCK = 2;
    private int accentColor;
    private int startPebblePadding;

    public enum Direction {
        LEFT, RIGHT;
    }

    public static final String TAG = PebbleView.class.getName();

    protected static final int BULLET_TYPE = 0;
    protected static final int PARTICLE_TYPE = 1;
    protected static final int FPS = 33;

    protected int alphaReductionRate;

    protected Direction direction;

    protected static float friction = 0.98f;

    protected static Point rangeSize = new Point(4, 8);

    protected static Point rangeSpeedX = new Point(30, 40);
    protected static Point rangeSpeedY = new Point(-12, -5);
    protected static Point rangeSpeedXLeft = new Point(6, 12);
    protected static Point rangeSpeedYLeft = new Point(-6, -3);

    protected static Point rangeSpeedYExploded = new Point(-5, 4);

    protected static Point rangeAmount = new Point(2, 7);
    protected static Point rangeExplodeFriction = new Point(15, 20); /* originally (5,10) */
    protected static Point rangeExplodedSize = new Point(2, 7);

    private TextPaint textPaint;

    private int startingPointLeft;
    private int offset;

    List<Pebble> pebbles = new ArrayList<>();
    float gravity;

    private Paint paint;
    protected int height;
    protected int width;

    public void setDirection(Direction direction) {
        this.direction = direction;
        switch (direction) {

            case LEFT:
                alphaReductionRate = ALPHA_REDUCTION_RATE_LEFT;
                gravity = ViewUtils.toPx(getContext(), GRAVITY_LEFT);
                break;
            case RIGHT:
                alphaReductionRate = ALPHA_REDUCTION_RATE_RIGHT;
                gravity = ViewUtils.toPx(getContext(), GRAVITY);
                break;
        }
    }

    public void setAccentColor(int color) {
        accentColor = color;
        paint.setColor(color);
    }

    public PebbleView(Context context) {
        super(context);
        init();
    }

    public PebbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PebbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        setDirection(Direction.RIGHT);

        startPebblePadding = ViewUtils.toPx(getContext(), DEFAULT_PEBBLE_OFFSET);

        textPaint = new TextPaint();
        textPaint.setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.wire__text_size__small));
        textPaint.setTypeface(TypefaceUtils.getTypeface(getResources().getString(R.string.wire__typeface__medium)));
        offset = getResources().getDimensionPixelSize(R.dimen.framework__general__left_padding);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Pebble pebble : pebbles) {
            pebble.draw(canvas);
        }
    }

    public void stop() {
        pebbles.clear();
    }

    public void startShot(String message, boolean hotKnock) {
        startingPointLeft = offset + (int) (textPaint.measureText(message)) + startPebblePadding;

        // calculate timing
        float intervalDivider;
        int amount;
        if (hotKnock) {
            amount = (int) Math.round(DEFAULT_NUM_OF_PEBBLES_HOT_KNOCK + Math.random() * 1);
            intervalDivider = INTERNAL_DIVIDER_HOT_KNOCK;
        } else {
            amount = (int) Math.round(DEFAULT_NUM_OF_PEBBLES_NOT_HOT_KNOCK + Math.random() * 1);
            intervalDivider = INTERNAL_DIVIDER_NOT_HOT_KNOCK;
        }

        float intervalModule = 1 / intervalDivider;

        for (int i = 0; i < amount; i++) {
            float intervalThrow = i / intervalDivider;
            float intervalRandom = (float) (intervalModule / 2 - intervalModule * Math.random() / 2);
            int delay = (int) ((intervalThrow + intervalRandom) * SEC_IN_MS);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startShot(startingPointLeft, getMeasuredHeight() / 2);
                }
            }, delay * i);
        }
    }


    public void startShot(int x, int y) {
        Pebble pebble;
        switch (direction) {
            case LEFT:
                pebble = new Pebble(BULLET_TYPE,
                                    random(rangeSize),
                                    x,
                                    y,
                                    -random(rangeSpeedXLeft),
                                    random(rangeSpeedYLeft),
                                    paint);
                break;
            default:
                pebble = new Pebble(BULLET_TYPE,
                                    random(rangeSize),
                                    x,
                                    y,
                                    random(rangeSpeedX),
                                    random(rangeSpeedY),
                                    paint);
                break;
        }


        pebbles.add(pebble);

        if (pebbles.size() == 1) {
            computeNextStep();
        }
    }

    private void computeNextStep() {
        List<Pebble> remove = new ArrayList<>();
        for (Pebble pebble : pebbles) {
            if (pebble.computeNextStep()) {
                remove.add(pebble);
            }
        }
        pebbles.removeAll(remove);

        invalidate();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pebbles.size() > 0) {
                    computeNextStep();
                }
            }
        }, FPS);
    }


    private class Pebble {
        int type;

        private float startVelocityX;
        private float startVelocityY;
        private final Paint pebblePaint;
        float currY;
        private int size;
        float currX;

        List<Pebble> internPebbles = new ArrayList<Pebble>();

        boolean isExploded;
        int alpha = 255;

        Pebble(int type,
               int size,
               float startPosX,
               float startPosY,
               float startVelocityX,
               float startVelocityY,
               Paint paint) {
            this.size = ViewUtils.toPx(getContext(), size) / 2;
            this.type = type;

            this.currX = startPosX;
            this.currY = startPosY;

            // padding
            switch (direction) {
                case LEFT:
                    this.currX -= size;
                    break;
                case RIGHT:
                    this.currX += size;
                    break;
            }
            this.startVelocityX = ViewUtils.toPx(getContext(), startVelocityX);
            this.startVelocityY = ViewUtils.toPx(getContext(), startVelocityY);
            this.pebblePaint = new Paint();
            pebblePaint.setAntiAlias(true);
            pebblePaint.setColor(accentColor);
        }

        public void draw(Canvas canvas) {
            // draw others
            if (isExploded) {
                if (internPebbles.size() > 0) {
                    for (Pebble pebble : internPebbles) {
                        pebble.draw(canvas);
                    }
                }
            } else {
                canvas.drawCircle(currX, currY, size, pebblePaint);
            }
        }

        public boolean computeNextStep() {
            if (!isExploded) {
                startVelocityX *= friction;
                startVelocityY += gravity;
                currX += startVelocityX;
                currY += startVelocityY;

                if (type == BULLET_TYPE) {
                    switch (direction) {
                        case LEFT:
                            if (currX < 0) {
                                explode();
                            }
                            break;
                        case RIGHT:
                            if (currX > getMeasuredWidth()) {
                                explode();
                            }
                            break;
                    }
                } else {
                    alpha -= alphaReductionRate;

                    if (alpha < 0) {
                        alpha = 0;
                    }
                    pebblePaint.setAlpha(alpha);
                    // return if out of view
                    if (currX < 0 ||
                        currX > width ||
                        currY < 0 ||
                        currY > height) {
                        return true;
                    }
                }

                return false;
            } else {
                if (internPebbles.size() > 0) {
                    List<Pebble> remove = new ArrayList<Pebble>();
                    for (Pebble pebble : internPebbles) {
                        if (pebble.computeNextStep()) {
                            remove.add(pebble);
                        }
                    }

                    internPebbles.removeAll(remove);
                }

                return internPebbles.size() == 0;
            }
        }

        private void explode() {
            if (isExploded) {
                return;
            }
            isExploded = true;
            int amount = random(rangeAmount);
            for (int i = 0; i < amount; i++) {
                internPebbles.add(createRandomPebble());
            }
        }

        private Pebble createRandomPebble() {
            int newSize = size / random(rangeExplodedSize) + 2;
            float newSpeed = -startVelocityX / random(rangeExplodeFriction);
            switch (direction) {
                case LEFT:
                    return new Pebble(PARTICLE_TYPE,
                                      newSize,
                                      size / 2,
                                      currY,
                                      newSpeed,
                                      random(rangeSpeedYExploded),
                                      pebblePaint);
                default:
                    return new Pebble(PARTICLE_TYPE,
                                      newSize,
                                      getMeasuredWidth() - size / 2,
                                      currY,
                                      newSpeed,
                                      random(rangeSpeedYExploded),
                                      pebblePaint);
            }

        }
    }

    private int random(Point p) {
        return p.x + (int) ((p.y - p.x) * Math.random());
    }
}
