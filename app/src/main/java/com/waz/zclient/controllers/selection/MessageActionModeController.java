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
package com.waz.zclient.controllers.selection;

import com.waz.api.Message;
import com.waz.zclient.core.api.scala.ModelObserver;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MessageActionModeController implements IMessageActionModeController {

    private final ModelObserver<Message> messageModelObserver = new ModelObserver<Message>() {
        @Override
        public void updated(Message model) {
            if (model.isDeleted()) {
                deselectMessage(model);
            }
        }
    };
    private Set<MessageActionModeObserver> observerSet;
    private Set<Message> selectedMessages;
    private boolean isActionModeEnabled;

    public MessageActionModeController() {
        this.observerSet = new CopyOnWriteArraySet<>();
        this.selectedMessages = new HashSet<>();
    }

    @Override
    public void addObserver(MessageActionModeObserver observer) {
        observerSet.add(observer);
        observer.onMessageSelectionChanged(selectedMessages);
    }

    @Override
    public void removeObserver(MessageActionModeObserver observer) {
        observerSet.remove(observer);
    }

    @Override
    public void selectMessage(Message message) {
        if (selectedMessages.contains(message)) {
            return;
        }
        selectedMessages.add(message);
        messageModelObserver.setAndUpdate(selectedMessages);
        for (MessageActionModeObserver observer : observerSet) {
            observer.onMessageSelectionChanged(selectedMessages);
        }
        for (MessageActionModeObserver observer : observerSet) {
            observer.onMessageSelected(message);
        }
    }

    @Override
    public void deselectMessage(Message message) {
        if (!selectedMessages.contains(message)) {
            return;
        }
        selectedMessages.remove(message);
        messageModelObserver.setAndUpdate(selectedMessages);
        for (MessageActionModeObserver observer : observerSet) {
            observer.onMessageSelectionChanged(selectedMessages);
        }
    }

    @Override
    public boolean isSelected(Message message) {
        return selectedMessages != null && selectedMessages.contains(message);
    }

    @Override
    public void resetSelections() {
        messageModelObserver.clear();
        selectedMessages.clear();
        for (MessageActionModeObserver observer : observerSet) {
            observer.onMessageSelectionChanged(selectedMessages);
        }
    }

    @Override
    public void tearDown() {
        isActionModeEnabled = false;
        messageModelObserver.clear();
        selectedMessages.clear();
        observerSet.clear();
    }

    @Override
    public Set<Message> getSelectedMessages() {
        return selectedMessages;
    }

    @Override
    public void onActionModeStarted() {
        isActionModeEnabled = true;
        for (MessageActionModeObserver observer : observerSet) {
            observer.onActionModeStarted();
        }
    }

    @Override
    public void onActionModeFinished() {
        resetSelections();
        isActionModeEnabled = false;
        for (MessageActionModeObserver observer : observerSet) {
            observer.onActionModeFinished();
        }
    }

    @Override
    public void finishActionMode() {
        if (!isActionModeEnabled) {
            return;
        }
        for (MessageActionModeObserver observer : observerSet) {
            observer.onFinishActionMode();
        }
    }

    @Override
    public boolean isActionModeEnabled() {
        return isActionModeEnabled;
    }

    public interface Selectable {
    }
}
