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
package com.waz.zclient.pages.main.pickuser;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import com.waz.api.User;
import com.waz.zclient.R;
import com.waz.zclient.ui.text.SpannableEditText;
import com.waz.zclient.utils.ViewUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PickUserEditText extends SpannableEditText implements TextWatcher {

    /**
     * This is needed so that the ends of the shrunken cursor fill back out to full size. Not too
     * sure why it's 2...
     */
    private static final int EXTRA_PADDING_DP = 2;


    // Stores value of search filter
    private boolean flagNotifyAfterTextChanged = true;
    private String hintTextSmallScreen = "";
    private int hintTextSize;

    private boolean lightTheme;
    private Set<User> users;

    private boolean hasText = false;

    public interface Callback {
        void onRemovedTokenSpan(User user);

        void afterTextChanged(String s);
    }

    private Callback callback;

    public PickUserEditText(Context context) {
        this(context, null);
    }

    public PickUserEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickUserEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributes(attrs);
        init();
    }

    public void applyLightTheme(boolean lightTheme) {
        this.lightTheme = lightTheme;
    }

    private void initAttributes(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PickUserEditText);
        hintTextSmallScreen = a.getString(R.styleable.PickUserEditText_hintSmallScreen);
        hintTextSize = a.getDimensionPixelSize(R.styleable.PickUserEditText_hintTextSize, 0);
        a.recycle();
    }

    private void init() {
        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        setLongClickable(false);
        setTextIsSelectable(false);
        setBackground(null);
        addTextChangedListener(this);
        setHintText(getHint());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        adjustHintTextForSmallScreen();
    }

    public void setHintText(CharSequence newHint) {
        SpannableString span = new SpannableString(newHint);
        span.setSpan(new AbsoluteSizeSpan(hintTextSize), 0, newHint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setHint(span);
    }

    public void setCallback(final Callback callback) {
        this.callback = callback;

        // Super callback
        super.setCallback(new SpannableEditText.Callback() {
            @Override
            public void onRemovedTokenSpan(String id) {
                for (User user : users) {
                    if (user.getId().equals(id)) {
                        callback.onRemovedTokenSpan(user);
                        return;
                    }
                }
            }

            @Override
            public void onClick(View v) {}
        });
    }

    /**
     * On Marshmallow devices, when there is no text in the SearchBox, then the cursor shrinks and rises.
     * The reason for this is that you can't specify a height for the cursor in default EditText, instead
     * the height is always calculated by the padding. This little hack reverse calculates what that
     * padding would be to in order to match the desired height.
     * @param cursorDrawable
     */
    @Override
    protected void setHintCursorSize(ShapeDrawable cursorDrawable) {
        if (hasText || Build.VERSION.SDK_INT <= 22) {
            return;
        }
        int padding = ViewUtils.toPx(getContext(), EXTRA_PADDING_DP);
        int textSizeDifferencePx = (int) getTextSize() - hintTextSize;
        int bottomPadding = textSizeDifferencePx + padding;
        cursorDrawable.setPadding(0, padding, 0, bottomPadding);
    }

    public void addUser(final User user) {
        if (hasToken(user)) {
            return;
        }
        if (users == null) {
            users = new HashSet<>();
        }
        users.add(user);
        flagNotifyAfterTextChanged = false;
        addUserToken(user.getId(), user.getDisplayName());
        flagNotifyAfterTextChanged = true;
        clearNonSpannableText();
        resetDeleteModeForSpans();
    }

    private boolean hasToken(User user) {
        if (user == null) {
            return false;
        }
        final Editable buffer = getText();
        final TokenSpan[] spans = buffer.getSpans(0, buffer.length(), TokenSpan.class);
        for (int i = spans.length - 1; i >= 0; i--) {
            final TokenSpan span = spans[i];
            if (span.getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    public void removeUser(final User user) {
        if (users == null ||
            !hasToken(user) ||
            !removeSpan(user.getId())) {
            return;
        }
        users.remove(user);
        resetDeleteModeForSpans();
    }

    public void setSelectedUsers(List<User> users) {
        if (this.users != null && equalLists(users, new ArrayList<>(this.users))) {
            return;
        }
        this.users = new HashSet<>(users);
        notifyDatasetChanged();
    }

    private boolean equalLists(List<User> one, List<User> two) {
        if (one == null && two == null) {
            return true;
        }
        if (one == null || two == null || one.size() != two.size()) {
            return false;
        }
        return one.containsAll(two) && two.containsAll(one);
    }

    private void notifyDatasetChanged() {
        reset();
        if (users == null) {
            return;
        }
        flagNotifyAfterTextChanged = false;
        for (User user : users) {
            addUserToken(user.getId(), user.getDisplayName());
        }
        flagNotifyAfterTextChanged = true;
    }

    public void reset() {
        clearNonSpannableText();
        setText("");
    }

    public String getSearchFilter() {
        return getNonSpannableText();
    }

    // All this just to detect a backspace, insane! Just a simple onKeyListener
    // doesn't work on all devices / Android versions
    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return new CustomInputConnection(conn, true);
    }

    /**
     * Adds a "bubble" for a user
     */
    private void addUserToken(String userId, String userName) {
        int lineWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
        UserTokenSpan userTokenSpan = new UserTokenSpan(userId, userName, getContext(), false, lineWidth);
        userTokenSpan.setDeleteModeTextColor(getAccentColor());
        if (lightTheme) {
            userTokenSpan.setTextColor(getResources().getColor(R.color.text__primary_light));
        }
        appendSpan(userTokenSpan);
        setSelection(getText().length());
    }

    /**
     * Append typed text always to the end if it was typed somewhere before the last token span
     *
     * @param start  Position of typed text
     * @param before Length of text being replaced by new text
     * @param count  Number of typed characters
     */
    private void moveTypedTextToEnd(int start, int before, int count) {
        Editable buffer = getText();
        ReplacementSpan[] allSpans = buffer.getSpans(0, buffer.length(), ReplacementSpan.class);
        if (allSpans.length <= 0) {
            return;
        }
        ReplacementSpan lastSpan = allSpans[allSpans.length - 1];
        int to = buffer.getSpanStart(lastSpan);

        if (start < to && before == 0) {
            String typedText = buffer.toString().substring(start, start + count);
            buffer.delete(start, start + count);
            append(typedText);
            setSelection(getText().length());
        }
    }

    /**
     * Checks dimensions of hint text and uses secondary hint text if original hint doesnt fit
     */
    private void adjustHintTextForSmallScreen() {
        if (TextUtils.isEmpty(getHint()) || TextUtils.isEmpty(hintTextSmallScreen)) {
            return;
        }
        TextPaint paint = getPaint();
        float hintWidth = paint.measureText(getHint(), 0, getHint().length());
        float availableTextSpace = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        if (hintWidth > availableTextSpace) {
            setHint(hintTextSmallScreen);
        }
    }

    private boolean removeSelectedUserToken() {
        Editable buffer = getText();
        UserTokenSpan[] spans = buffer.getSpans(0, buffer.length(), UserTokenSpan.class);
        for (UserTokenSpan span : spans) {
            if (span.getDeleteMode()) {
                super.removeSpan(span);
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes last span before cursor position if there is a span
     *
     * @return True if a span was deleted
     */
    private boolean deleteSpanBeforeSelection() {
        Editable buffer = getText();
        TokenSpan[] spans = buffer.getSpans(getSelectionStart(), getSelectionEnd(), TokenSpan.class);
        if (spans.length == 0) {
            return false;
        }

        int selectionEnd = getSelectionEnd();
        for (int i = spans.length - 1; i >= 0; i--) {
            TokenSpan span = spans[i];
            int end = buffer.getSpanEnd(span);
            boolean atLineBreak = (getLayout().getLineForOffset(end) != getLayout().getLineForOffset(selectionEnd));
            // Delete span before cursor, special value for offset when selection is at line break
            if (end <= selectionEnd || (end <= (selectionEnd + 1) && atLineBreak)) {
                super.removeSpan(span);
                setSelection(getText().length());
                return true;
            }
        }

        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        moveTypedTextToEnd(start, before, count);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!notifyTextWatcher) {
            return;
        }

        if (flagNotifyAfterTextChanged) {
            callback.afterTextChanged(getSearchFilter());
        }

        boolean hadText = hasText;
        hasText = s.length() > 0;
        if (hadText && !hasText ||
            !hadText && hasText) {
            updateCursor();
        }
    }

    private class CustomInputConnection extends InputConnectionWrapper {

        CustomInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)) &&
                   sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));

        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                if (removeSelectedUserToken()) {
                    return true;
                }
                if (deleteSpanBeforeSelection()) {
                    return true;
                }
            }
            return super.sendKeyEvent(event);
        }
    }
}
