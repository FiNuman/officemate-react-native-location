import { NativeModules, Platform, PermissionsAndroid, Alert } from 'react-native';

const {
  CustomLocation,
  LocationPermissions,
  PopupLocationAccess,
  BGLocation,
  FGLocation
} = NativeModules;

const requestLocationPermission = async () => {
  if (Platform.OS !== 'android') return true;

  const granted = await PermissionsAndroid.request(
    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
    {
      title: 'Location Permission',
      message: 'This app needs access to your location.',
      buttonPositive: 'OK',
    }
  );

  return granted === PermissionsAndroid.RESULTS.GRANTED;
};

const getCurrentPosition = async (options = {}) => {
  if (!(await requestLocationPermission())) {
    return {
      error: true,
      code: 'PERMISSION_DENIED',
      message: 'Location permission is required.',
    };
  }

  try {
    await FGLocation.requestForegroundPermission()
    await PopupLocationAccess.promptEnableLocation();
    const location = await CustomLocation.getCurrentPosition(options);
    return { error: false, location };
  } catch (err) {
    return {
      error: true,
      code: err.code || 'ERROR',
      message: err.message || 'Unknown error',
    };
  }
};

const startForegroundLocationUpdates = async () => {
  if (!(await requestLocationPermission())) {
    Alert.alert('Permission Denied', 'Location permission is required.');
    return;
  }
  CustomLocation.startForegroundLocationUpdates();
};

const stopForegroundLocationUpdates = () => {
  CustomLocation.stopForegroundLocationUpdates();
};

const BG_permission = () => LocationPermissions.BG_permission();
const FG_permission = () => LocationPermissions.FG_permission();
const location_access = () => PopupLocationAccess.promptEnableLocation();
const BG_permission_access_req = () => BGLocation.promptBackgroundLocationPermission();
const FG_permission_access_req = () => FGLocation.requestForegroundPermission();

export default {
  getCurrentPosition,
  startForegroundLocationUpdates,
  stopForegroundLocationUpdates,
  BG_permission,
  FG_permission,
  location_access,
  BG_permission_access_req,
  FG_permission_access_req,
};
