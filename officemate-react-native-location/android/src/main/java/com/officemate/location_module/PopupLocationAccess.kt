package com.officemate.location_module

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

class PopupLocationAccess(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

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

    override fun getName(): String = "PopupLocationAccess"

    @ReactMethod
    fun promptEnableLocation(promise: Promise) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(reactApplicationContext)
        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                // Location services already enabled
                promise.resolve(true)
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    locationSettingsPromise = promise
                    try {
                        exception.startResolutionForResult(currentActivity!!, LOCATION_SETTINGS_REQUEST_CODE)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        promise.reject("ERROR", sendEx.message)
                        locationSettingsPromise = null
                    }
                } else {
                    promise.reject("LOCATION_DISABLED", "Location services are off and cannot be resolved")
                }
            }
    }
}
