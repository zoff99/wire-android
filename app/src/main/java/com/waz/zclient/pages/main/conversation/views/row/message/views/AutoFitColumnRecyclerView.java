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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class AutoFitColumnRecyclerView extends RecyclerView {

    private int columnWidth;
    private int columnSpacing;
    private SpaceItemDecoration spaceItemDecoration;

    public AutoFitColumnRecyclerView(Context context) {
        this(context, null);
    }

    public AutoFitColumnRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitColumnRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs == null) {
            return;
        }
        int[] attrsArray = {android.R.attr.columnWidth};
        TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
        columnWidth = array.getDimensionPixelSize(0, -1);
        array.recycle();

        setLayoutManager(new GridLayoutManager(getContext(), 1));
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        final LayoutManager lm = getLayoutManager();
        if (columnWidth > 0 && lm instanceof GridLayoutManager) {
            final int width = getMeasuredWidth() + columnSpacing;
            final int itemWidth = columnWidth + columnSpacing;
            final int spanCount = Math.max(1, width / itemWidth);
            removeItemDecoration(spaceItemDecoration);
            ((GridLayoutManager) lm).setSpanCount(spanCount);
            addItemDecoration(spaceItemDecoration);
        }
    }

    public void setColumnSpacing(int columnSpacing) {
        this.columnSpacing = columnSpacing;
        spaceItemDecoration = new SpaceItemDecoration(columnSpacing);
        requestLayout();
    }
}
