import { type TurboModule } from 'react-native';
export interface Spec extends TurboModule {
    hasPermission(): Promise<boolean>;
    requestPermission(): void;
    startOverlay(componentName: string, options?: {
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
    }): void;
    stopOverlay(componentName: string): void;
    resizeOverlay(width: number, height: number, componentName: string): void;
    startMove(componentName: string): void;
    moveOverlay(dx: number, dy: number, componentName: string): void;
    commitMove(componentName: string): void;
}
declare const _default: Spec;
export default _default;
//# sourceMappingURL=NativeAndroidOverlay.d.ts.map