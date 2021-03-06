package com.jules-c.dataNotifications;

import androidx.annotation.NonNull;
//import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.getcapacitor.CapacitorFirebaseMessagingService;
import com.getcapacitor.JSObject;
// import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class BackgroundFCMService extends CapacitorFirebaseMessagingService {

  private static final String TAG = "BackgroundFCMService";

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  // [START receive_message]
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    // Check if message contains a data payload
     Log.d(TAG, remoteMessage.getMessageId());
    if (remoteMessage.getNotification() == null && remoteMessage.getData().size() > 0) {
      Log.d(TAG, "Message data payload: " + remoteMessage.getData());
      Log.d(TAG, "FirebaseMessage data payload: " + remoteMessage.getData());
      Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");
      String username = remoteMessage.getData().get("Username");
      String connectionId = remoteMessage.getData().get("ConnectionId");
      try {
          // Get converter with reflection so we use implementation of user
          Class BackgroundFCMHandler = Class.forName(getApplicationContext().getPackageName() + ".BackgroundFCMHandler");
          BackgroundFCMHandlerInterface converter = (BackgroundFCMHandlerInterface) BackgroundFCMHandler.newInstance();
          converter.setContext(this);
          converter.setAdditionalData(this.readFile());
          BackgroundFCMRemoteMessage message = new BackgroundFCMRemoteMessage();
          message.setId(remoteMessage.getMessageId());
          message.setData(new JSObject(remoteMessage.getData().toString()));
          BackgroundFCMData data = converter.handleNotification(message);
          if (data != null) {
              this.sendNotification(remoteMessage.getMessageId(), data.getTitle(), data.getBody(), remoteMessage.getData());
          }
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | JSONException e) {
          Log.e(TAG, e.toString());
      }


    }
    super.onMessageReceived(remoteMessage);
    DataNotification.onNewRemoteMessage(remoteMessage);

  }

  private void sendNotification(String id, String title, String message, Map<String, String> data) {
      Intent intent = new Intent(this, BackgroundFCMTapHandler.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.putExtra("id", id);
      intent.putExtra("data", data.toString());
      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
              PendingIntent.FLAG_ONE_SHOT);

      String channelId = "1";
      Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      int icon =  getApplicationContext().getResources().getIdentifier("icon_notification", "mipmap", getApplicationContext().getPackageName());
      if (icon == 0) {
          icon = getApplicationContext().getApplicationInfo().icon;
      }
      NotificationCompat.Builder notificationBuilder =
              new NotificationCompat.Builder(this, channelId)
                      .setSmallIcon(icon)
                      .setContentTitle(title)
                      .setContentText(message)
                      .setAutoCancel(true)
                      .setSound(defaultSoundUri)
                      .setContentIntent(pendingIntent);
      NotificationManager notificationManager =
              (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      // Since android Oreo notification channel is needed.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          NotificationChannel channel = new NotificationChannel(channelId,
                  "Channel human readable title",
                  NotificationManager.IMPORTANCE_DEFAULT);
          notificationManager.createNotificationChannel(channel);
      }
      int notId = 0;
      if (data.get("id") != null) {
          notId = Integer.parseInt(data.get("id"));
      }
      notificationManager.notify(notId, notificationBuilder.build());
  }
    private JSONObject readFile() {
        JSONObject jsonObject = new JSONObject();
        try {
            FileInputStream in = this.openFileInput("config.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            inputStreamReader.close();
            try {
                jsonObject = new JSONObject(sb.toString());
            }catch (JSONException err){
                Log.e(TAG, err.toString());
            }
        } catch(Exception e){
            Log.e(TAG, e.toString());
        }
        return jsonObject;
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        DataNotification.onNewToken(s);
    }
}


// public class MessagingService extends FirebaseMessagingService {

// private static final String TAG = "MyFirebaseMsgService";


//     @Override
//     public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//         Log.d(TAG, "From: " + remoteMessage.getFrom());

//         // Check if message contains a data payload.
//         if (remoteMessage.getData().size() > 0) {
//           Log.d(TAG, "FirebaseMessage data payload: " + remoteMessage.getData());
//           Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");
//           String username = remoteMessage.getData().get("Username");
//           String connectionId = remoteMessage.getData().get("ConnectionId");
//         }
//         super.onMessageReceived(remoteMessage);
//         DataNotification.onNewRemoteMessage(remoteMessage);
//     }

//     @Override
//     public void onNewToken(@NonNull String s) {
//         super.onNewToken(s);
//         DataNotification.onNewToken(s);
//     }
// }