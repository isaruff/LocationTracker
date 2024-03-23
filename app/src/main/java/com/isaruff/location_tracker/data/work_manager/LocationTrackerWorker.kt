package com.isaruff.location_tracker.data.work_manager

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.isaruff.location_tracker.data.location.awaitCurrentLocation
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class LocationTrackerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val locationProviderClient: FusedLocationProviderClient
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val currentLocation =
            locationProviderClient.awaitCurrentLocation() ?: return run {
                Log.d(
                    "LOCATION_TRACKER",
                    "Current location failed because it is null"
                )
                Result.failure()
            }

        Log.d(
            "LOCATION_TRACKER",
            "Current location lat: ${currentLocation.latitude}, lng: ${currentLocation.longitude} "
        )

        Toast.makeText(
            applicationContext,
            "Current location lat: ${currentLocation.latitude}, lng: ${currentLocation.longitude}",
            Toast.LENGTH_SHORT
        ).show()
        return Result.success()
    }
}