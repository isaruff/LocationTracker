package com.isaruff.location_tracker.data.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.isaruff.location_tracker.R
import com.isaruff.location_tracker.data.location.awaitCurrentLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackerService : Service() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var isDestroyed = false

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d(
                "Location_tracker",
                "Error occured cause: $throwable"
            )
        })

    inner class MyBinder : Binder() {
        val service: LocationTrackerService
            get() = this@LocationTrackerService
    }

    override fun onBind(intent: Intent?): IBinder = MyBinder()

    override fun onCreate() {
        super.onCreate()
        isDestroyed = false
        makeForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        observeLocation()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isDestroyed = true
    }

    private fun observeLocation() {
        scope.launch {
            while (true) {
                if (isDestroyed) break
                val currentLocation = fusedLocationProviderClient.awaitCurrentLocation()
                currentLocation?.let {
                    if (isDestroyed.not()) updateNotification(it)
                }
                delay(1000 * 10)
            }
        }
    }

    private fun makeForegroundService() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createNotification(),
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        )
    }

    @SuppressLint("MissingPermission")
    private fun createNotification(location: Location? = null): Notification {
        createNotificationChannelIfNeeded()

        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Location Tracking")
                .setContentText(buildString {
                    if (location != null) {
                        append("Current location Lat: ${location.latitude} Lng: ${location.longitude}")
                    } else {
                        append("Searching location...")
                    }
                })
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)

        return notificationBuilder.build()
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(location: Location) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(location))
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 999
        const val NOTIFICATION_CHANNEL_ID = "location_tracker_notification_channel_id"
        const val NOTIFICATION_CHANNEL_NAME = "Location Tracking"
    }
}