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
package com.waz.zclient.controllers.tracking.events.calling;

import android.support.annotation.NonNull;
import com.waz.service.call.AvsMetrics;
import com.waz.zclient.core.controllers.tracking.attributes.Attribute;
import com.waz.zclient.core.controllers.tracking.events.AVSMetricEvent;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

import java.util.Iterator;

public class EndedCallAVSMetricsEvent extends AVSMetricEvent {

    public EndedCallAVSMetricsEvent(AvsMetrics avsMetrics) {
        try {
            attributes.put(Attribute.CONVERSATION_TYPE.toString(), avsMetrics.kindOfCall().toString());
            JSONObject metricsJson = avsMetrics.json();
            Iterator<String> iter = metricsJson.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    Object value = metricsJson.get(key);
                    attributes.put(key, value.toString());
                } catch (JSONException e) {
                    Timber.i(e.getMessage(), "AVS JSON error %s");
                }
            }
        } catch (NullPointerException e) {
            // No AVS metrics
        }
    }

    @NonNull
    @Override
    public String getName() {
        // TODO: Change name to "calling.avs_metrics_ended_video_call" when avsMetrics.isVideoCall() works
        return "calling.avs_metrics_ended_call";
    }
}
