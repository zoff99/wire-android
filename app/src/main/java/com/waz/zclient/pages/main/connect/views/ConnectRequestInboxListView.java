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
import android.widget.ListView;
import com.waz.zclient.utils.LayoutSpec;
import com.waz.zclient.pages.main.connect.ConnectRequestInboxOnScrollListener;

// TODO: Replace with normal ListView if possible AN-2650
public class ConnectRequestInboxListView extends ListView {

    private ConnectRequestInboxOnScrollListener onScrollListener;

    public ConnectRequestInboxListView(Context context) {
        this(context, null);
    }

    public ConnectRequestInboxListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnectRequestInboxListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (LayoutSpec.isPhone(getContext())) {
            onScrollListener = new ConnectRequestInboxOnScrollListener(new ConnectRequestInboxOnScrollListener.Callback() {
                @Override
                public int getVerticalScrollOffset() {
                    return computeVerticalScrollOffset();
                }
            });
            this.setOnScrollListener(onScrollListener);
        }
    }
}
