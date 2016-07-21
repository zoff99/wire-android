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
package com.waz.zclient.core.controllers.tracking.events.settings;

import android.support.annotation.NonNull;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.events.Event;

public class ChangedSoundNotificationLevelEvent extends Event {

    public enum Level {
        NO_SOUNDS("no_sounds"),
        SOME_SOUNDS("some_sounds"),
        ALL_SOUNDS("all_sounds");
        public final String name;

        Level(String tagName) {
            name = tagName;
        }

    }

    public ChangedSoundNotificationLevelEvent(Level level) {
        attributes.put(Attribute.VALUE, level.name());
    }

    @NonNull
    @Override
    public String getName() {
        return "settings.changed_sound_notification_level";
    }
}
