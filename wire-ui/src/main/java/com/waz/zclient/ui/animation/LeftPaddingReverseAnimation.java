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
package com.waz.zclient.ui.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import com.waz.zclient.ui.animation.interpolators.ReverseInterpolator;
import com.waz.zclient.utils.ViewUtils;

public class LeftPaddingReverseAnimation extends Animation {

    private final int from;
    private final int amount;
    private View view;

    private Interpolator reverseInterpolaor = new ReverseInterpolator();

    public LeftPaddingReverseAnimation(int from, int amount, View view, int duration) {
        this.from = from;
        this.amount = amount;
        this.view = view;

        setDuration(duration);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int leftPadding = (int) (from + amount * reverseInterpolaor.getInterpolation(interpolatedTime));
        ViewUtils.setPaddingLeft(view, leftPadding);
    }
}
