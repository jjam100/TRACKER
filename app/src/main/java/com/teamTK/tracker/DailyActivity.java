package com.teamTK.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamTK.tracker.model.Datum;

import java.util.ArrayList;

public class DailyActivity extends AppCompatActivity {

    int year, month, day, trackerOrder;
    TextView dateText;
    RadioButton[] legend = new RadioButton[8];
    Button buttonSave;
    RadioGroup legendCheck;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        // 데이터 수신
        Intent intent = getIntent();
        ArrayList<String> data = intent.getExtras().getStringArrayList("btnData");

        year = Integer.parseInt(data.get(0));
        month = Integer.parseInt(data.get(1));
        day = Integer.parseInt(data.get(2));
        trackerOrder = Integer.parseInt(data.get(3));


        // 라디오 그룹
        legendCheck = (RadioGroup)findViewById(R.id.legendCheck);

        // 범례 라디오 버튼 값 표시
        legend[0] = (RadioButton)findViewById(R.id.legend1);
        legend[1] = (RadioButton)findViewById(R.id.legend2);
        legend[2] = (RadioButton)findViewById(R.id.legend3);
        legend[3] = (RadioButton)findViewById(R.id.legend4);
        legend[4] = (RadioButton)findViewById(R.id.legend5);
        legend[5] = (RadioButton)findViewById(R.id.legend6);
        legend[6] = (RadioButton)findViewById(R.id.legend7);
        legend[7] = (RadioButton)findViewById(R.id.legend8);

        for(int order = 4; order < data.size(); order+=3) {
            if(Boolean.valueOf(data.get(order+2))) {
                // 범례 표시
                legend[(order - 3)/3].setText(data.get(order));
            }
            else {
                legend[(order - 3)/3].setVisibility(View.INVISIBLE);
            }

        }

        //데이터 전체 표시
        for(int i = 0; i < data.size(); i++) {
            Log.d("데이터"+ i + " : ", data.get(i));
        }

        // 날짜 표시
        dateText = (TextView) findViewById(R.id.date_text);
        dateText.setText(year + "." + month + "." + day);

        // 저장
        buttonSave = (Button)findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            int checkedValue;
            @Override
            public void onClick(View v) {
                // 선택 여부
                for(int i = 0; i < 8; i++) {
                    if(legend[i].isChecked()) {
                        checkedValue = i;
                    }
                }

                //public Datum(Integer year, Integer month, Integer day, Integer value) {

                // 파이어 베이스 연결
                final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Datum inputData = new Datum(year,month,day,checkedValue);
                FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("tracker").child(trackerOrder+ "").child("size").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Incremental key를 만들기 위한 개삽질의 흔적
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("tracker").child(trackerOrder+ "").child("data").child(dataSnapshot.getValue() + "").setValue(inputData);
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("tracker").child(trackerOrder+ "").child("size").setValue(Integer.parseInt(String.valueOf(dataSnapshot.getValue())) + 1);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

    }


}
