package com.example.tracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class List extends AppCompatActivity {
    //다른 액티비티를 띄우기 위한 요청코드(상수)
    public static final int sub = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        // 트레커 이동 버튼 지정
        Button tracker_button1 = (Button)findViewById(R.id.tracker_button1);
        // 트래커 이동
        tracker_button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), Mood.class);
                startActivityForResult(intent, sub);
            }
        });
    }
}
