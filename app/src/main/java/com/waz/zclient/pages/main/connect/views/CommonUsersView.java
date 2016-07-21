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
package com.waz.zclient.pages.main.connect.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.waz.api.User;
import com.waz.zclient.R;
import com.waz.zclient.pages.main.participants.views.ChatheadWithTextFooter;
import com.waz.zclient.ui.theme.ThemeUtils;
import com.waz.zclient.utils.ViewUtils;

public class CommonUsersView extends FrameLayout {
    public static final int MAX_NUM_CHATHEADS = 3;
    private int chatheadWidth;
    private int chatheadNameWidth;
    private LinearLayout rowContainer;
    private FrameLayout othersCounterContainer;
    private TextView titleView;

    public CommonUsersView(Context context) {
        super(context);
        init();
    }

    public CommonUsersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommonUsersView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        View container = LayoutInflater.from(getContext()).inflate(R.layout.connect_request__common_users, this, true);
        rowContainer = ViewUtils.getView(container, R.id.ll__connect_request__common_users__chatheadrow);
        titleView = ViewUtils.getView(container, R.id.ttv__connect_request__common_users__label);
        othersCounterContainer = ViewUtils.getView(container,
                                                   R.id.fl__connect_request__common_users__others_counter_container);

        chatheadWidth = getContext().getResources().getDimensionPixelSize(R.dimen.connect_request__common_users__chathead__width);
        chatheadNameWidth = getContext().getResources().getDimensionPixelSize(R.dimen.connect_request__common_users__chathead_name__width);
    }

    public void setCommonUsers(User[] topUsers, int totalCount, CommonUsersCallback callback) {
        rowContainer.removeAllViews();

        // Toggle title
        if (topUsers.length == 0) {
            titleView.setVisibility(GONE);
        } else {
            titleView.setVisibility(VISIBLE);
        }

        // Add chatheads
        for (int i = 0; i < topUsers.length; i++) {
            if (i == MAX_NUM_CHATHEADS) {
                break;
            }
            addChatheadView(topUsers[i], callback);
        }

        // Counter for number of other shared users
        if (totalCount > MAX_NUM_CHATHEADS) {
            addOthersCounterView(totalCount - MAX_NUM_CHATHEADS);
        } else {
            othersCounterContainer.removeAllViews();
        }
    }

    public void setGravity(int gravity) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rowContainer.getLayoutParams();
        params.gravity = gravity;
        rowContainer.setLayoutParams(params);
        titleView.setGravity(gravity);
    }

    private void addChatheadView(final User user, final CommonUsersCallback callback) {
        ChatheadWithTextFooter chathead = new ChatheadWithTextFooter(getContext());
        chathead.setUser(user);
        chathead.setChatheadFooterFont(getResources().getString(R.string.wire__typeface__light));
        chathead.setChatheadDimension(chatheadWidth);
        chathead.setFooterWidth(chatheadNameWidth);

        int textColor;
        if (ThemeUtils.isDarkTheme(getContext())) {
            textColor = getResources().getColor(R.color.text__primary_dark);
        } else {
            textColor = getResources().getColor(R.color.text__primary_light);
        }
        chathead.setChatheadFooterTextColor(textColor);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                                                             FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        chathead.setLayoutParams(layoutParams);
        rowContainer.addView(chathead);

        // On click listener for chathead
        chathead.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onCommonUserClicked(view, user);
            }
        });
    }

    private void addOthersCounterView(int othersSize) {
        othersCounterContainer.removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.connect_request__common_users__others,
                                                  othersCounterContainer,
                                                  true);
        TextView counterView = ViewUtils.getView(this, R.id.ttv__connect_request__common_users__connection_counter);

        counterView.setText(String.format(getResources().getString(R.string.connect_request__common_users__connection_counter),
                                          othersSize));
    }
}
