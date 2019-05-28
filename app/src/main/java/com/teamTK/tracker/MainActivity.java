package com.teamTK.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //다른 액티비티를 띄우기 위한 요청코드(상수)
    public static final int sub = 1001;
    Button buttonSignin;
    Button buttonSignup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSignin = (Button)findViewById(R.id.sign_in);
        buttonSignup = (Button)findViewById(R.id.sign_up);

        buttonSignin.setOnClickListener(this);
        buttonSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == buttonSignin) {
            startActivity(new Intent(this, SignInActivity.class));
        }
        if(view == buttonSignup) {
            startActivity(new Intent(this, SignUpActivity.class));
        }
    }
}
