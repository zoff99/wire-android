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
package com.waz.zclient.pages.extendedcursor.image;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.waz.api.ImageAsset;
import com.waz.zclient.R;
import com.waz.zclient.pages.main.profile.views.ConfirmationMenu;
import com.waz.zclient.pages.main.profile.views.ConfirmationMenuListener;
import com.waz.zclient.ui.theme.OptionsDarkTheme;
import com.waz.zclient.ui.utils.ColorUtils;
import com.waz.zclient.utils.ViewUtils;
import com.waz.zclient.views.images.ImageAssetView;


public class CursorImagesPreviewLayout extends FrameLayout implements
                                                            ConfirmationMenuListener,
                                                            View.OnClickListener {

    public enum Source {
        IN_APP_GALLERY,
        DEVICE_GALLERY,
        CAMERA;
    }

    private ConfirmationMenu approveImageSelectionMenu;
    private ImageAssetView imageView;
    private FrameLayout conversationNameViewContainer;
    private TextView conversationNameView;
    private View sketchButton;
    private boolean sketchShouldBeVisible;

    private Callback callback;
    private ImageAsset imageAsset;
    private Source source;

    public CursorImagesPreviewLayout(Context context) {
        this(context, null);
    }

    public CursorImagesPreviewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CursorImagesPreviewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // eats the click
        setOnClickListener(this);
        imageView = ViewUtils.getView(this, R.id.iv__conversation__preview);
        approveImageSelectionMenu = ViewUtils.getView(this, R.id.cm__cursor_preview);
        sketchButton = ViewUtils.getView(this, R.id.ll__preview__sketch);
        conversationNameView = ViewUtils.getView(this, R.id.ttv__camera__conversation);
        conversationNameViewContainer = ViewUtils.getView(this, R.id.ttv__camera__conversation__container);

        imageView.setOnClickListener(this);

        approveImageSelectionMenu.setWireTheme(new OptionsDarkTheme(getContext()));
        approveImageSelectionMenu.setCancel(getResources().getString(R.string.confirmation_menu__cancel));
        approveImageSelectionMenu.setConfirm(getResources().getString(R.string.confirmation_menu__confirm_done));
        approveImageSelectionMenu.setConfirmationMenuListener(this);

        sketchButton.setOnClickListener(this);
        // By default sketch button is visible
        sketchShouldBeVisible = true;
    }

    @Override
    public void confirm() {
        callback.onSendPictureFromPreview(imageAsset, source);
    }

    @Override
    public void cancel() {
        callback.onCancelPreview();
    }

    public void setImageAsset(final ImageAsset imageAsset,
                              Source source,
                              Callback callback,
                              int color,
                              String conversationName) {
        this.source = source;
        this.imageAsset = imageAsset;
        this.callback = callback;
        approveImageSelectionMenu.setAccentColor(color);

        imageView.setImageAsset(imageAsset);

        sketchButton.setBackground(ColorUtils.getButtonBackground(Color.TRANSPARENT,
                                                                  color,
                                                                  0,
                                                                  getResources().getDimensionPixelSize(R.dimen.camera__sketch_button__corner_radius)));

        conversationNameView.setText(conversationName);
        conversationNameViewContainer.setVisibility(TextUtils.isEmpty(conversationNameView.getText()) ? GONE : VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv__conversation__preview:
                if (approveImageSelectionMenu.getVisibility() == VISIBLE) {
                    if (sketchShouldBeVisible) {
                        ViewUtils.fadeOutView(sketchButton);
                    }
                    ViewUtils.fadeOutView(approveImageSelectionMenu);

                    if (!TextUtils.isEmpty(conversationNameView.getText())) {
                        ViewUtils.fadeOutView(conversationNameViewContainer);
                    }
                } else {
                    if (sketchShouldBeVisible) {
                        ViewUtils.fadeInView(sketchButton);
                    }
                    ViewUtils.fadeInView(approveImageSelectionMenu);

                    if (!TextUtils.isEmpty(conversationNameView.getText())) {
                        ViewUtils.fadeInView(conversationNameViewContainer);
                    }
                }
                break;
            case R.id.ll__preview__sketch:
                if (callback != null) {
                    callback.onSketchPictureFromPreview(imageAsset, source);
                }
                break;
        }
    }

    public void showSketch(boolean show) {
        sketchShouldBeVisible = show;
        sketchButton.setVisibility(show ? VISIBLE : GONE);
    }

    public interface Callback {
        void onCancelPreview();

        void onSketchPictureFromPreview(ImageAsset imageAsset, Source source);

        void onSendPictureFromPreview(ImageAsset imageAsset, Source source);
    }

}
