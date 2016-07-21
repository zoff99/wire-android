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
package com.waz.zclient.controllers.vibrator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Vibrator;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import com.waz.zclient.R;
import com.waz.zclient.controllers.userpreferences.UserPreferencesController;

public class VibratorController implements IVibratorController {

    private Resources resources;
    private Vibrator vibrator;
    private AudioManager audioManager;
    private Context context;

    public VibratorController(Context context) {
        this.context = context;
        this.resources = context.getResources();
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void tearDown() {
        context = null;
        resources = null;
        audioManager = null;
        vibrator = null;
    }

    @Override
    public void vibrate(@ArrayRes int patternRes) {
        vibrate(patternRes, false);
    }

    @Override
    public void vibrate(@NonNull long[] pattern) {
        vibrate(pattern, false);
    }

    @Override
    public void vibrate(@ArrayRes int patternRes, boolean loop) {
        long[] longArray = resolveResource(resources, patternRes);
        vibrate(longArray, loop);
    }

    @Override
    public void vibrate(@NonNull long[] pattern, boolean loop) {
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
            !vibrator.hasVibrator()) {
            return;
        }
        if (!isEnabledInPreferences(context)) {
            return;
        }
        stopVibrate();
        vibrator.vibrate(pattern, loop ? 0 : -1);
    }

    @Override
    public void stopVibrate() {
        vibrator.cancel();
    }

    @SuppressWarnings("PMD.AvoidArrayLoops")
    public static long[] resolveResource(Resources resources, @ArrayRes int resId) {
        int[] intArray = resources.getIntArray(resId);
        long[] longArray = new long[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            longArray[i] = intArray[i];
        }
        return longArray;
    }

    public static boolean isEnabledInPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(UserPreferencesController.USER_PREFS_TAG, Context.MODE_PRIVATE);
        return preferences.getBoolean(context.getString(R.string.pref_options_vibration_key), true);
    }
}
