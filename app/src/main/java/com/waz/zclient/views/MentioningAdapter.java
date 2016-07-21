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
package com.waz.zclient.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.waz.api.UpdateListener;
import com.waz.api.User;
import com.waz.zclient.R;
import com.waz.zclient.utils.ViewUtils;
import com.waz.zclient.views.chathead.ChatheadImageView;

import java.util.ArrayList;
import java.util.List;

public class MentioningAdapter extends RecyclerView.Adapter<MentioningAdapter.ViewHolder> {
    private static final int MAX_USERS = 8;
    private static final int MAX_USERS_WITH_TEXT = 2;
    private List<User> users = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() <= MAX_USERS_WITH_TEXT) {
            return R.layout.mentioning_view_item_with_name;
        } else {
            return R.layout.mentioning_view_item;
        }
    }

    @Override
    public MentioningAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(MentioningAdapter.ViewHolder holder, int position) {
        holder.setUser(users.get(position));
        holder.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return Math.min(users.size(), MAX_USERS);
    }

    public void setUsers(List<User> users) {
        final List<User> existingUsers = new ArrayList<>(this.users);
        final List<User> intersection = intersection(existingUsers, users);
        if (intersection.size() == users.size() &&
            intersection.size() == existingUsers.size()) {
            return;
        }
        if (intersection.size() == 0 ||
            users.size() <= MAX_USERS_WITH_TEXT) {
            this.users.clear();
            for (User user : users) {
                this.users.add(user);
            }
        } else {
            final List<User> newUsers = new ArrayList<>(users);
            newUsers.removeAll(intersection);
            existingUsers.removeAll(intersection);
            for (User user : existingUsers) {
                this.users.remove(user);
            }
            for (User user : newUsers) {
                this.users.add(user);
            }
        }
        notifyDataSetChanged();
    }

    private List<User> intersection(List<User> list1, List<User> list2) {
        List<User> list = new ArrayList<>();
        for (User t : list1) {
            if (list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    public void setOnItemClickListener(MentioningAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements UpdateListener,
                                                                              View.OnClickListener {
        private final ChatheadImageView image;
        private final TextView name;
        private User user;
        private OnItemClickListener onItemClickListener;

        public ViewHolder(View itemView) {
            super(itemView);
            image = ViewUtils.getView(itemView, R.id.civ__mentioning_image);
            image.setOnClickListener(this);
            name = ViewUtils.getView(itemView, R.id.ttv__mentioning_name);
            if (name != null) {
                name.setOnClickListener(this);
            }
        }

        public void setUser(User user) {
            if (this.user != null) {
                if (this.image != null) {
                    this.image.setUser(null);
                }
                this.user.removeUpdateListener(this);
                this.user = null;
            }
            this.user = user;
            if (this.user != null) {
                this.user.addUpdateListener(this);
                if (this.image != null) {
                    image.setUser(user);
                }
            }
            updated();
        }

        @Override
        public void updated() {
            if (name == null) {
                return;
            }
            name.setText(user != null ? user.getDisplayName() : "");
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(user);
            }
        }
    }
}
