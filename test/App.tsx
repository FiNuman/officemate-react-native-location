
import { NewAppScreen } from '@react-native/new-app-screen';
import { StatusBar, StyleSheet, useColorScheme, View, Platform, PermissionsAndroid } from 'react-native';
import LocationModule from 'officemate-react-native-location';

function App() {

   const getLocation = async () => {
    try {
      console.log('Requesting current location...');

      const hasPermission = await LocationModule.BG_permission();
     
      const location = await LocationModule.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 10000,
      });

      console.log('Current Location:', location);

    } catch (err) {
      console.error('Error getting location:', err);
    }
  };

  getLocation();
  const isDarkMode = useColorScheme() === 'dark';

  return (
    <View style={styles.container}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <NewAppScreen templateFileName="App.tsx" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

export default App;
