
import { NewAppScreen } from '@react-native/new-app-screen';
import { StatusBar, StyleSheet, useColorScheme, View, Platform, PermissionsAndroid } from 'react-native';
import LocationModule from 'officemate-react-native-location';

function App() {

  const getLocation = async () => {
    try {

      let bg_access = await LocationModule.BG_permission_access_req();
      console.log(bg_access)
      // const location = await LocationModule.getCurrentPosition({
      //   enableHighAccuracy: true,
      //   timeout: 10000,
      // }); 

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
