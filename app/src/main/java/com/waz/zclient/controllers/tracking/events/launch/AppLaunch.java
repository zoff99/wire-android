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
package com.waz.zclient.controllers.tracking.events.launch;

import android.content.Intent;
import android.support.annotation.NonNull;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.events.Event;
import com.waz.zclient.utils.IntentUtils;

public class AppLaunch extends Event {

    public AppLaunch(Intent intent) {
        attributes.put(Attribute.APP_LAUNCH_MECHANISM, getMechanism(intent));
    }

    @NonNull
    @Override
    public String getName() {
        return "appLaunch";
    }

    private String getMechanism(Intent intent) {

        if (IntentUtils.isEmailVerificationIntent(intent)) {
            return Mechanism.REGISTRATION.name;
        }
        if (IntentUtils.isPasswordResetIntent(intent)) {
            return Mechanism.PASSWORD_RESET.name;
        }
        if (IntentUtils.isLaunchFromNotificationIntent(intent)) {
            return Mechanism.PUSH.name;
        }
        if (IntentUtils.isLaunchFromSharingIntent(intent)) {
            return Mechanism.SHARING.name;
        }
        if (IntentUtils.isInviteIntent(intent)) {
            return Mechanism.INVITE.name;
        }

        return Mechanism.DIRECT.name;
    }

    private enum Mechanism {
        DIRECT("direct"),
        PUSH("push"),
        PASSWORD_RESET("passwordReset"),
        REGISTRATION("registration"),
        INVITE("inviteClick"),
        SHARING("sharing");

        public final String name;

        Mechanism(String name) {
            this.name = name;
        }
    }
}
