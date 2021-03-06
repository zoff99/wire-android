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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.waz.api.ImageAsset;
import com.waz.api.ImageAssetFactory;
import com.waz.zclient.BuildConfig;
import com.waz.zclient.OnBackPressedListener;
import com.waz.zclient.R;
import com.waz.zclient.camera.CameraPreviewObserver;
import com.waz.zclient.camera.CameraPreviewTextureView;
import com.waz.zclient.camera.FlashMode;
import com.waz.zclient.controllers.accentcolor.AccentColorObserver;
import com.waz.zclient.controllers.camera.CameraActionObserver;
import com.waz.zclient.controllers.drawing.IDrawingController;
import com.waz.zclient.controllers.orientation.OrientationControllerObserver;
import com.waz.zclient.pages.BaseFragment;
import com.waz.zclient.pages.extendedcursor.image.CursorImagesPreviewLayout;
import com.waz.zclient.pages.main.profile.camera.controls.CameraBottomControl;
import com.waz.zclient.pages.main.profile.camera.controls.CameraTopControl;
import com.waz.zclient.ui.animation.interpolators.penner.Expo;
import com.waz.zclient.utils.LayoutSpec;
import com.waz.zclient.utils.SquareOrientation;
import com.waz.zclient.utils.TestingGalleryUtils;
import com.waz.zclient.utils.ViewUtils;
import com.waz.zclient.views.ProgressView;

public class CameraFragment extends BaseFragment<CameraFragment.Container> implements CameraPreviewObserver,
                                                                                      OrientationControllerObserver,
                                                                                      AccentColorObserver,
                                                                                      OnBackPressedListener,
                                                                                      CursorImagesPreviewLayout.Callback,
                                                                                      CameraTopControl.CameraTopControlCallback,
                                                                                      CameraBottomControl.CameraBottomControlCallback {
    public static final String TAG = CameraFragment.class.getName();

    public static final int REQUEST_GALLERY_CODE = 9411;
    public static final long CAMERA_ROTATION_COOLDOWN_DELAY = 1600L;
    private static final String INTENT_GALLERY_TYPE = "image/*";
    private static final String CAMERA_CONTEXT = "CAMERA_CONTEXT";
    private static final String SHOW_GALLERY = "SHOW_GALLERY";
    private static final String SHOW_CAMERA_FEED = "SHOW_CAMERA_FEED";
    private static final String ALREADY_OPENED_GALLERY = "ALREADY_OPENED_GALLERY";

    private FrameLayout imagePreviewContainer;
    private ProgressView previewProgressBar;

    private CameraPreviewTextureView cameraPreview;
    private TextView cameraNotAvailableTextView;
    private ImageAsset imageAsset;
    private CameraTopControl cameraTopControl;
    private CameraBottomControl cameraBottomControl;
    private CameraFocusView focusView;

    private CameraContext cameraContext = null;
    private boolean showCameraFeed;
    private boolean alreadyOpenedGallery;

    private int cameraPreviewAnimationDuration;
    private int cameraControlAnimationDuration;

    //TODO pictureFromCamera is for tracking only, try to remove
    private boolean pictureFromCamera;

    public static CameraFragment newInstance(CameraContext cameraContext) {
        return newInstance(cameraContext, false);
    }

    public static CameraFragment newInstance(CameraContext cameraContext, boolean showGallery) {
        return newInstance(cameraContext, showGallery, false);
    }

    public static CameraFragment newInstance(CameraContext cameraContext, boolean showGallery, boolean showCameraFeed) {
        CameraFragment fragment = new CameraFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(CAMERA_CONTEXT, cameraContext.ordinal());
        bundle.putBoolean(SHOW_GALLERY, showGallery);
        bundle.putBoolean(SHOW_CAMERA_FEED, showCameraFeed);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static CameraFragment newRestartedInstance(CameraContext cameraContext, boolean showCameraFeed) {
        return newInstance(cameraContext, false, showCameraFeed);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        // opening from profile
        ensureCameraContext();
        if (nextAnim == R.anim.camera__from__profile__transition) {
            int controlHeight = getResources().getDimensionPixelSize(R.dimen.camera__control__height);
            return new ProfileToCameraAnimation(enter,
                                                getResources().getInteger(R.integer.framework_animation_duration_medium),
                                                0,
                                                controlHeight, 0);
        }


        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle savedInstanceState) {
        showCameraFeed = getArguments().getBoolean(SHOW_CAMERA_FEED);
        ensureCameraContext();

        final View view = inflater.inflate(R.layout.fragment_camera, c, false);

        //TODO allow selection of a camera 'facing' for different cameraContexts
        cameraPreview = ViewUtils.getView(view, R.id.cptv__camera_preview);
        cameraPreview.setObserver(this);
        cameraNotAvailableTextView = ViewUtils.getView(view, R.id.ttv__camera_not_available_message);

        cameraTopControl = ViewUtils.getView(view, R.id.ctp_top_controls);
        cameraTopControl.setCameraTopControlCallback(this);
        cameraTopControl.setAlpha(0);
        if (cameraPreview.getNumberOfCameras() == 0) {
            hideCameraFeed();
        }
        cameraTopControl.setVisibility(View.VISIBLE);

        cameraBottomControl = ViewUtils.getView(view, R.id.cbc__bottom_controls);
        cameraBottomControl.setCameraBottomControlCallback(this);
        cameraBottomControl.setMode(cameraContext);
        cameraBottomControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing but consume event
            }
        });
        if (showCameraFeed) {
            cameraBottomControl.setConfirmationMenuVisible(false);
        }

        imagePreviewContainer = ViewUtils.getView(view, R.id.fl__preview_container);
        if (cameraContext != CameraContext.MESSAGE) {
            imagePreviewContainer.setVisibility(View.GONE);
        }
        imagePreviewContainer.setVisibility(View.GONE);

        previewProgressBar = ViewUtils.getView(view, R.id.pv__preview);
        previewProgressBar.setVisibility(View.GONE);

        focusView = ViewUtils.getView(view, R.id.cfv__focus);

        if (savedInstanceState != null) {
            alreadyOpenedGallery = savedInstanceState.getBoolean(ALREADY_OPENED_GALLERY);
        }

        cameraControlAnimationDuration = getResources().getInteger(R.integer.camera__control__ainmation__duration);
        cameraPreviewAnimationDuration = getResources().getInteger(R.integer.camera__preview__ainmation__duration);

        view.setBackgroundResource(R.color.black);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getControllerFactory().getAccentColorController().addAccentColorObserver(this);
        if (LayoutSpec.isPhone(getActivity())) {
            getControllerFactory().getOrientationController().addOrientationControllerObserver(this);
        }
        getStoreFactory().getInAppNotificationStore().setUserSendingPicture(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ALREADY_OPENED_GALLERY, alreadyOpenedGallery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        getControllerFactory().getAccentColorController().removeAccentColorObserver(this);
        getControllerFactory().getOrientationController().removeOrientationControllerObserver(this);
        getStoreFactory().getInAppNotificationStore().setUserSendingPicture(false);

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        imagePreviewContainer = null;
        previewProgressBar = null;
        imageAsset = null;
        focusView = null;
        cameraTopControl = null;


        cameraBottomControl.animate()
                           .translationY(getView().getMeasuredHeight())
                           .setDuration(cameraControlAnimationDuration)
                           .setInterpolator(new Expo.EaseIn());

        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {
        onClose();
        return true;
    }

    public CameraContext getCameraContext() {
        return cameraContext;
    }

    private void ensureCameraContext() {
        if (cameraContext != null) {
            return;
        }
        cameraContext = CameraContext.values()[getArguments().getInt(CAMERA_CONTEXT)];
    }

    public boolean isCameraFeedShown() {
        return cameraPreview.getVisibility() == View.VISIBLE;
    }

    private void disableCameraButtons() {
        if (getView() == null) {
            return;
        }
        ViewUtils.getView(getView(), R.id.gtv__camera_control__take_a_picture).setVisibility(View.GONE);
        ViewUtils.getView(getView(), R.id.gtv__camera__top_control__change_camera).setVisibility(View.GONE);
        ViewUtils.getView(getView(), R.id.gtv__camera__top_control__flash_setting).setVisibility(View.GONE);
    }

    @Override
    public void onCameraLoaded() {
        cameraTopControl.setFlashStates(cameraPreview.getSupportedFlashModes(), cameraPreview.getCurrentFlashMode());
        cameraTopControl.enableCameraSwitchButtion(cameraPreview.getNumberOfCameras() > 1);
        showCameraFeed();

        boolean openGalleryArg = getArguments().getBoolean(SHOW_GALLERY);
        if (!alreadyOpenedGallery && openGalleryArg) {
            alreadyOpenedGallery = true;
            openGallery();
        }
        cameraNotAvailableTextView.setVisibility(View.GONE);
    }

    @Override
    public void onCameraLoadingFailed() {
        if (getContainer() != null) {
            getControllerFactory().getCameraController().onCameraNotAvailable(cameraContext);
        }
        disableCameraButtons();
        cameraNotAvailableTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCameraReleased() {
        //no need to override since we don't exit the app
    }

    @Override
    public void onPictureTaken(ImageAsset imageAsset) {
        this.imageAsset = imageAsset;

        // TODO: Remove the workaround once issue https://wearezeta.atlassian.net/browse/AN-4223 fixed or Automation framework find a workaround
        if (BuildConfig.IS_TEST_GALLERY_ALLOWED &&
            LayoutSpec.isTablet(getActivity()) &&
            TestingGalleryUtils.isCustomGalleryInstalled(getActivity().getPackageManager())) {
            getControllerFactory().getCameraController().onBitmapSelected(imageAsset, false, cameraContext);
        } else {
            showPreview(imageAsset, true);
        }
    }

    @Override
    public void onFocusBegin(Rect focusArea) {
        focusView.setColor(getControllerFactory().getAccentColorController().getColor());
        int x = focusArea.centerX();
        int y = focusArea.centerY();
        focusView.setX(x - focusView.getWidth() / 2);
        focusView.setY(y - focusView.getHeight() / 2);
        focusView.showFocusView();
    }

    @Override
    public void onFocusComplete() {
        if (focusView != null) {
            focusView.hideFocusView();
        }
    }

    public void openGallery() {
        Intent i;
        if (BuildConfig.IS_TEST_GALLERY_ALLOWED &&
            TestingGalleryUtils.isCustomGalleryInstalled(getActivity().getPackageManager())) {
            i = new Intent("com.wire.testing.GET_PICTURE");
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.setType(INTENT_GALLERY_TYPE);
        } else {
            i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType(INTENT_GALLERY_TYPE);
        }
        getActivity().startActivityForResult(i, REQUEST_GALLERY_CODE);
        getActivity().overridePendingTransition(R.anim.camera_in, R.anim.camera_out);
    }

    @Override
    public void nextCamera() {
        cameraPreview.nextCamera();
    }

    @Override
    public void setFlashMode(FlashMode mode) {
        cameraPreview.setFlashMode(mode);
    }

    @Override
    public FlashMode getFlashMode() {
        return cameraPreview.getCurrentFlashMode();
    }

    @Override
    public void onClose() {
        cameraPreview.setFlashMode(FlashMode.OFF); //set back to default off when leaving camera
        getControllerFactory().getCameraController().closeCamera(cameraContext);
    }

    @Override
    public void onTakePhoto() {
        if (cameraContext != CameraContext.SIGN_UP) {
            previewProgressBar.setVisibility(View.VISIBLE);
        }
        ViewUtils.fadeOutView(cameraTopControl, cameraControlAnimationDuration);
        cameraPreview.takePicture();
    }

    @Override
    public void onOpenImageGallery() {
        openGallery();
    }

    @Override
    public void onCancelPreview() {
        final Activity activity = getActivity();
        if (activity != null &&
            LayoutSpec.isTablet(activity)) {
            ViewUtils.unlockOrientation(activity);
        }

        dismissPreview();
    }

    @Override
    public void onSketchPictureFromPreview(ImageAsset imageAsset, CursorImagesPreviewLayout.Source source) {
        getControllerFactory().getDrawingController().showDrawing(imageAsset,
                                                                  IDrawingController.DrawingDestination.CAMERA_PREVIEW_VIEW);
    }

    @Override
    public void onSendPictureFromPreview(ImageAsset imageAsset, CursorImagesPreviewLayout.Source source) {
        final Activity activity = getActivity();
        if (activity != null &&
            LayoutSpec.isTablet(activity)) {
            ViewUtils.unlockOrientation(activity);
        }

        getControllerFactory().getCameraController().onBitmapSelected(imageAsset, pictureFromCamera, cameraContext);
    }

    private void showPreview(ImageAsset imageAsset, boolean bitmapFromCamera) {
        if (LayoutSpec.isTablet(getActivity())) {
            ViewUtils.lockCurrentOrientation(getActivity(),
                                             getControllerFactory().getOrientationController().getLastKnownOrientation());
        }

        pictureFromCamera = bitmapFromCamera;
        hideCameraFeed();

        previewProgressBar.setVisibility(View.GONE);

        CursorImagesPreviewLayout cursorImagesPreviewLayout = (CursorImagesPreviewLayout) LayoutInflater.from(getContext()).inflate(
            R.layout.fragment_cursor_images_preview,
            imagePreviewContainer,
            false);
        cursorImagesPreviewLayout.showSketch(cameraContext == CameraContext.MESSAGE);
        String previewTitle = cameraContext == CameraContext.MESSAGE ?
                              getStoreFactory().getConversationStore().getCurrentConversation().getName() :
                              "";
        cursorImagesPreviewLayout.setImageAsset(imageAsset,
                                                CursorImagesPreviewLayout.Source.CAMERA,
                                                this,
                                                getControllerFactory().getAccentColorController().getAccentColor().getColor(),
                                                previewTitle);

        imagePreviewContainer.addView(cursorImagesPreviewLayout);
        imagePreviewContainer.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(imagePreviewContainer,
                               View.ALPHA,
                               0,
                               1).setDuration(cameraPreviewAnimationDuration).start();
        cameraBottomControl.setVisibility(View.GONE);
    }

    private void dismissPreview() {
        previewProgressBar.setVisibility(View.GONE);

        int animationDuration = getResources().getInteger(R.integer.camera__control__ainmation__duration);
        ObjectAnimator animator = ObjectAnimator.ofFloat(imagePreviewContainer, View.ALPHA, 1, 0);
        animator.setDuration(animationDuration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                hideImagePreviewOnAnimationEnd();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hideImagePreviewOnAnimationEnd();
            }
        });
        animator.start();

        showCameraFeed();
    }

    private void hideImagePreviewOnAnimationEnd() {
        if (imagePreviewContainer != null &&
            cameraBottomControl != null) {
            imagePreviewContainer.setVisibility(View.GONE);
            cameraBottomControl.setVisibility(View.VISIBLE);
        }
    }

    private void showCameraFeed() {
        ViewUtils.fadeInView(cameraTopControl, cameraControlAnimationDuration);
        if (cameraPreview != null) {
            cameraPreview.setVisibility(View.VISIBLE);
        }
        cameraBottomControl.enableShutterButton();
    }

    private void hideCameraFeed() {
        ViewUtils.fadeOutView(cameraTopControl, cameraControlAnimationDuration);
        if (cameraPreview != null) {
            cameraPreview.setVisibility(View.GONE);
        }
    }

    @Override
    public void onOrientationHasChanged(SquareOrientation squareOrientation) {
        cameraTopControl.setConfigOrientation(squareOrientation);
        cameraBottomControl.setConfigOrientation(squareOrientation);
    }

    @Override
    public void onAccentColorHasChanged(Object sender, int color) {
        previewProgressBar.setTextColor(color);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                final Uri uri = data.getData();
                processGalleryImage(uri);
            } else {
                showCameraFeed();
                Toast.makeText(getActivity(), "Failed to insert image ", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void processGalleryImage(Uri uri) {
        imageAsset = null;
        hideCameraFeed();
        if (cameraContext != CameraContext.SIGN_UP) {
            previewProgressBar.setVisibility(View.VISIBLE);
        }

        imageAsset = ImageAssetFactory.getImageAsset(uri);
        // TODO: Remove the workaround once issue https://wearezeta.atlassian.net/browse/AN-4223 fixed or Automation framework find a workaround
        if (BuildConfig.IS_TEST_GALLERY_ALLOWED &&
            LayoutSpec.isTablet(getActivity()) &&
            TestingGalleryUtils.isCustomGalleryInstalled(getActivity().getPackageManager())) {
            getControllerFactory().getCameraController().onBitmapSelected(imageAsset, false, cameraContext);
        } else {
            showPreview(imageAsset, false);
        }
    }

    public interface Container extends CameraActionObserver {
    }
}
