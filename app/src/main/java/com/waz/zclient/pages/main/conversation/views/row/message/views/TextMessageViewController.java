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
package com.waz.zclient.pages.main.conversation.views.row.message.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.waz.zclient.R;
import com.waz.zclient.controllers.selection.MessageActionModeController;
import com.waz.zclient.pages.main.conversation.views.MessageViewsContainer;
import com.waz.zclient.pages.main.conversation.views.row.message.RetryMessageViewController;
import com.waz.zclient.pages.main.conversation.views.row.separator.Separator;
import com.waz.zclient.ui.views.TouchFilterableLayout;
import com.waz.zclient.ui.views.TouchFilterableLinearLayout;
import com.waz.zclient.utils.ViewUtils;


public class TextMessageViewController extends RetryMessageViewController implements MessageActionModeController.Selectable {

    private TouchFilterableLinearLayout view;
    private TextMessageWithTimestamp textWithTimestamp;

    @SuppressLint("InflateParams")
    public TextMessageViewController(Context context, final MessageViewsContainer messageViewContainer) {
        super(context, messageViewContainer);
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (TouchFilterableLinearLayout) inflater.inflate(R.layout.row_conversation_text_message, null);
        textWithTimestamp = ViewUtils.getView(view, R.id.tmwt__message_and_timestamp);
        textWithTimestamp.setMessageViewsContainer(messageViewContainer);
        textWithTimestamp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (message == null ||
                    messageViewContainer == null ||
                    messageViewContainer.getControllerFactory() == null ||
                    messageViewContainer.getControllerFactory().isTornDown()) {
                    return false;
                }
                messageViewContainer.getControllerFactory().getMessageActionModeController().selectMessage(message);
                return true;
            }
        });

        afterInit();
    }

    @Override
    public void onSetMessage(Separator separator) {
        super.onSetMessage(separator);
        textWithTimestamp.setMessage(message);
    }

    @Override
    public TouchFilterableLayout getView() {
        return view;
    }

    @Override
    public void recycle() {
        textWithTimestamp.recycle();
        super.recycle();
    }
}
