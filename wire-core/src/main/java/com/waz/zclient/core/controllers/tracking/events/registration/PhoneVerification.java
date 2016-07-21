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

public class PhoneVerification extends Event {

    public static PhoneVerification success(Context context) {
        return new PhoneVerification(State.SUCCEEDED, context);
    }

    public static PhoneVerification resent(Context context) {
        return new PhoneVerification(State.RESENT, context);
    }

    public static PhoneVerification error(Context context) {
        return new PhoneVerification(State.ERROR, context);
    }

    public static PhoneVerification codeRequestErrorReg() {
        return createCodeErrorEventWithContext(Context.REGISTRATION);
    }

    public static PhoneVerification codeRequestErrorSignIn() {
        return createCodeErrorEventWithContext(Context.SIGN_IN);
    }

    private static PhoneVerification createCodeErrorEventWithContext(PhoneVerification.Context context) {
        return new PhoneVerification(State.ERROR, Description.CODE_REQUEST_ERROR, context);
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
        VERIFICATION_ERROR("phoneVerificationError"),
        VALIDATION_ERROR("phoneValidationError"),
        CODE_REQUEST_ERROR("codeRequestError"),
        ;

        public final String nameString;
        Description(String nameString) {
            this.nameString = nameString;
        }
    }

    public enum Context {
        PROFILE("profile"),
        REGISTRATION("registration"),
        SIGN_IN("signIn"),
        POST_LOGIN("postLogin")
        ;

        public final String nameString;
        Context(String nameString) {
            this.nameString = nameString;
        }
    }

    public PhoneVerification(State state, Description description, Context context) {
        this(state, context);
        attributes.put(Attribute.DESCRIPTION, description.nameString);
    }

    public PhoneVerification(State state, Context context) {
        attributes.put(Attribute.STATE, state.nameString);
        attributes.put(Attribute.CONTEXT, context.nameString);
    }

    @NonNull
    @Override
    public String getName() {
        return "PhoneVerification";
    }


}
