package com.teamTK.tracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.teamTK.tracker.model.Tracker;

import java.util.ArrayList;
import java.util.Calendar;

public class List extends AppCompatActivity {
    //다른 액티비티를 띄우기 위한 요청코드(상수)
    public static final int sub = 1001;

    // 트래커 이동 버튼
    Button[] trackerButton = new Button[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        // 트레커 이동 버튼 지정
        trackerButton[0] = findViewById(R.id.tracker_button1);
        trackerButton[1] = findViewById(R.id.tracker_button2);
        trackerButton[2] = findViewById(R.id.tracker_button3);
        trackerButton[3] = findViewById(R.id.tracker_button4);
        trackerButton[4] = findViewById(R.id.tracker_button5);

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("tracker").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Gson gson = new Gson();
                        ArrayList<Tracker> list = (ArrayList<Tracker>)dataSnapshot.getValue();
                           for(int i = 0; i < 5; i++) {
                               if(list.size() != 0) {
                                   Tracker extract = gson.fromJson(gson.toJson(list.get(i)),Tracker.class);
                                   Log.wtf("파싱값 : ", gson.toJson(extract));
                                   // 버튼 텍스트에 리스트 이름 지정
                                   trackerButton[i].setText(extract.getName());
                                   // Active 상태에 따라 표시여부 지정
                                   if(extract.isActive()) {
                                       trackerButton[i].setTextColor(Color.parseColor("#db1c5c"));
                                       trackerButton[i].setTextSize(20);
                                       // Active 라면....
                                       int finalI = i;
                                       trackerButton[i].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               ArrayList<String> data = new ArrayList<>();
                                               data.add(extract.getName()); //Tracker 제목
                                               for(int j = 0; j < 8; j++) {
                                                   data.add(String.valueOf(extract.getLegendColor().get(j).getLegendName()));
                                                   data.add(String.valueOf(extract.getLegendColor().get(j).getColor()));
                                                   data.add(String.valueOf(extract.getLegendColor().get(j).isActive()));
                                               }

                                               Gson gson = new Gson();

                                               Intent intent = new Intent(getApplicationContext(), TrackerActivity.class);
                                               intent.putExtra("year", Calendar.getInstance().get(Calendar.YEAR));
                                               intent.putExtra("month", Calendar.getInstance().get(Calendar.MONTH) + 1);
                                               intent.putExtra("data", data);
                                               intent.putExtra("order", finalI);
                                               intent.putExtra("datum",gson.toJson(extract.getData()));
                                               startActivityForResult(intent, sub);
                                               finish();
                                           }
                                       });
                                   }
                                   else {
                                       // Acvtive 아니라면...
                                       int createOrder = i;
                                       if(i == 0) {
                                           trackerButton[i].setText("감정 트래커 만들기");
                                           trackerButton[i].setTextColor(Color.parseColor("#db1c5c"));
                                       }
                                       else
                                           trackerButton[i].setText("새로운 트래커 만들기");
                                       trackerButton[i].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               ArrayList<String> data = new ArrayList<>();
                                               data.add( createOrder + ""); //생성할 Tracker 번째 수
                                               data.add(Calendar.getInstance().YEAR + ""); // 첫 생성 연도
                                               data.add(Calendar.getInstance().MONTH + ""); // 첫 생성 월
                                               data.add(Calendar.getInstance().DATE + ""); // 첫 생성 일
                                               Intent intent = new Intent(getApplicationContext(), createTrackerActivity.class);
                                               intent.putExtra("data", data);
                                               startActivityForResult(intent, sub);
                                               finish();
                                           }
                                       });

                                   }

                               }
                           }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("", "Failed to read value.", error.toException());
                    }
                }
        );

        // 트래커 이동
        //tracker_button1.setOnClickListener(new View.OnClickListener(){
        //    @Override
        //    public void onClick(View v){
        //        Intent intent = new Intent(getApplicationContext(), TrackerActivity.class);
        //        startActivityForResult(intent, sub);
        //    }
        //});
    }
}
