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
package com.waz.zclient.pages.main.conversationlist.views.row;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.waz.api.IConversation;
import com.waz.zclient.R;
import com.waz.zclient.core.stores.connect.InboxLinkConversation;
import com.waz.zclient.pages.main.conversation.ConversationUtils;
import com.waz.zclient.ui.animation.interpolators.penner.Quart;
import com.waz.zclient.utils.ViewUtils;
import com.waz.zclient.views.calling.OngoingCallingProgressSmallView;
import com.waz.zclient.views.calling.StaticCallingIndicator;

public class LeftIndicatorView extends FrameLayout {

    // if the voice channel is open this is pulsing
    private OngoingCallingProgressSmallView ongoingCallingProgressSmallView;

    // if there is a missed call
    private StaticCallingIndicator missedCallIndicator;

    // if there is a call that the user hasn't joined
    private StaticCallingIndicator unjoinedCallIndicator;

    ConversationIndicatorView conversationIndicatorView;

    // the current offset of the drawer
    private int offset;

    // the max offset of the drawer
    private float maxOffset;

    public LeftIndicatorView(Context context, int accentColor) {
        super(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                             LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.LEFT;
        setLayoutParams(layoutParams);
        LayoutInflater.from(getContext()).inflate(R.layout.conv_list_item_unread, this, true);

        ongoingCallingProgressSmallView = ViewUtils.getView(this, R.id.ocpvs__left_indicator);
        conversationIndicatorView = ViewUtils.getView(this, R.id.civ__list_row);
        missedCallIndicator = ViewUtils.getView(this, R.id.sci__list__missed_call);
        missedCallIndicator.setFillRings(true);
        unjoinedCallIndicator = ViewUtils.getView(this, R.id.sci__list__unjoined_call);
        unjoinedCallIndicator.setFillRings(false);

        ViewUtils.setPaddingTop(this, getResources().getDimensionPixelSize(R.dimen.calling__conversation_list__margin_top));
        setAccentColor(accentColor);
    }

    /* Getters/Setters */

    /**
     * the max offset is set by the SwipeListView. It needs to calculate its own width first.
     */
    public void setMaxOffset(float maxOffset) {
        this.maxOffset = maxOffset;
    }

    /**
     * The color of the circles.
     */
    public void setAccentColor(int accentColor) {
        ongoingCallingProgressSmallView.setAccentColor(accentColor);
        conversationIndicatorView.setAccentColor(accentColor);
        unjoinedCallIndicator.setColor(accentColor);
    }

    /**
     * Needed for the object animator.
     */
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;

        float alpha = 1;
        if (maxOffset > 0) {
            alpha = 1 - 4 * this.offset / maxOffset;
        }

        conversationIndicatorView.setAlpha(alpha);
        ongoingCallingProgressSmallView.setAlpha(alpha);
        missedCallIndicator.setAlpha(alpha);
        invalidate();
    }

    /**
     * Binds the conversation to this view.
     */
    public void setConversation(final IConversation conversation) {
        ongoingCallingProgressSmallView.setVisibility(View.GONE);
        conversationIndicatorView.setVisibility(View.GONE);
        missedCallIndicator.setVisibility(GONE);
        unjoinedCallIndicator.setVisibility(GONE);

        if (conversation.hasUnjoinedCall()) {
            return;
        }

        // conversation is in call
        if (conversation.hasVoiceChannel()) {
            ongoingCallingProgressSmallView.setVisibility(View.VISIBLE);
            return;
        }

        if (conversation.hasMissedCall()) {
            missedCallIndicator.setVisibility(VISIBLE);
            return;
        }

        conversationIndicatorView.setVisibility(View.VISIBLE);

        // Outlined indicator for link to connect inbox or outgoing pending connect request
        if (conversation instanceof InboxLinkConversation || conversation.getType() == IConversation.Type.WAIT_FOR_CONNECTION && !conversation.isArchived()) {
            conversationIndicatorView.setState(ConversationIndicatorView.State.PENDING);
            return;
        }

        if (conversation.getFailedCount() > 0) {
            conversationIndicatorView.setState(ConversationIndicatorView.State.UNSENT);
            return;
        }

        conversationIndicatorView.setState(ConversationIndicatorView.State.UNREAD);

        // show unread
        int radius = ConversationUtils.getListUnreadIndicatorRadiusPx(getContext(), conversation.getUnreadCount());

        conversationIndicatorView.setUnreadSize(radius);

        invalidate();
    }

    /**
     * knocking event occured. Fade out unread and pulser and start knocking animation.
     * Afterwards bring the view back into its original state.
     */
    public void knock() {
        animate().alpha(0).setInterpolator(new Quart.EaseOut()).setDuration(getResources().getInteger(R.integer.list_hello_indicator_fade_in_out_animation_duration));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resetAfterKnocking();
            }
        }, getResources().getInteger(R.integer.list_hello_indicator_fade_in_out_animation_duration));
    }

    /**
     * After knocking event has occured, bring the view back to its original state.
     */
    private void resetAfterKnocking() {
        animate().alpha(1).setInterpolator(new Quart.EaseOut()).setDuration(getResources().getInteger(R.integer.list_hello_indicator_fade_in_out_animation_duration));
    }
}
