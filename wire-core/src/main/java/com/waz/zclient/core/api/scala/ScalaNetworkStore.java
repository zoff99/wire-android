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
package com.waz.zclient.core.api.scala;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import com.waz.api.ConnectionIndicator;
import com.waz.api.ZMessagingApi;
import com.waz.zclient.core.stores.network.INetworkStore;
import com.waz.zclient.core.stores.network.NetworkAction;
import com.waz.zclient.core.stores.network.NetworkStoreObserver;

import java.util.HashSet;
import java.util.Set;

public class ScalaNetworkStore implements INetworkStore {

    private ConnectionIndicator connectionIndicator;
    private ConnectivityManager connectivityManager;
    private Set<NetworkStoreObserver> networkStoreObservers = new HashSet<>();

    final private ModelObserver<ConnectionIndicator> connectionIndicatorModelObserver = new ModelObserver<ConnectionIndicator>() {
        @Override
        public void updated(ConnectionIndicator model) {
            notifyConnectivityChange(!model.isConnectionError());
        }
    };

    public ScalaNetworkStore(Context context, ZMessagingApi zMessagingApi) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectionIndicator = zMessagingApi.getConnectionIndicator();
        connectionIndicatorModelObserver.setAndUpdate(connectionIndicator);
    }

    public void addNetworkControllerObserver(NetworkStoreObserver networkStoreObserver) {
        networkStoreObservers.add(networkStoreObserver);
        networkStoreObserver.onConnectivityChange(hasInternetConnection());
    }

    public void removeNetworkControllerObserver(NetworkStoreObserver networkStoreObserver) {
        networkStoreObservers.remove(networkStoreObserver);
    }

    @Override
    public void tearDown() {
        connectivityManager = null;
        connectionIndicator = null;
    }

    @Override
    public boolean hasInternetConnection() {
        if (connectionIndicator.isConnectionError()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasInternetConnectionWith2GAndHigher() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            return false;
        }

        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }

        if (activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
            return false;
        }

        switch (activeNetwork.getSubtype()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false;
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return false;
        }

    }

    @Override
    public boolean hasInternetConnectionWith3GAndHigher() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            return false;
        }

        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }

        if (activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE) {
            return false;
        }

        switch (activeNetwork.getSubtype()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_IDEN:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return false;
        }
    }

    @Override
    public boolean hasWifiConnection() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    @Override
    public void notifyNetworkAccessFailed() {
        for (NetworkStoreObserver observer : networkStoreObservers) {
            observer.onNetworkAccessFailed();
        }
    }

    @Override
    public void doIfNetwork(NetworkAction networkAction) {
        if (hasInternetConnection()) {
            networkAction.execute();
        } else {
            notifyNetworkAccessFailed();
            networkAction.onNoNetwork();
        }
    }

    private void notifyConnectivityChange(boolean connected) {
        for (NetworkStoreObserver networkStoreObserver : networkStoreObservers) {
            networkStoreObserver.onConnectivityChange(connected);
        }
    }
}
