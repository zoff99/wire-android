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
package com.waz.zclient.ui.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.waz.zclient.ui.R;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerDotLayout extends LinearLayout {

    public static final String TAG = ColorPickerDotLayout.class.getName();

    // the list of colors offered by the app
    private int[] accentColors;

    private List<ColorPickerDotView> colorDotViews = new ArrayList<>();

    private OnColorSelectedListener onColorSelectedListener;
    private OnWidthChangedListener onWidthChangedListener;

    private int currentDotRadius = getResources().getDimensionPixelSize(R.dimen.color_picker_small_dot_radius);

    public ColorPickerDotLayout(Context context) {
        this(context, null);
    }

    public ColorPickerDotLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerDotLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setAccentColors(getResources().getIntArray(R.array.accents_color));
        populateDotsView();
        invalidate();
    }


    private void populateDotsView() {
        if (accentColors == null) {
            return;
        }
        if (getChildCount() != 0) {
            removeAllViews();
        }

        for (int accentColor: accentColors) {
            final ColorPickerDotView dot = (ColorPickerDotView) LayoutInflater.from(getContext()).inflate(R.layout.color_picker_dot_view, this, false);
            dot.setColor(accentColor);
            dot.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCurrentColor(dot.getCircleColor());
                }
            });
            colorDotViews.add(dot);
            this.addView(dot);
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                //to notify of width change
                if (onWidthChangedListener == null) {
                    return;
                }
                onWidthChangedListener.onScrollWidthChanged(getWidth());
            }
        }, 0);
    }

    public void setAccentColors(int[] colors) {
        accentColors = colors;
        populateDotsView();
        invalidate();
    }

    public void setCurrentColor(int color) {
        for (ColorPickerDotView colorPickerDotView: colorDotViews) {
            if (colorPickerDotView.getCircleColor() != color) {
                colorPickerDotView.setUnselected();
            } else {
                colorPickerDotView.setSelected(currentDotRadius);
                currentDotRadius = colorPickerDotView.getDotRadius();
                onColorSelectedListener.onColorSelected(colorPickerDotView.getCircleColor(), colorPickerDotView.getStrokeSize());
            }
        }
        invalidate();
    }

    /**
     * Sets the callback of the parent
     */
    public void setOnColorSelectedListener(OnColorSelectedListener onColorSelectedListener) {
        this.onColorSelectedListener = onColorSelectedListener;
    }

    public void setOnWidthChangedListener(OnWidthChangedListener onWidthChangedListener) {
        this.onWidthChangedListener = onWidthChangedListener;
    }

    /**
     * Callback to parent
     */
    public interface OnColorSelectedListener {
        void onColorSelected(int color, int strokeSize);
    }

    public interface OnWidthChangedListener {
        void onScrollWidthChanged(int width);
    }
}
