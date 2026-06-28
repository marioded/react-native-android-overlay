package tech.zmario.androidoverlay.service;

import android.widget.FrameLayout;
import com.facebook.react.interfaces.fabric.ReactSurface;

public class OverlayInstance {

  private final FrameLayout overlayView;
  private final ReactSurface reactSurface;
  private int initialX = 0;
  private int initialY = 0;

  public OverlayInstance(FrameLayout overlayView, ReactSurface reactSurface) {
    this.overlayView = overlayView;
    this.reactSurface = reactSurface;
  }

  public FrameLayout getOverlayView() {
    return overlayView;
  }

  public ReactSurface getReactSurface() {
    return reactSurface;
  }

  public int getInitialX() {
    return initialX;
  }

  public void setInitialX(int initialX) {
    this.initialX = initialX;
  }

  public int getInitialY() {
    return initialY;
  }

  public void setInitialY(int initialY) {
    this.initialY = initialY;
  }
}
