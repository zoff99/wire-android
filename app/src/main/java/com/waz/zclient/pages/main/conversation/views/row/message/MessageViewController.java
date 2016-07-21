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
package com.waz.zclient.pages.main.conversation.views.row.message;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import com.waz.api.Asset;
import com.waz.api.Message;
import com.waz.zclient.R;
import com.waz.zclient.controllers.accentcolor.AccentColorObserver;
import com.waz.zclient.controllers.selection.MessageActionModeController;
import com.waz.zclient.controllers.selection.MessageActionModeObserver;
import com.waz.zclient.pages.main.conversation.views.MessageViewsContainer;
import com.waz.zclient.pages.main.conversation.views.row.separator.Separator;
import com.waz.zclient.ui.theme.ThemeUtils;
import com.waz.zclient.ui.utils.ColorUtils;
import com.waz.zclient.ui.utils.ResourceUtils;
import com.waz.zclient.ui.views.TouchFilterableLayout;

import java.util.Set;

import static com.waz.api.AssetStatus.META_DATA_SENT;
import static com.waz.api.AssetStatus.PREVIEW_SENT;
import static com.waz.api.AssetStatus.UPLOAD_IN_PROGRESS;
import static com.waz.api.AssetStatus.UPLOAD_NOT_STARTED;

public abstract class MessageViewController implements ConversationItemViewController,
                                                       AccentColorObserver,
                                                       MessageActionModeObserver,
                                                       TouchFilterableLayout.OnClickListener,
                                                       TouchFilterableLayout.OnLongClickListener {

    protected Context context;
    protected Message message;
    protected MessageViewsContainer messageViewsContainer;
    protected final float selectionAlpha;

    public MessageViewController(Context context, MessageViewsContainer messageViewsContainer) {
        this.context = context;
        this.messageViewsContainer = messageViewsContainer;
        if (ThemeUtils.isDarkTheme(context)) {
            selectionAlpha = ResourceUtils.getResourceFloat(context.getResources(), R.dimen.selection__alpha_dark);
        } else {
            selectionAlpha = ResourceUtils.getResourceFloat(context.getResources(), R.dimen.selection__alpha_light);
        }

    }

    protected void afterInit() {}

    /**
     * Set the model to be displayed. The UI setup logic for each message type goes here.
     *
     * @param separator passed only to help determine the padding around the message,
     *                  as this varies depending on what precedes it.
     */
    abstract protected void onSetMessage(Separator separator);

    @CallSuper
    public void setMessage(@NonNull Message message, @NonNull Separator separator) {
        final Message oldMessage = this.message;
        if (oldMessage != null &&
            message.getId().equals(oldMessage.getId())) {
            return;
        }
        recycle();
        beforeSetMessage(oldMessage, message);
        this.message = message;
        onSetMessage(separator);
        if (messageViewsContainer.getControllerFactory().getMessageActionModeController().isActionModeEnabled()) {
            onActionModeStarted();
        }
        messageViewsContainer.getControllerFactory().getAccentColorController().addAccentColorObserver(this);
        messageViewsContainer.getControllerFactory().getMessageActionModeController().addObserver(this);
    }

    protected void beforeSetMessage(@Nullable Message oldMessage, Message newMessage) {}

    /**
     * This will be called for you before {@link MessageViewController#onSetMessage(Separator)}
     */
    @Override
    @CallSuper
    public void recycle() {
        message = null;
        setSelected(false);
        onActionModeFinished();
        if (messageViewsContainer != null && !messageViewsContainer.isTornDown()) {
            messageViewsContainer.getControllerFactory().getAccentColorController().removeAccentColorObserver(this);
            messageViewsContainer.getControllerFactory().getMessageActionModeController().removeObserver(this);
        }
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public final void onMessageSelectionChanged(Set<Message> selectedMessages) {
        setSelected(selectedMessages.contains(message));
    }

    @Override
    public void onMessageSelected(Message message) {

    }

    protected void setSelected(boolean selected) {
        if (message == null ||
            messageViewsContainer == null ||
            messageViewsContainer.isTornDown() ||
            getSelectionView() == null) {
            return;
        }
        final int accentColor = messageViewsContainer.getControllerFactory().getAccentColorController().getColor();
        int targetAccentColor;
        if (selected) {
            targetAccentColor = ColorUtils.injectAlpha(selectionAlpha, accentColor);
        } else {
            targetAccentColor = ContextCompat.getColor(context, R.color.transparent);
        }
        getSelectionView().setBackgroundColor(targetAccentColor);
    }

    @Override
    public void onActionModeStarted() {
        getView().setFilterAllClickEvents(true);
        if (this instanceof MessageActionModeController.Selectable) {
            getView().setOnClickListener(this);
            getView().setOnLongClickListener(this);
        }
    }

    @Override
    public void onActionModeFinished() {
        getView().setFilterAllClickEvents(false);
        getView().setOnClickListener(null);
        getView().setOnLongClickListener(null);
    }

    @Override
    public void onFinishActionMode() {
    }

    @Override
    @CallSuper
    public void onAccentColorHasChanged(Object sender, int color) {
        if (message == null ||
            messageViewsContainer == null ||
            messageViewsContainer.isTornDown() ||
            getSelectionView() == null) {
            return;
        }
        setSelected(messageViewsContainer.getControllerFactory().getMessageActionModeController().isSelected(message));
    }

    protected View getSelectionView() {
        return getView().getLayout();
    }

    @Override
    public void onClick() {
        if (messageViewsContainer == null ||
            messageViewsContainer.isTornDown() ||
            !(this instanceof MessageActionModeController.Selectable)) {
            return;
        }
        if (messageViewsContainer.getControllerFactory().getMessageActionModeController().isSelected(message)) {
            messageViewsContainer.getControllerFactory().getMessageActionModeController().deselectMessage(message);
        } else {
            messageViewsContainer.getControllerFactory().getMessageActionModeController().selectMessage(message);
        }
    }

    @Override
    public void onLongClick() {
        onClick();
    }

    protected boolean receivingMessage(Asset asset) {
        return asset != null &&
               (asset.getStatus() == UPLOAD_NOT_STARTED ||
                asset.getStatus() == META_DATA_SENT ||
                asset.getStatus() == PREVIEW_SENT ||
                asset.getStatus() == UPLOAD_IN_PROGRESS) &&
               message.getMessageStatus() == Message.Status.SENT;
    }

    protected String getConversationTypeString() {
        return messageViewsContainer.getConversationType() != null ?
               messageViewsContainer.getConversationType().name() :
               "unspecified";
    }
}
