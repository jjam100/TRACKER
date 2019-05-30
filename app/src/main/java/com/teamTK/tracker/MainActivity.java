package com.teamTK.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //다른 액티비티를 띄우기 위한 요청코드(상수)
    public static final int REQUEST_CODE_MENU = 101;
    private static final String TAG = "TkMS_MAIN";
    Button buttonSignin;
    Button buttonSignup;

    // FCM을 위한 단말의 등록 토큰
    String regToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        buttonSignin = (Button)findViewById(R.id.sign_in);
        buttonSignup = (Button)findViewById(R.id.sign_up);

        buttonSignin.setOnClickListener(this);
        buttonSignup.setOnClickListener(this);

        getRegistrationId();
    }

    public void getRegistrationId() {
        Log.d(TAG, "getRegistrationId 호출");

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // FCM 토큰값 획득
                        regToken = task.getResult().getToken();
                        Log.d(TAG, "FCM 토큰 값 : " + regToken);
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if(view == buttonSignin) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.putExtra("deviceToken", regToken);
            startActivityForResult(intent, REQUEST_CODE_MENU);
        }
        if(view == buttonSignup) {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.putExtra("deviceToken", regToken);
            startActivityForResult(intent, REQUEST_CODE_MENU);
        }
    }
}
