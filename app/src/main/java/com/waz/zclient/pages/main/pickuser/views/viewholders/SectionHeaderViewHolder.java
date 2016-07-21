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
package com.waz.zclient.pages.main.pickuser.views.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.waz.zclient.R;
import com.waz.zclient.pages.main.pickuser.SearchResultAdapter;
import com.waz.zclient.utils.ViewUtils;

public class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView sectionHeaderView;

    public SectionHeaderViewHolder(View itemView) {
        super(itemView);
        sectionHeaderView = ViewUtils.getView(itemView, R.id.ttv_startui_section_header);
    }

    public void bind(int itemType) {
        String title = "";
        switch (itemType) {
            case SearchResultAdapter.ITEM_TYPE_TOP_USER:
                title = sectionHeaderView.getContext().getResources().getString(R.string.people_picker__top_users_header_title);
                break;
            case SearchResultAdapter.ITEM_TYPE_CONVERSATION:
                title = sectionHeaderView.getContext().getResources().getString(R.string.people_picker__search_result_conversations_header_title);
                break;
            case SearchResultAdapter.ITEM_TYPE_CONNECTED_USER:
                title = sectionHeaderView.getContext().getResources().getString(R.string.people_picker__search_result_connections_header_title);
                break;
            case SearchResultAdapter.ITEM_TYPE_OTHER_USER:
                title = sectionHeaderView.getContext().getResources().getString(R.string.people_picker__search_result_others_header_title);
                break;
            case SearchResultAdapter.ITEM_TYPE_CONTACT:
                title = sectionHeaderView.getContext().getResources().getString(R.string.people_picker__search_result_contacts_header_title);
                break;

        }
        sectionHeaderView.setText(title);
    }
}
