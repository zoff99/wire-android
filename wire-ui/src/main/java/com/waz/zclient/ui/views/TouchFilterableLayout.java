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
package com.waz.zclient.ui.views;

import android.view.ViewGroup;

public interface TouchFilterableLayout<T extends ViewGroup> {
    T getLayout();

    void setOnClickListener(OnClickListener onClickListener);

    void setOnLongClickListener(OnLongClickListener onLongClickListener);

    void setFilterAllClickEvents(boolean filterAllClickEvents);

    interface OnClickListener {
        void onClick();
    }

    interface OnLongClickListener {
        void onLongClick();
    }
}
