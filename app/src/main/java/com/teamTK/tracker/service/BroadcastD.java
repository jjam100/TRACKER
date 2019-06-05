package com.teamTK.tracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamTK.tracker.model.UserModel;

public class BroadcastD extends BroadcastReceiver {
    String INTENT_ACTION = Intent.ACTION_BOOT_COMPLETED;
    public static final String KEY_INTENT_DATA = "deviceToken";
    private static final String TAG = "TkMS_BR";
    private UserModel userModel;
    String regToken;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "브로드캐스트 리시브");
        getUserTokenFromServer();
    }

    void getUserTokenFromServer() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG, "사용자 uid : " + uid);
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(UserModel.class);
                    regToken = userModel.getToken();
                    if (regToken != null) {
                        Log.d(TAG, "사용자 토큰 : " + regToken);
                        FCMPush fcmPush = new FCMPush(regToken);
                        fcmPush.sendMessage();
                    } else {
                        Log.d(TAG, "사용자 토큰 없음");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            Log.d(TAG, "로그아웃 유저");
        }
    }
}
