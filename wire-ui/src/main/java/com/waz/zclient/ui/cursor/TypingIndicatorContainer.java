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
package com.waz.zclient.ui.cursor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class TypingIndicatorContainer extends FrameLayout {
    public static final String TAG = TypingIndicatorContainer.class.getName();

    private TypingIndicator typingIndicator;

    public TypingIndicatorContainer(Context context) {
        super(context);
    }

    public TypingIndicatorContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TypingIndicatorContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addTypingIndicatorView(View view) {
        addView(view);
        if (!(view instanceof TypingIndicator)) {
            throw new IllegalStateException("The view added as typing indicator has to implement TypingIndicator");
        }
        typingIndicator = (TypingIndicator) view;
    }

    public void setSelfIsTyping(boolean isTyping) {
        if (typingIndicator == null) {
            return;
        }
        typingIndicator.showSelfTyping(isTyping);
    }

    public void setOtherIsTyping(boolean isTyping) {
        if (typingIndicator == null) {
            return;
        }
    }

}
