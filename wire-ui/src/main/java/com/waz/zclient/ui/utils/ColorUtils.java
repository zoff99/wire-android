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
package com.waz.zclient.ui.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;

public class ColorUtils {

    private ColorUtils() {
    }

    public static int injectAlpha(int alpha, int color) {
        return android.support.v4.graphics.ColorUtils.setAlphaComponent(color, alpha);
    }

    public static int injectAlpha(float alpha, int color) {
        return android.support.v4.graphics.ColorUtils.setAlphaComponent(color, (int) (255 * alpha));
    }


    public static ColorStateList createButtonTextColorStateList(int[] colors) {
        int[][] states = {{android.R.attr.state_pressed}, {android.R.attr.state_focused}, {android.R.attr.state_enabled}, {-android.R.attr.state_enabled}};
        return new ColorStateList(states, colors);
    }

    public static int adjustBrightness(int color, float percentage) {
        return Color.argb(Color.alpha(color), (int) (Color.red(color) * percentage), (int) (Color.green(color) * percentage), (int) (Color.blue(color) * percentage));
    }
}
