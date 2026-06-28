package tech.zmario.androidoverlay.module;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import tech.zmario.androidoverlay.NativeAndroidOverlaySpec;
import tech.zmario.androidoverlay.service.OverlayService;

public class OverlayModule extends NativeAndroidOverlaySpec {

  public OverlayModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @ReactMethod
  public void hasPermission(Promise promise) {
    promise.resolve(Settings.canDrawOverlays(getReactApplicationContext()));
  }

  @ReactMethod
  public void requestPermission() {
    if (!Settings.canDrawOverlays(getReactApplicationContext())) {
      Intent intent =
          new Intent(
              Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
              Uri.parse("package:" + getReactApplicationContext().getPackageName()));
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      getReactApplicationContext().startActivity(intent);
    }
  }

  @ReactMethod
  public void startOverlay(String componentName, ReadableMap options) {
    if (!Settings.canDrawOverlays(getReactApplicationContext())) {
      return;
    }
    Intent intent = new Intent(getReactApplicationContext(), OverlayService.class);
    intent.putExtra("componentName", componentName);

    if (options != null) {
      if (options.hasKey("width")) intent.putExtra("width", options.getDouble("width"));
      if (options.hasKey("height")) intent.putExtra("height", options.getDouble("height"));
      if (options.hasKey("x")) intent.putExtra("x", options.getDouble("x"));
      if (options.hasKey("y")) intent.putExtra("y", options.getDouble("y"));
      if (options.hasKey("gravity")) intent.putExtra("gravity", options.getString("gravity"));

      if (options.hasKey("draggable"))
        intent.putExtra("draggable", options.getBoolean("draggable"));

      if (options.hasKey("touchable"))
        intent.putExtra("touchable", options.getBoolean("touchable"));

      if (options.hasKey("focusable"))
        intent.putExtra("focusable", options.getBoolean("focusable"));

      if (options.hasKey("notificationTitle"))
        intent.putExtra("notificationTitle", options.getString("notificationTitle"));

      if (options.hasKey("notificationText"))
        intent.putExtra("notificationText", options.getString("notificationText"));

      if (options.hasKey("notificationIcon"))
        intent.putExtra("notificationIcon", options.getString("notificationIcon"));

      if (options.hasKey("channelId")) intent.putExtra("channelId", options.getString("channelId"));

      if (options.hasKey("channelName"))
        intent.putExtra("channelName", options.getString("channelName"));

      if (options.hasKey("foreground"))
        intent.putExtra("foreground", options.getBoolean("foreground"));
    }

    boolean foreground = true;

    if (options != null && options.hasKey("foreground")) {
      foreground = options.getBoolean("foreground");
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && foreground) {
      getReactApplicationContext().startForegroundService(intent);
    } else {
      getReactApplicationContext().startService(intent);
    }
  }

  @ReactMethod
  public void stopOverlay(String componentName) {
    if (OverlayService.getInstance() != null) {
      OverlayService.getInstance().stopOverlayInstance(componentName);
    }
  }

  @ReactMethod
  public void resizeOverlay(double width, double height, String componentName) {
    float density = getReactApplicationContext().getResources().getDisplayMetrics().density;
    int pxWidth = (int) (width * density);
    int pxHeight = (int) (height * density);

    if (OverlayService.getInstance() != null) {
      OverlayService.getInstance().resizeOverlay(componentName, pxWidth, pxHeight);
    }
  }

  @ReactMethod
  public void startMove(String componentName) {
    if (OverlayService.getInstance() != null) {
      OverlayService.getInstance().startMove(componentName);
    }
  }

  @ReactMethod
  public void moveOverlay(double dx, double dy, String componentName) {
    float density = getReactApplicationContext().getResources().getDisplayMetrics().density;

    if (OverlayService.getInstance() != null) {
      OverlayService.getInstance()
          .moveOverlay(componentName, (int) (dx * density), (int) (dy * density));
    }
  }

  @ReactMethod
  public void commitMove(String componentName) {
    if (OverlayService.getInstance() != null) {
      OverlayService.getInstance().commitMove(componentName);
    }
  }
}
