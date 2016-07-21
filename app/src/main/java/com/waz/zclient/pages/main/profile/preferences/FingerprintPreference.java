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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.Toast;
import com.waz.api.Fingerprint;
import com.waz.zclient.R;
import net.xpece.android.support.preference.Preference;

public class FingerprintPreference extends Preference {

    public FingerprintPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("PrivateResource")
    public FingerprintPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Material);
    }

    public FingerprintPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public FingerprintPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onClick() {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getContext().getString(R.string.pref_devices_device_fingerprint_copy_description), getTitle());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), R.string.pref_devices_device_fingerprint_copy_toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    @SuppressLint("com.waz.ViewUtils")
    public void onBindViewHolder(PreferenceViewHolder holder) {
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        titleView.setSingleLine(false);
        super.onBindViewHolder(holder);
    }

    public void setFingerprint(Fingerprint fingerprint) {
        setTitle(DevicesPreferencesUtil.getFormattedFingerprint(getContext(), new String(fingerprint.getRawBytes())));
    }
}
