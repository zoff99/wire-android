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
package com.waz.zclient.controllers.conversationlist;

import java.util.HashSet;
import java.util.Set;

public class ConversationListController implements IConversationListController {
    private Set<ConversationListObserver> conversationListObservers = new HashSet<>();

    private int pullOffset;

    @Override
    public void tearDown() {
        if (conversationListObservers != null) {
            conversationListObservers.clear();
            conversationListObservers = null;
        }
    }

    @Override
    public void addConversationListObserver(ConversationListObserver conversationListObserver) {
        conversationListObservers.add(conversationListObserver);
    }

    @Override
    public void removeConversationListObserver(ConversationListObserver conversationListObserver) {
        conversationListObservers.remove(conversationListObserver);
    }

    @Override
    public void notifyScrollOffsetChanged(int offset, int scrolledToBottom) {
        for (ConversationListObserver conversationListObserver : conversationListObservers) {
            conversationListObserver.onListViewScrollOffsetChanged(offset, scrolledToBottom);
        }
    }

    @Override
    public void onListViewOffsetChanged(int offset) {
        if (this.pullOffset == offset) {
            return;
        }
        this.pullOffset = offset;
        for (ConversationListObserver conversationListObserver : conversationListObservers) {
            conversationListObserver.onListViewPullOffsetChanged(offset);
        }
    }

    @Override
    public void onReleasedPullDownFromBottom(int offset) {
        for (ConversationListObserver conversationListObserver : conversationListObservers) {
            conversationListObserver.onReleasedPullDownFromBottom(offset);
        }
    }
}
