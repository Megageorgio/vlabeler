package com.sdercolin.vlabeler.tracking

import com.sdercolin.vlabeler.env.Log
import com.sdercolin.vlabeler.tracking.event.TrackingEvent
import com.sdercolin.vlabeler.ui.AppRecordStore
import kotlinx.coroutines.CoroutineScope

/**
 * Tracking service. Android: analytics are not sent from the Android port, events are only logged in debug mode.
 */
@Suppress("UNUSED_PARAMETER")
class TrackingService(appRecordStore: AppRecordStore, mainScope: CoroutineScope) {

    fun track(event: TrackingEvent) {
        Log.info("Tracking event (not sent on Android): ${event.name}")
    }
}
