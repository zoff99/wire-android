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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class CameraHandler extends Handler {
    public static final int LOAD_CAMERA = 1;
    public static final int CLOSE_CAMERA = 2;
    public static final int QUIT = 3;
    public static final String CAMERA_ID = "CAMERA_ID";

    private Camera camera;
    private Handler responseHandler;
    private CameraManagerCallback cameraManagerCallback;

    public CameraHandler(Handler responseHandler, Looper looper, CameraManagerCallback cameraManagerCallback) {
        super(looper);
        this.responseHandler = responseHandler;
        this.cameraManagerCallback = cameraManagerCallback;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case LOAD_CAMERA:
                if (camera != null) {
                    responseHandler.removeCallbacksAndMessages(null);
                    camera.release();
                }
                final int cameraId = msg.getData().getInt(CAMERA_ID);

                try {
                    camera = Camera.open(cameraId);
                    responseHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cameraManagerCallback != null) {
                                cameraManagerCallback.onCameraLoaded(camera, cameraId);
                            }
                        }
                    });

                } catch (Exception e) {
                    responseHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cameraManagerCallback != null) {
                                cameraManagerCallback.onCameraLoadingFailed();
                            }
                        }
                    });
                }
                break;
            case CLOSE_CAMERA:
                if (camera != null) {
                    responseHandler.removeCallbacksAndMessages(null);
                    camera.release();
                    camera = null;
                }
                break;
            case QUIT:
                getLooper().quit();
                break;
        }
    }
}
