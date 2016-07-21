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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.waz.api.User;
import com.waz.zclient.R;
import com.waz.zclient.utils.ViewUtils;
import com.waz.zclient.views.chathead.ChatheadImageView;

public class ParticipantsChangedAdapter extends RecyclerView.Adapter<ParticipantsChangedAdapter.ViewHolder> implements ParticipantsChangedMessageViewController.OnUserClickedListener {

    private final boolean removeMode;
    private User[] items;
    private ParticipantsChangedMessageViewController.OnUserClickedListener onUserClickedListener;

    public ParticipantsChangedAdapter(@NonNull User[] items, boolean removeMode) {
        this.items = items;
        this.removeMode = removeMode;
    }

    public void setItems(User[] items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.row_conversation_participants_changed_grid_item,
                                                 parent,
                                                 false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items[position], removeMode);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycle();
    }

    public void clear() {
        this.items = null;
        notifyDataSetChanged();
    }

    public void setOnUserClickedListener(ParticipantsChangedMessageViewController.OnUserClickedListener onUserClickedListener) {
        this.onUserClickedListener = onUserClickedListener;
    }

    @Override
    public void onUserClicked(View view, User user) {
        if (onUserClickedListener != null) {
            onUserClickedListener.onUserClicked(view, user);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ChatheadImageView chatheadImageView;
        private final ParticipantsChangedMessageViewController.OnUserClickedListener onUserClickedListener;
        private User user;

        public ViewHolder(View itemView, ParticipantsChangedMessageViewController.OnUserClickedListener onUserClickedListener) {
            super(itemView);
            this.onUserClickedListener = onUserClickedListener;
            chatheadImageView = ViewUtils.getView(itemView, R.id.civ__row_conversation__people_changed__grid_item);
            chatheadImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ViewHolder.this.onUserClickedListener == null || user == null) {
                        return;
                    }
                    ViewHolder.this.onUserClickedListener.onUserClicked(chatheadImageView, user);
                }
            });
        }

        public void bind(User user, boolean removed) {
            this.user = user;
            chatheadImageView.setUser(user);
            if (removed) {
                chatheadImageView.setAlpha(0.4f);
            } else {
                chatheadImageView.setAlpha(1f);
            }
        }

        public void recycle() {
            chatheadImageView.setUser(null);
        }
    }
}
