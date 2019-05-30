package com.teamTK.tracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class TkFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "TkMS";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived() 호출됨");
        if(remoteMessage.getData() == null)
            return;
    }

    private void sendNotification(String title, String content) {
        if(title == null)
            title = "TRACKER 알림입니다";

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 오레오 이상부터는 채널을 생성해야 알림이 감....
        final String CHANNEL_ID = "TRACKER_00";
        NotificationManager mManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_NAME = "TRACKER_DEV";
            final String CHANNEL_DESCRIPTION = "TRACKER 애플리케이션 개발자 알림입니다.";
            final int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[] { 100, 200, 100, 200 });
            mChannel.setSound(defaultSoundUri, null);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setWhen(System.currentTimeMillis());
        // 나중에 우리 아이콘 크기별로 조정해서 집어넣어야...
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText(content);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // 아래 설정은 오레오부터 deprecated 되면서 NotificationChannel에서 동일기능을 하는 메소드를 사용
            builder.setContentTitle(title);
            builder.setSound(defaultSoundUri);
            builder.setVibrate(new long[] { 500, 500 });
        }

        mManager.notify(0, builder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d(TAG, "New Token : " + token);
    }
}
