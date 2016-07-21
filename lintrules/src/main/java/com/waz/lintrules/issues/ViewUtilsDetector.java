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
package com.waz.lintrules.issues;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.ClassContext;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.List;

public class ViewUtilsDetector extends Detector implements Detector.ClassScanner {

    public static final Issue ISSUE = Issue.create(
        "com.waz.ViewUtils",
        "Using `ViewUtils#getView()` instead of `findViewById`",
        "To avoid casting errors when using the `findViewById` method in an Activity, " +
        "a ViewGroup or a Fragment use the suitable `com.waz.zclient.utils.ViewUtils#getView` method.",
        Category.CORRECTNESS,
        5,
        Severity.WARNING,
        new Implementation(ViewUtilsDetector.class, Scope.CLASS_FILE_SCOPE));

    public ViewUtilsDetector() {
    }

    @NonNull
    @Override
    public Speed getSpeed() {
        return Speed.FAST;
    }

    @Override
    @Nullable
    public List<String> getApplicableCallNames() {
        return Arrays.asList("findViewById");
    }

    @Override
    public void checkCall(@NonNull ClassContext context, @NonNull ClassNode classNode,
                          @NonNull MethodNode method, @NonNull MethodInsnNode call) {
        final boolean isAndroidOwned = call.owner.startsWith("android/");
        final boolean isAndroidSuperOwned = classNode.superName.startsWith("android/");
        if (isAndroidOwned || isAndroidSuperOwned) {
            String message = String.format("Use com.waz.zclient.utils.ViewUtils#getView instead of %1$s()", call.name);
            context.report(ISSUE, method, call, context.getLocation(call), message);
        }
    }
}
