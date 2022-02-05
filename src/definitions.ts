
export type PresentationOption = 'badge' | 'sound' | 'alert';
import { PushNotificationActionPerformed, PluginListenerHandle } from '@capacitor/core'

// declare module '@capacitor/core' {
//   interface PluginRegistry {
//     DataNotification: DataNotificationPlugin;
//   }
// }

// export interface DataNotificationPlugin {
//   echo(options: { value: string }): Promise<{ value: string }>;
// }


declare module '@capacitor/core' {
    interface PluginRegistry {
     DataNotification: DataNotificationPlugin;
   }
//    export interface DataNotificationPlugin {
//   echo(options: { value: string }): Promise<{ value: string }>;
// }
 }
  export interface PluginsConfig {
    DataNotification?: {
      /**
       * This is an array of strings you can combine. Possible values in the array are:
       *   - `badge`: badge count on the app icon is updated (default value)
       *   - `sound`: the device will ring/vibrate when the push notification is received
       *   - `alert`: the push notification is displayed in a native dialog
       *
       * An empty array can be provided if none of the options are desired.
       *
       * Only available for iOS.
       *
       * @since 1.0.0
       * @example ["badge", "sound", "alert"]
       */
      presentationOptions?: PresentationOption[];
    };
  }


export interface DataNotificationPlugin {
  /**
   * Check permission to receive push notifications.
   *
   * Will always return "granted" on Android
   *
   * @since 1.0.0
   */
  checkPermissions(): Promise<PermissionStatus>;

  /**
   * Request permission to receive push notifications.
   *
   * Will always return "granted" on Android
   *
   * @since 1.0.0
   */
  requestPermissions(): Promise<PermissionStatus>;

  /**
   * Register the app to receive push notifications.
   *
   * @since 1.0.0
   */
  register(): Promise<void>;

  /**
   * Should be called to unregister the Firebase Instance. For example if a User logs out.
   *
   * @since 1.1.0
   */
  unregister(): Promise<void>;

  /**
   * Get icon badge Value
   *
   * Only available on iOS
   *
   * @since 1.2.0
   */
  getBadgeNumber(): Promise<BadgeCount>;

  /**
   * Set icon badge Value
   *
   * Only available on iOS
   *
   * @since 1.2.0
   */
  setBadgeNumber(options: BadgeCount): Promise<void>;

  /**
   * Get notifications in Notification Center
   *
   * @since 1.2.0
   */
  getDeliveredNotifications(): Promise<NotificationsResult>;

  /**
   * Remove notifications from the notifications screen based on the id
   *
   * @since 1.2.0
   */
  removeDeliveredNotifications(options: NotificationsIds): Promise<void>;

  /**
   * Remove all notifications from the notifications screen
   *
   * @since 1.2.0
   */
  removeAllDeliveredNotifications(): Promise<void>;

  /**
   * Called when a new fcm token is created
   *
   * @since 1.0.0
   */
  addListener(
    eventName: 'token',
    listenerFunc: (result: TokenResult) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Called when a new message is received
   *
   * @since 1.0.0
   */
  addListener(
    eventName: 'message',
    listenerFunc: (message: any) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Remove all native listeners for this plugin.
   *
   * @since 1.0.0
   */
  removeAllListeners(): Promise<void>;

  setAdditionalData(options: { value: string }): Promise<{value: string}>;
  addListener(eventName: 'pushNotificationActionPerformed', listenerFunc: (notification: PushNotificationActionPerformed) => void): PluginListenerHandle;

}

/**
 * @since 1.0.0
 */
export interface TokenResult {
  token: string;
}

/**
 * @since 1.2.0
 */
export interface BadgeCount {
  /**
   * @since 1.2.0
   */
  count: number;
}

/**
 * @since 1.2.0
 */
export interface NotificationsResult {
  /**
   * @since 1.2.0
   */
  notifications: any[];
}

/**
 * @since 1.2.0
 */
export interface NotificationsIds {
  /**
   * @since 1.2.0
   */
  ids: string[];
}