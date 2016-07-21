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
package com.waz.zclient.controllers.tracking;

import android.content.Context;
import android.content.SharedPreferences;
import com.waz.api.Message;
import com.waz.model.MessageContent;
import com.waz.service.media.RichMediaContentParser;
import com.waz.zclient.core.controllers.tracking.attributes.RangedAttribute;

public class SessionEventAggregator {

    private static final String PREF_SESSION_START_TIME = "PREF_SESSION_START_TIME";
    private static final String PREF_PAUSE_TIME = "PREF_PAUSE_TIME";
    private static final String PREF_IS_FIRST_SESSION = "PREF_IS_FIRST_SESSION";
    private static final String PREF_SEARCHED_FOR_PEOPLE = "PREF_SEARCHED_FOR_PEOPLE";

    private SharedPreferences trackingPrefs;

    public SessionEventAggregator(Context context) {
        trackingPrefs = context.getSharedPreferences("TRACKING_PREFS", Context.MODE_PRIVATE);
        if (trackingPrefs.getLong(PREF_SESSION_START_TIME, 0) == 0) {
            markSessionStartTime();
        }
    }

    public void incrementEventCount(RangedAttribute attribute, String... params) {
        incrementRichMediaLinkCounts(attribute, params);
        incrementEventCountBy(attribute, 1);
    }

    public int getEventCount(RangedAttribute attribute) {
        if (attribute == RangedAttribute.SESSION_DURATION) {
            return getSecondsSinceSessionStart();
        }
        return trackingPrefs.getInt(attribute.name, 0);
    }

    public void markPauseTime() {
        trackingPrefs.edit().putLong(PREF_PAUSE_TIME, System.currentTimeMillis()).apply();
    }

    public long getElapsedTimeSincePause() {
        long currentTime = System.currentTimeMillis();
        return currentTime - trackingPrefs.getLong(PREF_PAUSE_TIME, currentTime);
    }

    public void markAsFirstSession() {
        trackingPrefs.edit().putBoolean(PREF_IS_FIRST_SESSION, true).apply();
    }

    public boolean isFirstSession() {
        return trackingPrefs.getBoolean(PREF_IS_FIRST_SESSION, false);
    }

    public void searchedForPeople() {
        if (!hasSearchedForPeople()) {
            trackingPrefs.edit().putBoolean(PREF_SEARCHED_FOR_PEOPLE, true).apply();
        }
    }

    public boolean hasSearchedForPeople() {
        return trackingPrefs.getBoolean(PREF_SEARCHED_FOR_PEOPLE, false);
    }

    protected void restartSession() {
        trackingPrefs.edit().clear().apply();
        markSessionStartTime();
    }

    private void incrementRichMediaLinkCounts(RangedAttribute attribute, String[] params) {
        if (attribute == RangedAttribute.TEXT_MESSAGES_SENT && params.length > 0) {
            String messageBody = params[0];

            int youtubeLinks = 0;
            int soundCloudLinks = 0;

            RichMediaContentParser parser = new RichMediaContentParser();
            for (MessageContent richMediaPair : parser.javaSplitContent(messageBody)) {

                if (richMediaPair.tpe() == Message.Part.Type.SOUNDCLOUD) {
                    soundCloudLinks++;
                }
                if (richMediaPair.tpe() == Message.Part.Type.YOUTUBE) {
                    youtubeLinks++;
                }
            }

            incrementEventCountBy(RangedAttribute.YOUTUBE_LINKS_SENT, youtubeLinks);
            incrementEventCountBy(RangedAttribute.SOUNDCLOUD_LINKS_SENT, soundCloudLinks);
        }
    }

    private void incrementEventCountBy(RangedAttribute attribute, int incrementBy) {
        trackingPrefs.edit().putInt(attribute.name, getEventCount(attribute) + incrementBy).apply();
    }

    private void markSessionStartTime() {
        trackingPrefs.edit().putLong(PREF_SESSION_START_TIME, System.currentTimeMillis()).apply();
    }

    private int getSecondsSinceSessionStart() {
        long currentTime = System.currentTimeMillis();
        return (int) ((currentTime - trackingPrefs.getLong(PREF_SESSION_START_TIME, currentTime)) / 1000);
    }
}
