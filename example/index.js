import { AppRegistry } from 'react-native';
import App from './src/App';
import OverlayView from './src/OverlayView';
import { name as appName } from './app.json';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerComponent('MyOverlay', () => OverlayView);

if (typeof document !== 'undefined') {
  AppRegistry.runApplication(appName, {
    rootTag: document.getElementById('root'),
  });
}
