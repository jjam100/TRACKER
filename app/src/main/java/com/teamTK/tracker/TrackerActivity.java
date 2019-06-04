package com.teamTK.tracker;

import android.annotation.SuppressLint;
import android.graphics.ColorSpace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.teamTK.tracker.R.*;

public class TrackerActivity extends AppCompatActivity {
    Button thisMonth;
    Button[] dateblock = new Button[42];
    int[] tracker_colors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_tracker);

        //색상 200가지
        //색상코드가 #f4ecf7 인 경우에는 -725769 으로 저장됨. 이를 HEX로 변경하면 FFFFF...F4ECF7 으로 변함
        tracker_colors = getResources().getIntArray(array.tracker_colors);

        // 해당 월 버튼
        thisMonth = (Button)findViewById(id.this_month);
        thisMonth.setBackgroundColor(tracker_colors[140]);

        // 데이트 블록들
        // 01 : 0x7f09004f ~ 42 : 0x7f090078
        for(int i = 0; i < dateblock.length; i++) {
            dateblock[i] = (Button)findViewById(id.dateblock01 + i);
            dateblock[i].setBackgroundColor(tracker_colors[140]);
        }

        makeCalender(dateblock, thisMonth,2019, 6, 2);


    }

    // 달력 구성
    // 입력 : 데이터 시작 연월일
    public void makeCalender(Button[] dateblock, Button thisMonth, int startYear, int startMonth, int startDay) {

        Calendar cal = Calendar.getInstance();
        int month = cal.get(cal.MONTH) + 1; // 현재 월
        int today = cal.get(cal.DATE); // 현재 일자

        cal.set(Calendar.DAY_OF_MONTH,1);
        int firstDayOfWeek = cal.get(cal.DAY_OF_WEEK); // 해당 월 1일 요일
        int finalDay = cal.getActualMaximum(cal.DAY_OF_MONTH); // 해당 월 마지막 일자

        for(int i = 0; i < 42; i++) {
            if(i >= firstDayOfWeek - 1 && i <= firstDayOfWeek + finalDay - 2) {
                dateblock[i].setText("" + (i - firstDayOfWeek + 2));
                if(i <= today + firstDayOfWeek - 2) {
                    //
                    dateblock[i].setBackgroundColor(tracker_colors[140]);
                }
                else {
                    dateblock[i].setBackgroundColor(tracker_colors[146]);
                    dateblock[i].setClickable(false);
                }

            } else {
                dateblock[i].setVisibility(View.INVISIBLE);
            }
        }




    }




}
