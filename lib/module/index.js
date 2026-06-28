"use strict";

import AndroidOverlay from "./NativeAndroidOverlay.js";
export const OverlayManager = {
  startOverlay(componentName, options = {}) {
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
  resizeOverlay(width, height, componentName = 'Overlay') {
    AndroidOverlay.resizeOverlay(width, height, componentName);
  },
  startMove(componentName = 'Overlay') {
    AndroidOverlay.startMove(componentName);
  },
  moveOverlay(dx, dy, componentName = 'Overlay') {
    AndroidOverlay.moveOverlay(dx, dy, componentName);
  },
  commitMove(componentName = 'Overlay') {
    AndroidOverlay.commitMove(componentName);
  }
};
//# sourceMappingURL=index.js.map