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

  private boolean isDragging = false;

  private int initialX = 0;
  private int initialY = 0;
  private float initialTouchX = 0f;
  private float initialTouchY = 0f;

  public OverlayContainerView(Context context, WindowManager windowManager, boolean draggable) {
    super(context);
    this.windowManager = windowManager;
    this.draggable = draggable;

    ViewConfiguration vc = ViewConfiguration.get(context);
    this.touchSlop = vc.getScaledTouchSlop();
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (!draggable) return super.onInterceptTouchEvent(ev);

    switch (ev.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        isDragging = false;
        initialTouchX = ev.getRawX();
        initialTouchY = ev.getRawY();

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
        if (params != null) {
          initialX = params.x;
          initialY = params.y;
        }
        break;

      case MotionEvent.ACTION_MOVE:
        float dx = Math.abs(ev.getRawX() - initialTouchX);
        float dy = Math.abs(ev.getRawY() - initialTouchY);

        if (dx > touchSlop || dy > touchSlop) {
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
        if (!isDragging) {
          float dxCheck = Math.abs(event.getRawX() - initialTouchX);
          float dyCheck = Math.abs(event.getRawY() - initialTouchY);

          if (dxCheck > touchSlop || dyCheck > touchSlop) {
            isDragging = true;
          }
        }

        if (isDragging) {
          WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();

          if (params == null) {
            return true;
          }

          int totalDx = (int) (event.getRawX() - initialTouchX);
          int totalDy = (int) (event.getRawY() - initialTouchY);

          int targetX = initialX + totalDx;
          int targetY;

          if ((params.gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            targetY = initialY - totalDy;
          } else {
            targetY = initialY + totalDy;
          }

          DisplayMetrics metrics = getResources().getDisplayMetrics();

          int viewWidth = (params.width > 0) ? params.width : getWidth();
          int viewHeight = (params.height > 0) ? params.height : getHeight();

          int maxDragX = (metrics.widthPixels - viewWidth) / 2;
          int maxDragY = metrics.heightPixels - viewHeight;

          if (targetX < -maxDragX) targetX = -maxDragX;
          if (targetX > maxDragX) targetX = maxDragX;
          if (targetY < 0) targetY = 0;
          if (targetY > maxDragY) targetY = maxDragY;

          params.x = targetX;
          params.y = targetY;

          if (windowManager != null) {
            try {
              windowManager.updateViewLayout(this, params);
            } catch (IllegalArgumentException ignored) {
            }
          }
        }
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        isDragging = false;
        break;
    }

    return true;
  }
}
