package com.teamTK.tracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.teamTK.tracker.common.FCMPush;
import com.teamTK.tracker.common.Util9;
import com.teamTK.tracker.model.UserModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_MENU = 104;
    private static final String TAG = "TkMS_HOME";
    private static final int PICK_FROM_ALBUM = 1;
    private ImageView user_photo;
    private EditText user_id;
    private EditText user_name;
    private Button saveBtn;
    private Button changePWBtn;
    private Button enterBtn;
    private Button pushdevBtn;

    private UserModel userModel;
    private Uri userPhotoUri;

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //view objects
    private Button buttonLogout;
    private TextView textivewDelete;

    // FCM을 위한 단말의 등록 토큰
    String regToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //initializing views
        //textViewUserEmail = (TextView) findViewById(R.id.textviewUserEmail);
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        enterBtn = (Button) findViewById(R.id.enter_button);
        textivewDelete = (TextView) findViewById(R.id.textviewDelete);
        user_id = (EditText) findViewById(R.id.user_id);
        user_id.setEnabled(false);
        user_name = (EditText) findViewById(R.id.user_name);
        user_photo = (ImageView) findViewById(R.id.user_photo);
        user_photo.setOnClickListener(userPhotoIVClickListener);

        saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(saveBtnClickListener);
        changePWBtn = (Button) findViewById(R.id.changePWBtn);
        changePWBtn.setOnClickListener(changePWBtnClickListener);
        pushdevBtn = (Button) findViewById(R.id.push_dev);
        pushdevBtn.setOnClickListener(toastdevBtnClickListener);


        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();
        //유저가 로그인 하지 않은 상태라면 null 상태이고 이 액티비티를 종료하고 로그인 액티비티를 연다.
        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, SignInActivity.class));
        }

        //유저가 있다면, null이 아니면 계속 진행
        FirebaseUser user = firebaseAuth.getCurrentUser();
        getUserInfoFromServer();

        //logout button event
        buttonLogout.setOnClickListener(this);
        enterBtn.setOnClickListener(this);
        textivewDelete.setOnClickListener(this);
    }

    void getUserInfoFromServer(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "사용자 uid : " + uid);
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModel = dataSnapshot.getValue(UserModel.class);
                user_id.setText(userModel.getUserid());
                user_name.setText(userModel.getUsernm());
                regToken = userModel.getToken();
                Log.d(TAG, "사용자 user_id : " + userModel.getUserid());
                Log.d(TAG, "사용자 user_name : " + userModel.getUsernm());
                if (userModel.getUserphoto()!= null && !"".equals(userModel.getUserphoto())) {
                    Glide.with(HomeActivity.this)
                            .load(FirebaseStorage.getInstance().getReference("userPhoto/"+userModel.getUserphoto()))
                            .into(user_photo);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    Button.OnClickListener userPhotoIVClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_ALBUM);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==PICK_FROM_ALBUM && resultCode== this.RESULT_OK) {
            user_photo.setImageURI(data.getData());
            userPhotoUri = data.getData();
        }
    }

    Button.OnClickListener saveBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            if (!validateForm()) return;
            userModel.setUsernm(user_name.getText().toString());

            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (userPhotoUri==null) {
                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Util9.showMessage(HomeActivity.this, "Success to Save.");
                    }
                });
            } else {
                FirebaseStorage.getInstance().getReference().child("userPhoto").child(uid).putFile(userPhotoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        userModel.setUserphoto( uid );
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Util9.showMessage(HomeActivity.this, "Success to Save.");
                            }
                        });
                    }
                });
            }
        }
    };

    Button.OnClickListener changePWBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            //getFragmentManager().beginTransaction().replace(R.id.container, new UserPWFragment()).commit();
            startActivity(new Intent(HomeActivity.this, ChangePWActivity.class));
        }
    };

    Button.OnClickListener toastdevBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FCMPush fcmPush = new FCMPush(regToken);
            fcmPush.sendMessage();
        }
    };

    private boolean validateForm() {
        boolean valid = true;

        String userName = user_name.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            user_name.setError("Required.");
            valid = false;
        } else {
            user_name.setError(null);
        }

        Util9.hideKeyboard(this);

        return valid;
    }

    @Override
    public void onClick(View view) {
        if (view == buttonLogout) {
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("token").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    firebaseAuth.signOut();
                    finish();
                    Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                    intent.putExtra("deviceToken", regToken);
                    startActivityForResult(intent, REQUEST_CODE_MENU);
                }
            });
        }
        if (view == enterBtn)
        {
            finish();
            startActivity(new Intent(this, List.class));
        }
        //회원탈퇴를 클릭하면 회원정보를 삭제한다. 삭제전에 컨펌창을 하나 띄워야 겠다.
        if(view == textivewDelete) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(HomeActivity.this);
            alert_confirm.setMessage("정말 계정을 삭제 할까요?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(HomeActivity.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                                            finish();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        }
                                    });
                        }
                    }
            );
            alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(HomeActivity.this, "취소", Toast.LENGTH_LONG).show();
                }
            });
            alert_confirm.show();
        }
    }
}
