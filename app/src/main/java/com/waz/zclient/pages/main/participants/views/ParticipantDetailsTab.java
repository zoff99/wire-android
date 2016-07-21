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
package com.waz.zclient.pages.main.participants.views;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.waz.api.ImageAsset;
import com.waz.api.User;
import com.waz.zclient.R;
import com.waz.zclient.utils.ViewUtils;
import com.waz.zclient.views.images.ImageAssetImageView;
import com.waz.zclient.views.menus.FooterMenu;
import com.waz.zclient.views.menus.FooterMenuCallback;

public class ParticipantDetailsTab extends LinearLayout {

    private ImageAssetImageView imageAssetImageView;
    private FooterMenu footerMenu;

    public ParticipantDetailsTab(Context context) {
        this(context, null);
    }

    public ParticipantDetailsTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParticipantDetailsTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.single_participant_tab_details, this, true);

        imageAssetImageView = ViewUtils.getView(this, R.id.iaiv__single_participant);
        imageAssetImageView.setDisplayType(ImageAssetImageView.DisplayType.CIRCLE);
        footerMenu = ViewUtils.getView(this, R.id.fm__footer);

        setOrientation(VERTICAL);
    }

    public void setUser(User user) {
        ImageAsset imageAsset = user != null ? user.getPicture() : null;
        if (imageAsset == null) {
            imageAssetImageView.resetBackground();
        } else {
            imageAssetImageView.connectImageAsset(imageAsset);
        }
    }

    public void updateFooterMenu(@StringRes int leftAction, @StringRes int leftActionLabel, @StringRes int rightAction,
                                 @StringRes int rightActionLabel, FooterMenuCallback callback) {
        if (footerMenu == null) {
            return;
        }
        footerMenu.setLeftActionText(getContext().getString(leftAction));
        footerMenu.setLeftActionLabelText(getContext().getString(leftActionLabel));
        footerMenu.setRightActionText(getContext().getString(rightAction));
        footerMenu.setRightActionLabelText(getContext().getString(rightActionLabel));
        footerMenu.setCallback(callback);
    }
}
