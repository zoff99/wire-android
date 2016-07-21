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
package com.waz.zclient.controllers.orientation;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;
import com.waz.zclient.utils.SquareOrientation;

import java.util.HashSet;
import java.util.Set;

import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;

public class OrientationController implements IOrientationController {
    public static final String TAG = OrientationController.class.getName();

    private final OrientationEventListener orientationEventListener;
    private SquareOrientation lastKnownOrientation = null;
    Set<OrientationControllerObserver> orientationControllerObservers = new HashSet<>();
    private Activity activity;
    private int deviceOrientation;

    public OrientationController(final Context context) {
        deviceOrientation = 0;
        orientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceOrientation = (orientation + 45) / 90 * 90;
                lastKnownOrientation = SquareOrientation.getOrientation(orientation, context);
                notifyOrientationHasChanged(lastKnownOrientation);
            }
        };
        orientationEventListener.enable();
    }

    @Override
    public void setActivity(Activity activity) {
        this.activity = activity;
        lastKnownOrientation = SquareOrientation.getOrientation(activity);
    }

    @Override
    public void tearDown() {
        orientationEventListener.disable();
        activity = null;
    }

    @Override
    public void addOrientationControllerObserver(OrientationControllerObserver orientationControllerObserver) {
        orientationControllerObservers.add(orientationControllerObserver);
    }

    @Override
    public void removeOrientationControllerObserver(OrientationControllerObserver orientationControllerObserver) {
        orientationControllerObservers.remove(orientationControllerObserver);
    }

    @Override
    public SquareOrientation getLastKnownOrientation() {
        return lastKnownOrientation;
    }

    @Override
    public boolean isInPortrait() {
        return lastKnownOrientation == SquareOrientation.PORTRAIT_STRAIGHT ||
               lastKnownOrientation == SquareOrientation.PORTRAIT_UPSIDE_DOWN;
    }

    @Override
    public int getDeviceOrientation() {
        return deviceOrientation;
    }

    @Override
    public int getActivityRotationDegrees() {
        if (activity == null) {
            return 0;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case ROTATION_0:
                return 0;
            case ROTATION_90:
                return 90;
            case ROTATION_180:
                return 180;
            case ROTATION_270:
                return 270;
        }
        return 0;
    }

    private void notifyOrientationHasChanged(SquareOrientation squareOrientation) {
        for (OrientationControllerObserver orientationControllerObserver : orientationControllerObservers) {
            orientationControllerObserver.onOrientationHasChanged(squareOrientation);
        }
    }
}
