package com.officemate.location_module.popups

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import com.facebook.react.bridge.*
import com.facebook.react.bridge.BaseActivityEventListener
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.facebook.react.bridge.ReactApplicationContext

class BGLocation(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val LOCATION_SETTINGS_REQUEST_CODE = 1001
    private var locationSettingsPromise: Promise? = null

    init {
        reactContext.addActivityEventListener(object : BaseActivityEventListener() {
            override fun onActivityResult(
                activity: Activity,
                requestCode: Int,
                resultCode: Int,
                data: Intent?
            ) {
                if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
                    if (resultCode == Activity.RESULT_OK) {
                        locationSettingsPromise?.resolve(true)
                    } else {
                        locationSettingsPromise?.reject("LOCATION_NOT_ENABLED", "User denied to enable location")
                    }
                    locationSettingsPromise = null
                }
            }
        })
    }

    override fun getName(): String = "BGLocation"

    @ReactMethod
    fun promptBackgroundLocationPermission(promise: Promise) {
        val currentActivity = currentActivity
        if (currentActivity == null) {
            promise.reject("NO_ACTIVITY", "Current activity is null")
            return
        }

        // Build a LocationRequest for high accuracy, suitable for background location
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)  // optional
            .setWaitForAccurateLocation(true)   // optional, API 31+
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true) // To always show the dialog

        val settingsClient = LocationServices.getSettingsClient(reactContext)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location settings are satisfied, no need to prompt
            promise.resolve(true)
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                locationSettingsPromise = promise
                try {
                    // Show the dialog by launching the resolution
                    exception.startResolutionForResult(currentActivity, LOCATION_SETTINGS_REQUEST_CODE)
                } catch (sendEx: IntentSender.SendIntentException) {
                    locationSettingsPromise = null
                    promise.reject("ERROR", sendEx.message)
                }
            } else {
                promise.reject("LOCATION_SETTINGS_ERROR", "Cannot resolve location settings: ${exception.localizedMessage}")
            }
        }
    }
}
