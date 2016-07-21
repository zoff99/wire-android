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
package com.waz.zclient.views.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.waz.api.ImageAsset;
import com.waz.api.LoadHandle;
import com.waz.api.UpdateListener;
import com.waz.zclient.utils.ViewUtils;
import timber.log.Timber;

public class ImageAssetView extends ImageView implements UpdateListener {

    private BitmapLoadedCallback bitmapLoadedCallback;
    private LoadHandle bitmapLoadHandle;
    private boolean shouldScaleForPortraitMode;
    private ImageAsset imageAsset;
    private boolean showPreview;

    public ImageAssetView(Context context) {
        super(context);
    }

    public ImageAssetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageAssetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageAsset(@Nullable final ImageAsset imageAsset) {
        if (imageAsset == null) {
            unbindImageAsset();
            return;
        }

        setImageAsset(imageAsset, false);
    }

    private void setImageAsset(final ImageAsset imageAsset, final boolean blurred) {
        cancelPreviousBitmapLoad();
        bindImageAsset(imageAsset, blurred);
        setMirroring();
        loadBitmap();
    }

    public void setShouldScaleForPortraitMode(boolean shouldScaleForPortraitMode) {
        this.shouldScaleForPortraitMode = shouldScaleForPortraitMode;
    }

    public void setShowPreview(boolean showPreview) {
        this.showPreview = showPreview;
    }

    public void clearImage() {
        cancelPreviousBitmapLoad();
        super.setImageDrawable(null);
    }

    public void setBitmapLoadedCallback(BitmapLoadedCallback bitmapLoadedCallback) {
        this.bitmapLoadedCallback = bitmapLoadedCallback;
    }

    @Override
    public void updated() {
        loadBitmap();
    }

    @Override
    protected void onDetachedFromWindow() {
        unbindImageAsset();
        super.onDetachedFromWindow();
    }

    private void bindImageAsset(ImageAsset imageAsset, boolean blurred) {
        Timber.i("Binding ImageAssetView %s to ImageAsset %s", this, imageAsset.getId());
        this.imageAsset = imageAsset;
        this.imageAsset.addUpdateListener(this);
    }

    private void unbindImageAsset() {
        cancelPreviousBitmapLoad();
        if (imageAsset != null) {
            Timber.i("Unbinding ImageAssetView %s from ImageAsset %s", this, imageAsset.getId());
            imageAsset.removeUpdateListener(this);
            imageAsset = null;
        }
    }

    private void loadBitmap() {
        if (imageAsset.isEmpty()) {
            Timber.i("ImageAssetView %s :: loadBitmap() empty ImageAsset", this);
            if (bitmapLoadedCallback != null) {
                bitmapLoadedCallback.onBitmapLoadFinished();
            }
            return;
        }
        Timber.i("ImageAssetView %s :: loadBitmap(), id=%s", this, imageAsset.getId());

        final ImageAsset.BitmapCallback callback = new ImageAsset.BitmapCallback() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, boolean isPreview) {
                Timber.i("ImageAssetView %s :: onBitmapLoaded(), id=%s preview=%b", ImageAssetView.this, imageAsset.getId(), isPreview);
                if (isPreview && !showPreview) {
                    return;
                }
                setScalePolicy(bitmap);
                setImageBitmap(bitmap);
                if (bitmapLoadedCallback != null && !isPreview) {
                    bitmapLoadedCallback.onBitmapLoadFinished();
                }
            }

            @Override
            public void onBitmapLoadingFailed() {
                logBitmapLoadError();
                clearImage();
                if (bitmapLoadedCallback != null) {
                    bitmapLoadedCallback.onBitmapLoadFinished();
                }
            }
        };

        bitmapLoadHandle = imageAsset.getBitmap(ViewUtils.getOrientationIndependentDisplayWidth(getContext()), callback);
    }

    private void setMirroring() {
        if (imageAsset.mirrored()) {
            setScaleX(-1f);
        } else {
            setScaleX(1f);
        }
    }

    private void setScalePolicy(Bitmap bitmap) {
        if (shouldScaleForPortraitMode) {
            if (bitmap.getWidth() > bitmap.getHeight()) {
                setScaleType(ScaleType.FIT_CENTER);
            } else {
                setScaleType(ScaleType.CENTER_CROP);
            }
        }
    }

    private void logBitmapLoadError() {
        Timber.e("ImageAssetView %s :: failed loading bitmap for %s id=%s", this, imageAsset, imageAsset != null ? imageAsset.getId() : null);
    }

    private void cancelPreviousBitmapLoad() {
        if (bitmapLoadHandle != null) {
            bitmapLoadHandle.cancel();
        }
    }

    public interface BitmapLoadedCallback {
        void onBitmapLoadFinished();
    }
}
