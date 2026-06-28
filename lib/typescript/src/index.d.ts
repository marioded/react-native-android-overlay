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
export declare const OverlayManager: {
    startOverlay(componentName: string, options?: OverlayOptions): void;
    hasPermission(): Promise<boolean>;
    requestPermission(): void;
    stopOverlay(componentName?: string): void;
    resizeOverlay(width: number, height: number, componentName?: string): void;
    startMove(componentName?: string): void;
    moveOverlay(dx: number, dy: number, componentName?: string): void;
    commitMove(componentName?: string): void;
};
//# sourceMappingURL=index.d.ts.map