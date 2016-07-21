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
package com.waz.zclient.pages.main.connect;

import android.widget.AbsListView;

public class ConnectRequestInboxOnScrollListener implements AbsListView.OnScrollListener {

    public interface Callback {
        int getVerticalScrollOffset();
    }

    private Callback callback;

    private boolean userHasScrolled  = false;
    private int currentFirstVisibleItem = 0;

    private int scrollOffsetY;

    public ConnectRequestInboxOnScrollListener(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            userHasScrolled = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!userHasScrolled) {
            return;
        }

        updateVerticalScrollOffet();

        // First visible item has changed
        if (currentFirstVisibleItem != firstVisibleItem) {
            onFirstVisibleItemChanged(firstVisibleItem);
        }
    }

    private void onFirstVisibleItemChanged(int firstVisibleItem) {
        currentFirstVisibleItem = firstVisibleItem;
    }

    private void updateVerticalScrollOffet() {
        int newScrollOffsetY = callback.getVerticalScrollOffset();
        if (scrollOffsetY != newScrollOffsetY) {
            scrollOffsetY = newScrollOffsetY;
        }
    }
}
