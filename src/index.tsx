import AndroidOverlay from './NativeAndroidOverlay';

export interface OverlayOptions {
  width?: number;
  height?: number;
  x?: number;
  y?: number;

  gravity?: string;

  draggable?: boolean;
  touchable?: boolean;
  focusable?: boolean;

  foreground?: boolean;

  notificationTitle?: string;
  notificationText?: string;
  notificationIcon?: string;

  channelId?: string;
  channelName?: string;
}

export const OverlayManager = {
  startOverlay(componentName: string, options: OverlayOptions = {}) {
    AndroidOverlay.startOverlay(componentName, options);
  },

  hasPermission() {
    return AndroidOverlay.hasPermission();
  },

  requestPermission() {
    AndroidOverlay.requestPermission();
  },

  stopOverlay(componentName = 'Overlay') {
    AndroidOverlay.stopOverlay(componentName);
  },

  resizeOverlay(width: number, height: number, componentName = 'Overlay') {
    AndroidOverlay.resizeOverlay(width, height, componentName);
  },

  startMove(componentName = 'Overlay') {
    AndroidOverlay.startMove(componentName);
  },

  moveOverlay(dx: number, dy: number, componentName = 'Overlay') {
    AndroidOverlay.moveOverlay(dx, dy, componentName);
  },

  commitMove(componentName = 'Overlay') {
    AndroidOverlay.commitMove(componentName);
  },
};
