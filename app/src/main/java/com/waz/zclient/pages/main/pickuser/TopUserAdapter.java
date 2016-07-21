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
package com.waz.zclient.pages.main.pickuser;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.waz.api.User;
import com.waz.zclient.R;
import com.waz.zclient.pages.main.pickuser.views.viewholders.TopUserViewHolder;
import java.util.Set;

public class TopUserAdapter extends RecyclerView.Adapter<TopUserViewHolder> {

    private Callback callback;
    private User[] topUsers;

    public TopUserAdapter(Callback callback) {
        this.callback = callback;
    }

    @Override
    public TopUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.startui_top_user, parent, false);
        TopUserViewHolder holder = new TopUserViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(TopUserViewHolder holder, int position) {
        User user = topUsers[position];
        holder.bind(user);
        boolean selected = callback.getSelectedUsers().contains(user);
        holder.setSelected(selected);
    }

    @Override
    public int getItemCount() {
        return topUsers == null ? 0 : topUsers.length;
    }

    public void setTopUsers(User[] topUsers) {
        this.topUsers = topUsers;
        notifyDataSetChanged();
    }

    public void reset() {
        topUsers = null;
    }

    public interface Callback {
        Set<User> getSelectedUsers();
    }
}
