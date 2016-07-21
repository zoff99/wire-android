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
package com.waz.zclient.views.chathead;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ConversationChatheadView extends View {

    public ConversationChatheadView(Context context) {
        this(context, null);
    }

    public ConversationChatheadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationChatheadView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        init(context);
    }

    private void init(Context context) {
        Drawable bgDrawable = new ConversationChatheadDrawable(context);
        setBackground(bgDrawable);

        // Enable for transparent cutout
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
}
