package com.officemate.location_module.popups

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*

class FGLocation(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1010
    private var permissionPromise: Promise? = null

    init {
        reactContext.addActivityEventListener(this)
    }

    override fun getName(): String = "FGLocation"

    @ReactMethod
    fun requestForegroundPermission(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.reject("NO_ACTIVITY", "Current activity is null")
            return
        }

        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
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

    // Proper method from ActivityEventListener
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        // Not needed here
    }

    // Proper method from ActivityEventListener
    override fun onNewIntent(intent: Intent) {
        // Not needed here
    }

    // Use this method only in your MainActivity if needed, not here.
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && permissionPromise != null) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionPromise?.resolve(true)
            } else {
                permissionPromise?.resolve(false)
            }
            permissionPromise = null
        }
    }
}
