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
package com.waz.zclient.pages.main.conversation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import com.waz.zclient.BuildConfig;
import com.waz.zclient.utils.PermissionUtils;
import com.waz.zclient.utils.TestingGalleryUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AssetIntentsManager {
    private static final String SAVED_STATE_PENDING_URI = "SAVED_STATE_PENDING_URI";

    private static final String[] CAMERA_PERMISSIONS = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final String INTENT_GALLERY_TYPE = "image/*";
    private static final String INTENT_VIDEO_TYPE = "video/*";

    @TargetApi(19)
    private static String openDocumentAction() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT;
    }

    private Callback callback;
    private Uri pendingFileUri;
    private boolean shouldUseCustomGallery;

    public AssetIntentsManager(Activity activity, Callback callback, Bundle savedInstanceState) {
        shouldUseCustomGallery = BuildConfig.IS_TEST_GALLERY_ALLOWED && TestingGalleryUtils.isCustomGalleryInstalled(
            activity.getPackageManager());
        setCallback(callback);

        if (savedInstanceState != null) {
            pendingFileUri = savedInstanceState.getParcelable(SAVED_STATE_PENDING_URI);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void maybeOpenFileSharing() {
        Intent intent = new Intent();
        if (shouldUseCustomGallery) {
            intent = new Intent("com.wire.testing.GET_DOCUMENT");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
        } else {
            intent.setAction(openDocumentAction());
        }
        intent.setType("*/*");
        callback.openIntent(intent, IntentType.FILE_SHARING);
    }

    private void openCamera() {
        Intent intent;
        if (shouldUseCustomGallery) {
            intent = new Intent("com.wire.testing.GET_PICTURE");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setType(INTENT_GALLERY_TYPE);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pendingFileUri = getOutputMediaFileUri(IntentType.CAMERA);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pendingFileUri);
        }

        callback.openIntent(intent, IntentType.CAMERA);
    }

    public void maybeOpenVideo(Activity activity, IntentType type) {
        if (PermissionUtils.hasSelfPermissions(activity, CAMERA_PERMISSIONS)) {
            openVideo(type);
        } else {
            ActivityCompat.requestPermissions(activity, CAMERA_PERMISSIONS, type.permissionCode);
        }
    }


    private void openVideo(IntentType type) {
        Intent intent;
        if (shouldUseCustomGallery) {
            intent = new Intent("com.wire.testing.GET_VIDEO");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setType(INTENT_VIDEO_TYPE);
        } else {
            intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            pendingFileUri = getOutputMediaFileUri(IntentType.VIDEO);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pendingFileUri);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            }
        }
        callback.openIntent(intent, type);
    }


    public void openGallery(FragmentActivity activity) {
        Intent intent;
        if (BuildConfig.IS_TEST_GALLERY_ALLOWED &&
            TestingGalleryUtils.isCustomGalleryInstalled(activity.getPackageManager())) {
            intent = new Intent("com.wire.testing.GET_PICTURE");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setType(INTENT_GALLERY_TYPE);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(INTENT_GALLERY_TYPE);
        }
        callback.openIntent(intent, IntentType.GALLERY);
    }


    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if (callback == null) {
            throw new IllegalStateException("A callback must be set!");
        }

        IntentType type = IntentType.get(requestCode);

        if (type == IntentType.UNKOWN) {
            return false;
        }

        if (resultCode == Activity.RESULT_CANCELED) {
            callback.onCanceled(type);
            return true;
        }

        if (resultCode != Activity.RESULT_OK) {
            callback.onFailed(type);
            return true;
        }

        if (data == null || TextUtils.isEmpty(data.getDataString())) {
            if ((type == IntentType.CAMERA || type == IntentType.VIDEO || type == IntentType.VIDEO_CURSOR_BUTTON) && pendingFileUri != null) {
                callback.onDataReceived(type, pendingFileUri);
                pendingFileUri = null;
            } else {
                callback.onFailed(type);
            }
        } else {
            Uri uri;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                uri = Uri.parse(data.getDataString());
            } else {
                uri = data.getData();
            }
            if (uri == null) {
                callback.onFailed(type);
            } else {
                callback.onDataReceived(type, uri);
            }
        }

        return true;
    }

    /**
     * Create a file Uri for saving an image or video
     *
     * @param type
     */
    private static Uri getOutputMediaFileUri(IntentType type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     *
     * @param type
     */
    private static File getOutputMediaFile(IntentType type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                        "WIRE_MEDIA");
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            return null;
        }

        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(date.getTime());

        switch (type) {
            case VIDEO_CURSOR_BUTTON:
            case VIDEO:
                return new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
            case CAMERA:
                return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        }
        return null;
    }

    public boolean onRequestPermissionsResult(int requestCode, int[] grantResults) {
        IntentType type = IntentType.getByPermissionCode(requestCode);

        if (type == IntentType.UNKOWN) {
            return false;
        }

        if (!PermissionUtils.verifyPermissions(grantResults)) {
            callback.onPermissionFailed(type);
            return true;
        }

        switch (type) {
            case GALLERY:
                return true;
            case VIDEO_CURSOR_BUTTON:
            case VIDEO:
                openVideo(type);
                return true;
            case CAMERA:
                openCamera();
                return true;
            default:
                return false;
        }
    }


    public enum IntentType {
        UNKOWN(-1, -1),
        GALLERY(9411, 8411),
        VIDEO(9412, 8412),
        VIDEO_CURSOR_BUTTON(9415, 8415),
        CAMERA(9413, 8413),
        FILE_SHARING(9414, 8414);

        public int requestCode;
        private int permissionCode;

        IntentType(int requestCode, int permissionCode) {
            this.requestCode = requestCode;
            this.permissionCode = permissionCode;
        }

        public static IntentType get(int requestCode) {

            if (requestCode == GALLERY.requestCode) {
                return GALLERY;
            }

            if (requestCode == CAMERA.requestCode) {
                return CAMERA;
            }

            if (requestCode == VIDEO.requestCode) {
                return VIDEO;
            }

            if (requestCode == VIDEO_CURSOR_BUTTON.requestCode) {
                return VIDEO_CURSOR_BUTTON;
            }

            if (requestCode == FILE_SHARING.requestCode) {
                return FILE_SHARING;
            }

            return UNKOWN;
        }


        public static IntentType getByPermissionCode(int permissionCode) {

            if (permissionCode == GALLERY.permissionCode) {
                return GALLERY;
            }

            if (permissionCode == CAMERA.permissionCode) {
                return CAMERA;
            }

            if (permissionCode == VIDEO.permissionCode) {
                return VIDEO;
            }

            if (permissionCode == VIDEO_CURSOR_BUTTON.permissionCode) {
                return VIDEO_CURSOR_BUTTON;
            }

            if (permissionCode == FILE_SHARING.permissionCode) {
                return FILE_SHARING;
            }

            return UNKOWN;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (pendingFileUri != null) {
            outState.putParcelable(SAVED_STATE_PENDING_URI, pendingFileUri);
        }
    }

    public interface Callback {
        void onDataReceived(IntentType type, Uri uri);

        void onCanceled(IntentType type);

        void onFailed(IntentType type);

        void openIntent(Intent intent, AssetIntentsManager.IntentType intentType);

        void onPermissionFailed(IntentType type);
    }
}
