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
package com.waz.zclient.core.controllers.tracking.events.registration;

import android.support.annotation.NonNull;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.events.Event;

public class EmailVerification extends Event {

    public static EmailVerification success(Context context) {
        return new EmailVerification(State.SUCCEEDED, context);
    }

    public static EmailVerification submitted(Context context) {
        return new EmailVerification(State.SUBMITTED, context);
    }

    public static EmailVerification resent(Context context) {
        return new EmailVerification(State.RESENT, context);
    }

    public static EmailVerification error(Context context) {
        return new EmailVerification(State.ERROR, Description.VERIFICATION_ERROR, context);
    }

    public enum State {
        SUBMITTED("submitted"),
        RESENT("resent"),
        ERROR("error"),
        SUCCEEDED("succeeded"),
        ;

        public final String nameString;
        State(String nameString) {
            this.nameString = nameString;
        }
    }

    public enum Description {
        VERIFICATION_ERROR("verificationError"),
        ;

        public final String nameString;
        Description(String nameString) {
            this.nameString = nameString;
        }
    }

    public enum Context {
        PROFILE("profile"),
        REGISTRATION("registration"),
        POST_LOGIN("postLogin")
        ;

        public final String nameString;
        Context(String nameString) {
            this.nameString = nameString;
        }
    }

    public EmailVerification(State state, Context context) {
        attributes.put(Attribute.STATE, state.nameString);
        attributes.put(Attribute.CONTEXT, context.nameString);
    }

    public EmailVerification(State state, Description description, Context context) {
        attributes.put(Attribute.STATE, state.nameString);
        attributes.put(Attribute.DESCRIPTION, description.nameString);
        attributes.put(Attribute.CONTEXT, context.nameString);
    }

    @NonNull
    @Override
    public String getName() {
        return "EmailVerification";
    }


}
