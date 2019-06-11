package com.teamTK.tracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teamTK.tracker.common.Util9;
import com.teamTK.tracker.model.UserModel;
import com.teamTK.tracker.service.BroadcastD;
import com.teamTK.tracker.service.FCMPush;

import java.util.Calendar;

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

    // FirebaseStorage 사용
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

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

        Log.d(TAG, "이건 뭐지? " + user.getProviderData());
        Log.d(TAG, "이건 뭐지? " + user.getProviderId());

        //logout button event
        buttonLogout.setOnClickListener(this);
        enterBtn.setOnClickListener(this);
        textivewDelete.setOnClickListener(this);

        // PM 11:00에 푸시알림 발송
        new PushHATT(getApplicationContext()).Push();
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
                Log.d(TAG, "유저포토 : " + userModel.getUserphoto());
                if (userModel.getUserphoto()!= null && !"".equals(userModel.getUserphoto())) {
                    Log.d(TAG, "여기까진 오니???");
                    StorageReference storageRef = storage.getReference();
                    StorageReference spaceRef = storageRef.child("userPhoto/"+userModel.getUserphoto());
                    Glide.with(HomeActivity.this)
                            .using(new FirebaseImageLoader())
                            .load(spaceRef)
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
                        Toast.makeText(HomeActivity.this, "저장되었습니다!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                StorageReference spaceRef = storageRef.child("userPhoto/" + uid);
                spaceRef.putFile(userPhotoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Log.d(TAG, "여기까지 오지도 않니????????");
                    }
                });
                userModel.setUserphoto(uid);
                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "여긴 오지도 않니????????");
                        Toast.makeText(HomeActivity.this, "저장되었습니다!", Toast.LENGTH_SHORT).show();
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
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            });
        }
        if (view == enterBtn)
        {
            startActivity(new Intent(this, List.class));
        }
        //회원탈퇴를 클릭하면 회원정보를 삭제한다. 삭제전에 컨펌창을 하나 띄워야 겠다.
        if(view == textivewDelete) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(HomeActivity.this);
            alert_confirm.setMessage("정말 계정을 삭제 할까요?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG, "사용자 계정 삭제");
                                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Log.d(TAG, "사용자 데이터 삭제");
                                                    if(userModel.getUserphoto() != null) {
                                                        StorageReference storageRef = storage.getReference();
                                                        StorageReference spaceRef = storageRef.child("userPhoto/"+userModel.getUserphoto());
                                                        spaceRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "사용자 유저포토 삭제");
                                                                firebaseAuth.signOut();
                                                                Toast.makeText(HomeActivity.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                                                                finish();
                                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(HomeActivity.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                                                        firebaseAuth.signOut();
                                                        finish();
                                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                    }
                                                }
                                            });
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

    // 푸시 알림 예약 클래스
    public class PushHATT {
        private Context context;
        public PushHATT(Context context) {
            this.context = context;
        }
        public void Push() {
            Log.d(TAG, "푸시알림 등록");
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(HomeActivity.this, BroadcastD.class);
            PendingIntent sender = PendingIntent.getBroadcast(HomeActivity.this, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();

            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 23, 00,0);

            // 만약 알림 시간이 현재시간보다 이전이면, 다음날로 설정
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            }
        }
    }
}
