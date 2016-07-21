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
package com.waz.zclient.pages.main.onboarding;

public enum OnBoardingHintType {
    NONE("NONE"),
    DISCOVER_CHANGE_PROFILE_PICTURE("DISCOVER_CHANGE_PROFILE_PICTURE"),
    DISCOVER_CHANGE_ACCENT_COLOR("DISCOVER_CHANGE_ACCENT_COLOR"),
    DISCOVER_CHANGE_CONVERSATION_COLOR("DISCOVER_CHANGE_CONVERSATION_COLOR"),
    SHARE_CONTACTS("SHARE_CONTACTS"),
    INVITATION_BANNER("INVITATION_BANNER");

    public final String name;

    OnBoardingHintType(String name) {
        this.name = name;
    }
}
