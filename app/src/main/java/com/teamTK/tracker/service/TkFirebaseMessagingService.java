package com.teamTK.tracker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.teamTK.tracker.MainActivity;
import com.teamTK.tracker.R;

public class TkFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "TkMS";
    private static final int IconColor = 0x578db1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        // 메시지에는 두가지 종류가 있다고 한다...
        // 1. 메시지가 data payload를 포함하는 경우...
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }
        // 2. 메시지가 notification payload를 포함하는 경우...
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        //String title = remoteMessage.getNotification().getTitle();
        //String body = remoteMessage.getNotification().getBody();
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("content");
        String click_action = remoteMessage.getData().get("clickAction");
        sendNotification(title, body, click_action);
    }

    // 아래 둘은 나중에 알아 볼것.
    private void scheduleJob() {
        Log.d(TAG, "스케쥴잡.");
    }
    private void handleNow() {
        Log.d(TAG, "10초이내 처리됨");
    }

    private void sendNotification(String title, String content, String click_action) {
        if(title == null)
            title = "TRACKER 알림";

        Intent intent;
        if (click_action.equals("MainActivity")) {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 오레오 이상부터는 채널을 생성해야 알림이 감....
        final String CHANNEL_ID = "TRACKER_00";
        NotificationManager mManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_NAME = "TRACKER";
            final String CHANNEL_DESCRIPTION = "TRACKER 애플리케이션 알림입니다.";
            final int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[] { 100, 200, 100, 200 });
            mChannel.setSound(defaultSoundUri, null);
            mChannel.setLightColor(Color.GREEN);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentText(content);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        builder.setSmallIcon(getNotificationIcon());
        builder.setColor(IconColor);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // 아래 설정은 오레오부터 deprecated 되면서 NotificationChannel에서 동일기능을 하는 메소드를 사용
            builder.setSound(defaultSoundUri);
            builder.setLights(Color.GREEN, 1, 1);
            builder.setVibrate(new long[] { 500, 500 });
        }

        mManager.notify(0, builder.build());
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_stat_name : R.drawable.ic_stat_name;
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d(TAG, "New Token : " + token);
    }
}
