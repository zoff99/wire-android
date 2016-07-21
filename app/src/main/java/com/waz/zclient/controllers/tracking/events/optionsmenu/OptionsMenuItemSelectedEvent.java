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
package com.waz.zclient.controllers.tracking.events.optionsmenu;

import android.support.annotation.NonNull;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.attributes.ConversationType;
import com.waz.zclient.core.controllers.tracking.events.Event;


public class OptionsMenuItemSelectedEvent extends Event {

    public enum Context {
        LIST("list"),
        PARTICIPANTS("participants");

        public final String name;
        Context(String name) {
            this.name = name;
        }
    }

    public enum Method {
        TAP("tap"),
        SWIPE("swipe");

        public final String name;
        Method(String name) {
            this.name = name;
        }
    }

    public enum Action {
        SILENCE("silence"),
        NOTIFY("notify"),
        ARCHIVE("archive"),
        UNARCHIVE("unarchive"),
        LEAVE("leave"),
        DELETE("delete"),
        BLOCK("block"),
        UNBLOCK("unblock"),
        RENAME("rename");

        public final String name;
        Action(String name) {
            this.name = name;
        }
    }

    public OptionsMenuItemSelectedEvent(Action action, Context context, ConversationType type, Method method) {
        attributes.put(Attribute.ACTION, action.name);
        attributes.put(Attribute.CONTEXT, context.name);
        attributes.put(Attribute.TYPE, type.name);
        attributes.put(Attribute.METHOD, method.name);
    }

    @NonNull
    @Override
    public String getName() {
        return "optionsMenuItemSelected";
    }
}
