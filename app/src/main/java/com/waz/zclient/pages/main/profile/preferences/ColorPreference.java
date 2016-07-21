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
package com.waz.zclient.pages.main.profile.preferences;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.waz.zclient.R;
import com.waz.zclient.ui.utils.DrawableUtils;
import com.waz.zclient.ui.views.FilledCircularBackgroundDrawable;
import net.xpece.android.support.preference.Preference;

public class ColorPreference extends Preference {

    private int diameter;

    public ColorPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    public ColorPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ColorPreference(Context context) {
        super(context);
        setup();
    }

    private void setup() {
        diameter = getContext().getResources().getDimensionPixelSize(R.dimen.pref_account_icon_size);
        FilledCircularBackgroundDrawable icon = new FilledCircularBackgroundDrawable(Color.TRANSPARENT, diameter);
        setIcon(DrawableUtils.drawableToBitmapDrawable(getContext().getResources(), icon, diameter));
    }

    public void setAccentColor(int color) {
        FilledCircularBackgroundDrawable icon = new FilledCircularBackgroundDrawable(color, diameter);
        setIcon(DrawableUtils.drawableToBitmapDrawable(getContext().getResources(), icon, diameter));
    }
}
