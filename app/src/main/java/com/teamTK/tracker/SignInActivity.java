package com.teamTK.tracker;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    //다른 액티비티를 띄우기 위한 요청코드(상수)
    public static final int REQUEST_CODE_MENU = 102;
    public static final String KEY_INTENT_DATA = "deviceToken";
    private static final String TAG = "TkMS_SignIn";
    //define view objects
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonSignin;
    TextView textviewSignin;
    TextView textviewMessage;
    TextView textviewFindPassword;
    ProgressDialog progressDialog;
    // 파이어베이스 오브젝트
    FirebaseAuth firebaseAuth;
    // FCM을 위한 단말의 등록 토큰
    String regToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // 전달 받은 인텐트 확인
        Intent intent = getIntent();
        processIntent(intent);

        // FirebaseAuth 오브젝트 초기화
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            //이미 로그인 되었다면 이 액티비티를 종료함
            finish();
            //그리고 Home 액티비티를 연다.
            startActivity(new Intent(getApplicationContext(), HomeActivity.class)); //추가해 줄 ProfileActivity
        }
        // 뷰 초기화
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textviewSignin= (TextView) findViewById(R.id.textViewSignin);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);
        textviewFindPassword = (TextView) findViewById(R.id.textViewFindpassword);
        buttonSignin = (Button) findViewById(R.id.buttonSignup);

        progressDialog = new ProgressDialog(this);

        // 버튼 클릭 이벤트
        buttonSignin.setOnClickListener(this);
        textviewSignin.setOnClickListener(this);
        textviewFindPassword.setOnClickListener(this);
    }

    private void processIntent (Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            regToken = (String) bundle.getString(KEY_INTENT_DATA);
            Log.d(TAG, "넘겨받은 데이터 : " + regToken);
        }
    }

    // 파이어베이스 유저 로그인 함수
    private void userLogin(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "email을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "password를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("로그인중입니다. 잠시 기다려 주세요...");
        progressDialog.show();

        // 유저 로그인
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()) {
                            // 이메일 인증 확인
                            if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                                final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("token").setValue(regToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getApplicationContext(), "환영합니다!", Toast.LENGTH_LONG).show();
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "이메일 인증이 되지 않았습니다.\n이메일을 확인해 주세요!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "계정이나 패스워드가 맞지 않습니다. 다시 확인해 주세요!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }



    @Override
    public void onClick(View view) {
        if(view == buttonSignin) {
            userLogin();
        }
        if(view == textviewSignin) {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.putExtra("deviceToken", regToken);
            startActivityForResult(intent, REQUEST_CODE_MENU);
            finish();
        }
        if(view == textviewFindPassword) {
            finish();
            startActivity(new Intent(this, ResetPWActivity.class));
        }
    }
}
