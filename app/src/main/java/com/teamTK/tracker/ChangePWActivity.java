package com.teamTK.tracker;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.teamTK.tracker.common.Util9;

public class ChangePWActivity extends AppCompatActivity {
    private EditText user_pw1;
    private EditText user_pw2;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_pwcg);

        user_pw1 = findViewById(R.id.user_pw1);
        user_pw2 = findViewById(R.id.user_pw2);
        saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(saveBtnClickListener);
    }


    Button.OnClickListener saveBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            String pw1 = user_pw1.getText().toString().trim();
            if (pw1.length()<8) {
                Toast.makeText(getApplicationContext(), "비밀번호는 최소 8자리 이상이여야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pw1.equals(user_pw2.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "패스워드가 일치하지 않습니다.\n다시 확인해 주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.updatePassword(pw1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Util9.showMessage(getApplicationContext(), "Password changed");

                    InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(user_pw2.getWindowToken(), 0);

                    onBackPressed();
                }
            });
        }
    };
}
