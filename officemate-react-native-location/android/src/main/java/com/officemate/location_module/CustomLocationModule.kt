//CustomLocationModule.kt
package com.officemate.location_module 
 
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.android.gms.location.*


import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.os.Looper
import android.location.LocationManager
import android.content.Context

class CustomLocationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(reactContext)
    private var locationCallback: LocationCallback? = null
    private val reactContext = reactContext

    override fun getName(): String = "CustomLocation" 
    
    @ReactMethod
    fun getCurrentPosition(options: ReadableMap?, promise: Promise) {
        if (!hasLocationPermission()) {
            promise.reject("PERMISSION_DENIED", "Location permission not granted")
            return
        }

        if (!isLocationEnabled()) {
            promise.reject("LOCATION_DISABLED", "GPS or Location Services are turned off")
            return
        }

        // Default values
        val highAccuracy = options?.getBoolean("enableHighAccuracy") ?: false
        val timeout = options?.getInt("timeout") ?: 10000  // timeout in ms

        val priority = if (highAccuracy) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        // Try last known location first
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val result = Arguments.createMap()
                    fillLocationMap(result, location)
                    promise.resolve(result)
                } else {
                    // Request single update with requested priority and timeout
                    val locationRequest = LocationRequest.Builder(priority, timeout.toLong())
                        .setMaxUpdates(1)
                        .build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val newLocation = locationResult.lastLocation
                            if (newLocation != null) {
                                val result = Arguments.createMap()
                                fillLocationMap(result, newLocation)
                                promise.resolve(result)
                            } else {
                                promise.reject("NO_LOCATION", "Location is null")
                            }
                            fusedLocationClient.removeLocationUpdates(this)
                        }

                        override fun onLocationAvailability(availability: LocationAvailability) {
                            if (!availability.isLocationAvailable) {
                                promise.reject("LOCATION_UNAVAILABLE", "Location unavailable")
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                }
            }
            .addOnFailureListener { e ->
                promise.reject("ERROR", e.message)
            }
    }



    @ReactMethod
    fun startForegroundLocationUpdates() {
        if (!hasLocationPermission()) {
            sendEvent("onLocationError", "Location permission not granted")
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                val location = locationResult.lastLocation ?: return

                val params = Arguments.createMap()
                fillLocationMap(params, location)
                sendEvent("onLocationUpdate", params)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
    }

    @ReactMethod
    fun stopForegroundLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
    val locationManager = reactApplicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
           locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

    private fun fillLocationMap(map: WritableMap, location: android.location.Location) {
        map.putDouble("latitude", location.latitude)
        map.putDouble("longitude", location.longitude)
        map.putDouble("accuracy", location.accuracy.toDouble())
        map.putDouble("altitude", location.altitude)
        map.putDouble("heading", location.bearing.toDouble())
        map.putDouble("speed", location.speed.toDouble())
    }

    private fun sendEvent(eventName: String, params: Any) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }
}
