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
package com.waz.zclient.core.api.scala;

import android.content.Context;
import com.waz.api.ErrorsList;
import com.waz.api.IConversation;
import com.waz.api.IncomingMessagesList;
import com.waz.api.Message;
import com.waz.api.ZMessagingApi;
import com.waz.zclient.core.stores.inappnotification.InAppNotificationStore;
import com.waz.zclient.utils.LayoutSpec;
import com.waz.zclient.utils.ViewUtils;

public class ScalaInAppNotificationStore extends InAppNotificationStore implements IncomingMessagesList.MessageListener,
                                                                                   IncomingMessagesList.KnockListener,
                                                                                   ErrorsList.ErrorListener {

    private IncomingMessagesList incomingMessages;
    private ErrorsList syncErrors;
    private boolean isTablet;
    private boolean isInLandscape;
    private boolean conversationScrolledToBottom;
    private boolean userLookingAtParticipants;
    private boolean userLookingAtPeoplePicker;
    private boolean userSendingPicture;

    public ScalaInAppNotificationStore(Context context, ZMessagingApi zMessagingApi) {
        isTablet = LayoutSpec.isTablet(context);
        isInLandscape = ViewUtils.isInLandscape(context);
        incomingMessages = zMessagingApi.getIncomingMessages();
        incomingMessages.addMessageListener(this);
        incomingMessages.addKnockListener(this);

        syncErrors = zMessagingApi.getErrors();
        syncErrors.addErrorListener(this);
    }

    @Override
    public void tearDown() {
        incomingMessages.removeMessageListener(this);
        incomingMessages.removeKnockListener(this);
        incomingMessages = null;

        syncErrors.removeErrorListener(this);
        syncErrors = null;
    }

    @Override
    public boolean shouldShowChatheads(IConversation currentConversation, Message message) {
        if (isTablet && isInLandscape) {
            return false;
        }

        if (message.getConversation().equals(currentConversation) &&
            (conversationScrolledToBottom || userLookingAtParticipants)) {
            return false;
        }

        if (userLookingAtParticipants || userLookingAtPeoplePicker || userSendingPicture) {
            return false;
        }

        return true;

    }

    ////////////////////////////////////////////////////////////////////////////////
    // Notifications
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setUserLookingAtPeoplePicker(boolean userLookingAtPeoplePicker) {
        this.userLookingAtPeoplePicker = userLookingAtPeoplePicker;
    }

    @Override
    public void setUserLookingAtParticipants(boolean userLookingAtParticipants) {
        this.userLookingAtParticipants = userLookingAtParticipants;
    }

    @Override
    public void setUserSendingPicture(boolean userSendingPicture) {
        this.userSendingPicture = userSendingPicture;
    }

    @Override
    public void setIsLandscape(boolean isInLandscape) {
        this.isInLandscape = isInLandscape;
    }

    @Override
    public void dismissError(String errorId) {
        for (int i = 0, length = syncErrors.size(); i < length; i++) {
            ErrorsList.ErrorDescription error = syncErrors.get(i);
            if (error.getId().equals(errorId)) {
                error.dismiss();
            }
        }
    }

    @Override
    public ErrorsList.ErrorDescription getError(String errorId) {
        for (int i = 0, length = syncErrors.size(); i < length; i++) {
            ErrorsList.ErrorDescription error = syncErrors.get(i);
            if (error.getId().equals(errorId)) {
                return error;
            }
        }
        return null;
    }

    @Override
    public ErrorsList getErrorList() {
        return syncErrors;
    }

    @Override
    public void onIncomingMessage(Message message) {
        notifyIncomingMessageObservers(message);
    }

    @Override
    public void onError(ErrorsList.ErrorDescription error) {
        notifySyncErrorObservers(error);
    }

    @Override
    public void onScrolledToBottom() {
        conversationScrolledToBottom = true;
    }

    @Override
    public void onScrolledAwayFromBottom() {
        conversationScrolledToBottom = false;
    }

    @Override
    public void onKnock(final Message message) {
        notifyIncomingKnock(message);
    }

}
