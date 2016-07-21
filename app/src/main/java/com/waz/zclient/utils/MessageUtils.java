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
package com.waz.zclient.utils;

import android.support.annotation.Nullable;
import com.waz.api.Message;

public class MessageUtils {

    private static int getFirstRichMediaPartIndex(Message.Part[] parts) {
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].getPartType() == Message.Part.Type.YOUTUBE ||
                parts[i].getPartType() == Message.Part.Type.SOUNDCLOUD ||
                parts[i].getPartType() == Message.Part.Type.SPOTIFY ||
                parts[i].getPartType() == Message.Part.Type.WEB_LINK) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    public static Message.Part getFirstRichMediaPart(Message message) {
        final Message.Part[] parts = message.getParts();
        if (parts == null) {
            return null;
        }
        int index = getFirstRichMediaPartIndex(parts);
        if (index < 0) {
            return null;
        }
        return parts[index];
    }
}
