package tech.zmario.androidoverlay.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactHost;
import com.facebook.react.interfaces.fabric.ReactSurface;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import tech.zmario.androidoverlay.view.OverlayContainerView;

public class OverlayService extends Service {

  private static final String STOP_SERVICE_ACTION_NAME = "STOP_SERVICE_ACTION";
  private static final String TAG = "OverlayService";
  private static final String DEFAULT_CHANNEL_ID = "OverlayServiceChannel";
  private static final int NOTIFICATION_ID = 1;
  private static OverlayService instance = null;

  private final Map<String, OverlayInstance> overlays = new ConcurrentHashMap<>();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private Application.ActivityLifecycleCallbacks lifecycleCallbacks = null;
  private WindowManager windowManager;
  private boolean isDestroyed = false;

  public static OverlayService getInstance() {
    return instance;
  }

  private ReactHost getReactHost() {
    if (getApplication() instanceof ReactApplication reactApplication) {
      return reactApplication.getReactHost();
    }

    return null;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    isDestroyed = false;
    windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

    keepReactHostAlive();

    lifecycleCallbacks =
        new Application.ActivityLifecycleCallbacks() {
          @Override
          public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {}

          @Override
          public void onActivityStarted(@NonNull Activity activity) {}

          @Override
          public void onActivityResumed(@NonNull Activity activity) {
            keepReactHostAlive();
          }

          @Override
          public void onActivityPaused(@NonNull Activity activity) {
            mainHandler.postDelayed(() -> keepReactHostAlive(), 100);
          }

          @Override
          public void onActivityStopped(@NonNull Activity activity) {}

          @Override
          public void onActivitySaveInstanceState(
              @NonNull Activity activity, @NonNull Bundle outState) {}

          @Override
          public void onActivityDestroyed(@NonNull Activity activity) {
            mainHandler.postDelayed(() -> keepReactHostAlive(), 100);
          }
        };

    getApplication().registerActivityLifecycleCallbacks(lifecycleCallbacks);
    Log.d(TAG, "Service onCreate");
  }

  private void keepReactHostAlive() {
    if (isDestroyed) return;
    ReactHost reactHost = getReactHost();

    if (reactHost != null) {
      reactHost.onHostResume(null);
    }
  }

  private boolean isAppInForeground() {
    ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
    ActivityManager.getMyMemoryState(appProcessInfo);
    return (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
  }

  private void runOnMainThread(Runnable runnable) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      runnable.run();
    } else {
      mainHandler.post(runnable);
    }
  }

  private void showOverlay(
      String componentName,
      double width,
      double height,
      double x,
      double y,
      String gravity,
      boolean focusable,
      boolean draggable,
      boolean touchable) {
    runOnMainThread(
        () -> {
          if (isDestroyed || overlays.containsKey(componentName) || windowManager == null) return;
          Log.d(TAG, "Creating overlay component: " + componentName);

          float density = getResources().getDisplayMetrics().density;
          int screenWidth = getResources().getDisplayMetrics().widthPixels;

          WindowManager.LayoutParams params =
              OverlayLayoutParamsBuilder.build(
                  width, height, x, y, gravity, focusable, touchable, density, screenWidth);
          ReactHost reactHost = getReactHost();

          if (reactHost == null) return;

          keepReactHostAlive();

          ReactSurface reactSurface =
              reactHost.createSurface(getApplicationContext(), componentName, null);
          reactSurface.start();

          View reactView = reactSurface.getView();
          OverlayContainerView container =
              new OverlayContainerView(OverlayService.this, windowManager, draggable);

          container.addView(
              reactView,
              new FrameLayout.LayoutParams(
                  FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

          try {
            windowManager.addView(container, params);

            OverlayInstance newInstance = new OverlayInstance(container, reactSurface);

            overlays.put(componentName, newInstance);
          } catch (Exception e) {
            Log.e(TAG, "Failed to add window overlay", e);
            reactSurface.stop();
          }
        });
  }

  public void stopOverlayInstance(String componentName) {
    runOnMainThread(
        () -> {
          OverlayInstance inst = overlays.remove(componentName);
          if (inst != null) {
            Log.d(TAG, "Stopping overlay: " + componentName);
            cleanUpInstance(inst);
          }

          if (overlays.isEmpty() && !isDestroyed) {
            stopSelf();
          }
        });
  }

  public void startMove(String componentName) {
    runOnMainThread(
        () -> {
          OverlayInstance inst = getOverlayInstance(componentName);
          if (inst == null) return;
          WindowManager.LayoutParams params =
              (WindowManager.LayoutParams) inst.getOverlayView().getLayoutParams();
          inst.setInitialX(params.x);
          inst.setInitialY(params.y);
        });
  }

  public void moveOverlay(String componentName, int dx, int dy) {
    runOnMainThread(
        () -> {
          OverlayInstance inst = getOverlayInstance(componentName);
          if (inst == null) return;
          WindowManager.LayoutParams params =
              (WindowManager.LayoutParams) inst.getOverlayView().getLayoutParams();

          params.x = inst.getInitialX() + dx;
          if ((params.gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            params.y = inst.getInitialY() - dy;
          } else {
            params.y = inst.getInitialY() + dy;
          }

          try {
            windowManager.updateViewLayout(inst.getOverlayView(), params);
          } catch (Exception e) {
            Log.e(TAG, "Failed to move overlay", e);
          }
        });
  }

  public void commitMove(String componentName) {
    runOnMainThread(
        () -> {
          OverlayInstance inst = getOverlayInstance(componentName);

          if (inst == null) return;
          WindowManager.LayoutParams params =
              (WindowManager.LayoutParams) inst.getOverlayView().getLayoutParams();

          inst.setInitialX(params.x);
          inst.setInitialY(params.y);
        });
  }

  public void resizeOverlay(String componentName, int width, int height) {
    runOnMainThread(
        () -> {
          OverlayInstance inst = getOverlayInstance(componentName);
          if (inst == null) return;
          WindowManager.LayoutParams params =
              (WindowManager.LayoutParams) inst.getOverlayView().getLayoutParams();

          if (params.width != width || params.height != height) {
            params.width = width;
            params.height = height;
            try {
              windowManager.updateViewLayout(inst.getOverlayView(), params);
            } catch (Exception e) {
              Log.e(TAG, "Failed to resize overlay", e);
            }
          }
        });
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      String action = intent.getAction();

      if (action != null && action.equals(STOP_SERVICE_ACTION_NAME)) {
        stopSelf();
        return START_NOT_STICKY;
      }

      if (action == null) {
        boolean foreground = intent.getBooleanExtra("foreground", true);

        if (foreground) {
          Notification notification = createForegroundNotification(intent);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
          } else {
            startForeground(NOTIFICATION_ID, notification);
          }
        }

        String componentName = intent.getStringExtra("componentName");
        double width = intent.getDoubleExtra("width", -1);
        double height = intent.getDoubleExtra("height", -1);
        double x = intent.getDoubleExtra("x", 0);
        double y = intent.getDoubleExtra("y", 150);
        boolean focusable = intent.getBooleanExtra("focusable", false);
        boolean draggable = intent.getBooleanExtra("draggable", true);
        boolean touchable = intent.getBooleanExtra("touchable", true);
        String gravity = intent.getStringExtra("gravity");

        if (componentName == null) componentName = "Overlay";
        if (gravity == null) gravity = "bottom";

        showOverlay(componentName, width, height, x, y, gravity, focusable, draggable, touchable);
      }
    }
    return START_STICKY;
  }

  private void cleanUpInstance(OverlayInstance inst) {
    if (inst.getOverlayView() != null && windowManager != null) {
      try {
        inst.getOverlayView().removeAllViews();
        windowManager.removeView(inst.getOverlayView());
      } catch (Exception e) {
        Log.e(TAG, "Error removing view", e);
      }
    }
    if (inst.getReactSurface() != null) {
      inst.getReactSurface().stop();
    }
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "Service onDestroy");
    isDestroyed = true;
    instance = null;

    if (lifecycleCallbacks != null) {
      getApplication().unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
      lifecycleCallbacks = null;
    }

    ReactHost reactHost = getReactHost();

    if (reactHost != null && !isAppInForeground()) {
      reactHost.onHostPause();
    }

    stopForeground(STOP_FOREGROUND_REMOVE);

    if (windowManager != null) {
      for (OverlayInstance inst : overlays.values()) {
        cleanUpInstance(inst);
      }
    }
    overlays.clear();
    windowManager = null;
    super.onDestroy();
  }

  private void createNotificationChannel(String channelId, String channelName) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel serviceChannel =
          new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
      serviceChannel.setShowBadge(false);

      NotificationManager manager = getSystemService(NotificationManager.class);
      if (manager != null) {
        manager.createNotificationChannel(serviceChannel);
      }
    }
  }

  private OverlayInstance getOverlayInstance(String componentName) {
    if (windowManager == null || componentName == null) return null;
    OverlayInstance inst = overlays.get(componentName);
    if (inst == null || inst.getOverlayView() == null) return null;
    return inst;
  }

  private Notification createForegroundNotification(Intent intent) {
    String title = intent.getStringExtra("notificationTitle");
    String text = intent.getStringExtra("notificationText");
    String iconName = intent.getStringExtra("notificationIcon");
    String channelId = intent.getStringExtra("channelId");
    String channelName = intent.getStringExtra("channelName");

    if (channelId == null) channelId = DEFAULT_CHANNEL_ID;
    if (channelName == null) channelName = "Overlay Service";

    createNotificationChannel(channelId, channelName);

    int smallIcon = android.R.drawable.ic_media_play;
    if (iconName != null) {
      int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
      if (resId == 0) {
        resId = getResources().getIdentifier(iconName, "mipmap", getPackageName());
      }
      if (resId != 0) {
        smallIcon = resId;
      }
    }

    return new NotificationCompat.Builder(this, channelId)
        .setContentTitle(title != null ? title : "Overlay running")
        .setContentText(text != null ? text : "View is running in the background")
        .setSmallIcon(smallIcon)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setOngoing(true)
        .build();
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    Intent stopIntent = new Intent(this, OverlayService.class);

    stopIntent.setAction(STOP_SERVICE_ACTION_NAME);

    this.startService(stopIntent);
    super.onTaskRemoved(rootIntent);
  }
}
