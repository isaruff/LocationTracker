package com.isaruff.location_tracker.data.location

import android.annotation.SuppressLint
import android.os.Looper
import androidx.core.location.LocationRequestCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.awaitCurrentLocation() =
    suspendCancellableCoroutine { cont ->
        getCurrentLocation(
            CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build(),
            null
        ).addOnCompleteListener { task ->
            cont.resume(task.result.takeIf { task.isSuccessful })
        }
    }


val FusedLocationProviderClient.locationUpdatesFlow
    @SuppressLint("MissingPermission")
    get() = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    println("LOCATION UPDATED $location")
                    launch { send(location) }
                }
            }
        }

        requestLocationUpdates(
            LocationRequest.Builder(1_000 * 10)
                .setPriority(LocationRequestCompat.QUALITY_HIGH_ACCURACY)
                .build(),
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            removeLocationUpdates(callback)
        }
    }