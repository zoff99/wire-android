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
package com.waz.zclient;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import com.waz.api.Permission;
import com.waz.api.PermissionProvider;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class ScalaPermissionRequest {
    public static final int REQUEST_SE_PERMISSION = 162;
    private final WeakReference<Activity> weakActivity;
    private final WeakReference<PermissionProvider.ResponseHandler> weakTarget;
    private final Set<Permission> permissions;
    private final String[] permissionIds;

    ScalaPermissionRequest(Activity activity,
                           PermissionProvider.ResponseHandler target,
                           Set<Permission> permissions) {
        this.weakActivity = new WeakReference<>(activity);
        this.weakTarget = new WeakReference<>(target);
        this.permissions = permissions;
        this.permissionIds = new String[permissions.size()];
        int i = 0;
        for (Permission permission : permissions) {
            permissionIds[i] = permission.id;
            i++;
        }
    }

    public void proceed() {
        Activity target = weakActivity.get();
        if (target == null) {
            return;
        }
        ActivityCompat.requestPermissions(target, permissionIds, REQUEST_SE_PERMISSION);
    }

    public void cancel() {
        PermissionProvider.ResponseHandler target = weakTarget.get();
        if (target == null) {
            return;
        }
        target.handleResponse(zip(permissions, Permission.Status.DENIED));
    }

    public void grant() {
        PermissionProvider.ResponseHandler target = weakTarget.get();
        if (target == null) {
            return;
        }
        target.handleResponse(zip(permissions, Permission.Status.GRANTED));
    }

    private Map<Permission, Permission.Status> zip(Set<Permission> permissions, Permission.Status status) {
        Map<Permission, Permission.Status> map = new HashMap<>();
        for (Permission permission : permissions) {
            map.put(permission, status);
        }
        return map;
    }

    public PermissionProvider.ResponseHandler getTargetResponseHandler() {
        return weakTarget.get();
    }

    public String[] getPermissionIds() {
        return permissionIds;
    }
}
