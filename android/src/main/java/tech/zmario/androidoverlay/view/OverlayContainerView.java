package tech.zmario.androidoverlay.view;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class OverlayContainerView extends FrameLayout {

  private final WindowManager windowManager;
  private final boolean draggable;
  private final int touchSlop;

  private boolean isDragging;

  private int initialX;
  private int initialY;
  private float initialTouchX;
  private float initialTouchY;

  public OverlayContainerView(Context context, WindowManager windowManager, boolean draggable) {
    super(context);
    this.windowManager = windowManager;
    this.draggable = draggable;
    this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (!draggable) {
      return super.onInterceptTouchEvent(ev);
    }

    switch (ev.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        isDragging = false;
        initialTouchX = ev.getRawX();
        initialTouchY = ev.getRawY();

        WindowManager.LayoutParams params =
          (WindowManager.LayoutParams) getLayoutParams();

        if (params != null) {
          initialX = params.x;
          initialY = params.y;
        }
        break;

      case MotionEvent.ACTION_MOVE:
        if (Math.abs(ev.getRawX() - initialTouchX) > touchSlop
          || Math.abs(ev.getRawY() - initialTouchY) > touchSlop) {
          isDragging = true;
          return true;
        }
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        isDragging = false;
        break;
    }

    return super.onInterceptTouchEvent(ev);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!draggable) {
      return super.onTouchEvent(event);
    }

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_MOVE:
        if (isDragging) {
          updateOverlayPosition(event);
        }
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        isDragging = false;
        break;
    }

    return true;
  }

  private void updateOverlayPosition(MotionEvent event) {
    WindowManager.LayoutParams params =
      (WindowManager.LayoutParams) getLayoutParams();

    if (params == null) {
      return;
    }

    int totalDx = (int) (event.getRawX() - initialTouchX);
    int totalDy = (int) (event.getRawY() - initialTouchY);

    DisplayMetrics metrics = getResources().getDisplayMetrics();

    int viewWidth = params.width > 0 ? params.width : getWidth();
    int viewHeight = params.height > 0 ? params.height : getHeight();

    boolean isBottom = (params.gravity & Gravity.BOTTOM) == Gravity.BOTTOM;
    boolean isTop = (params.gravity & Gravity.TOP) == Gravity.TOP;

    int targetX = initialX + totalDx;
    int targetY = initialY + (isBottom ? -totalDy : totalDy);

    int minY;
    int maxY;

    if (isTop || isBottom) {
      minY = 0;
      maxY = metrics.heightPixels - viewHeight;
    } else {
      maxY = (metrics.heightPixels - viewHeight) / 2;
      minY = -maxY;
    }

    int maxDragX = (metrics.widthPixels - viewWidth) / 2;

    params.x = clamp(targetX, -maxDragX, maxDragX);
    params.y = clamp(targetY, minY, maxY);

    try {
      windowManager.updateViewLayout(this, params);
    } catch (IllegalArgumentException ignored) {
    }
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(value, max));
  }
}
