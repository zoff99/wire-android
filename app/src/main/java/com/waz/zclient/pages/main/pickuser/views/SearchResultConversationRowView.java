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
package com.waz.zclient.pages.main.pickuser.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.waz.api.IConversation;
import com.waz.api.UpdateListener;
import com.waz.zclient.R;
import com.waz.zclient.utils.ViewUtils;

public class SearchResultConversationRowView extends LinearLayout implements ConversationRowView,
                                                                             UpdateListener {
    private IConversation conversation;
    private TextView nameView;

    public SearchResultConversationRowView(Context context) {
        this(context, null);
    }

    public SearchResultConversationRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchResultConversationRowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.list_row_pickuser_searchconversation, this, true);
        nameView = ViewUtils.getView(this, R.id.ttv_pickuser_searchconversation_name);
    }

    @Override
    public IConversation getConversation() {
        return conversation;
    }

    public void setConversation(IConversation conversation) {
        if (this.conversation != null) {
            this.conversation.removeUpdateListener(this);
        }
        this.conversation  = conversation;
        if (this.conversation  != null) {
            this.conversation .addUpdateListener(this);
        }
        updated();
    }

    @Override
    public void updated() {
        if (conversation == null) {
            return;
        }
        nameView.setText(conversation.getName());
    }
}
