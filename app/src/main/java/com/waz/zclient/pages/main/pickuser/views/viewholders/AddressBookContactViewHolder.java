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
import com.waz.api.Contact;
import com.waz.zclient.R;
import com.waz.zclient.pages.main.pickuser.views.ContactRowView;
import com.waz.zclient.utils.ViewUtils;

public class AddressBookContactViewHolder extends RecyclerView.ViewHolder {
    private ContactRowView contactRowView;

    public AddressBookContactViewHolder(View itemView, boolean darkTheme) {
        super(itemView);
        contactRowView = ViewUtils.getView(itemView, R.id.crv__contactlist_user);
        if (darkTheme) {
            contactRowView.applyDarkTheme();
        }
    }

    public void bind(Contact contact, ContactRowView.Callback callback, int accentColor) {
        contactRowView.setCallback(callback);
        contactRowView.setContact(contact);
        contactRowView.setAccentColor(accentColor);
    }
}
