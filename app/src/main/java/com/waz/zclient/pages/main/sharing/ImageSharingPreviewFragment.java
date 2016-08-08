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
package com.waz.zclient.pages.main.sharing;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.waz.api.IConversation;
import com.waz.api.ImageAsset;
import com.waz.api.ImageAssetFactory;
import com.waz.zclient.R;
import com.waz.zclient.controllers.accentcolor.AccentColorObserver;
import com.waz.zclient.controllers.sharing.SharedContentType;
import com.waz.zclient.pages.BaseFragment;
import com.waz.zclient.ui.utils.TextViewUtils;
import com.waz.zclient.ui.views.ZetaButton;
import com.waz.zclient.utils.TrackingUtils;
import com.waz.zclient.utils.ViewUtils;
import com.waz.zclient.views.images.ImageAssetView;

import java.util.List;

public class ImageSharingPreviewFragment extends BaseFragment<ImageSharingPreviewFragment.Container> implements AccentColorObserver {

    public static final String TAG = ImageSharingPreviewFragment.class.getName();


    private ImageAssetView previewImageAssetView;
    private ZetaButton confirmButton;
    private ZetaButton cancelButton;
    private TextView previewTitle;


    public static ImageSharingPreviewFragment newInstance() {
        return new ImageSharingPreviewFragment();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  LifeCycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sharing, container, false);
        previewImageAssetView = ViewUtils.getView(view, R.id.iv__image_sharing__preview);
        previewTitle = ViewUtils.getView(view, R.id.ttv__image_sharing__title);

        confirmButton = ViewUtils.getView(view, R.id.positive);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmShareImages();
            }
        });

        cancelButton = ViewUtils.getView(view, R.id.negative);
        cancelButton.setIsFilled(false);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDetached()) {
                    return;
                }
                getActivity().finish();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showShareImagePreview();
    }

    @Override
    public void onStart() {
        super.onStart();
        getControllerFactory().getAccentColorController().addAccentColorObserver(this);
    }

    @Override
    public void onStop() {
        getControllerFactory().getAccentColorController().removeAccentColorObserver(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        previewImageAssetView = null;
        previewTitle = null;
        confirmButton = null;
        cancelButton = null;
        super.onDestroyView();
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  AccentColorObserver
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAccentColorHasChanged(Object sender, int color) {
        confirmButton.setAccentColor(color);
        cancelButton.setAccentColor(color);
        cancelButton.setTextColor(color);
    }

    private void showShareImagePreview() {
        SharedContentType sharedContentType = getControllerFactory().getSharingController().getSharedContentType();
        if (sharedContentType == null) {
            return;
        }

        String title = "";
        IConversation currentConversation = getControllerFactory().getSharingController().getDestination();
        List<Uri> sharedImageUris = getControllerFactory().getSharingController().getSharedFileUris();
        Uri previewImageUri = sharedImageUris.get(0);
        switch (sharedContentType) {
            case IMAGE:
                title = String.format(getString(R.string.sharing__image_preview__title__single),
                                      currentConversation.getName().toUpperCase(
                                          getResources().getConfiguration().locale));
                break;
        }
        previewTitle.setText(title);
        TextViewUtils.highlightAndBoldText(previewTitle,
                                           getResources().getColor(R.color.sharing__image_preview__title__color));

        ImageAsset imageAsset = ImageAssetFactory.getImageAsset(previewImageUri);
        previewImageAssetView.setImageAsset(imageAsset);
    }

    private void confirmShareImages() {
        SharedContentType sharedContentType = getControllerFactory().getSharingController().getSharedContentType();
        if (sharedContentType == null) {
            return;
        }

        IConversation destination = getControllerFactory().getSharingController().getDestination();
        List<Uri> sharedImageUris = getControllerFactory().getSharingController().getSharedFileUris();
        switch (sharedContentType) {
            case IMAGE:
                getStoreFactory().getConversationStore().sendMessage(destination, ImageAssetFactory.getImageAsset(sharedImageUris.get(0)));
                TrackingUtils.onSentPhotoMessageFromSharing(getControllerFactory().getTrackingController(),
                                                            destination);
                break;
        }
        getControllerFactory().getSharingController().onContentShared(getActivity(), destination);
        getActivity().finish();
    }

    public interface Container {
    }
}
