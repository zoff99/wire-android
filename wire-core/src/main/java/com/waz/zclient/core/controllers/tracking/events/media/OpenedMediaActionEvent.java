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
package com.waz.zclient.core.controllers.tracking.events.media;

import android.support.annotation.NonNull;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.attributes.ConversationType;
import com.waz.zclient.core.controllers.tracking.attributes.OpenedMediaAction;
import com.waz.zclient.core.controllers.tracking.events.Event;

public class OpenedMediaActionEvent extends Event {

    public static OpenedMediaActionEvent audiocall(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.AUDIO_CALL, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.AUDIO_CALL, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    public static OpenedMediaActionEvent videocall(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.VIDEO_CALL, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.VIDEO_CALL, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    public static OpenedMediaActionEvent sketch(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.SKETCH, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.SKETCH, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    public static OpenedMediaActionEvent ping(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.PING, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.PING, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    public static OpenedMediaActionEvent photo(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.PHOTO, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.PHOTO, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    public static OpenedMediaActionEvent giphy(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.GIPHY, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.GIPHY, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    public static OpenedMediaActionEvent file(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.FILE, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.FILE, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    public static OpenedMediaActionEvent videomessage(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.VIDEO_MESSAGE, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.VIDEO_MESSAGE, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    public static OpenedMediaActionEvent audiomessage(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.AUDIO_MESSAGE, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.AUDIO_MESSAGE, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    public static OpenedMediaActionEvent location(boolean fromGroupConversation) {
        if (fromGroupConversation) {
            return new OpenedMediaActionEvent(OpenedMediaAction.LOCATION, ConversationType.GROUP_CONVERSATION);
        }
        return new OpenedMediaActionEvent(OpenedMediaAction.LOCATION, ConversationType.ONE_TO_ONE_CONVERSATION);
    }

    private OpenedMediaActionEvent(OpenedMediaAction action, ConversationType type) {
        attributes.put(Attribute.TARGET, action.nameString);
        attributes.put(Attribute.TYPE, type.name);
    }

    @NonNull
    @Override
    public String getName() {
        return "media.opened_action";
    }
}
