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
package com.waz.zclient.pages.main.grid;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.waz.zclient.R;
import com.waz.zclient.pages.BaseFragment;
import com.waz.zclient.utils.debug.ShakeEventListener;

public class GridFragment extends BaseFragment<GridFragment.Container> {
    public static final String TAG = GridFragment.class.getName();

    private View mainLayout;
    private SensorManager sensorManager;
    private ShakeEventListener sensorListener;
    private boolean isEnabled;

    public static GridFragment newInstance() {
        return new GridFragment();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  LifeCycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainLayout = inflater.inflate(R.layout.fragment_grid, container, false);


        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorListener = new ShakeEventListener();
        sensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {
            @Override
            public void onShake() {
                isEnabled = !isEnabled;
                setGridEnabled(isEnabled);
            }
        });

        setGridEnabled(false);

        return mainLayout;
    }

    @Override
    public void onResume() {
        super.onResume();

        // register sensor manager
        sensorManager.registerListener(sensorListener,
                                       sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                       SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        sensorManager.unregisterListener(sensorListener);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        sensorManager = null;
        super.onDestroyView();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Notifications
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setGridEnabled(boolean enabled) {
        if (enabled) {
            mainLayout.setVisibility(View.VISIBLE);
        } else {
            mainLayout.setVisibility(View.GONE);
        }
    }

    public interface Container {
    }
}
