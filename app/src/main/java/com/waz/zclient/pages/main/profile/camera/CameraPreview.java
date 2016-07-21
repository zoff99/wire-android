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
package com.waz.zclient.pages.main.profile.camera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import com.waz.api.ImageAsset;
import com.waz.api.ImageAssetFactory;
import com.waz.zclient.ServiceContainer;
import com.waz.zclient.pages.main.profile.camera.colorlens.CameraGLRenderer;
import com.waz.zclient.pages.main.profile.camera.colorlens.CameraGLSurfaceView;
import com.waz.zclient.pages.main.profile.camera.manager.CameraDirection;
import com.waz.zclient.utils.LayoutSpec;
import com.waz.zclient.utils.device.DeviceDetector;
import timber.log.Timber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.hardware.Camera.PictureCallback;

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered
 * preview of the Camera to the surface. We need to center the SurfaceView
 * because not all devices have cameras that support preview sizes at the same
 * aspect ratio as the device's display.
 */
public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback,
                                                        CameraGLRenderer.OnSurfaceCreatedListener,
                                                        View.OnTouchListener {
    public static final String TAG = CameraPreview.class.getName();
    private final Container container;
    private final CameraType cameraType;
    private final Context context;
    SurfaceView surfaceView;
    SurfaceHolder holder;
    Camera.Size previewSize;
    List<Camera.Size> supportedPreviewSizes;
    Camera camera;
    boolean surfaceCreated = false;
    private int cameraId;
    private boolean cameraParamsHasBeenSet;

    // For focus
    private boolean settingFocus = false;
    private boolean previewRunning = false;
    private boolean clickToFocusSupported = false;
    private static final int CAMERA_AREA_WIDTH = 2000;
    private static final int CAMERA_AREA_OFFSET = 1000;

    private static final double ASPECT_TOLERANCE = 0.1;

    public CameraPreview(Context context,
                         CameraType cameraType,
                         final Container container) {
        super(context);
        this.context = context;
        this.container = container;
        this.cameraType = cameraType;

        if (cameraType == CameraType.COLOR_FILTERED) {
            surfaceView = new SurfaceView(context);
            surfaceView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    container.onFilteredPreviewClicked();
                }
            });
        } else {
            surfaceView = new SurfaceView(context);
            surfaceView.setOnTouchListener(this);
        }

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(surfaceView, params);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        holder = surfaceView.getHolder();
        holder.addCallback(this);
    }

    public void setCamera(Camera camera, int cameraId) {
        this.cameraId = cameraId;
        if (this.camera != null) {
            this.camera.stopPreview();
        }
        this.camera = camera;
        if (this.camera != null) {
            try {
                supportedPreviewSizes = this.camera.getParameters().getSupportedPreviewSizes();
            } catch (Exception e) {
                Timber.e(e, "Investigate why this happens");
            }
            if (surfaceCreated) {
                requestLayout();
            }
        }
        cameraParamsHasBeenSet = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (clickToFocusSupported && event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            float touchMajor = event.getTouchMajor();
            float touchMinor = event.getTouchMinor();

            Rect touchRect = new Rect(
                (int) (x - touchMajor / 2),
                (int) (y - touchMinor / 2),
                (int) (x + touchMajor / 2),
                (int) (y + touchMinor / 2));

            setFocusArea(touchRect);
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
        if (supportedPreviewSizes != null) {
            if (LayoutSpec.isPhone(context) || container.getControllerFactory().getOrientationController().isInPortrait()) {
                previewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
            } else {
                previewSize = getOptimalPreviewSize(supportedPreviewSizes, height, width);
            }
        }

        if (camera != null && !cameraParamsHasBeenSet) {
            cameraParamsHasBeenSet = true;
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            List<Camera.Size> supportedPictureSizes = camera.getParameters().getSupportedPictureSizes();
            Camera.Size pictureSize = getOptimalPictureSize(supportedPictureSizes, width, height);
            parameters.setPictureSize(pictureSize.width, pictureSize.height);

            setCameraOrientation();
            camera.setParameters(parameters);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (previewSize != null) {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                int scaledChildHeight = previewHeight * width / previewWidth;
                // scaledChildWidth = width;
                child.layout(
                    0,
                    (height - scaledChildHeight) / 2,
                    width,
                    (height + scaledChildHeight) / 2);
            } else {


                float scaleHeight = height / (float) previewWidth;
                float scaleWidth = width / (float) previewHeight;

                if (scaleWidth > scaleHeight) {
                    int cutoffHeight = (int) ((previewWidth * scaleWidth - height) / 2);
                    child.layout(0,
                                 -cutoffHeight,
                                 width,
                                 height + cutoffHeight);
                } else {
                    int cutoffWidth = (int) ((previewHeight * scaleHeight - width) / 2);
                    child.layout(-cutoffWidth,
                                 0,
                                 width + cutoffWidth,
                                 height);
                }

            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where to draw.
        try {
            if (camera != null) {
                camera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Timber.e(exception, "IOException caused by setPreviewDisplay()");
        }
        if (previewSize == null) {
            requestLayout();
        }
        surfaceCreated = true;
    }

    @Override
    public void onSurfaceCreated() {
        try {
            ((CameraGLSurfaceView) surfaceView).bindToCamera(camera, cameraId);
            camera.startPreview();
            previewRunning = true;
            notifyContainerPreviewStarted();
        } catch (Exception e) {
            Timber.e(e, "Failed starting preview!");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        stopPreview();
    }

    private Camera.Size getOptimalPictureSize(List<Camera.Size> sizes, int w, int h) {
        //Nexus 4 returns an unsupported picture size
        //for front-facing camera as the first size. Use second.
        if (cameraId == CameraDirection.FRONT_FACING.id &&
            "Nexus 4".equals(Build.MODEL)) {
            return sizes.get(1);
        }
        return sizes.get(0);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int screenWidth, int screenHeight) {
        if (sizes == null ||
            camera == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        final double targetRatio = ((double) camera.getParameters().getPictureSize().width) / (double)
            camera.getParameters().getPictureSize().height;
        int targetHeight = Math.min(screenHeight, screenWidth);
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
            if (camera == null) {
                return;
            }
            // Now that the size is known, set up the camera parameters and begin the preview.
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            requestLayout();

            clickToFocusSupported = parameters.getMaxNumFocusAreas() > 0 &&
                                    parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO);

            if (clickToFocusSupported) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            } else if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            camera.setParameters(parameters);
            camera.startPreview();
            previewRunning = true;
            notifyContainerPreviewStarted();
        } catch (Exception e) {
            Timber.e(e, "Failed adjusting cam surface!");
        }
    }

    private void setFocusArea(final Rect touchRect) {
        try {
            if (previewRunning && !settingFocus) {
                settingFocus = true;
                container.onFocus(touchRect);
                Rect focusArea = new Rect();

                focusArea.set(touchRect.left * CAMERA_AREA_WIDTH / surfaceView.getWidth() - CAMERA_AREA_OFFSET,
                              touchRect.top * CAMERA_AREA_WIDTH / surfaceView.getHeight() - CAMERA_AREA_OFFSET,
                              touchRect.right * CAMERA_AREA_WIDTH / surfaceView.getWidth() - CAMERA_AREA_OFFSET,
                              touchRect.bottom * CAMERA_AREA_WIDTH / surfaceView.getHeight() - CAMERA_AREA_OFFSET);

                // Submit focus area to camera
                ArrayList<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(focusArea, 1000));
                Camera.Parameters cameraParameters = camera.getParameters();
                cameraParameters.setFocusAreas(focusAreas);
                camera.setParameters(cameraParameters);

                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        container.onFocusComplete();
                        settingFocus = false;
                    }
                });
            }
        } catch (RuntimeException e) {
            Timber.e("Unable to tap-to-focus");
            container.onFocusComplete();
        }

    }

    public void stopPreview() {
        if (camera != null) {
            previewRunning = false;
            camera.stopPreview();
        }
    }

    public void startPreviewAgain() {
        if (camera != null) {
            camera.startPreview();
            notifyContainerPreviewStarted();
        }
    }

    private void notifyContainerPreviewStarted() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (cameraType == CameraType.COLOR_FILTERED) {
                    container.onFilteredPreviewStarted();
                } else {
                    container.onFullColorPreviewStarted();
                }
            }
        });
    }

    public void shoot(final OnCameraBitmapLoadedListener onCameraBitmapLoadedListener) {
        if (camera == null) {
            return;
        }

        final Handler uiHandler = new Handler();
        setCameraOrientation();
        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, final Camera camera) {
                if (container == null) {
                    return;
                }
                container.onPictureTaken();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (container == null ||
                            container.getControllerFactory() == null ||
                            container.getControllerFactory().isTornDown()) {
                            return;
                        }
                        ImageAsset imageAsset;

                        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT || DeviceDetector.isNexus7_2012()) {
                            imageAsset = ImageAssetFactory.getMirroredImageAsset(data);
                        } else {
                            imageAsset = ImageAssetFactory.getImageAsset(data);
                        }

                        onCameraBitmapLoadedListener.onCameraBitmapLoadedListener(imageAsset,
                                                                                  container.getControllerFactory().getOrientationController().getLastKnownOrientation());
                    }
                });
            }
        });
    }

    private void setCameraOrientation() {
        int degrees = container.getControllerFactory().getOrientationController().getActivityRotationDegrees();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);

        int deviceOrientation = container.getControllerFactory().getOrientationController().getDeviceOrientation();
        int cameraRotation;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraRotation = (info.orientation - deviceOrientation + 360) % 360;
        } else {  // back-facing camera
            cameraRotation = (info.orientation + deviceOrientation) % 360;
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(cameraRotation);
        camera.setParameters(parameters);
    }

    public interface Container extends ServiceContainer {
        void onFullColorPreviewStarted();

        void onFilteredPreviewStarted();

        void onFilteredPreviewClicked();

        void onFocus(Rect foucsArea);

        void onFocusComplete();

        void onPictureTaken();
    }

}
