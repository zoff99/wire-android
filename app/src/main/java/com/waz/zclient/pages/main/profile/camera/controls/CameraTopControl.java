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
package com.waz.zclient.pages.main.profile.camera.controls;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.waz.zclient.R;
import com.waz.zclient.utils.SquareOrientation;
import com.waz.zclient.pages.main.profile.camera.manager.CameraDirection;
import com.waz.zclient.pages.main.profile.camera.manager.FlashState;
import com.waz.zclient.utils.ViewUtils;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

public class CameraTopControl extends FrameLayout {
    CameraTopControlCallback cameraTopControlCallback;

    private FlashState currentFlashState = FlashState.OFF;
    private CameraDirection cameraDirection;
    private TextView cameraDirectionButton;
    private TextView cameraFlashButton;
    private List<FlashState> supportedFlashStates;
    private SquareOrientation currentConfigOrientation = SquareOrientation.NONE;
    private Camera camera;

    public void setCameraTopControlCallback(CameraTopControlCallback cameraTopControlCallback) {
        this.cameraTopControlCallback = cameraTopControlCallback;
    }

    public void removeTopControlCallback(CameraTopControlCallback cameraTopControlCallback) {
        cameraTopControlCallback = null;
    }

    public CameraTopControl(Context context) {
        super(context);
        init();
    }

    public CameraTopControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraTopControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.camera_top_control, this, true);
        cameraDirectionButton = ViewUtils.getView(this, R.id.gtv__camera__top_control__back_camera);
        cameraDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCameraDirection();
            }


        });

        cameraFlashButton = ViewUtils.getView(this, R.id.gtv__camera__top_control__flash_light);
        cameraFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlashState nextFlashState = getNextFlashState(currentFlashState);
                if (cameraTopControlCallback != null) {
                    cameraTopControlCallback.setSavedFlashState(nextFlashState);
                }
                setFlashState(nextFlashState);
            }
        });

        cameraFlashButton.setVisibility(View.GONE);
    }

    private void setFlashStateButton(FlashState flashState) {
        switch (flashState) {
            case OFF:
                cameraFlashButton.setText(getResources().getString(R.string.glyph__flash_off));
                break;
            case ON:
                cameraFlashButton.setText(getResources().getString(R.string.glyph__flash));
                break;
            case AUTO:
                cameraFlashButton.setText(getResources().getString(R.string.glyph__flash_auto));
                break;
            case TORCH:
                cameraFlashButton.setText(getResources().getString(R.string.glyph__plus));
                break;
            case RED_EYE:
                cameraFlashButton.setText(getResources().getString(R.string.glyph__redo));
                break;
        }

        ObjectAnimator.ofFloat(cameraFlashButton, View.ALPHA, 0, 1).setDuration(getResources().getInteger(R.integer.camera__control__ainmation__duration_long)).start();
    }


    private FlashState getNextFlashState(FlashState currentFlashState) {
        switch (currentFlashState) {
            case OFF:
                if (supportedFlashStates.contains(FlashState.ON)) {
                    return FlashState.ON;
                } else if (supportedFlashStates.contains(FlashState.AUTO)) {
                    return FlashState.AUTO;
                }
            case ON:
                if (supportedFlashStates.contains(FlashState.AUTO)) {
                    return FlashState.AUTO;
                }

                return FlashState.OFF;
            case AUTO:
                return FlashState.OFF;
            case TORCH:
                break;
            case RED_EYE:
                break;
        }
        return FlashState.OFF;
    }

    private void switchCameraDirection() {
        if (cameraDirection == null) {
            return;
        }

        switch (cameraDirection) {
            case BACK_FACING:
                cameraDirection = CameraDirection.FRONT_FACING;
                break;
            case FRONT_FACING:
                cameraDirection = CameraDirection.BACK_FACING;
                break;
        }

        notifyCameraDirectionHasChanged(cameraDirection);
    }

    private void setFlashState(FlashState flashState) {
        try {
            currentFlashState = flashState;
            setFlashStateButton(currentFlashState);
            Camera.Parameters parameters = camera.getParameters();
            String flash;
            switch (currentFlashState) {
                case AUTO:
                    flash = Camera.Parameters.FLASH_MODE_AUTO;
                    break;
                case OFF:
                    flash = Camera.Parameters.FLASH_MODE_OFF;
                    break;
                case ON:
                    flash = Camera.Parameters.FLASH_MODE_ON;
                    break;
                case TORCH:
                    flash = Camera.Parameters.FLASH_MODE_TORCH;
                    break;
                case RED_EYE:
                    flash = Camera.Parameters.FLASH_MODE_RED_EYE;
                    break;
                default:
                    flash = Camera.Parameters.FLASH_MODE_OFF;
                    break;

            }
            parameters.setFlashMode(flash);
            camera.setParameters(parameters);
        } catch (RuntimeException e) {
            Timber.e("Unable to set flash state");
        }
    }

    private void notifyCameraDirectionHasChanged(CameraDirection cameraDirection) {
        if (cameraTopControlCallback != null) {
            cameraTopControlCallback.loadCamera(cameraDirection);
        }
    }


    public void setCamera(Camera camera, int cameraId) {
        this.camera = camera;
        supportedFlashStates = detectSupportedFlashStates(camera);

        if (!supportedFlashStates.contains(FlashState.AUTO) && !supportedFlashStates.contains(FlashState.ON)) {
            cameraFlashButton.setVisibility(View.GONE);
            setFlashState(FlashState.OFF);
        } else {
            cameraFlashButton.setVisibility(View.VISIBLE);
            setFlashState(cameraTopControlCallback.getSavedFlashState());
        }

        cameraDirection = CameraDirection.getDirection(cameraId);
    }

    private List<FlashState> detectSupportedFlashStates(Camera camera) {
        List<FlashState> supportedFlashStates = new ArrayList<>();
        List<String> supportedFlashModes = camera.getParameters().getSupportedFlashModes();
        if (supportedFlashModes != null) {
            for (String supportedFlashMode : supportedFlashModes) {
                supportedFlashStates.add(FlashState.get(supportedFlashMode));
            }
        }
        return supportedFlashStates;
    }

    public void enableCameraSwitchButtion(boolean enableCameraSwitch) {
        if (cameraDirectionButton == null) {
            return;
        }
        cameraDirectionButton.setVisibility(enableCameraSwitch ? VISIBLE : GONE);
    }

    public void setConfigOrientation(SquareOrientation configOrientation) {
        if (configOrientation.equals(currentConfigOrientation)) {
            return;
        }

        int currentOrientation = (int) cameraDirectionButton.getRotation();
        int rotation = 0;

        switch (configOrientation) {

            case NONE:
                break;
            case PORTRAIT_STRAIGHT:
                rotation = 0;
                break;
            case PORTRAIT_UPSIDE_DOWN:
                rotation = 2 * currentOrientation;
                break;
            case LANDSCAPE_LEFT:
                if (currentOrientation == -180) {
                    setRotation(180);
                }
                rotation = 90;
                break;
            case LANDSCAPE_RIGHT:
                if (currentOrientation == 180) {
                    setRotation(-180);
                }
                rotation = -90;
                break;
        }

        currentConfigOrientation = configOrientation;

        cameraDirectionButton.animate().rotation(rotation).start();
        cameraFlashButton.animate().rotation(rotation).start();
    }

    private void setRotation(int rotation) {
        cameraDirectionButton.setRotation(rotation);
        cameraFlashButton.setRotation(rotation);
    }

}
