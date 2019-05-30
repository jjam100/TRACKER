package com.teamTK.tracker.common;


import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class NotificationModel {
    public String to;

    public Notification notification = new Notification();
    public Data data = new Data();

    public static class Notification {
        public String title;
        public String content;
        public String clickAction;
    }

    public static class Data {
        public String title;
        public String content;
        public String clickAction;
    }
}

public class FCMPush {
    String regToken;
    private static final String TAG = "TkMS_FCM";

    public FCMPush(String regToken) {
        this.regToken = regToken;
    }

    public void sendMessage() {
        Gson gson = new Gson();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.notification.title = "TRACKER DEV 알림";
        notificationModel.notification.content = "이 알람은 TRACKER DEV용 알람입니다.";
        notificationModel.notification.clickAction = "MainActivity";
        notificationModel.data.title = "TRACKER DEV 알림";
        notificationModel.data.content = "이 알람은 TRACKER DEV용 알람입니다.";
        notificationModel.data.clickAction = "MainActivity";

        notificationModel.to = regToken;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                // 해당 키 뒤에는 Firebase -> 설정 -> 클라우드 메시징 탭에 있는 서버 키를 복사해다 붙여넣어주세요.
                .addHeader("Authorization", "key=")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {                }
            @Override
            public void onResponse(Call call, Response response) throws IOException {                }
        });
    }
}