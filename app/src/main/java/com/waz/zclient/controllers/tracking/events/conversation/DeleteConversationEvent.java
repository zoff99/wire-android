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
package com.waz.zclient.controllers.tracking.events.conversation;


import android.support.annotation.NonNull;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.attributes.ConversationType;
import com.waz.zclient.core.controllers.tracking.events.Event;

public class DeleteConversationEvent extends Event {

    public enum Context {
        LIST("list"),
        PARTICIPANTS("participants")
        ;

        public final String name;
        Context(String name) {
            this.name = name;
        }
    }

    public enum Response {
        DELETE("delete"),
        CANCEL("cancel")
        ;

        public final String name;
        Response(String name) {
            this.name = name;
        }
    }

    public DeleteConversationEvent(ConversationType conversationType, Context context, Response response) {
        attributes.put(Attribute.CONVERSATION_TYPE, conversationType.name);
        attributes.put(Attribute.CONTEXT, context.name);
        attributes.put(Attribute.CONFIRMATION_RESPONSE, response.name);
    }

    @NonNull
    @Override
    public String getName() {
        return "deleteConversation";
    }
}
