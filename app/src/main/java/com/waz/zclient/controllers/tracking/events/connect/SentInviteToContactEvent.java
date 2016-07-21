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
package com.waz.zclient.controllers.tracking.events.connect;

import android.support.annotation.NonNull;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.attributes.BooleanAttribute;
import com.waz.zclient.core.controllers.tracking.events.Event;

public class SentInviteToContactEvent extends Event {

    public enum Method {
        EMAIL("email"),
        PHONE("phone");

        private final String name;

        Method(String tagName) {
            name = tagName;
        }
    }

    public SentInviteToContactEvent(Method method, boolean isResendingFlag, boolean fromContactSearch) {
        attributes.put(Attribute.METHOD, method.name);
        attributes.put(Attribute.IS_RESENDING, BooleanAttribute.getValue(isResendingFlag));
        attributes.put(Attribute.FROM_SEARCH, BooleanAttribute.getValue(fromContactSearch));
    }

    @NonNull
    @Override
    public String getName() {
        return "connect.sent_invite_to_contact";
    }
}
