package tech.zmario.androidoverlay.service;

import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;

public class OverlayLayoutParamsBuilder {

  private OverlayLayoutParamsBuilder() {}

  public static WindowManager.LayoutParams build(
      double width,
      double height,
      double x,
      double y,
      String gravity,
      boolean focusable,
      boolean touchable,
      float density,
      int screenWidth) {
    int layoutFlag =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
    int widthPx = width > 0 ? (int) (width * density) : (int) (screenWidth * 0.95);
    int heightPx = height > 0 ? (int) (height * density) : (int) (140f * density);

    int flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

    if (focusable) {
      flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
    } else {
      flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }
    if (!touchable) {
      flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }

    WindowManager.LayoutParams params =
        new WindowManager.LayoutParams(
            widthPx, heightPx, layoutFlag, flags, PixelFormat.TRANSLUCENT);

    int gravityFlag = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
    if ("top".equalsIgnoreCase(gravity)) {
      gravityFlag = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
    } else if ("center".equalsIgnoreCase(gravity)) {
      gravityFlag = Gravity.CENTER;
    }

    params.gravity = gravityFlag;
    params.x = (int) (x * density);
    params.y = (int) (y * density);

    return params;
  }
}
