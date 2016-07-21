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
package com.waz.zclient.ui.calling;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.waz.zclient.ui.R;
import com.waz.zclient.ui.text.GlyphTextView;
import com.waz.zclient.ui.text.TypefaceTextView;
import com.waz.zclient.ui.theme.OptionsDarkTheme;
import com.waz.zclient.ui.theme.OptionsLightTheme;
import com.waz.zclient.utils.ViewUtils;

public class CallControlButtonView extends LinearLayout {

    private boolean isPressed;

    private GlyphTextView buttonView;
    private TypefaceTextView buttonLabelView;

    public CallControlButtonView(Context context) {
        this(context, null);
    }

    public CallControlButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CallControlButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setButtonPressed(boolean isPressed) {
        if (this.isPressed == isPressed) {
            return;
        }
        this.isPressed = isPressed;
        if (isPressed) {
            buttonView.setTextColor(new OptionsLightTheme(getContext()).getTextColorPrimarySelector());
            buttonView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.selector__icon_button__background__calling_toggled));
        } else {
            buttonView.setTextColor(new OptionsDarkTheme(getContext()).getTextColorPrimarySelector());
            buttonView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.selector__icon_button__background__calling));
        }
    }

    public void setGlyph(int glyphId) {
        buttonView.setText(getResources().getText(glyphId));
    }

    public void setText(int stringId) {
        buttonLabelView.setText(getResources().getText(stringId));
    }

    private void init(AttributeSet attrs) {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));

        int circleIconDimension = 0;
        String circleIconGlyph = "";
        int circleIconStyle = 0;
        String labelText = "";
        int buttonLabelWidth = 0;
        int labelTextSize = 0;
        String labelFont = "";

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CallControlButtonView, 0, 0);
            circleIconDimension = a.getDimensionPixelSize(R.styleable.CallControlButtonView_circleIconDimension, 0);
            circleIconGlyph = a.getString(R.styleable.CallControlButtonView_circleIconGlyph);
            circleIconStyle = attrs.getStyleAttribute();
            labelText = a.getString(R.styleable.CallControlButtonView_labelText);
            buttonLabelWidth = a.getDimensionPixelSize(R.styleable.CallControlButtonView_labelWidth, 0);
            labelTextSize = a.getDimensionPixelSize(R.styleable.CallControlButtonView_labelTextSize, 0);
            labelFont = a.getString(R.styleable.CallControlButtonView_labelFont);
            a.recycle();
        }

        buttonView = new GlyphTextView(getContext(), null, circleIconStyle);
        buttonView.setLayoutParams(new LayoutParams(circleIconDimension, circleIconDimension));
        buttonView.setText(circleIconGlyph);
        buttonView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.wire__icon_button__text_size));
        buttonView.setGravity(Gravity.CENTER);
        if (circleIconStyle == 0) {
            buttonView.setTextColor(new OptionsDarkTheme(getContext()).getTextColorPrimarySelector());
            buttonView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.selector__icon_button__background__calling));
        }
        addView(buttonView);


        buttonLabelView = new TypefaceTextView(getContext(), null, R.attr.callingControlButtonLabel);
        buttonLabelView.setText(labelText);
        buttonLabelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);
        buttonLabelView.setTypeface(labelFont);
        buttonLabelView.setGravity(Gravity.CENTER);
        if (buttonLabelWidth > 0) {
            addView(buttonLabelView, new LinearLayout.LayoutParams(buttonLabelWidth,
                                                                   ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            addView(buttonLabelView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                   ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        ViewUtils.setMarginTop(buttonLabelView,
                               getContext().getResources().getDimensionPixelSize(R.dimen.calling__controls__button__label__margin_top));
    }
}
