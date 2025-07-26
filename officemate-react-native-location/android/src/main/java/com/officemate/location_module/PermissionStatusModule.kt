package com.officemate.location_module

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*

object Status {
    const val GRANTED = "granted"
    const val DENIED = "denied"
    const val UNDETERMINED = "undetermined"
}

class LocationPermissionsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "LocationPermissions"

    private fun checkPermission(permission: String): String {
        return when (ContextCompat.checkSelfPermission(reactApplicationContext, permission)) {
            PackageManager.PERMISSION_GRANTED -> Status.GRANTED
            PackageManager.PERMISSION_DENIED -> Status.DENIED
            else -> Status.UNDETERMINED
        }
    }

    @ReactMethod
    fun FG_permission(promise: Promise) {
        val status = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val result = Arguments.createMap()
        result.putString("status", status)
        promise.resolve(result)
    }

    @ReactMethod
    fun BG_permission(promise: Promise) {
        val sdkInt = android.os.Build.VERSION.SDK_INT
        val status = if (sdkInt >= 29) {
            checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            // Before Android 10 background permission is implicitly granted if foreground granted
            val fgStatus = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            if (fgStatus == Status.GRANTED) Status.GRANTED else Status.DENIED
        }
        val result = Arguments.createMap()
        result.putString("status", status)
        promise.resolve(result)
    }
}
