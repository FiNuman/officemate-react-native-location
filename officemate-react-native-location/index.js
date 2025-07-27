import { NativeModules, Platform, PermissionsAndroid, Alert } from 'react-native';

const { CustomLocation, LocationPermissions, PopupLocationAccess } = NativeModules;

async function requestLocationPermission() {
  if (Platform.OS === 'android') {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      {
        title: 'Location Permission',
        message: 'This app needs access to your location.',
        buttonPositive: 'OK',
      }
    );
    return granted === PermissionsAndroid.RESULTS.GRANTED;
  }
  return true; // iOS handled in native
}

async function getCurrentPosition(options = {}) {
  const hasPermission = await requestLocationPermission();
  if (!hasPermission) {
    return { error: true, code: 'PERMISSION_DENIED', message: 'Location permission is required.' };
  }
  try {
    
    await PopupLocationAccess.promptEnableLocation()
    const location = await CustomLocation.getCurrentPosition(options); 
    return { error: false, location };

  } catch (err) {
    return { error: true, code: err.code || 'ERROR', message: err.message || 'Unknown error' };
  }
}

async function startForegroundLocationUpdates() {
  const hasPermission = await requestLocationPermission();
  if (!hasPermission) {
    Alert.alert('Permission Denied', 'Location permission is required.');
    return;
  }
  CustomLocation.startForegroundLocationUpdates();
}

function stopForegroundLocationUpdates() {
  CustomLocation.stopForegroundLocationUpdates();
}


async function BG_permission() {
  const hasPermission = await LocationPermissions.BG_permission();
  return hasPermission;
}

async function FG_permission() {
  const hasPermission = await LocationPermissions.FG_permission();
  return hasPermission;
}

async function location_access_popup() {
  const hasPermission = await PopupLocationAccess.promptEnableLocation();
  return hasPermission;
}

export default {
  getCurrentPosition,
  startForegroundLocationUpdates,
  stopForegroundLocationUpdates,
  BG_permission,
  FG_permission,
  location_access: location_access_popup
};