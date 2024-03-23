package com.isaruff.location_tracker.ui.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.isaruff.location_tracker.data.service.LocationTrackerService
import com.isaruff.location_tracker.ui.theme.LocationTrackerTheme

class MainActivity : ComponentActivity() {

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) = Unit
        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermission()
        setContent {
            LocationTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = ::bindTrackingService) {
                            Text(text = "Start Tracking")
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = ::unbindTrackingService) {
                            Text(text = "Stop Tracking")
                        }
                    }
                }
            }
        }
    }

    private fun bindTrackingService() {
        bindService(
            Intent(this, LocationTrackerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        startService(Intent(this, LocationTrackerService::class.java))
    }

    private fun unbindTrackingService() {
        try {
            unbindService(serviceConnection)
            stopService(Intent(this, LocationTrackerService::class.java))
        } catch (e: Exception) {

        }
    }


    private fun requestLocationPermission() {
        val permissions = buildList {
            addAll(
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        val ungrantedPermissions = mutableListOf<String>()

        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ungrantedPermissions.add(permission)
            }
        }

        if (ungrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                ungrantedPermissions.toTypedArray(),
                1
            )
        }
    }
}
