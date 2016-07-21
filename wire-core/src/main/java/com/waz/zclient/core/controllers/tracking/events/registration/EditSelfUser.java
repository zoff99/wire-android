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

public class EditSelfUser extends Event {

    public static EditSelfUser emailAddedSignIn() {
        return new EditSelfUser(
            Field.EMAIL,
            Action.ADDED,
            Context.SIGN_IN);
    }

    public static EditSelfUser passwordAddedSignIn() {
        return new EditSelfUser(
            Field.PASSWORD,
            Action.ADDED,
            Context.SIGN_IN);
    }

    public static EditSelfUser phoneAddedSignIn() {
        return new EditSelfUser(
            Field.PHONE_NUMBER,
            Action.ADDED,
            Context.SIGN_IN);
    }

    public enum Field {
        NAME("name"),
        EMAIL("email"),
        PASSWORD("password"),
        PHONE_NUMBER("phoneNumber"),
        PICTURE("picture"),
        TERMS_OF_USE("termsOfUse")
        ;

        public final String nameString;
        Field(String nameString) {
            this.nameString = nameString;
        }
    }

    public enum Action {
        ADDED("added"),
        MODIFIED("modified")
        ;

        public final String nameString;
        Action(String nameString) {
            this.nameString = nameString;
        }
    }

    public enum Context {
        PROFILE("profile"),
        REGISTRATION("registration"),
        SIGN_IN("signIn")
        ;

        public final String nameString;
        Context(String nameString) {
            this.nameString = nameString;
        }
    }

    public EditSelfUser(Field field, Action action, Context context) {
        attributes.put(Attribute.FIELD, field.nameString);
        attributes.put(Attribute.ACTION, action.nameString);
        attributes.put(Attribute.CONTEXT, context.nameString);
    }

    @NonNull
    @Override
    public String getName() {
        return "EditSelfUser";
    }


}
