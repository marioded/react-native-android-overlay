import { useEffect, useState } from 'react';
import {
  Alert,
  ScrollView,
  StatusBar,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { OverlayManager } from 'react-native-android-overlay';

export default function App() {
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);
  const [isOverlayActive, setIsOverlayActive] = useState(false);

  const [width, setWidth] = useState('350');
  const [height, setHeight] = useState('160');
  const [x, setX] = useState('0');
  const [y, setY] = useState('100');
  const [gravity, setGravity] = useState<'top' | 'bottom' | 'center'>('bottom');

  const [draggable, setDraggable] = useState(true);
  const [touchable, setTouchable] = useState(true);
  const [focusable, setFocusable] = useState(false);

  const [notificationTitle, setNotificationTitle] = useState(
    'Floating Overlay Window'
  );
  const [notificationText, setNotificationText] = useState(
    'Interactive React Native overlay is active'
  );
  const [notificationIcon, setNotificationIcon] = useState('ic_launcher');

  useEffect(() => {
    checkPermission();
  }, []);

  const checkPermission = async () => {
    try {
      const permitted = await OverlayManager.hasPermission();
      setHasPermission(permitted);
    } catch (e) {
      console.error('Failed to check permission', e);
    }
  };

  const requestPermission = () => {
    OverlayManager.requestPermission();
    setTimeout(checkPermission, 1500);
  };

  const handleStartOverlay = async () => {
    if (!hasPermission) {
      Alert.alert(
        'Permission Required',
        'Please grant the Draw Over Other Apps permission first.',
        [
          { text: 'Cancel', style: 'cancel' },
          { text: 'Grant', onPress: requestPermission },
        ]
      );
      return;
    }

    const options = {
      width: parseInt(width, 10) || 350,
      height: parseInt(height, 10) || 160,
      x: parseInt(x, 10) || 0,
      y: parseInt(y, 10) || 0,
      gravity,
      draggable,
      touchable,
      focusable,
      foreground: true,
      notificationTitle: notificationTitle,
      notificationText: notificationText,
      notificationIcon: notificationIcon,
    };

    setIsOverlayActive(true);
    OverlayManager.startOverlay('MyOverlay', options);
  };

  const handleStopOverlay = () => {
    setIsOverlayActive(false);
    OverlayManager.stopOverlay('MyOverlay');
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a1a" />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Android overlay demo</Text>
        </View>

        <View
          style={[
            styles.card,
            hasPermission ? styles.permissionGranted : styles.permissionDenied,
          ]}
        >
          <Text style={styles.cardTitle}>Draw over other apps permission</Text>
          <Text style={styles.cardDescription}>
            Status: {hasPermission ? 'granted' : 'denied'}
          </Text>
          {!hasPermission && (
            <TouchableOpacity
              style={styles.permissionBtn}
              onPress={requestPermission}
            >
              <Text style={styles.permissionBtnText}>Grant</Text>
            </TouchableOpacity>
          )}
        </View>

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Window constraints</Text>

          <View style={styles.row}>
            <View style={styles.col}>
              <Text style={styles.label}>Width (dp)</Text>
              <TextInput
                style={styles.input}
                keyboardType="numeric"
                value={width}
                onChangeText={setWidth}
              />
            </View>
            <View style={styles.col}>
              <Text style={styles.label}>Height (dp)</Text>
              <TextInput
                style={styles.input}
                keyboardType="numeric"
                value={height}
                onChangeText={setHeight}
              />
            </View>
          </View>

          <View style={styles.row}>
            <View style={styles.col}>
              <Text style={styles.label}>X offset (dp)</Text>
              <TextInput
                style={styles.input}
                keyboardType="numeric"
                value={x}
                onChangeText={setX}
              />
            </View>
            <View style={styles.col}>
              <Text style={styles.label}>Y offset (dp)</Text>
              <TextInput
                style={styles.input}
                keyboardType="numeric"
                value={y}
                onChangeText={setY}
              />
            </View>
          </View>

          <Text style={styles.label}>Gravity</Text>
          <View style={styles.gravityContainer}>
            {(['top', 'center', 'bottom'] as const).map((g) => (
              <TouchableOpacity
                key={g}
                style={[
                  styles.gravityBtn,
                  gravity === g && styles.gravityBtnActive,
                ]}
                onPress={() => setGravity(g)}
              >
                <Text
                  style={[
                    styles.gravityBtnText,
                    gravity === g && styles.gravityBtnTextActive,
                  ]}
                >
                  {g.toUpperCase()}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Interactivity options</Text>

          <View style={styles.switchRow}>
            <Text style={styles.switchLabel}>Draggable</Text>
            <Switch value={draggable} onValueChange={setDraggable} />
          </View>

          <View style={styles.switchRow}>
            <Text style={styles.switchLabel}>Touchable</Text>
            <Switch value={touchable} onValueChange={setTouchable} />
          </View>

          <View style={styles.switchRow}>
            <Text style={styles.switchLabel}>
              Focusable (for keyboard input)
            </Text>
            <Switch value={focusable} onValueChange={setFocusable} />
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Notification settings</Text>

          <Text style={styles.label}>Title</Text>
          <TextInput
            style={styles.input}
            value={notificationTitle}
            onChangeText={setNotificationTitle}
          />

          <Text style={styles.label}>Message</Text>
          <TextInput
            style={styles.input}
            value={notificationText}
            onChangeText={setNotificationText}
          />

          <Text style={styles.label}>Icon Resource Name</Text>
          <TextInput
            style={styles.input}
            value={notificationIcon}
            onChangeText={setNotificationIcon}
          />
        </View>

        <View style={styles.actionContainer}>
          {!isOverlayActive ? (
            <TouchableOpacity
              style={[styles.btn, styles.btnStart]}
              onPress={handleStartOverlay}
            >
              <Text style={styles.btnText}>Start overlay</Text>
            </TouchableOpacity>
          ) : (
            <TouchableOpacity
              style={[styles.btn, styles.btnStop]}
              onPress={handleStopOverlay}
            >
              <Text style={styles.btnText}>Stop overlay</Text>
            </TouchableOpacity>
          )}
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#121214',
  },
  scrollContent: {
    padding: 16,
  },
  header: {
    marginVertical: 16,
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  card: {
    backgroundColor: '#1c1c1e',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#2c2c2e',
  },
  permissionGranted: {
    borderLeftWidth: 4,
    borderLeftColor: '#30d158',
  },
  permissionDenied: {
    borderLeftWidth: 4,
    borderLeftColor: '#ff453a',
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  cardDescription: {
    fontSize: 14,
    color: '#a1a1a5',
    marginTop: 4,
  },
  permissionBtn: {
    borderRadius: 6,
    padding: 10,
    alignItems: 'center',
    marginTop: 12,
  },
  permissionBtnText: {
    color: '#ffffff',
    fontWeight: 'bold',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 12,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  col: {
    width: '48%',
  },
  label: {
    fontSize: 12,
    color: '#8e8e93',
    marginBottom: 4,
    marginTop: 8,
  },
  input: {
    backgroundColor: '#2c2c2e',
    borderWidth: 1,
    borderColor: '#3a3a3c',
    borderRadius: 6,
    padding: 10,
    fontSize: 14,
    color: '#ffffff',
    marginBottom: 8,
  },
  gravityContainer: {
    flexDirection: 'row',
    backgroundColor: '#2c2c2e',
    borderRadius: 8,
    padding: 2,
    marginTop: 4,
  },
  gravityBtn: {
    flex: 1,
    paddingVertical: 8,
    alignItems: 'center',
    borderRadius: 6,
  },
  gravityBtnActive: {
    backgroundColor: '#30d158',
  },
  gravityBtnText: {
    fontSize: 12,
    color: '#aeaeaf',
    fontWeight: 'bold',
  },
  gravityBtnTextActive: {
    color: '#ffffff',
  },
  switchRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
  },
  switchLabel: {
    fontSize: 14,
    color: '#ffffff',
  },
  actionContainer: {
    marginVertical: 12,
  },
  btn: {
    borderRadius: 8,
    height: 48,
    justifyContent: 'center',
    alignItems: 'center',
  },
  btnStart: {
    backgroundColor: '#30d158',
  },
  btnStop: {
    backgroundColor: '#ff453a',
  },
  btnText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: 'bold',
  },
});
