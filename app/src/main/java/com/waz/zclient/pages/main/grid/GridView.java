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
package com.waz.zclient.pages.main.grid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.waz.zclient.ui.utils.ColorUtils;
import com.waz.zclient.utils.ViewUtils;

public class GridView extends View {
    private Paint whitePaint = new Paint();
    private Paint grayPaint = new Paint();
    private int distance;
    private int screenWidth;
    private int screenHeight;

    public GridView(Context context) {
        super(context);
        init();
    }

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        whitePaint = new Paint();
        whitePaint.setAntiAlias(true);
        whitePaint.setColor(ColorUtils.injectAlpha(40, Color.WHITE));

        grayPaint = new Paint();
        grayPaint.setAntiAlias(true);
        grayPaint.setColor(ColorUtils.injectAlpha(20, Color.BLACK));

        distance = ViewUtils.toPx(getContext(), 8);
        screenHeight = ViewUtils.getOrientationIndependentDisplayHeight(getContext());
        screenWidth = ViewUtils.getOrientationIndependentDisplayWidth(getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int currentDistanceWidth = distance;
        int currentDistanceHeight = distance;

        int pos = 0;
        // rows
        while (currentDistanceHeight < screenHeight) {
            if (pos % 5 == 4) {
                canvas.drawLine(0, currentDistanceHeight, screenWidth, currentDistanceHeight, whitePaint);
            } else {
                canvas.drawLine(0, currentDistanceHeight, screenWidth, currentDistanceHeight, grayPaint);
            }
            currentDistanceHeight += distance;
            pos++;
        }

        pos = 0;
        while (currentDistanceWidth < screenWidth) {
            if (pos % 5 == 4) {
                canvas.drawLine(currentDistanceWidth, 0, currentDistanceWidth, screenHeight, whitePaint);
            } else {
                canvas.drawLine(currentDistanceWidth, 0, currentDistanceWidth, screenHeight, grayPaint);
            }
            currentDistanceWidth += distance;
            pos++;
        }
    }
}
