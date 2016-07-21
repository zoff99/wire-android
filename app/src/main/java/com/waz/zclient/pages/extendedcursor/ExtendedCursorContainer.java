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
package com.waz.zclient.pages.extendedcursor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.waz.api.AudioAssetForUpload;
import com.waz.api.AudioEffect;
import com.waz.zclient.R;
import com.waz.zclient.controllers.globallayout.KeyboardHeightObserver;
import com.waz.zclient.controllers.globallayout.KeyboardVisibilityObserver;
import com.waz.zclient.pages.extendedcursor.voicefilter.VoiceFilterLayout;
import com.waz.zclient.ui.animation.interpolators.penner.Expo;
import com.waz.zclient.ui.utils.KeyboardUtils;
import com.waz.zclient.utils.ViewUtils;

public class ExtendedCursorContainer extends FrameLayout implements KeyboardHeightObserver,
                                                                    KeyboardVisibilityObserver,
                                                                    VoiceFilterLayout.Callback {
    public static final String TAG = ExtendedCursorContainer.class.getSimpleName();
    private static final String PREF__NAME = "PREF__NAME";
    private static final String PREF__KEY__KEYBOARD_HEIGHT = "PREF__KEY__KEYBOARD_HEIGHT";
    private static final String PREF__KEY__KEYBOARD_HEIGHT_LANDSCAPE = "PREF__KEY__KEYBOARD_HEIGHT_LANDSCAPE";
    private final SharedPreferences sharedPreferences;
    private int defaultExtendedContainerHeight;

    private Type type;
    private int accentColor;
    private Callback callback;
    private boolean isExpanded;

    private VoiceFilterLayout voiceFilterLayout;

    private int keyboardHeightLandscape;
    private int keyboardHeight;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public ExtendedCursorContainer(Context context) {
        this(context, null);
    }

    public ExtendedCursorContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtendedCursorContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedPreferences = getContext().getSharedPreferences(PREF__NAME, Context.MODE_PRIVATE);
        accentColor = getResources().getColor(R.color.accent_blue);
        isExpanded = false;
        type = Type.NONE;
        initKeyboardHeight();
    }

    private void initKeyboardHeight() {
        defaultExtendedContainerHeight = getResources().getDimensionPixelSize(R.dimen.extend_container_height);

        if (sharedPreferences.contains(PREF__KEY__KEYBOARD_HEIGHT)) {
            keyboardHeight = sharedPreferences.getInt(PREF__KEY__KEYBOARD_HEIGHT,
                                                      getResources().getDimensionPixelSize(R.dimen.extend_container_height));
        } else {
            keyboardHeight = -1;
        }

        if (sharedPreferences.contains(PREF__KEY__KEYBOARD_HEIGHT_LANDSCAPE)) {
            keyboardHeightLandscape = sharedPreferences.getInt(PREF__KEY__KEYBOARD_HEIGHT_LANDSCAPE,
                                                               getResources().getDimensionPixelSize(R.dimen.extend_container_height));
        } else {
            keyboardHeightLandscape = -1;
        }
    }

    public void setKeyboardHeight(int height) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (height > keyboardHeightLandscape && height > defaultExtendedContainerHeight) {
                keyboardHeightLandscape = height;
                sharedPreferences.edit().putInt(PREF__KEY__KEYBOARD_HEIGHT_LANDSCAPE, height).apply();
            }
        } else {
            if (height > keyboardHeight && height > defaultExtendedContainerHeight) {
                keyboardHeight = height;
                sharedPreferences.edit().putInt(PREF__KEY__KEYBOARD_HEIGHT_LANDSCAPE, height).apply();
            }
        }

        updateHeight();
    }

    private void updateHeight() {
        int newHeight = defaultExtendedContainerHeight;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (keyboardHeightLandscape != -1) {
                newHeight = keyboardHeightLandscape;
            }
        } else {
            if (keyboardHeight != -1) {
                newHeight = keyboardHeight;
            }
        }

        getLayoutParams().height = newHeight;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateHeight();
    }

    public void openWithType(Type type) {
        if (this.type == type) {
            return;
        }

        this.type = type;

        removeAllViews();

        switch (type) {
            case VOICE_FILTER_RECORDING:
                voiceFilterLayout = (VoiceFilterLayout) LayoutInflater.from(getContext()).inflate(R.layout.voice_filter_layout,
                                                                                                  this,
                                                                                                  false);
                voiceFilterLayout.setAccentColor(accentColor);
                voiceFilterLayout.setCallback(this);
                addView(voiceFilterLayout);
                break;
        }

        if (KeyboardUtils.isKeyboardVisible(getContext())) {
            KeyboardUtils.closeKeyboardIfShown((Activity) getContext());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateUp();
                }
            }, getResources().getInteger(R.integer.animation_delay_short));
        } else if (!isExpanded) {
            animateUp();
        }
    }

    private void animateUp() {
        setTranslationY(ViewUtils.toPx(getContext(), 160));
        animate()
            .translationY(0)
            .setDuration(150)
            .setInterpolator(new Expo.EaseOut())
            .withStartAction(new Runnable() {
                @Override
                public void run() {
                    setVisibility(View.VISIBLE);
                }
            })
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    isExpanded = true;
                }
            });
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
        switch (type) {
            case NONE:
                break;
            case VOICE_FILTER_RECORDING:
                voiceFilterLayout.setAccentColor(accentColor);
                break;
        }
    }

    @Override
    public void onKeyboardHeightChanged(int keyboardHeight) {
        setKeyboardHeight(keyboardHeight);
    }

    @Override
    public void onKeyboardVisibilityChanged(boolean keyboardIsVisible, int keyboardHeight, View currentFocus) {
        if (keyboardIsVisible) {
            close(true);
        }
    }

    public void close(boolean immediate) {
        type = Type.NONE;

        if (voiceFilterLayout != null) {
            voiceFilterLayout.onClose();
        }

        if (immediate) {
            isExpanded = false;
            setVisibility(View.GONE);
            return;
        }

        setTranslationY(0);
        animate()
            .translationY(ViewUtils.toPx(getContext(), 160))
            .setDuration(150)
            .setInterpolator(new Expo.EaseOut())
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    isExpanded = false;
                    setVisibility(View.GONE);
                }
            });
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    @Override
    public void onCancel() {
        close(false);
    }

    @Override
    public void onAudioMessageRecordingStarted() {
        callback.onAudioMessageRecordingStarted();
    }

    @Override
    public void sendRecording(AudioAssetForUpload audioAssetForUpload, AudioEffect appliedAudioEffect) {
        callback.onSendAudioMessage(audioAssetForUpload, appliedAudioEffect);
        close(false);
    }

    public enum Type {
        NONE,
        VOICE_FILTER_RECORDING
    }

    public interface Callback {
        void onAudioMessageRecordingStarted();

        void onSendAudioMessage(AudioAssetForUpload audioAssetForUpload, AudioEffect appliedAudioEffect);
    }
}
