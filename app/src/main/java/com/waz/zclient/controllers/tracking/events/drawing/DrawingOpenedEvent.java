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
package com.waz.zclient.controllers.tracking.events.drawing;

import android.support.annotation.NonNull;

import com.waz.zclient.controllers.drawing.IDrawingController;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.events.Event;

import timber.log.Timber;

public class DrawingOpenedEvent extends Event {

    private enum Source {
        CAMERA("camera"),
        SINGLE_IMAGE("singleimage"),
        CURSOR("cursor");

        private String name;

        Source(String name) {
            this.name = name;
        }
    }

    private DrawingOpenedEvent(Source source) {
        attributes.put(Attribute.SOURCE, source.name);
    }

    public static DrawingOpenedEvent newInstance(IDrawingController.DrawingDestination drawingDestination) {
        switch (drawingDestination) {
            case CAMERA_PREVIEW_VIEW:
                return new DrawingOpenedEvent(Source.CAMERA);
            case SINGLE_IMAGE_VIEW:
                return new DrawingOpenedEvent(Source.SINGLE_IMAGE);
            case SKETCH_BUTTON:
                return new DrawingOpenedEvent(Source.CURSOR);
            default:
                Timber.e("Invalid drawingDestination!");
                return new DrawingOpenedEvent(Source.CURSOR);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "sketchpadOpened";
    }

}
