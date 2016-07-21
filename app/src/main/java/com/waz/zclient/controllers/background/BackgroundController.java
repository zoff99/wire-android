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
package com.waz.zclient.controllers.background;

import com.waz.api.ImageAsset;
import com.waz.api.Self;
import com.waz.api.UpdateListener;
import java.util.HashSet;
import java.util.Set;

public class BackgroundController implements IBackgroundController {
    private Set<BackgroundObserver>  backgroundObservers;
    private Self self;

    private final UpdateListener selfUpdateListener = new UpdateListener() {
        @Override
        public void updated() {
            setImageAsset(self.getPicture());
        }
    };

    public BackgroundController() {
        backgroundObservers = new HashSet<>();
    }

    @Override
    public void setSelf(Self self) {
        if (self == null) {
            return;
        }

        if (this.self != null) {
            this.self.removeUpdateListener(selfUpdateListener);
        }

        this.self = self;
        self.addUpdateListener(selfUpdateListener);
        selfUpdateListener.updated();
    }

    @Override
    public void setImageAsset(ImageAsset imageAsset) {
        for (BackgroundObserver backgroundObserver : backgroundObservers) {
            backgroundObserver.onLoadImageAsset(imageAsset);
        }
    }

    @Override
    public void addBackgroundObserver(BackgroundObserver backgroundObserver) {
        backgroundObservers.add(backgroundObserver);
        update();
    }

    @Override
    public void removeBackgroundObserver(BackgroundObserver backgroundObserver) {
        backgroundObservers.remove(backgroundObserver);
    }

    @Override
    public void onStop() {
        if (this.self == null) {
            return;
        }
        self.removeUpdateListener(selfUpdateListener);
        self = null;
    }

    @Override
    public void update() {
        if (self == null) {
            return;
        }
        selfUpdateListener.updated();
    }

    @Override
    public void tearDown() {
        if (backgroundObservers != null) {
            backgroundObservers.clear();
            backgroundObservers = null;
        }
    }

    @Override
    public void expand(boolean expand) {
        for (BackgroundObserver backgroundObserver : backgroundObservers) {
            backgroundObserver.onScaleToMax(expand);
        }
    }
}
