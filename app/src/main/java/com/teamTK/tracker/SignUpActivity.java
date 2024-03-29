package com.teamTK.tracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.teamTK.tracker.model.LegendColor;
import com.teamTK.tracker.model.Tracker;
import com.teamTK.tracker.model.UserModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    //다른 액티비티를 띄우기 위한 요청코드(상수)
    public static final int REQUEST_CODE_MENU = 103;
    public static final String KEY_INTENT_DATA = "deviceToken";
    private static final String TAG = "TkMS_SignUp";
    //define view objects
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonSignup;
    TextView textviewSignin;
    TextView textviewMessage;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    // 파이어베이스 오브젝트
    FirebaseAuth firebaseAuth;
    // FCM을 위한 단말의 등록 토큰
    String regToken;
    Calendar cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 전달 받은 인텐트 확인
        Intent intent = getIntent();
        processIntent(intent);

        // FirebaseAuth 오브젝트 초기화
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            //이미 로그인 되었다면 이 액티비티를 종료함
            finish();
            //그리고 profile 액티비티를 연다.
            startActivity(new Intent(getApplicationContext(), HomeActivity.class)); //추가해 줄 ProfileActivity
        }
        //initializing views
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textviewSignin= (TextView) findViewById(R.id.textViewSignin);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);
        buttonSignup = (Button) findViewById(R.id.buttonSignup);
        progressDialog = new ProgressDialog(this);

        //button click event
        buttonSignup.setOnClickListener(this);
        textviewSignin.setOnClickListener(this);
    }

    private void processIntent (Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            regToken = (String) bundle.getString(KEY_INTENT_DATA);
            Log.d(TAG, "넘겨받은 데이터 : " + regToken);
        }
    }

    // Firebase에 새 유저 생성
    private void registerUser() {
        // 사용자가 입력하는 email, password를 가져온다.
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        // email과 password가 비었는지 아닌지를 체크 한다.
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(password.length()<8) {
            Toast.makeText(this, "비밀번호는 최소 8자리 이상이여야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // email과 password가 제대로 입력되어 있다면 계속 진행된다.
        progressDialog.setMessage("등록중입니다. 기다려 주세요...");
        progressDialog.show();

        // 새 유저 생성
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String uid = FirebaseAuth.getInstance().getUid();
                            UserModel userModel = new UserModel();
                            userModel.setUid(uid);
                            userModel.setUserid(email);
                            userModel.setUsernm(extractIDFromEmail(email));
                            userModel.setToken(regToken);

                            List<Tracker> trackers = new ArrayList<Tracker>();
                            List<LegendColor> legendColors = new ArrayList<>();
                            Calendar cal;
                            cal = Calendar.getInstance();

                            // 범례 8개 고정
                            for(int i = 1; i <= 8; i++) {
                                legendColors.add(new LegendColor("항목"+i,"#FFFFFF",false));
                            }

                            // 트래커 5개 고정
                            for(int i = 1; i <= 5; i++) {
                                trackers.add(new Tracker((i == 1) ? "감정" : "트래커" + i, cal.get(cal.YEAR),cal.get(cal.MONTH),cal.get(cal.DATE), legendColors, null, false));
                            }

                            userModel.setTracker(trackers);


                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    // 해당기기의 언어 설정
                                    firebaseAuth.useAppLanguage();
                                    // 이메일 인증 전송
                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "이메일 전송 성공");
                                                Toast.makeText(SignUpActivity.this, "회원가입이 완료되었습니다!\n이메일을 확인해주세요!", Toast.LENGTH_SHORT).show();
                                                Intent  intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                firebaseAuth.signOut();
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Log.d(TAG, "이메일 전송 실패");
                                                Toast.makeText(SignUpActivity.this, "이메일 전송에 실패하였습니다.\n관리자에게 문의주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            //에러발생시
                            textviewMessage.setText("이미 등록된 이메일입니다.\n비밀번호 찾기를 이용해 주세요.");
                            //Snackbar.make(getWindow().getDecorView().getRootView(), "등록 에러!", Snackbar.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    // 이메일에서 @의 앞부분을 닉네임으로 설정
    String extractIDFromEmail(String email) {
        String[] parts = email.split("@");
        return parts[0];
    }

    // 버튼 클릭 이벤트
    @Override
    public void onClick(View view) {
        if(view == buttonSignup) {
            //TODO
            registerUser();
        }

        if(view == textviewSignin) {
            //TODO
            Intent intent = new Intent(this, SignInActivity.class);
            intent.putExtra("deviceToken", regToken);
            startActivityForResult(intent, REQUEST_CODE_MENU);
        }
    }
}
