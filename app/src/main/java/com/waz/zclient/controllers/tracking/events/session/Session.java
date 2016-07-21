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
package com.waz.zclient.controllers.tracking.events.session;

import android.support.annotation.NonNull;
import com.waz.api.TrackingData;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.attributes.RangedAttribute;
import com.waz.zclient.core.controllers.tracking.events.Event;

public class Session extends Event {

    public Session(int connectRequestsSent,
                   int connectRequestsAccepted,
                   int voiceCallsInitiated,
                   int incomingCallsAccepted,
                   int incomingCallsMuted,
                   int groupConversationsStarted,
                   int usersAddedToConversations,
                   int textMessagesSent,
                   int youtubeLinksSent,
                   int soundcloudLinksSent,
                   int pingsSent,
                   int imagesSent,
                   int imageContentsClicks,
                   int soundcloudContentClicks,
                   int youtubeContentClicks,
                   int conversationRenames,
                   int sessionDuration,
                   int openedSearch,
                   boolean isFirstSession,
                   boolean searchedForPeople) {

        rangedAttributes.put(RangedAttribute.CONNECT_REQUESTS_SENT, connectRequestsSent);
        rangedAttributes.put(RangedAttribute.CONNECT_REQUESTS_ACCEPTED, connectRequestsAccepted);
        rangedAttributes.put(RangedAttribute.VOICE_CALLS_INITIATED, voiceCallsInitiated);
        rangedAttributes.put(RangedAttribute.INCOMING_CALLS_ACCEPTED, incomingCallsAccepted);
        rangedAttributes.put(RangedAttribute.INCOMING_CALLS_SILENCED, incomingCallsMuted);
        rangedAttributes.put(RangedAttribute.GROUP_CONVERSATIONS_STARTED, groupConversationsStarted);
        rangedAttributes.put(RangedAttribute.USERS_ADDED_TO_CONVERSATIONS, usersAddedToConversations);
        rangedAttributes.put(RangedAttribute.TEXT_MESSAGES_SENT, textMessagesSent);
        rangedAttributes.put(RangedAttribute.YOUTUBE_LINKS_SENT, youtubeLinksSent);
        rangedAttributes.put(RangedAttribute.SOUNDCLOUD_LINKS_SENT, soundcloudLinksSent);
        rangedAttributes.put(RangedAttribute.PINGS_SENT, pingsSent);
        rangedAttributes.put(RangedAttribute.IMAGES_SENT, imagesSent);
        rangedAttributes.put(RangedAttribute.IMAGE_CONTENT_CLICKS, imageContentsClicks);
        rangedAttributes.put(RangedAttribute.SOUNDCLOUD_CONTENT_CLICKS, soundcloudContentClicks);
        rangedAttributes.put(RangedAttribute.YOUTUBE_CONTENT_CLICKS, youtubeContentClicks);
        rangedAttributes.put(RangedAttribute.CONVERSATION_RENAMES, conversationRenames);
        rangedAttributes.put(RangedAttribute.SESSION_DURATION, sessionDuration);
        rangedAttributes.put(RangedAttribute.OPENED_SEARCH, openedSearch);

        attributes.put(Attribute.SESSION_FIRST_SESSION, Boolean.toString(isFirstSession));
        attributes.put(Attribute.SESSION_SEARCHED_FOR_PEOPLE, Boolean.toString(searchedForPeople));
    }

    @NonNull
    @Override
    public String getName() {
        return "session";
    }

    @Override
    public void addSyncEngineTrackingData(@NonNull TrackingData trackingData) {
        rangedAttributes.put(RangedAttribute.NUMBER_OF_CONTACTS, trackingData.getNotBlockedContactCount());
        rangedAttributes.put(RangedAttribute.NUMBER_OF_GROUP_CONVERSATIONS, trackingData.getGroupConversationCount());
        rangedAttributes.put(RangedAttribute.NUMBER_OF_ARCHIVED_CONVERSATIONS, trackingData.getArchivedConversationCount());
        rangedAttributes.put(RangedAttribute.NUMBER_OF_MUTED_CONVERSATIONS, trackingData.getMutedConversationCount());
    }
}
