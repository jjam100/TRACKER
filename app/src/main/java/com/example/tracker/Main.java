package com.example.tracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class Main extends AppCompatActivity {
    //다른 액티비티를 띄우기 위한 요청코드(상수)
    public static final int sub = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 페이지 전환용 버튼 지정
        Button enter_button = (Button)findViewById(R.id.enter_button);

        enter_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), List.class);
                startActivityForResult(intent, sub);
            }
        });

    }
}
