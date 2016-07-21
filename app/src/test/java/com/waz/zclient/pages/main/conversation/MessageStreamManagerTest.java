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
package com.waz.zclient.pages.main.conversation;

import android.view.View;
import com.waz.api.Message;
import com.waz.zclient.pages.main.conversation.views.listview.ConversationListView;
import com.waz.zclient.testutils.MockConversation;
import com.waz.zclient.testutils.MockMessagesList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageStreamManagerTest {

    MessageAdapter adapter;
    ConversationListView listView;
    MessageStreamManager manager;

    @Before
    public void setup() {
        adapter = mock(MessageAdapter.class);
        listView = mock(ConversationListView.class);
        when(listView.post(any(Runnable.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                invocation.getArgumentAt(0, Runnable.class).run();
                return null;
            }
        });
        manager = new MessageStreamManager(listView, adapter);
    }

    @Test
    public void changedConversationScrollsToLastRead() {
        MockConversation conversation = new MockConversation(1);
        MockMessagesList messages = (MockMessagesList) conversation.getMessages();
        messages.setMessages(5);
        messages.setLastReadMessageIndex(4);
        when(adapter.getCount()).thenReturn(messages.size());

        manager.setConversation(conversation, false);
        verify(adapter).resetState();
        verify(adapter).setMessages(messages);
        verify(listView).setSelectionFromTop(5, 0);
    }

    @Test
    public void whenScrolledToBottomNewMessageCausesScrolling() {
        MockConversation conversation = new MockConversation(1);
        MockMessagesList messages = (MockMessagesList) conversation.getMessages();
        messages.setMessages(5);
        messages.setLastReadMessageIndex(4);
        when(adapter.getCount()).thenReturn(messages.size());

        manager.setConversation(conversation, false);
        manager.onScrolledToBottom(true);

        messages.addExtraMessage(true);

        verify(adapter, times(2)).setMessages(messages);
        verify(listView, times(2)).setSelectionFromTop(5, 0);
    }

    @Test
    public void whenNotScrolledToBottom_andNewMessageReceived_positionIsMaintained() {
        MockConversation conversation = new MockConversation(1);
        MockMessagesList messages = (MockMessagesList) conversation.getMessages();
        messages.setMessages(20);
        messages.setLastReadMessageIndex(19);

        manager.setConversation(conversation, false);
        manager.onScrolledToBottom(false);

        when(listView.getChildAt(0)).thenReturn(mock(View.class));
        when(adapter.getIndexOfMessage(any(Message.class))).thenReturn(3); //currently looking at the 4th message
        messages.addExtraMessage(false);

        verify(adapter, times(2)).setMessages(messages);
        verify(listView).setSelectionFromTop(3, 0);
    }

    @Test
    public void changesToCursorState_causeScrollToBottom() {
        MockConversation conversation = new MockConversation(1);
        MockMessagesList messages = (MockMessagesList) conversation.getMessages();
        messages.setMessages(20);
        messages.setLastReadMessageIndex(19);

        manager.setConversation(conversation, false);
        manager.onScrolledToBottom(false);

        when(listView.getChildAt(0)).thenReturn(mock(View.class));
        when(adapter.getIndexOfMessage(any(Message.class))).thenReturn(3); //currently looking at the 4th message
        manager.onCursorStateEdit(); //user opens keyboard or starts typing


    }
}

