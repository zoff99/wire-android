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
package com.waz.zclient.pages.main.profile.camera.colorlens;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import timber.log.Timber;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class CameraGLRenderer implements GLSurfaceView.Renderer {

    private final CameraGLSurfaceView cameraSurface;
    DirectVideo directVideo;
    private SurfaceTexture surfaceTexture;
    OnSurfaceCreatedListener onSurfaceCreatedListener;

    public CameraGLRenderer(CameraGLSurfaceView cameraSurface) {
        this.cameraSurface = cameraSurface;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        onSurfaceCreatedListener.onSurfaceCreated();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if (surfaceTexture == null || directVideo == null) {
            return;
        }
        float[] mtx = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mtx);
        directVideo.draw();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void bindToCamera(Camera camera, int cameraId) {
        directVideo = new DirectVideo(cameraId);
        surfaceTexture = new SurfaceTexture(directVideo.getTexture());
        surfaceTexture.setOnFrameAvailableListener(cameraSurface);
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (Exception e) {
            Timber.e(e, "Failed setting preview texture");
        }
    }

    public interface OnSurfaceCreatedListener {
        void onSurfaceCreated();
    }
}
