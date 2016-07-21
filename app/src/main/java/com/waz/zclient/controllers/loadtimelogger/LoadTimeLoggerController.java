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
package com.waz.zclient.controllers.loadtimelogger;

import com.waz.zclient.ui.utils.MathUtils;
import timber.log.Timber;

public class LoadTimeLoggerController implements ILoadTimeLoggerController {

    private static final long UNINITIALIZED = -1;
    private long appStartTime;
    private long conversationClickStartTime;
    private long conversationContentSyncStartTime;

    public LoadTimeLoggerController() {
        appStartTime = UNINITIALIZED;
        conversationClickStartTime = UNINITIALIZED;
        conversationContentSyncStartTime = UNINITIALIZED;
    }

    @Override
    public void tearDown() {
    }

    @Override
    public void appStart() {
        appStartTime = System.nanoTime();
        Timber.w("App starting at %d", appStartTime);
    }

    @Override
    public void hideLaunchScreen() {
        if (MathUtils.floatEqual(appStartTime, UNINITIALIZED)) {
            return;
        }
        long loadTime = System.nanoTime() - appStartTime;
        Timber.w("App launch time %dns", loadTime);
        appStartTime = UNINITIALIZED;
    }

    @Override
    public void loginPressed() {
        Timber.w("Login pressed at %dns", System.nanoTime());
    }

    @Override
    public void loginSuccess() {
        Timber.w("Login completed at %dns", System.nanoTime());
    }

    @Override
    public void loginFail() {
        Timber.w("Login failed at %dns", System.nanoTime());
    }

    @Override
    public void clickConversationInList() {
        conversationClickStartTime = System.nanoTime();
        Timber.w("Conversation clicked at %d", conversationClickStartTime);
    }

    @Override
    public void conversationPageVisible() {
        if (MathUtils.floatEqual(conversationClickStartTime, UNINITIALIZED)) {
            return;
        }
        long loadTime = System.nanoTime() - conversationClickStartTime;
        Timber.w("Conversation page visible after %dns", loadTime);
        conversationClickStartTime = UNINITIALIZED;
    }

    @Override
    public void conversationContentSyncStart() {
        // Only setting once since this can be called multiple times
        if (!MathUtils.floatEqual(conversationContentSyncStartTime, UNINITIALIZED)) {
            return;
        }
        conversationContentSyncStartTime = System.nanoTime();
        Timber.w("Conversation content start sync at %d", conversationContentSyncStartTime);
    }

    @Override
    public void conversationContentSyncFinish() {
        if (MathUtils.floatEqual(conversationContentSyncStartTime, UNINITIALIZED)) {
            return;
        }
        long loadTime = System.nanoTime() - conversationContentSyncStartTime;
        Timber.w("Conversation content sync complete after %dns", loadTime);
        conversationContentSyncStartTime = UNINITIALIZED;
    }

    public static class DisabledLoadTimeLoggerController implements ILoadTimeLoggerController {
        @Override
        public void tearDown() {}

        @Override
        public void appStart() {}

        @Override
        public void hideLaunchScreen() {}

        @Override
        public void loginPressed() {}

        @Override
        public void loginSuccess() {}

        @Override
        public void loginFail() {}

        @Override
        public void clickConversationInList() {}

        @Override
        public void conversationPageVisible() {}

        @Override
        public void conversationContentSyncStart() {}

        @Override
        public void conversationContentSyncFinish() {}
    }

}
