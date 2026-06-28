package tech.zmario.androidoverlay;

import androidx.annotation.NonNull;
import com.facebook.react.BaseReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;
import com.facebook.react.uimanager.ViewManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tech.zmario.androidoverlay.module.OverlayModule;

public class OverlayPackage extends BaseReactPackage {

  @Override
  public NativeModule getModule(String name, @NonNull ReactApplicationContext reactContext) {
    if (name.equals(NativeAndroidOverlaySpec.NAME)) {
      return new OverlayModule(reactContext);
    }

    return null;
  }

  @NonNull
  @Override
  public ReactModuleInfoProvider getReactModuleInfoProvider() {
    return () -> {
      Map<String, ReactModuleInfo> moduleInfos = new HashMap<>();
      boolean isTurboModule = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;

      moduleInfos.put(
          NativeAndroidOverlaySpec.NAME,
          new ReactModuleInfo(
              NativeAndroidOverlaySpec.NAME,
              NativeAndroidOverlaySpec.NAME,
              false,
              false,
              false,
              isTurboModule));
      return moduleInfos;
    };
  }

  @NonNull
  @Override
  public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }
}
