# react-native-android-overlay

A lightweight and performant React Native library for Android that allows you to render registered React Native
components inside a floating system overlay window on top of other applications.

---

## Features

- Render React Native components as floating overlays above other apps
- Built on top of the new React Native architecture
- Automatically tracks the application activity lifecycle
- Keeps the JavaScript runtime active when the app is minimized, allowing animations, timers and JS logic to continue
  running
- Supports drag and move gestures with touch interception
- Fully configurable Android foreground service notification:
  - Title
  - Description
  - Icon
  - Notification channel

---

## Requirements

- React Native >= 0.76
- Android >= 8.0 (API 26)
- New Architecture enabled

---

## Installation

```bash
npm install react-native-android-overlay
```

or:

```bash
yarn add react-native-android-overlay
```

or:

```bash
pnpm add react-native-android-overlay
```

---

## Android information

The library automatically merges the required permissions into your Android manifest:

```xml

<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE"/>
```

### Permissions

- `SYSTEM_ALERT_WINDOW`
  - Allows displaying overlays above other applications

- `FOREGROUND_SERVICE`
  - Required to keep the overlay service alive in background

- `FOREGROUND_SERVICE_SPECIAL_USE`
  - Required on Android 14+

---

## Foreground service

This library uses an Android foreground service to keep the React Native runtime alive while your application is
minimized.

When `foreground: true`:

- Android displays a persistent foreground service notification
- JavaScript logic continues running

> NOTE: Android requires a foreground service notification to keep the service alive.
> On Android 13+, the notification visibility depends on the user's notification permission settings.
> By default,
> the notification may not be visible unless the app has requested and been granted notification permission.

When `foreground: false`:

- Android may stop the process at any time depending on system policies

---

# Usage

## 1. Register your overlay component

In your root entry file (`index.js` / `index.ts`):

```javascript
import { AppRegistry } from 'react-native';
import App from './App';
import MyOverlayComponent from './MyOverlayComponent';

AppRegistry.registerComponent(
  'MainApp',
  () => App
);

// Register component used by the overlay
AppRegistry.registerComponent(
  'OverlayView',
  () => MyOverlayComponent
);
```

---

## 2. Start the overlay

```typescript
import { OverlayManager } from 'react-native-android-overlay';

const startOverlay = async () => {
  const hasPermission = await OverlayManager.hasPermission();

  if (!hasPermission) {
    OverlayManager.requestPermission();
    return;
  }

  OverlayManager.startOverlay('OverlayView', {
    width: 300,
    height: 150,

    x: 0,
    y: 150,

    gravity: 'bottom',

    draggable: true,
    touchable: true,
    focusable: false,

    foreground: true,

    notificationTitle: 'Overlay Active',
    notificationText: 'Overlay is running in background',
    notificationIcon: 'ic_launcher',

    channelId: 'overlay_channel',
    channelName: 'Overlay Service'
  });
};
```

---

## 3. Stop the overlay

```typescript
const stopOverlay = () => {
  OverlayManager.stopOverlay('OverlayView');
};
```

---

## Example overlay component

```tsx
import { View, Text } from 'react-native';

export default function MyOverlayComponent() {
  return (
    <View>
      <Text>
        Hello from overlay
      </Text>
    </View>
  );
}
```

---

## Overlay resizing

Resize the overlay container at runtime:

```typescript
OverlayManager.resizeOverlay(
  320,
  200,
  'OverlayView'
);
```

---

## Overlay movement

Move the overlay window manually from JavaScript:

```typescript
OverlayManager.startMove('OverlayView');

OverlayManager.moveOverlay(
  dx,
  dy,
  'OverlayView'
);

OverlayManager.commitMove('OverlayView');
```

---

# API reference

## OverlayManager

| Method                | Arguments                                               | Returns            | Description                                 |
|-----------------------|---------------------------------------------------------|--------------------|---------------------------------------------|
| `hasPermission()`     | None                                                    | `Promise<boolean>` | Checks if overlay permission is granted     |
| `requestPermission()` | None                                                    | `void`             | Opens Android overlay permission settings   |
| `startOverlay()`      | `componentName: string, options?: OverlayOptions`       | `void`             | Starts the overlay service                  |
| `stopOverlay()`       | `componentName?: string`                                | `void`             | Removes overlay and stops service if unused |
| `resizeOverlay()`     | `width: number, height: number, componentName?: string` | `void`             | Changes overlay window size                 |
| `startMove()`         | `componentName?: string`                                | `void`             | Prepares overlay movement                   |
| `moveOverlay()`       | `dx: number, dy: number, componentName?: string`        | `void`             | Moves overlay by offset                     |
| `commitMove()`        | `componentName?: string`                                | `void`             | Saves final overlay position                |

---

## OverlayOptions

```typescript
interface OverlayOptions {
  width?: number;              // Width of the overlay window in DP
  height?: number;             // Height of the overlay window in DP

  x?: number;                  // Horizontal coordinates offset in DP. Defaults to 0
  y?: number;                  // Vertical coordinates offset in DP. Defaults to 150dp

  gravity?:
    'top' |
    'bottom' |
    'center';                  // Gravity anchor for overlay positioning. Defaults to 'bottom'

  draggable?: boolean;         // Enable/disable dragging gestures to move overlay (defaults to true)
  touchable?: boolean;         // If false, touch events pass through to windows beneath (defaults to true)
  focusable?: boolean;         // If true, overlay can receive keyboard/input focus (defaults to false)

  foreground?: boolean;        // Runs overlay inside a persistent foreground service (defaults to true) so that JS logic continues running when app is minimized

  notificationTitle?: string;  // Custom title for foreground service notification
  notificationText?: string;   // Custom message text for foreground service notification
  notificationIcon?: string;   // Resdrawable name of the notification icon (e.g. "ic_launcher")

  channelId?: string;          // Notification channel ID (defaults to "OverlayServiceChannel")
  channelName?: string;        // Notification channel name (defaults to "Overlay Service")
}
```
