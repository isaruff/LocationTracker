package com.isaruff.location_tracker.data.work_manager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerFactory @Inject constructor(
    private val locationProviderClient: FusedLocationProviderClient
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return LocationTrackerWorker(appContext, workerParameters, locationProviderClient)
    }

}