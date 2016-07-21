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
package com.waz.zclient.controllers.notifications;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.waz.annotations.Controller;
import com.waz.api.ImageAsset;
import com.waz.api.NotificationsHandler;

@Controller
public interface INotificationsController extends NotificationsHandler {

    void showImageSavedNotification(@NonNull ImageAsset image, @NonNull Uri uri);

    // Want to get the Uri here so that we could maybe show multiple image saved notifications
    // and cancel them separately
    void dismissImageSavedNotification(@Nullable Uri uri);
    
    void tearDown();

}
