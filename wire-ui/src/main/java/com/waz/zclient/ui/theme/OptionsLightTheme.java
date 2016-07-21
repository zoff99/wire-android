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
package com.waz.zclient.ui.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.waz.zclient.ui.R;

public class OptionsLightTheme implements OptionsTheme {
    private Resources resource;

    public OptionsLightTheme(Context context) {
        resource = context.getResources();
    }

    @Override
    public Type getType() {
        return Type.LIGHT;
    }

    @Override
    public int getTextColorPrimary() {
        return resource.getColor(R.color.text__primary_light);
    }

    @Override
    public ColorStateList getTextColorPrimarySelector() {
        return resource.getColorStateList(R.color.wire__text_color_primary_light_selector);
    }

    @Override
    public int getOverlayColor() {
        return resource.getColor(R.color.wire__overlay__light);
    }

    @Override
    public void tearDown() {
        resource = null;
    }

    @Override
    public int getCheckboxTextColor() {
        return resource.getColor(R.color.text__primary_light);
    }

    @Override
    public Drawable getCheckBoxBackgroundSelector() {
        return resource.getDrawable(R.drawable.selector__check_box__background__light);
    }

    @Override
    public ColorStateList getIconButtonTextColor() {
        return resource.getColorStateList(R.color.selector__icon_button__text_color__light);
    }

    @Override
    public Drawable getIconButtonBackground() {
        return resource.getDrawable(R.drawable.selector__icon_button__background__light);
    }
}
