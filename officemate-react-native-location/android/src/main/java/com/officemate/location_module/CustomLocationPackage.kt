package com.officemate.location_module

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager 
import com.officemate.location_module.popups.PopupLocationAccess
import com.officemate.location_module.popups.BGLocation 
import com.officemate.location_module.popups.FGLocation

class CustomLocationPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {

        return listOf(
            CustomLocationModule(reactContext),
            LocationPermissionsModule(reactContext),
            PopupLocationAccess(reactContext),
            BGLocation(reactContext),
            FGLocation(reactContext) 
        ) 
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}
