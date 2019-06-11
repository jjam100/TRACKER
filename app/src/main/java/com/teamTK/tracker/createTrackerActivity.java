package com.teamTK.tracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;
import com.teamTK.tracker.model.LegendColor;
import com.teamTK.tracker.model.Tracker;

import java.util.ArrayList;
import java.util.List;

public class createTrackerActivity extends AppCompatActivity {

    EditText trackerName;
    EditText[] legendName = new EditText[8];
    ImageView[] legendColor = new ImageView[8];
    Button creteButton;
    // 색상값을 저장하는 임시 변수
    String[] colorSave = new String[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tracker);

        Intent intent = getIntent();
        ArrayList<String> data = intent.getExtras().getStringArrayList("data");

        // 인텐트 수신 데이터
        int createOrder = Integer.parseInt(data.get(0));
        int firstYear = Integer.parseInt(data.get(1));
        int firstMonth = Integer.parseInt(data.get(2));
        int firstDay = Integer.parseInt(data.get(3));

        // 렌더링
        trackerName = (EditText) findViewById(R.id.trackerName);
        if(createOrder == 0) {
            trackerName.setText("감정");
            trackerName.setEnabled(false);
        }

        legendName[0] = (EditText)findViewById(R.id.create_legend_name1);
        legendName[1] = (EditText)findViewById(R.id.create_legend_name2);
        legendName[2] = (EditText)findViewById(R.id.create_legend_name3);
        legendName[3] = (EditText)findViewById(R.id.create_legend_name4);
        legendName[4] = (EditText)findViewById(R.id.create_legend_name5);
        legendName[5] = (EditText)findViewById(R.id.create_legend_name6);
        legendName[6] = (EditText)findViewById(R.id.create_legend_name7);
        legendName[7] = (EditText)findViewById(R.id.create_legend_name8);

        legendColor[0] = (ImageView)findViewById(R.id.create_legend_color1);
        legendColor[1] = (ImageView)findViewById(R.id.create_legend_color2);
        legendColor[2] = (ImageView)findViewById(R.id.create_legend_color3);
        legendColor[3] = (ImageView)findViewById(R.id.create_legend_color4);
        legendColor[4] = (ImageView)findViewById(R.id.create_legend_color5);
        legendColor[5] = (ImageView)findViewById(R.id.create_legend_color6);
        legendColor[6] = (ImageView)findViewById(R.id.create_legend_color7);
        legendColor[7] = (ImageView)findViewById(R.id.create_legend_color8);

        creteButton = (Button)findViewById(R.id.create_button);


        for(int i = 0; i < 8; i++) {
            // 임시 변수
            int order = i;
            legendColor[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ColorPicker cp = new ColorPicker(createTrackerActivity.this, 0,0,0);
                    cp.show();
                    cp.enableAutoClose();
                    cp.setCallback(new ColorPickerCallback() {
                        // 색 선택 시
                        @Override
                        public void onColorChosen(@ColorInt int color) {
                            //선택값 저장
                            String selected = String.format("#%06X", (0xFFFFFF & color));
                            // 배경 지정
                            legendColor[order].setBackgroundColor(Color.parseColor(selected));
                            // 임시 배열 저장 및 수정
                            colorSave[order] = selected;
                            Log.d(order + "번째 색상 : ", colorSave[order]);
                            cp.dismiss();
                        }
                    });
                }
            });
        }


        // 저장 버튼을 누를 시 이벤트
        creteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputTrackerName = trackerName.getText().toString().trim();
                if(inputTrackerName.length() == 0) {
                    // 빈칸 입력시 막아내기
                    Toast.makeText(createTrackerActivity.this, "트래커 이름은 반드시 입력해야 합니다.", Toast.LENGTH_LONG).show();

                }
                else {
                    List<LegendColor> inputLegendList = new ArrayList<>();
                    for(int i = 0; i < 8; i++) {
                        String inputLegendName = legendName[i].getText().toString().trim();
                        if(inputLegendName.length() > 0 && colorSave[i].length() > 0)
                            inputLegendList.add(new LegendColor(inputLegendName,colorSave[i],true));
                        else
                            inputLegendList.add(new LegendColor(" "," ",false));
                    }

                    Gson gson = new Gson();
                    Log.wtf("출력은???", gson.toJson(inputLegendList));

                    Tracker inputTracker = new Tracker(inputTrackerName,firstYear,firstMonth,firstDay,inputLegendList,null,true);

                    // 파이어 베이스 연결
                    final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("tracker").child(createOrder + "").setValue(inputTracker).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    });
                }
            }
        });


    }
}
