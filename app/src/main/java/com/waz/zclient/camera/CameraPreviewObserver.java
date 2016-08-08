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
package com.waz.zclient.camera;

import android.graphics.Rect;
import com.waz.api.ImageAsset;

public interface CameraPreviewObserver {

    void onCameraLoaded();

    void onCameraLoadingFailed();

    /**
     * This method needs to be overriden if the view using the CameraPreview wants to leave the app (and potentially
     * let another app use the camera). Only when this callback method returns can we be sure that the camera is closed
     * and that it's safe for other apps to attempt to open it.
     */
    void onCameraReleased();

    void onPictureTaken(ImageAsset imageAsset);

    void onFocusBegin(Rect focusArea);

    void onFocusComplete();
}
