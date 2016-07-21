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
package com.waz.zclient.pages.main.sharing;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.waz.zclient.R;
import com.waz.zclient.utils.ViewUtils;

public class SharingIndicatorView extends FrameLayout {

    private View contentView;

    private final int animationDuration;
    private ImageView backgroundImageView;

    public SharingIndicatorView(Context context) {
        this(context, null);
    }

    public SharingIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SharingIndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        animationDuration = getContext().getResources().getInteger(R.integer.network_indicator__animation_duration);

        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.sharing_indicator, this, true);
        backgroundImageView = ViewUtils.getView(contentView, R.id.sharing_background);
        float removedPosition = -getContext().getResources().getDimension(R.dimen.network_indicator__expanded_height);
        contentView.setY(removedPosition);
    }

    private void show() {
        contentView.animate().y(0)
                   .setDuration(animationDuration)
                   .setStartDelay(0)
                   .start();
    }

    public void setAccentColor(int color) {
        backgroundImageView.setImageDrawable(new ColorDrawable(color));
        show();
    }
}
