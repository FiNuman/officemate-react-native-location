package com.officemate.location_module.popups

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*

class BGLocation(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1020
    private var permissionPromise: Promise? = null

    init {
        reactContext.addActivityEventListener(this)
    }

    override fun getName(): String = "BGLocation"

    @ReactMethod
    fun requestBackgroundPermission(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.reject("NO_ACTIVITY", "Current activity is null")
            return
        }

        val permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            promise.resolve(true)
        } else {
            permissionPromise = promise
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // This method is NOT an override, call this from MainActivity.onRequestPermissionsResult()
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && permissionPromise != null) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionPromise?.resolve(true)
            } else {
                permissionPromise?.resolve(false)
            }
            permissionPromise = null
        }
    }

    // ActivityEventListener overrides with exact signatures:
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        // No implementation needed here
    }

    override fun onNewIntent(intent: Intent) {
        // No implementation needed here
    }
}
