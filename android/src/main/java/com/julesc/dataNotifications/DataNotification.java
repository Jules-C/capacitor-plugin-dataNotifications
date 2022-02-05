package com.julesc.datanotifications;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginHandle;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import com.getcapacitor.Bridge;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.json.JSONException;

import com.getcapacitor.PluginRequestCodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//@CapacitorPlugin(requestCodes = PluginRequestCodes.NOTIFICATION_OPEN)
//changed name here
@CapacitorPlugin(name = "DataNotification", permissions = @Permission(strings = {}, alias = "receive"))
public class DataNotification extends Plugin {

    private static final String TAG = "FirebasePushPlugin";
    private static final String TAG2 = "BackgroundFCM";

    public static BackgroundFCMRemoteMessage remoteMessage;

    public static Bridge staticBridge = null;

    private static boolean registered = false;
    private static ArrayList<Bundle> notificationStack = null;

    public NotificationManager notificationManager;

    @Override
    //method to load the NotificationManager class
    public void load() {
        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        staticBridge = this.bridge;
        if (this.remoteMessage != null) {
            this.handleNotificationTap(this.remoteMessage);
            this.remoteMessage = null;
        }
    }

    @PluginMethod
    public void register(PluginCall call) {
        new Handler()
            .post(
                () -> {
                    FirebaseApp.initializeApp(this.getContext());
                    registered = true;
                    this.sendStacked();
                    call.resolve();

                    FirebaseMessaging
                        .getInstance()
                        .getToken()
                        .addOnCompleteListener(
                            task -> {
                                if (task.isSuccessful()) {
                                    this.sendToken(task.getResult());
                                }
                            }
                        );
                }
            );
    }

    @PluginMethod
    public void unregister(PluginCall call) {
        new Handler()
            .post(
                () -> {
                    FirebaseInstallations.getInstance().delete();
                    call.resolve();
                }
            );
    }

    @PluginMethod
    public void getDeliveredNotifications(PluginCall call) {
        JSArray notifications = new JSArray();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

            for (StatusBarNotification notif : activeNotifications) {
                JSObject jsNotif = new JSObject();

                jsNotif.put("id", notif.getId());

                Notification notification = notif.getNotification();
                if (notification != null) {
                    jsNotif.put("title", notification.extras.getCharSequence(Notification.EXTRA_TITLE));
                    jsNotif.put("body", notification.extras.getCharSequence(Notification.EXTRA_TEXT));
                    jsNotif.put("group", notification.getGroup());
                    jsNotif.put("groupSummary", 0 != (notification.flags & Notification.FLAG_GROUP_SUMMARY));

                    JSObject extras = new JSObject();

                    for (String key : notification.extras.keySet()) {
                        extras.put(key, notification.extras.get(key));
                    }

                    jsNotif.put("data", extras);
                }

                notifications.put(jsNotif);
            }
        }

        JSObject result = new JSObject();
        result.put("notifications", notifications);
        call.resolve(result);
    }

    @PluginMethod
    public void removeDeliveredNotifications(PluginCall call) {
        JSArray notifications = call.getArray("ids");

        List<Integer> ids = new ArrayList<>();
        try {
            ids = notifications.toList();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }

        for (int id : ids) {
            notificationManager.cancel(id);
        }

        call.resolve();
    }

    @PluginMethod
    public void removeAllDeliveredNotifications(PluginCall call) {
        notificationManager.cancelAll();
        call.resolve();
    }

    @PluginMethod
    public void getBadgeNumber(PluginCall call) {
        call.unimplemented("Not implemented on Android.");
    }

    @PluginMethod
    public void setBadgeNumber(PluginCall call) {
        call.unimplemented("Not implemented on Android.");
    }

    public void sendToken(String token) {
        JSObject data = new JSObject();
        data.put("token", token);
        notifyListeners("token", data, true);
    }

    public void sendRemoteMessage(RemoteMessage message) {
        String messageType = "data";
        String title = null;
        String body = null;
        String id = null;
        String sound = null;
        String vibrate = null;
        String color = null;
        String icon = null;
        String channelId = null;

        Map<String, String> data = message.getData();

        if (message.getNotification() != null) {
            messageType = "notification";
            id = message.getMessageId();
            RemoteMessage.Notification notification = message.getNotification();
            title = notification.getTitle();
            body = notification.getBody();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channelId = notification.getChannelId();
            }
            sound = notification.getSound();
            color = notification.getColor();
            icon = notification.getIcon();
        }

        if (TextUtils.isEmpty(id)) {
            Random rand = new Random();
            int n = rand.nextInt(50) + 1;
            id = Integer.toString(n);
        }

        Log.d(
            TAG,
            "sendMessage(): messageType=" +
            messageType +
            "; id=" +
            id +
            "; title=" +
            title +
            "; body=" +
            body +
            "; sound=" +
            sound +
            "; vibrate=" +
            vibrate +
            "; color=" +
            color +
            "; icon=" +
            icon +
            "; channel=" +
            channelId +
            "; data=" +
            data.toString()
        );
        Bundle bundle = new Bundle();
        for (String key : data.keySet()) {
            bundle.putString(key, data.get(key));
        }
        bundle.putString("messageType", messageType);
        this.putKVInBundle("google.message_id", id, bundle);
        this.putKVInBundle("title", title, bundle);
        this.putKVInBundle("body", body, bundle);
        this.putKVInBundle("sound", sound, bundle);
        this.putKVInBundle("vibrate", vibrate, bundle);
        this.putKVInBundle("color", color, bundle);
        this.putKVInBundle("icon", icon, bundle);
        this.putKVInBundle("channel_id", channelId, bundle);
        this.putKVInBundle("from", message.getFrom(), bundle);
        this.putKVInBundle("collapse_key", message.getCollapseKey(), bundle);
        this.putKVInBundle("google.sent_time", String.valueOf(message.getSentTime()), bundle);
        this.putKVInBundle("google.ttl", String.valueOf(message.getTtl()), bundle);

        if (!registered) {
            if (DataNotification.notificationStack == null) {
                DataNotification.notificationStack = new ArrayList<>();
            }
            notificationStack.add(bundle);
            return;
        }

        this.sendRemoteBundle(bundle);
    }

    private void sendRemoteBundle(Bundle bundle) {
        JSObject json = new JSObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            json.put(key, bundle.get(key));
        }
        notifyListeners("message", json, true);
    }

    public static void onNewToken(String newToken) {
        DataNotification pushPlugin = DataNotification.getInstance();
        if (pushPlugin != null) {
            pushPlugin.sendToken(newToken);
        }
    }

    public static void onNewRemoteMessage(RemoteMessage message) {
        DataNotification pushPlugin = DataNotification.getInstance();
        if (pushPlugin != null) {
            pushPlugin.sendRemoteMessage(message);
        }
    }

    @Override
    public void handleOnNewIntent(Intent intent) {
        final Bundle data = intent.getExtras();
        if (data != null && data.containsKey("google.message_id")) {
            data.putString("messageType", "notification");
            data.putString("tap", "background");
            Log.d(TAG, "Notification message on new intent: " + data.toString());
            this.sendRemoteBundle(data);
        }
    }

    private void sendStacked() {
        if (DataNotification.notificationStack != null) {
            for (Bundle bundle : DataNotification.notificationStack) {
                this.sendRemoteBundle(bundle);
            }
            DataNotification.notificationStack.clear();
        }
    }

    public static DataNotification getInstance() {
        if (staticBridge != null && staticBridge.getWebView() != null) {
            PluginHandle handle = staticBridge.getPlugin("DataNotification");
            if (handle == null) {
                return null;
            }
            return (DataNotification) handle.getInstance();
        }
        return null;
    }

    private void putKVInBundle(String k, String v, Bundle o) {
        if (v != null && !o.containsKey(k)) {
            o.putString(k, v);
        }
    }
    @PluginMethod()
    public void setAdditionalData(PluginCall call) {
        String data = call.getString("value");
        Context context = staticBridge.getContext();
        File path = context.getFilesDir();
        File file = new File(path, "config.txt");
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file);
            stream.write(data.getBytes());

            stream.close();
            JSObject ret = new JSObject();
            ret.put("value", data);
            call.success(ret);
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
            call.error("File write failed: " + e.toString());
        }
    }

    public void handleNotificationTap(BackgroundFCMRemoteMessage remoteMessage) {
        JSObject notificationJson = new JSObject();
        notificationJson.put("id", remoteMessage.getId());
        notificationJson.put("data", remoteMessage.getData());
        JSObject actionJson = new JSObject();
        actionJson.put("actionId", "tap");
        actionJson.put("notification", notificationJson);
        notifyListeners("pushNotificationActionPerformed", actionJson, true);
    }

    public static void onNotificationTap(BackgroundFCMRemoteMessage remoteMessage) {
        DataNotification pushPlugin = DataNotification.getBackgroundFCMInstance();
        if (pushPlugin == null) {
            DataNotification.remoteMessage = remoteMessage;
        } else {
            pushPlugin.handleNotificationTap(remoteMessage);
        }

    }

    public static DataNotification getBackgroundFCMInstance() {
        if (staticBridge != null && staticBridge.getWebView() != null) {
            PluginHandle handle = staticBridge.getPlugin("DataNotification");
            if (handle == null) {
                return null;
            }
            return (DataNotification) handle.getInstance();
        }
        return null;
    }
}


// @NativePlugin(requestCodes = PluginRequestCodes.NOTIFICATION_OPEN)

// @NativePlugin
// public class DataNotification extends Plugin {

//     @PluginMethod
//     public void echo(PluginCall call) {
//         String value = call.getString("value");

//         JSObject ret = new JSObject();
//         ret.put("value", value);
//         call.success(ret);
//     }
// }
