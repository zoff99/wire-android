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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import com.waz.zclient.R;
import com.waz.zclient.ui.views.e2ee.ShieldView;
import net.xpece.android.support.preference.Preference;

public class DevicePreference extends Preference {

    private boolean isVerified;

    public DevicePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public DevicePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Material_DevicePreference);
    }

    public DevicePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.devicePreferenceStyle);
    }

    public DevicePreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DevicePreference, defStyleAttr, defStyleRes);
        isVerified = a.getBoolean(R.styleable.DevicePreference_verified, false);
        a.recycle();
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
        notifyChanged();
    }

    @Override
    @SuppressLint("com.waz.ViewUtils")
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ShieldView shieldView = (ShieldView) holder.findViewById(R.id.sv__pref__device);
        shieldView.setVerified(isVerified);
    }
}
