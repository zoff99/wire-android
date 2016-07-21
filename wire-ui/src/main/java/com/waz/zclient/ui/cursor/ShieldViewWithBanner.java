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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.waz.api.IConversation;
import com.waz.api.UpdateListener;
import com.waz.api.Verification;
import com.waz.zclient.ui.R;

public class ShieldViewWithBanner extends LinearLayout implements View.OnClickListener, UpdateListener {
    private static final long BANNER_DURATION = 2000;

    private TextView textViewBanner;
    private boolean isShowingBanner;
    private IConversation conversation;
    private Verification verification;

    public ShieldViewWithBanner(Context context) {
        this(context, null);
    }

    public ShieldViewWithBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShieldViewWithBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);
        setEnabled(false);
        checkState();
    }

    public void setConversation(IConversation conversation) {
        if (this.conversation != null) {
            this.conversation.removeUpdateListener(this);
        }
        this.conversation = conversation;
        setAlpha(0);
        setVisibility(View.GONE);
        verification = Verification.UNKNOWN;
        conversation.addUpdateListener(this);
        updated();
    }

    public void tearDown() {
        if (conversation != null) {
            conversation.removeUpdateListener(this);
            conversation = null;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2) {
            throw new IllegalStateException("ShieldViewWithBanner needs 2 children.");
        }

        textViewBanner = (TextView) getChildAt(0);
        textViewBanner.setAlpha(0);
        textViewBanner.setVisibility(View.VISIBLE);
    }

    public void showBanner() {
        isShowingBanner = true;
        textViewBanner
                .animate()
                .alpha(1)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        textViewBanner.setVisibility(View.VISIBLE);
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                closeBanner();
                            }
                        }, BANNER_DURATION);
                    }
                });
    }

    public void closeBanner() {
        textViewBanner
                .animate()
                .alpha(0)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        textViewBanner.setVisibility(View.GONE);
                        isShowingBanner = false;
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (!isShowingBanner) {
            showBanner();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() == enabled) {
            return;
        }
        super.setEnabled(enabled);
        checkState();
    }

    private void checkState() {
        if (verification == Verification.VERIFIED && isEnabled()) {
            showView();
        } else {
            hideView();
        }
    }

    private void showView() {
        animate()
                .alpha(1)
                .setDuration(getResources().getInteger(R.integer.wire__animation__delay__very_short))
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        setVisibility(View.VISIBLE);
                    }
                });
    }

    private void hideView() {
        animate()
                .alpha(0)
                .setDuration(getResources().getInteger(R.integer.wire__animation__delay__very_short))
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        setVisibility(View.VISIBLE);
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void updated() {
        Verification newVerifcation = conversation.getVerified();
        if (verification != Verification.UNKNOWN && newVerifcation == verification) {
            return;
        }

        verification = newVerifcation;
        checkState();
    }
}
