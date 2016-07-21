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
package com.waz.zclient.pages.main.conversation.views.header;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.waz.api.MediaAsset;
import com.waz.api.Message;
import com.waz.zclient.R;
import com.waz.zclient.controllers.mediaplayer.MediaPlayerState;
import com.waz.zclient.controllers.navigation.PagerControllerObserver;
import com.waz.zclient.controllers.streammediaplayer.IStreamMediaPlayerController;
import com.waz.zclient.controllers.streammediaplayer.StreamMediaPlayerObserver;
import com.waz.zclient.pages.BaseFragment;
import com.waz.zclient.ui.animation.interpolators.penner.Quart;
import com.waz.zclient.ui.text.TypefaceTextView;
import com.waz.zclient.ui.theme.ThemeUtils;
import com.waz.zclient.utils.LayoutSpec;
import com.waz.zclient.utils.ViewUtils;

public class StreamMediaPlayerBarFragment extends BaseFragment<StreamMediaPlayerBarFragment.Container> implements StreamMediaPlayerObserver,
                                                                                                                  PagerControllerObserver {
    public static final String TAG = StreamMediaPlayerBarFragment.class.getName();

    private TypefaceTextView titleTypefaceTextView;
    private TypefaceTextView subTitleTypefaceTextView;
    private Toolbar toolbar;

    private IStreamMediaPlayerController streamMediaController;
    private View view;

    public static StreamMediaPlayerBarFragment newInstance() {
        return new StreamMediaPlayerBarFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.conversation_header_mediabar, container, false);
        toolbar = ViewUtils.getView(view, R.id.tb__conversation_header__mediabar);
        toolbar.inflateMenu(R.menu.conversaton_header_mediabar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.close:
                        if (streamMediaController == null) {
                            return false;
                        }
                        Message mediaTrackMetaData = streamMediaController.getMessage();
                        streamMediaController.stop(mediaTrackMetaData.getConversationId());
                        return true;
                }
                return false;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (streamMediaController == null) {
                    return;
                }
                Message mediaTrackMetaData = streamMediaController.getMessage();
                MediaPlayerState mediaPlayerState = streamMediaController.getMediaPlayerState(mediaTrackMetaData);
                if (mediaPlayerState.isPauseControl()) {
                    streamMediaController.pause(mediaTrackMetaData.getConversationId());
                } else if (mediaPlayerState.isPlayControl()) {
                    streamMediaController.play(mediaTrackMetaData.getConversationId());
                }
            }
        });
        titleTypefaceTextView = ViewUtils.getView(view, R.id.ttv__conversation_header__mediabar__title);
        subTitleTypefaceTextView = ViewUtils.getView(view, R.id.ttv__conversation_header__mediabar__subtitle);
        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (streamMediaController == null) {
                    return;
                }
                streamMediaController.requestScroll();
            }
        };
        toolbar.setOnClickListener(clickListener);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        streamMediaController = getControllerFactory().getStreamMediaPlayerController();
        streamMediaController.addStreamMediaObserver(this);
        if (LayoutSpec.isTablet(getActivity())) {
            getControllerFactory().getNavigationController().addPagerControllerObserver(this);
        }
        updateInformation();
    }

    @Override
    public void onStop() {
        if (LayoutSpec.isTablet(getActivity())) {
            getControllerFactory().getNavigationController().removePagerControllerObserver(this);
        }
        streamMediaController.removeStreamMediaObserver(this);
        streamMediaController = null;
        super.onStop();
    }

    private void updateInformation() {
        //TODO titleTypefaceTextView null check addresses a crash encountered in RC testing, but this is a stop-gap
        //     and needs to be investigated further. See log attached in AN-1263.
        if (streamMediaController == null ||
            titleTypefaceTextView == null ||
            subTitleTypefaceTextView == null) {
            return;
        }
        MediaAsset mediaTrack = streamMediaController.getMediaTrack();
        titleTypefaceTextView.setText(mediaTrack.getArtistName());
        subTitleTypefaceTextView.setText(mediaTrack.getTitle());
        updateMediaPlayerIndicator();
    }

    private void updateMediaPlayerIndicator() {
        if (streamMediaController == null) {
            return;
        }
        Message trackMetaData = streamMediaController.getMessage();
        MediaPlayerState mediaPlayerState = streamMediaController.getMediaPlayerState(trackMetaData);
        if (mediaPlayerState == MediaPlayerState.PlaybackCompleted) {
            return;
        }
        if (mediaPlayerState.isPauseControl()) {
            toolbar.setNavigationIcon(ThemeUtils.isDarkTheme(getContext()) ? R.drawable.ic_action_pause_light : R.drawable.ic_action_pause_dark);
        } else if (mediaPlayerState.isPlayControl()) {
            toolbar.setNavigationIcon(ThemeUtils.isDarkTheme(getContext()) ? R.drawable.ic_action_play_light : R.drawable.ic_action_play_dark);
        }
    }

    public void show() {
        if (view == null || view.getVisibility() == View.VISIBLE) {
            return;
        }
        view.animate()
            .alpha(1f)
            .setDuration(getResources().getInteger(R.integer.framework_animation_duration_medium))
            .setInterpolator(new Quart.EaseOut())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }
            })
            .start();
    }

    public void hide() {
        if (view == null || view.getVisibility() == View.GONE) {
            return;
        }
        view.animate()
            .alpha(0f)
            .setDuration(getResources().getInteger(R.integer.framework_animation_duration_medium))
            .setInterpolator(new Quart.EaseOut())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            })
            .start();
    }

    public boolean isShown() {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    ////////////////////////////////////////////////////////////
    //
    //  StreamMediaPlayerObserver
    //
    ////////////////////////////////////////////////////////////

    @Override
    public void onPlay(Message message) {
        updateInformation();
    }

    @Override
    public void onPause(Message message) {
        updateMediaPlayerIndicator();
    }

    // CHECKSTYLE:OFF
    @Override
    public void onStop(Message message) {
        updateInformation();
    }
    // CHECKSTYLE:ON

    @Override
    public void onPrepared(Message message) {
        updateMediaPlayerIndicator();
    }

    @Override
    public void onError(Message message) {
        updateInformation();
    }

    @Override
    public void onComplete(Message message) {
        updateInformation();
    }

    @Override
    public void onTrackChanged(Message newMessage) {
        hide();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (view == null || view.getVisibility() == View.GONE) {
            return;
        }
        view.setAlpha(positionOffset);
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPagerEnabledStateHasChanged(boolean enabled) {

    }

    public interface Container {
    }
}
