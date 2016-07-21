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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class CameraManager {
    private final CameraHandler cameraHandler;

    private boolean hasFrontCamera;
    private boolean hasBackCamera;

    public CameraManager(CameraManagerCallback cameraManagerCallback) {
        HandlerThread cameraThread = new HandlerThread("CAMERA");
        cameraThread.start();
        cameraHandler = new CameraHandler(new Handler(), cameraThread.getLooper(), cameraManagerCallback);

        // Find the total number of cameras available
        int numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the rear-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            try {
                Camera.getCameraInfo(i, cameraInfo);
            } catch (Exception e) {
                continue;
            }
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                hasFrontCamera = true;
            }

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                hasBackCamera = true;
            }
        }
    }

    public void pause() {
        releaseCamera();
    }

    public void tearDown() {
        releaseCamera();

        Message msgObj = cameraHandler.obtainMessage();
        msgObj.what = CameraHandler.QUIT;
        cameraHandler.sendMessage(msgObj);
    }

    private void releaseCamera() {
        Message msgObj = cameraHandler.obtainMessage();
        msgObj.what = CameraHandler.CLOSE_CAMERA;
        cameraHandler.sendMessage(msgObj);
    }

    public void loadCamera(int cameraId) {
        Message msgObj = cameraHandler.obtainMessage();
        msgObj.what = CameraHandler.LOAD_CAMERA;
        Bundle b = new Bundle();
        b.putInt(CameraHandler.CAMERA_ID, cameraId);
        msgObj.setData(b);
        cameraHandler.sendMessage(msgObj);
    }

    public boolean hasFrontCamera() {
        return hasFrontCamera;
    }

    public boolean hasBackCamera() {
        return hasBackCamera;
    }

    public boolean hasNoCameras() {
        return !hasBackCamera && !hasFrontCamera;
    }

}
