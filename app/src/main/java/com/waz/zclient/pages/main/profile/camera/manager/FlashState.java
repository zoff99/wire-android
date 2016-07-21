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
package com.waz.zclient.pages.main.profile.camera.manager;

import android.hardware.Camera;
import android.text.TextUtils;

public enum FlashState {

    /*
        off, auto, on, torch, red-eye
     */
    OFF(Camera.Parameters.FLASH_MODE_OFF),
    ON(Camera.Parameters.FLASH_MODE_ON),
    AUTO(Camera.Parameters.FLASH_MODE_AUTO),
    TORCH(Camera.Parameters.FLASH_MODE_TORCH),
    RED_EYE(Camera.Parameters.FLASH_MODE_RED_EYE);

    public String mode;

    FlashState(String mode) {
        this.mode = mode;
    }

    public static FlashState get(String mode) {
        if (TextUtils.isEmpty(mode)) {
            return OFF;
        }

        for (int i = 0; i < FlashState.values().length; i++) {
            FlashState state = FlashState.values()[i];
            if (mode.equals(state.mode)) {
                return state;
            }
        }

        return OFF;
    }
}
