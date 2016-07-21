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
package com.waz.zclient.pages.main.conversation.views;

import android.graphics.Color;
import com.waz.api.AccentColor;
import com.waz.api.Asset;
import com.waz.api.Message;
import com.waz.api.ProgressIndicator;
import com.waz.api.User;
import com.waz.zclient.BaseActivity;
import com.waz.zclient.TestActivity;
import com.waz.zclient.testutils.MockHelper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ViewControllerMockHelper {

    public static void setupMessageMock(Message message, User user, Asset asset, TestActivity activity) {
        setupMessageMock(message, user, asset, null, activity);
    }

    public static void setupMessageMock(Message message, User user, Asset asset, ProgressIndicator progressIndicator, TestActivity activity) {
        AccentColor accentColor = mock(AccentColor.class);
        when(accentColor.getColor()).thenReturn(Color.GREEN);
        when(message.getAsset()).thenReturn(asset);
        when(message.getUser()).thenReturn(user);
        when(user.getAccent()).thenReturn(accentColor);
        MockHelper.setupObservableMocks(asset, activity);
        MockHelper.setupObservableMocks(user, activity);
        MockHelper.setupObservableMocks(message, activity);

        if (progressIndicator != null) {
            when(asset.getUploadProgress()).thenReturn(progressIndicator);
            when(asset.getDownloadProgress()).thenReturn(progressIndicator);
            MockHelper.setupObservableMocks(progressIndicator, activity);
        }
    }

    public static MessageViewsContainer getMockMessageViewsContainer(BaseActivity activity) {
        MessageViewsContainer messageViewsContainer = mock(MessageViewsContainer.class);
        when(messageViewsContainer.getControllerFactory()).thenReturn(activity.getControllerFactory());
        when(messageViewsContainer.getStoreFactory()).thenReturn(activity.getStoreFactory());
        return messageViewsContainer;
    }
}
