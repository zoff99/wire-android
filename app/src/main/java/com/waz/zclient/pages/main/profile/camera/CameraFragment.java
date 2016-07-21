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

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.waz.api.IConversation;
import com.waz.api.ImageAsset;
import com.waz.api.ImageAssetFactory;
import com.waz.zclient.BuildConfig;
import com.waz.zclient.OnBackPressedListener;
import com.waz.zclient.R;
import com.waz.zclient.controllers.accentcolor.AccentColorObserver;
import com.waz.zclient.controllers.camera.CameraActionObserver;
import com.waz.zclient.controllers.drawing.IDrawingController;
import com.waz.zclient.controllers.orientation.OrientationControllerObserver;
import com.waz.zclient.controllers.permission.RequestPermissionsObserver;
import com.waz.zclient.pages.BaseFragment;
import com.waz.zclient.pages.main.backgroundmain.views.AccentColorInterpolator;
import com.waz.zclient.pages.main.profile.camera.controls.CameraBottomControl;
import com.waz.zclient.pages.main.profile.camera.controls.CameraBottomControlCallback;
import com.waz.zclient.pages.main.profile.camera.controls.CameraTopControl;
import com.waz.zclient.pages.main.profile.camera.controls.CameraTopControlCallback;
import com.waz.zclient.pages.main.profile.camera.manager.CameraDirection;
import com.waz.zclient.pages.main.profile.camera.manager.CameraManager;
import com.waz.zclient.pages.main.profile.camera.manager.CameraManagerCallback;
import com.waz.zclient.pages.main.profile.camera.manager.FlashState;
import com.waz.zclient.ui.animation.interpolators.penner.Expo;
import com.waz.zclient.ui.text.GlyphTextView;
import com.waz.zclient.ui.utils.BitmapUtils;
import com.waz.zclient.ui.utils.ResourceUtils;
import com.waz.zclient.utils.LayoutSpec;
import com.waz.zclient.utils.PermissionUtils;
import com.waz.zclient.utils.SquareOrientation;
import com.waz.zclient.utils.TestingGalleryUtils;
import com.waz.zclient.utils.ViewUtils;
import com.waz.zclient.utils.device.DeviceDetector;
import com.waz.zclient.views.ProgressView;
import com.waz.zclient.views.images.ImageAssetView;

public class CameraFragment extends BaseFragment<CameraFragment.Container> implements CameraManagerCallback,
                                                                                      OrientationControllerObserver,
                                                                                      AccentColorObserver,
                                                                                      CameraPreview.Container,
                                                                                      OnBackPressedListener,
                                                                                      RequestPermissionsObserver {
    // Tags
    public static final String TAG = CameraFragment.class.getName();
    public static final int REQUEST_GALLERY_CODE = 9411;
    public static final long CAMERA_ROTATION_COOLDOWN_DELAY = 1600L;
    private static final String INTENT_GALLERY_TYPE = "image/*";
    private static final String CAMERA_CONTEXT = "CAMERA_CONTEXT";
    private static final String SHOW_GALLERY = "SHOW_GALLERY";
    private static final String SHOW_CAMERA_FEED = "SHOW_CAMERA_FEED";

    private static final String[] CAMERA_PERMISSIONS = new String[] {Manifest.permission.CAMERA};
    private static final int FRONT_CAMERA_PERMISSION_REQUEST_ID = 7;
    private static final int BACK_CAMERA_PERMISSION_REQUEST_ID = 8;

    // views
    private ImageAssetView previewImageView;
    private GlyphTextView sketchButton;
    private boolean cameraFeedIsShown;
    private FrameLayout previewImageViewContainer;
    private ProgressView previewProgressBar;

    // camera

    private CameraManager cameraManager;
    private CameraPreview cameraPreview;
    private FrameLayout previewTargetConversationContainer;
    private TextView previewTargetConversation;
    private boolean pictureFromCamera;
    private ImageAsset imageAsset;
    private CameraTopControl cameraTopControl;
    private CameraBottomControl cameraBottomControl;
    private View vignetteOverlay;
    private View colorOverlay;
    private CameraDirection cameraDirection;
    private static final String STATE_CAMERA_DIRECTION = "STATE_CAMERA_DIRECTION";
    private static final String ALREADY_OPENED_GALLERY = "ALREADY_OPENED_GALLERY";
    private boolean alreadyOpenedGallery;

    private Handler mainHandler = new Handler();
    private FrameLayout cameraContainer;
    private CameraFocusView focusView;

    private enum CameraState {
        IDLE,
        LOADING_CAMERA,
        LOADING_CAMERA_FAILED,
        PROCESSING_PICTURE
    }

    private CameraContext cameraContext = null;
    private CameraState cameraState = CameraState.IDLE;
    private CameraType cameraType;
    private boolean showCameraFeed;

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  LifeCycle
    //
    //////////////////////////////////////////////////////////////////////////////////////////

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
        cameraManager = new CameraManager(this);
        showCameraFeed = getArguments().getBoolean(SHOW_CAMERA_FEED);
        ensureCameraContext();
        cameraType = cameraContext.cameraType;

        final View view = inflater.inflate(R.layout.fragment_camera, c, false);
        cameraTopControl = ViewUtils.getView(view, R.id.ctp);
        cameraBottomControl = ViewUtils.getView(view, R.id.cbc__camera);
        previewImageView = ViewUtils.getView(view, R.id.iv__preview);
        previewImageViewContainer = ViewUtils.getView(view, R.id.fl__preview_container);
        previewProgressBar = ViewUtils.getView(view, R.id.pv__preview);
        previewTargetConversation = ViewUtils.getView(view, R.id.ttv__camera__conversation);
        previewTargetConversationContainer = ViewUtils.getView(view, R.id.ttv__camera__conversation__container);
        if (cameraContext != CameraContext.MESSAGE) {
            previewImageViewContainer.setVisibility(View.GONE);
        }
        vignetteOverlay = ViewUtils.getView(view, R.id.iv__vignette_overlay);
        colorOverlay = ViewUtils.getView(view, R.id.v__color_filter_overlay);
        cameraContainer = ViewUtils.getView(view, R.id.fl__camera__container);
        focusView = ViewUtils.getView(view, R.id.cfv__focus);

        if (savedInstanceState != null) {
            cameraDirection = CameraDirection.getDirection(savedInstanceState.getInt(STATE_CAMERA_DIRECTION));
            alreadyOpenedGallery = savedInstanceState.getBoolean(ALREADY_OPENED_GALLERY);
        } else {
            cameraDirection = CameraDirection.UNKNOWN;
        }

        // dismiss camera if clicked anywhere else
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cameraFeedIsShown) {
                    cameraBottomControlCallback.onDismiss();
                }
            }
        });

        // camera top control
        cameraTopControl.setCameraTopControlCallback(cameraTopControlCallback);
        cameraTopControl.setAlpha(0);
        if (cameraManager.hasNoCameras()) {
            cameraTopControl.setVisibility(View.GONE);
        }

        // camera bottom control
        cameraBottomControl.setCameraBottomControlCallback(cameraBottomControlCallback);
        cameraBottomControl.setMode(cameraContext);
        cameraBottomControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing but consume event
            }
        });
        if (showCameraFeed) {
            cameraBottomControl.setControlState(ControlState.CAMERA);
        }

        // preview image
        previewImageViewContainer.setVisibility(View.GONE);
        previewImageView.setShouldScaleForPortraitMode(cameraContext == CameraContext.MESSAGE);
        sketchButton = ViewUtils.getView(view, R.id.gtv__sketch_image_paint_button);
        sketchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getControllerFactory().getDrawingController().showDrawing(imageAsset,
                                                                          IDrawingController.DrawingDestination.CAMERA_PREVIEW_VIEW);
            }
        });
        if (cameraContext != CameraContext.MESSAGE) {
            sketchButton.setVisibility(View.GONE);
        }

        // progressbar
        previewProgressBar.setVisibility(View.GONE);

        // filtered / unfiltered camera setup
        if (cameraType == CameraType.COLOR_FILTERED) {
            vignetteOverlay.setBackground(new BitmapDrawable(getResources(),
                                                             BitmapUtils.getVignetteBitmap(getResources())));
            vignetteOverlay.setVisibility(View.VISIBLE);
            colorOverlay.setVisibility(View.VISIBLE);
            colorOverlay.setAlpha(ResourceUtils.getResourceFloat(getResources(),
                                                                 R.dimen.background_color_overlay_opacity_overdriven));
            cameraTopControl.setVisibility(View.GONE);
        } else {
            vignetteOverlay.setVisibility(View.GONE);
            colorOverlay.setVisibility(View.GONE);
            cameraTopControl.setVisibility(View.VISIBLE);
        }

        view.setBackgroundResource(R.color.black);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getControllerFactory().getRequestPermissionsController().addObserver(this);
        cameraDirection = getCameraDirection();
        cameraBottomControl.setAccentColor(getControllerFactory().getAccentColorController().getColor());
        colorOverlay.setBackgroundColor(getControllerFactory().getAccentColorController().getColor());

        switch (cameraContext) {
            case SETTINGS:
            case SIGN_UP:
                loadDefaultCamera();
                break;
            case MESSAGE:
                if (DeviceDetector.isNexus7_2012()) {
                    loadBackCamera();
                    cameraTopControl.enableCameraSwitchButtion(false);
                } else if (cameraDirection != null && cameraDirection != CameraDirection.UNKNOWN) {
                    if (cameraDirection == CameraDirection.FRONT_FACING) {
                        loadFrontCamera();
                    } else {
                        loadBackCamera();
                    }
                } else if (cameraManager.hasBackCamera()) {
                    loadBackCamera();
                } else {
                    loadDefaultCamera();
                }
                break;
        }

        getControllerFactory().getAccentColorController().addAccentColorObserver(this);
        if (LayoutSpec.isPhone(getActivity())) {
            getControllerFactory().getOrientationController().addOrientationControllerObserver(this);
        }
        getStoreFactory().getInAppNotificationStore().setUserSendingPicture(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CAMERA_DIRECTION, cameraDirection.id);
        outState.putBoolean(ALREADY_OPENED_GALLERY, alreadyOpenedGallery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        if (cameraPreview != null) {
            cameraPreview.setCamera(null, -1);
        }
        cameraManager.pause();

        getControllerFactory().getRequestPermissionsController().removeObserver(this);
        getControllerFactory().getAccentColorController().removeAccentColorObserver(this);
        getControllerFactory().getOrientationController().removeOrientationControllerObserver(this);
        getStoreFactory().getInAppNotificationStore().setUserSendingPicture(false);

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        cameraManager.tearDown();
        previewImageView = null;
        sketchButton = null;
        previewImageViewContainer = null;
        previewProgressBar = null;
        imageAsset = null;
        previewTargetConversationContainer = null;
        cameraContainer = null;
        focusView = null;
        previewTargetConversation = null;

        cameraTopControl.removeTopControlCallback(cameraTopControlCallback);
        cameraTopControl = null;


        cameraBottomControl.animate()
                           .translationY(getView().getMeasuredHeight())
                           .setDuration(getResources().getInteger(R.integer.calling_animation_duration_medium))
                           .setInterpolator(new Expo.EaseIn());

        cameraBottomControl.removeCameraBottomControlCallback();
        cameraBottomControl = null;

        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {
        getControllerFactory().getCameraController().closeCamera(cameraContext);
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
        return cameraFeedIsShown;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Control camera
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    private void disableCameraButtons() {
        if (getView() == null) {
            return;
        }
        ViewUtils.getView(getView(), R.id.gtv__camera_control__take_a_picture).setVisibility(View.GONE);
        ViewUtils.getView(getView(), R.id.gtv__camera__top_control__back_camera).setVisibility(View.GONE);
        ViewUtils.getView(getView(), R.id.gtv__camera__top_control__flash_light).setVisibility(View.GONE);
    }

    @Override
    public void onCameraLoaded(Camera camera, int cameraId) {
        View view = getView();
        if (view == null) {
            return;
        }

        cameraPreview = new CameraPreview(getActivity(), cameraType, this);
        cameraContainer.removeAllViews();
        cameraContainer.addView(cameraPreview, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                            ViewGroup.LayoutParams.MATCH_PARENT));
        cameraPreview.setCamera(camera, cameraId);
        if (!cameraBottomControl.isCameraState()) {
            cameraPreview.setVisibility(View.GONE);
        }
        cameraState = CameraState.IDLE;
        cameraTopControl.setCamera(camera, cameraId);
        showCameraFeed();

        boolean openGalleryArg = getArguments().getBoolean(SHOW_GALLERY);
        if (!alreadyOpenedGallery && openGalleryArg) {
            alreadyOpenedGallery = true;
            openGallery();
        }
    }

    @Override
    public void onCameraLoadingFailed() {
        cameraState = CameraState.LOADING_CAMERA_FAILED;
        if (getContainer() != null) {
            getControllerFactory().getCameraController().onCameraNotAvailable(cameraContext);
        }
        disableCameraButtons();
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

    private void loadDefaultCamera() {
        boolean hasFrontCamera = cameraManager.hasFrontCamera();
        boolean hasBackCamera = cameraManager.hasBackCamera();
        if (hasFrontCamera && cameraDirection == CameraDirection.FRONT_FACING) {
            loadFrontCamera();
            cameraTopControl.enableCameraSwitchButtion(hasBackCamera);
        } else if (hasBackCamera && cameraDirection == CameraDirection.BACK_FACING) {
            loadBackCamera();
            cameraTopControl.enableCameraSwitchButtion(hasFrontCamera);
        } else if (DeviceDetector.isNexus7_2012()) {
            loadBackCamera();
            cameraTopControl.enableCameraSwitchButtion(false);
        } else {
            getControllerFactory().getCameraController().onCameraNotAvailable(cameraContext);
            disableCameraButtons();
        }
    }

    private void loadFrontCamera() {
        if (DeviceDetector.isNexus7_2012()) {
            loadBackCamera();
        } else {
            if (PermissionUtils.hasSelfPermissions(getActivity(), CAMERA_PERMISSIONS)) {
                loadCamera(CameraDirection.FRONT_FACING);
            } else {
                ActivityCompat.requestPermissions(getActivity(), CAMERA_PERMISSIONS, FRONT_CAMERA_PERMISSION_REQUEST_ID);
            }
        }
    }

    private void loadBackCamera() {
        if (PermissionUtils.hasSelfPermissions(getActivity(), CAMERA_PERMISSIONS)) {
            loadCamera(CameraDirection.BACK_FACING);
        } else {
            ActivityCompat.requestPermissions(getActivity(), CAMERA_PERMISSIONS, BACK_CAMERA_PERMISSION_REQUEST_ID);
        }
    }

    private void loadCamera(CameraDirection cameraDirection) {
        if (cameraState == CameraState.LOADING_CAMERA) {
            return;
        }
        this.cameraDirection = cameraDirection;
        getControllerFactory().getUserPreferencesController().setRecentCameraDirection(cameraDirection);
        cameraState = CameraState.LOADING_CAMERA;
        if (cameraPreview != null) {
            cameraPreview.setCamera(null, cameraDirection.id);
        }

        cameraManager.loadCamera(cameraDirection.id);
    }

    @NonNull
    private CameraDirection getCameraDirection() {
        if (DeviceDetector.isNexus7_2012()) {
            return CameraDirection.BACK_FACING;
        }
        CameraDirection savedCameraDirection = CameraDirection.UNKNOWN;
        if (cameraDirection == CameraDirection.UNKNOWN) {
            savedCameraDirection = getControllerFactory().getUserPreferencesController().getRecentCameraDirection();
        }

        if (savedCameraDirection == CameraDirection.FRONT_FACING &&
            cameraManager.hasFrontCamera()) {
            return CameraDirection.FRONT_FACING;
        }

        if (cameraManager.hasBackCamera()) {
            return CameraDirection.BACK_FACING;
        }
        return CameraDirection.UNKNOWN;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  control state actions
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * callback of top controller - select camera
     */
    private final CameraTopControlCallback cameraTopControlCallback = new CameraTopControlCallback() {
        @Override
        public void loadCamera(CameraDirection cameraDirection) {
            if (cameraDirection == CameraDirection.FRONT_FACING) {
                loadFrontCamera();
            } else if (cameraDirection == CameraDirection.BACK_FACING) {
                loadBackCamera();
            }
        }

        @Override
        public FlashState getSavedFlashState() {
            return FlashState.get(getControllerFactory().getUserPreferencesController().getSavedFlashState());
        }

        @Override
        public void setSavedFlashState(FlashState state) {
            getControllerFactory().getUserPreferencesController().setSavedFlashState(state.mode);
        }
    };

    /**
     * Callback of bottom controller
     */
    private final CameraBottomControlCallback cameraBottomControlCallback = new CameraBottomControlCallback() {
        @Override
        public boolean onOpenCamera() {
            if (getStoreFactory().getNetworkStore().hasInternetConnection()) {
                showCameraFeed();
                return true;
            }
            showNoNetworkError();
            return false;
        }

        @Override
        public void onDismiss() {
            if (cameraContext == CameraContext.SIGN_UP && cameraType == CameraType.NORMAL) {
                cameraType = CameraType.COLOR_FILTERED;
                loadDefaultCamera();
            } else {
                getControllerFactory().getCameraController().closeCamera(cameraContext);
            }
        }

        @Override
        public void onTakePhoto() {
            if (cameraContext != CameraContext.SIGN_UP) {
                previewProgressBar.setVisibility(View.VISIBLE);
            }
            pictureFromCamera = true;
            cameraState = CameraState.PROCESSING_PICTURE;

            ObjectAnimator.ofFloat(cameraTopControl,
                                   View.ALPHA,
                                   1,
                                   0).setDuration(getResources().getInteger(R.integer.camera__control__ainmation__duration)).start();

            cameraPreview.shoot(new OnCameraBitmapLoadedListener() {
                @Override
                public void onCameraBitmapLoadedListener(ImageAsset imageAsset,
                                                         SquareOrientation squareOrientation) {
                    CameraFragment.this.imageAsset = imageAsset;
                    // TODO: Remove the workaround once issue https://wearezeta.atlassian.net/browse/AN-4223 fixed or Automation framework find a workaround
                    if (BuildConfig.IS_TEST_GALLERY_ALLOWED &&
                        LayoutSpec.isTablet(getActivity()) &&
                        TestingGalleryUtils.isCustomGalleryInstalled(getActivity().getPackageManager())) {
                        getControllerFactory().getCameraController().onBitmapSelected(imageAsset,
                                                                                      pictureFromCamera,
                                                                                      cameraContext);
                    } else {
                        previewImageViewContainer.setVisibility(View.VISIBLE);
                        showPreview(imageAsset, true);
                        cameraState = CameraState.IDLE;
                        previewProgressBar.setVisibility(View.GONE);
                    }
                }
            });
        }

        @Override
        public void onOpenImageGallery() {
            openGallery();
        }

        @Override
        public void onApproveDeletion(boolean approved) {
            if (approved) {
                getControllerFactory().getCameraController().onDeleteImage(cameraContext);
            }
        }

        @Override
        public void onApproveSelection(boolean approved) {
            if (cameraState != CameraState.IDLE && pictureFromCamera) {
                return;
            }

            final Activity activity = getActivity();
            if (activity != null &&
                LayoutSpec.isTablet(activity)) {
                ViewUtils.unlockOrientation(activity);
            }

            if (approved) {
                getControllerFactory().getCameraController().onBitmapSelected(imageAsset, pictureFromCamera, cameraContext);

                // hack to avoid layout pass after setting the image
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (cameraPreview != null) {
                            cameraPreview.setVisibility(View.GONE);
                        }
                    }
                }, 0);
            } else {
                if (pictureFromCamera) {
                    dismissPreview();
                    if (cameraPreview != null) {
                        cameraPreview.startPreviewAgain();
                    }
                    cameraBottomControl.setControlState(ControlState.CAMERA);
                } else {
                    dismissPreview();
                    cameraBottomControl.setControlState(ControlState.CAMERA);
                }
            }
        }
    };

    private void showPreview(ImageAsset imageAsset, boolean bitmapFromCamera) {
        if (LayoutSpec.isTablet(getActivity())) {
            ViewUtils.lockCurrentOrientation(getActivity(),
                                             getControllerFactory().getOrientationController().getLastKnownOrientation());
        }

        pictureFromCamera = bitmapFromCamera;

        if (bitmapFromCamera) {
            cameraPreview.setVisibility(View.GONE);
        }

        int duration = getResources().getInteger(R.integer.camera__preview__ainmation__duration);
        previewProgressBar.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cameraBottomControl != null) {
                    cameraBottomControl.setControlState(ControlState.DIALOG_APPROVE_SELECTION);
                }
            }
        }, duration);

        previewImageView.setImageAsset(imageAsset);
        final IConversation currentConversation = getStoreFactory().getConversationStore().getCurrentConversation();
        if (currentConversation != null && cameraContext == CameraContext.MESSAGE) {
            previewTargetConversation.setText(currentConversation.getName());
            previewTargetConversationContainer.setVisibility(View.VISIBLE);
        } else {
            previewTargetConversationContainer.setVisibility(View.GONE);
        }

        ObjectAnimator.ofFloat(previewImageView, View.ALPHA, 0, 1).setDuration(duration).start();
        cameraState = CameraState.IDLE;
    }

    private void dismissPreview() {
        previewProgressBar.setVisibility(View.GONE);
        int animationDuration = getResources().getInteger(R.integer.camera__control__ainmation__duration);
        ObjectAnimator.ofFloat(previewImageView, View.ALPHA, 1, 0).setDuration(animationDuration).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (previewImageViewContainer != null && previewImageView != null) {
                    previewImageViewContainer.setVisibility(View.GONE);
                    previewImageView.setImageDrawable(null);
                }
            }
        }, animationDuration);
        showCameraFeed();
    }


    private void showCameraFeed() {
        cameraFeedIsShown = true;
        if (cameraState == CameraState.LOADING_CAMERA_FAILED) {
            loadDefaultCamera();
        } else if (cameraPreview != null) {
            cameraPreview.setVisibility(View.VISIBLE);
        }

        if (cameraType == CameraType.NORMAL) {
            ViewUtils.fadeInView(cameraTopControl,
                                 getResources().getInteger(R.integer.camera__control__ainmation__duration));
        } else {
            ViewUtils.fadeOutView(cameraTopControl,
                                  getResources().getInteger(R.integer.camera__control__ainmation__duration));
        }
    }

    private void hideCameraFeed() {
        cameraFeedIsShown = false;

        ObjectAnimator.ofFloat(cameraTopControl,
                               View.ALPHA,
                               1,
                               0).setDuration(getResources().getInteger(R.integer.camera__control__ainmation__duration)).start();

        if (cameraPreview != null) {
            cameraPreview.setVisibility(View.GONE);
        }
    }

    private void showNoNetworkError() {
        ViewUtils.showAlertDialog(getActivity(),
                                  R.string.alert_dialog__no_network__header,
                                  R.string.profile_pic__no_network__message,
                                  R.string.alert_dialog__confirmation,
                                  null, true);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Notifications
    //
    //////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onOrientationHasChanged(SquareOrientation squareOrientation) {
        cameraTopControl.setConfigOrientation(squareOrientation);
        cameraBottomControl.setConfigOrientation(squareOrientation);
    }

    @Override
    public void onAccentColorHasChanged(Object sender, int color) {
        colorOverlay.setBackgroundColor(color);
        float accentColorOpacity = ResourceUtils.getResourceFloat(getResources(),
                                                                  R.dimen.background_color_overlay_opacity);
        float accentColorOpacityOverdriven = ResourceUtils.getResourceFloat(getResources(),
                                                                            R.dimen.background_color_overlay_opacity_overdriven);
        ObjectAnimator anim = ObjectAnimator.ofFloat(colorOverlay,
                                                     View.ALPHA,
                                                     0,
                                                     accentColorOpacityOverdriven,
                                                     accentColorOpacityOverdriven,
                                                     accentColorOpacityOverdriven / 2,
                                                     accentColorOpacity);
        anim.setInterpolator(new AccentColorInterpolator());
        anim.setDuration(getResources().getInteger(R.integer.background_accent_color_transition_animation_duration));
        anim.start();
        cameraBottomControl.setAccentColor(color);
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
        cameraState = CameraState.PROCESSING_PICTURE;
        imageAsset = null;
        hideCameraFeed();
        previewImageViewContainer.setVisibility(View.VISIBLE);
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

    @Override
    public void onFullColorPreviewStarted() {
        if (getContainer() == null || getActivity() == null) {
            return;
        }
        ViewUtils.fadeOutView(vignetteOverlay,
                              getResources().getInteger(R.integer.camera__preview_lens_transition__duration),
                              getResources().getInteger(R.integer.camera__preview_lens_transition__delay));
        ViewUtils.fadeOutView(colorOverlay,
                              getResources().getInteger(R.integer.camera__preview_lens_transition__duration),
                              getResources().getInteger(R.integer.camera__preview_lens_transition__delay));
        getControllerFactory().getCameraController().onCameraTypeChanged(cameraType, cameraContext);
    }

    @Override
    public void onFilteredPreviewStarted() {
        if (getContainer() == null || getActivity() == null) {
            return;
        }
        ViewUtils.fadeInView(vignetteOverlay,
                             getResources().getInteger(R.integer.camera__preview_lens_transition__duration));
        ViewUtils.fadeInView(colorOverlay,
                             ResourceUtils.getResourceFloat(getResources(),
                                                            R.dimen.background_color_overlay_opacity_overdriven),
                             getResources().getInteger(R.integer.camera__preview_lens_transition__duration),
                             0);
        getControllerFactory().getCameraController().onCameraTypeChanged(cameraType, cameraContext);
    }

    @Override
    public void onFilteredPreviewClicked() {
        cameraType = CameraType.NORMAL;
        loadDefaultCamera();
    }

    @Override
    public void onFocus(Rect focusArea) {
        focusView.setColor(getControllerFactory().getAccentColorController().getColor());
        int x = focusArea.centerX();
        int y = focusArea.centerY();
        focusView.setX(x - focusView.getWidth() / 2);
        focusView.setY(y - focusView.getHeight() / 2);
        focusView.showFocusView();
    }

    @Override
    public void onFocusComplete() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (focusView != null) {
                    focusView.hideFocusView();
                }
            }
        });
    }

    @Override
    public void onPictureTaken() {
        getStoreFactory().getMediaStore().playSound(R.raw.camera);
        getControllerFactory().getVibratorController().vibrate(R.array.camera);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        switch (requestCode) {
            case BACK_CAMERA_PERMISSION_REQUEST_ID:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    loadCamera(CameraDirection.BACK_FACING);
                } else {
                    onCameraLoadingFailed();
                }
                break;
            case FRONT_CAMERA_PERMISSION_REQUEST_ID:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    loadCamera(CameraDirection.FRONT_FACING);
                } else {
                    onCameraLoadingFailed();
                }
                break;
            default:
                break;
        }
    }

    public interface Container extends CameraActionObserver {
    }
}
