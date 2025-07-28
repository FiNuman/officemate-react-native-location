import { NativeModules, Platform, PermissionsAndroid, Alert } from 'react-native';

const {
  CustomLocation,
  LocationPermissions,
  PopupLocationAccess,
  BGLocation,
  FGLocation,
} = NativeModules;


const getCurrentPosition = async (options = {}) => {

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
  Alert.alert('Permission Denied', 'Location permission is required.');
  return;
  // CustomLocation.startForegroundLocationUpdates();
};

const stopForegroundLocationUpdates = () => {
  CustomLocation.stopForegroundLocationUpdates();
};

const BG_permission = () => LocationPermissions.BG_permission();
const FG_permission = () => LocationPermissions.FG_permission();
const location_access = () => PopupLocationAccess.promptEnableLocation();
const FG_permission_access_req = () => FGLocation.requestForegroundPermission();
const BG_permission_access_req = () => BGLocation.requestBackgroundPermission();

export default {
  getCurrentPosition,
  startForegroundLocationUpdates,
  stopForegroundLocationUpdates,
  BG_permission,
  FG_permission,
  location_access,
  FG_permission_access_req,
  BG_permission_access_req,
};
