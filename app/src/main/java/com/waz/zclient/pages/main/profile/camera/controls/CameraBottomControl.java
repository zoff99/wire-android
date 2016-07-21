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

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ViewAnimator;
import com.waz.zclient.R;
import com.waz.zclient.pages.main.profile.camera.CameraContext;
import com.waz.zclient.pages.main.profile.camera.ControlState;
import com.waz.zclient.pages.main.profile.views.ConfirmationMenu;
import com.waz.zclient.pages.main.profile.views.ConfirmationMenuListener;
import com.waz.zclient.ui.animation.interpolators.penner.Expo;
import com.waz.zclient.ui.theme.OptionsDarkTheme;
import com.waz.zclient.utils.SquareOrientation;
import com.waz.zclient.utils.ViewUtils;

public class CameraBottomControl extends ViewAnimator {
    private CameraBottomControlCallback cameraBottomControlCallback;
    private ControlState currentControlState;
    private ConfirmationMenu approveImageSelectionMenu;
    private SquareOrientation currentConfigOrientation = SquareOrientation.NONE;
    private View backToChangeButton;
    private View takeAPictureButton;
    private View galleryPickerButton;

    public void setCameraBottomControlCallback(CameraBottomControlCallback cameraBottomControlCallback) {
        this.cameraBottomControlCallback = cameraBottomControlCallback;
    }

    public void removeCameraBottomControlCallback() {
        this.cameraBottomControlCallback = null;
    }

    public void setAccentColor(int accentColor) {
        if (approveImageSelectionMenu == null) {
            return;
        }
        approveImageSelectionMenu.setAccentColor(accentColor);
    }

    public boolean isCameraState() {
        return currentControlState == ControlState.CAMERA;
    }

    public CameraBottomControl(Context context) {
        super(context);
    }

    public CameraBottomControl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMode(CameraContext cameraContext) {
        switch (cameraContext) {
            case SETTINGS:
                setupSettingsMode();
                break;
            case SIGN_UP:
                setupSignUpMode();
                break;
            case MESSAGE:
                setupMessageMode(true);
                break;
        }
    }

    private void setupSignUpMode() {
        setupMessageMode(false);
    }

    private void setupSettingsMode() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        addTakeAPhotoController(inflater, true, true);
        addConfirmSelectionController();

        setControlState(ControlState.CAMERA);
    }

    private void setupMessageMode(boolean allowCloseButton) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        addTakeAPhotoController(inflater, allowCloseButton, true);
        addConfirmSelectionController();

        setControlState(ControlState.CAMERA);
    }

    private void addConfirmSelectionController() {
        approveImageSelectionMenu = new ConfirmationMenu(getContext());
        approveImageSelectionMenu.setWireTheme(new OptionsDarkTheme(getContext()));
        approveImageSelectionMenu.setCancel(getResources().getString(R.string.confirmation_menu__cancel));
        approveImageSelectionMenu.setConfirm(getResources().getString(R.string.confirmation_menu__confirm_done));
        approveImageSelectionMenu.setConfirmationMenuListener(new ConfirmationMenuListener() {
            @Override
            public void confirm() {
                if (cameraBottomControlCallback != null) {
                    cameraBottomControlCallback.onApproveSelection(true);
                }
            }

            @Override
            public void cancel() {
                if (cameraBottomControlCallback != null) {
                    cameraBottomControlCallback.onApproveSelection(false);
                }
            }
        });
        addView(approveImageSelectionMenu);
    }

    private void addTakeAPhotoController(LayoutInflater inflater, boolean allowCloseButton, boolean showGalleryButton) {
        inflater.inflate(R.layout.camera_control_choose_image_source, this, true);


        // show image gallery
        galleryPickerButton = ViewUtils.getView(this, R.id.gtv__camera_control__pick_from_gallery_in_camera);
        if (showGalleryButton) {
            galleryPickerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraBottomControlCallback.onOpenImageGallery();
                }
            });
        } else {
            galleryPickerButton.setVisibility(View.GONE);
        }

        // go to change image
        backToChangeButton = ViewUtils.getView(this, R.id.gtv__camera_control__back_to_change_image);
        backToChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraBottomControlCallback != null) {
                    cameraBottomControlCallback.onDismiss();
                }
            }
        });
        if (!allowCloseButton) {
            backToChangeButton.setVisibility(GONE);
        }

        // take picture
        takeAPictureButton = ViewUtils.getView(this, R.id.gtv__camera_control__take_a_picture);
        takeAPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraBottomControlCallback != null) {
                    disableShutterButton();
                    cameraBottomControlCallback.onTakePhoto();
                }
            }
        });
    }

    public void setControlState(ControlState controlState) {
        currentControlState = controlState;
        switch (controlState) {
            case CAMERA:
                enableShutterButton();
                setDisplayedChild(0);
                break;
            case DIALOG_APPROVE_SELECTION:
                setDisplayedChild(1);
                break;
        }
    }

    public void setConfigOrientation(SquareOrientation configOrientation) {
        if (configOrientation.equals(currentConfigOrientation) ||
            backToChangeButton == null ||
            takeAPictureButton == null ||
            galleryPickerButton == null) {
            return;
        }

        int currentOrientation = (int) backToChangeButton.getRotation();
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

        // go to change image
        backToChangeButton.animate()
                          .rotation(rotation)
                          .start();

        // take picture
        takeAPictureButton.animate()
                          .rotation(rotation)
                          .start();

        // show image gallery
        galleryPickerButton.animate()
                           .rotation(rotation)
                           .start();
    }

    private void setRotation(int rotation) {
        if (backToChangeButton == null ||
            takeAPictureButton == null ||
            galleryPickerButton == null) {
            return;
        }
        // go to change image
        backToChangeButton.setRotation(rotation);

        // take picture
        takeAPictureButton.setRotation(rotation);

        // show image gallery
        galleryPickerButton.setRotation(rotation);
    }

    @Override
    public void setInAnimation(Animation inAnimation) {
        inAnimation.setStartOffset(getResources().getInteger(R.integer.camera__control__ainmation__in_delay));
        inAnimation.setInterpolator(new Expo.EaseOut());
        inAnimation.setDuration(getContext().getResources().getInteger(R.integer.calling_animation_duration_medium));
        super.setInAnimation(inAnimation);
    }

    @Override
    public void setOutAnimation(Animation outAnimation) {
        outAnimation.setInterpolator(new Expo.EaseIn());
        outAnimation.setDuration(getContext().getResources().getInteger(R.integer.calling_animation_duration_medium));
        super.setOutAnimation(outAnimation);
    }

    private void enableShutterButton() {
        if (takeAPictureButton != null) {
            takeAPictureButton.setEnabled(true);
        }
    }

    private void disableShutterButton() {
        if (takeAPictureButton != null) {
            takeAPictureButton.setEnabled(false);
        }
    }

}
