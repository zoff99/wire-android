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
import timber.log.Timber;

public enum CameraDirection {

    BACK_FACING(Camera.CameraInfo.CAMERA_FACING_BACK),
    FRONT_FACING(Camera.CameraInfo.CAMERA_FACING_FRONT),
    UNKNOWN(-1);

    private static final String TAG = CameraDirection.class.getName();
    public final int id;

    CameraDirection(int id) {
        this.id = id;
    }

    public static CameraDirection getDirection(int id) {
        for (CameraDirection direction : CameraDirection.values()) {
            if (direction.id == id) {
                return direction;
            }
        }
        Timber.e("Unknown camera direction id: %d", id);
        return UNKNOWN;
    }
}
