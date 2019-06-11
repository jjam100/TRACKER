package com.teamTK.tracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamTK.tracker.common.YearMonthPickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

import static com.teamTK.tracker.R.array;
import static com.teamTK.tracker.R.id;
import static com.teamTK.tracker.R.layout;

public class TrackerActivity extends AppCompatActivity {
    private TextView titleText;
    private Button thisMonth;
    private int[] tracker_colors;
    private Button[] dateblock = new Button[42];
    private ImageView[] legendColors = new ImageView[8];
    private TextView[] legendNames = new TextView[8];
    private Calendar cal;
    private DatabaseReference colorDBref;
    private TextView deleteTracker;
    private int year;
    private int month;

    // 데이트 피커 리스너
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int yearOfYear, int monthOfYear, int dayOfMonth){
            Log.d("TkMS", "year = " + year + ", month = " + monthOfYear + ", day = " + dayOfMonth);
            year = yearOfYear;
            month = monthOfYear;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_tracker);
        thisMonth = (Button)findViewById(id.this_month);

        Intent intent = getIntent();
        ArrayList<String> data = intent.getExtras().getStringArrayList("data");
        year = intent.getExtras().getInt("year");
        month = intent.getExtras().getInt("month");
        titleText = (TextView)findViewById(R.id.Tracker_head);
        titleText.setText(data.get(0));
        int trackerOrder = intent.getExtras().getInt("order");
        Log.wtf("아아아아아아아아아아아",intent.getExtras().getString("datum"));

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("tracker").child(trackerOrder + "").child("active").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.wtf("???잉", String.valueOf(dataSnapshot.getValue()));
                if(!Boolean.valueOf(String.valueOf(dataSnapshot.getValue()))) {
                    // Active 되어 있지 않다면 종료 한 후, List 액티비티로 돌린다.
                    finish();
                    startActivity(new Intent(getApplicationContext(), List.class)); //추가해 줄 ProfileActivity
                }else {
                    // Active 되어 있다면 다음으로 넘어간다.

                    // 현재 날짜 기준 달력 구성
                    cal = Calendar.getInstance();

                    //색상 200가지
                    //색상코드가 #f4ecf7 인 경우에는 -725769 으로 저장됨. 이를 HEX로 변경하면 FFFFF...F4ECF7 으로 변함
                    tracker_colors = getResources().getIntArray(array.tracker_colors);

                    // 해당 월 버튼
                    //thisMonth.setBackgroundColor(tracker_colors[140]);

                    // 데이트 블록들
                    // 01 : 0x7f09004f ~ 42 : 0x7f090078
                    for(int i = 0; i < dateblock.length; i++) {
                        dateblock[i] = (Button)findViewById(id.dateblock01 + i);
                        dateblock[i].setBackgroundColor(tracker_colors[140]);
                    }
                    // 달력 구성
                    makeCalender(dateblock, thisMonth, cal, data, trackerOrder);

                    // 범례 구성
                    legendColors[0] = (ImageView)findViewById(R.id.legend_color1);
                    legendColors[1] = (ImageView)findViewById(R.id.legend_color2);
                    legendColors[2] = (ImageView)findViewById(R.id.legend_color3);
                    legendColors[3] = (ImageView)findViewById(R.id.legend_color4);
                    legendColors[4] = (ImageView)findViewById(R.id.legend_color5);
                    legendColors[5] = (ImageView)findViewById(R.id.legend_color6);
                    legendColors[6] = (ImageView)findViewById(R.id.legend_color7);
                    legendColors[7] = (ImageView)findViewById(R.id.legend_color8);

                    legendNames[0] = (TextView)findViewById(id.legend_name1);
                    legendNames[1] = (TextView)findViewById(id.legend_name2);
                    legendNames[2] = (TextView)findViewById(id.legend_name3);
                    legendNames[3] = (TextView)findViewById(id.legend_name4);
                    legendNames[4] = (TextView)findViewById(id.legend_name5);
                    legendNames[5] = (TextView)findViewById(id.legend_name6);
                    legendNames[6] = (TextView)findViewById(id.legend_name7);
                    legendNames[7] = (TextView)findViewById(id.legend_name8);

                    for(int i = 1; i < data.size(); i+=3) {
                        if(data.get(i+2).equals("false")) {
                            legendNames[(i-1)/3].setText("항목 추가 ");
                            legendNames[(i-1)/3].setTextSize(15);
                            legendColors[(i-1)/3].setBackgroundColor(Color.parseColor("#E0E0E0"));
                            legendNames[(i-1)/3].setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {

                                }
                            });
                        }
                        else {
                            legendNames[(i-1)/3].setText(data.get(i));
                            legendColors[(i-1)/3].setBackgroundColor(Color.parseColor(data.get(i+1)));
                        }

                    }

                    // 트래커 삭제
                    deleteTracker = (TextView) findViewById(id.delete_tracker);
                    if(trackerOrder == 0) {
                        deleteTracker.setVisibility(View.INVISIBLE);
                    }
                    deleteTracker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("tracker").child(trackerOrder + "").child("active").setValue(false);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        thisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YearMonthPickerDialog pd = new YearMonthPickerDialog();
                pd.setListener(d);
                pd.show(getSupportFragmentManager(), "YearMonthPicker");;
            }
        });
    }

    // 달력 구성
    // 입력 : 데이터 시작 연월일
    public void makeCalender(Button[] dateblock, Button thisMonth, Calendar cal, ArrayList<String> exData, int trackerOrder) {
        int today = cal.get(cal.DATE); // 현재 일자
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month + 1);
        cal.set(Calendar.DAY_OF_MONTH,1); // 왜 에러?
        int firstDayOfWeek = cal.get(cal.DAY_OF_WEEK); // 해당 월 1일 요일
        int finalDay = cal.getActualMaximum(cal.DAY_OF_MONTH); // 해당 월 마지막 일자

        // 해당 월 표시
        thisMonth.setText(year + "년 " + month + "월");

        for(int i = 0; i < 42; i++) {
            if(i >= firstDayOfWeek - 1 && i <= firstDayOfWeek + finalDay - 2) {
                dateblock[i].setText("" + (i - firstDayOfWeek + 2));
                Log.d("Tk_MS", "아" + String.valueOf(year) + " 젠 " + String.valueOf(cal.get(cal.YEAR)) + " 장 " + String.valueOf(month) + " 씻 " + String.valueOf(cal.get(cal.MONTH)));
                if(year == cal.get(cal.YEAR) && month == cal.get(cal.MONTH) - 1 && i <= today + firstDayOfWeek - 2) {
                    //
                    dateblock[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
                }
                else {
                    dateblock[i].setBackgroundColor(Color.parseColor("#AAAAAA"));
                    dateblock[i].setEnabled(false);
                }


            } else {
                dateblock[i].setVisibility(View.INVISIBLE);
            }
            dateblock[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button thisBtn = (Button) v;
                    // 송신할 데이터 : 연, 월, 일, 트래커 순번
                    ArrayList<String> data = new ArrayList<>();
                    data.add(year + ""); //연
                    data.add(month + ""); //월
                    data.add(thisBtn.getText().toString() + ""); //일
                    data.add(trackerOrder + "");

                    for(int i = 1; i < exData.size(); i+=2) {
                        // 범례 명
                        data.add(exData.get(i));
                        // 색상
                        data.add(exData.get(i + 1));
                    }


                    Intent intent = new Intent(getApplicationContext(), DailyActivity.class);
                    intent.putExtra("btnData",data);
                    startActivityForResult(intent, 1001);
                }
            });
        }
    }
}
