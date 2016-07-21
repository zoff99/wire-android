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
package com.waz.zclient.controllers.mediaplayer;

import java.util.EnumSet;

public enum MediaPlayerState {
    // Official states
    Idle, Initialized, Prepared, Started, Paused,
    Stopped, PlaybackCompleted, End, Error;

    private final static EnumSet<MediaPlayerState> GET_CURRENT_POSITION_ALLOWED_STATES =
        // Are usually Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted
        EnumSet.of(Started, Paused);
    private final static EnumSet<MediaPlayerState> GET_DURATION_ALLOWED_STATES =
        EnumSet.of(Prepared, Started, Paused, Stopped, PlaybackCompleted);
    private final static EnumSet<MediaPlayerState> IS_PLAYING_ALLOWED_STATES =
        EnumSet.of(Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted);
    private final static EnumSet<MediaPlayerState> PAUSE_ALLOWED_STATES =
        EnumSet.of(Started, Paused, PlaybackCompleted);
    private final static EnumSet<MediaPlayerState> PREPARE_ALLOWED_STATES =
        EnumSet.of(Initialized, Stopped);
    private final static EnumSet<MediaPlayerState> RELEASE_ALLOWED_STATES =
        EnumSet.allOf(MediaPlayerState.class);
    private final static EnumSet<MediaPlayerState> RESET_ALLOWED_STATES =
        EnumSet.allOf(MediaPlayerState.class);
    private final static EnumSet<MediaPlayerState> SEEK_ALLOWED_STATES =
        EnumSet.of(Prepared, Started, Paused, PlaybackCompleted);
    private final static EnumSet<MediaPlayerState> SET_AUDIO_STREAM_TYPE_ALLOWED_STATES =
        EnumSet.of(Idle, Initialized, Stopped, Prepared, Started, Paused, PlaybackCompleted);
    private final static EnumSet<MediaPlayerState> SET_DATASOURCE_ALLOWED_STATES =
        EnumSet.of(Idle);
    private final static EnumSet<MediaPlayerState> START_ALLOWED_STATES =
        EnumSet.of(Prepared, Started, Paused, PlaybackCompleted);
    private final static EnumSet<MediaPlayerState> STOP_ALLOWED_STATES =
        EnumSet.of(Prepared, Started, Stopped, Paused, PlaybackCompleted);


    private final static EnumSet<MediaPlayerState> PAUSE_CONTROL_STATES =
        EnumSet.of(Started);
    private final static EnumSet<MediaPlayerState> PLAY_CONTROL_STATES =
        EnumSet.of(Idle, Stopped, Paused, PlaybackCompleted);
    private final static EnumSet<MediaPlayerState> REQUIRE_INITIALIZATION_STATES =
        EnumSet.of(Error);
    private final static EnumSet<MediaPlayerState> SCHEDULE_TIME_UPDATES =
        EnumSet.of(Idle, Initialized, Prepared, Started, Paused);

    public final boolean isPauseControl() {
        return PAUSE_CONTROL_STATES.contains(this);
    }

    public final boolean isPlayControl() {
        return PLAY_CONTROL_STATES.contains(this);
    }

    public final boolean isGetCurrentPositionAllowed() {
        return GET_CURRENT_POSITION_ALLOWED_STATES.contains(this);
    }

    public final boolean isGetDurationAllowed() {
        return GET_DURATION_ALLOWED_STATES.contains(this);
    }

    public final boolean isIsPlayingAllowed() {
        return IS_PLAYING_ALLOWED_STATES.contains(this);
    }

    public final boolean isPauseAllowed() {
        return PAUSE_ALLOWED_STATES.contains(this);
    }

    public final boolean isPrepareAllowed() {
        return PREPARE_ALLOWED_STATES.contains(this);
    }

    public final boolean isReleaseAllowed() {
        return RELEASE_ALLOWED_STATES.contains(this);
    }

    public final boolean isSetAudioStreamTypeAllowed() {
        return SET_AUDIO_STREAM_TYPE_ALLOWED_STATES.contains(this);
    }

    public final boolean isSetDatasourceAllowed() {
        return SET_DATASOURCE_ALLOWED_STATES.contains(this);
    }

    public final boolean isStopAllowed() {
        return STOP_ALLOWED_STATES.contains(this);
    }

    public final boolean isStartAllowed() {
        return START_ALLOWED_STATES.contains(this);
    }

    public final boolean isSeekToAllowed() {
        return SEEK_ALLOWED_STATES.contains(this);
    }

    public final boolean isResetAllowed() {
        return RESET_ALLOWED_STATES.contains(this);
    }

    public final boolean isTimeUpdateScheduleAllowed() {
        return SCHEDULE_TIME_UPDATES.contains(this);
    }

    public boolean needInitialization() {
        return REQUIRE_INITIALIZATION_STATES.contains(this);
    }
}
