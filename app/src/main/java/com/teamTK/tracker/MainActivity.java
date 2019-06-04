package com.teamTK.tracker;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teamTK.tracker.common.ObjectUtils;
import com.teamTK.tracker.common.Util9;
import com.teamTK.tracker.model.UserModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    //다른 액티비티를 띄우기 위한 요청코드(상수)
    public static final int REQUEST_CODE_MENU = 101;
    private static final String TAG = "TkMS_MAIN";
    // 구글 로그인 구현을 위한 상수
    private static final int RC_SIGN_IN = 1000;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;
    Button buttonSignin;
    Button buttonGoogleCustom;
    LoginButton buttonFacebookSignin;
    Button buttonFacebookCustom;
    TextView buttonSignup;
    //firebase auth object
    private FirebaseAuth firebaseAuth;

    // FCM을 위한 단말의 등록 토큰
    String regToken;
    // 페이스북 로그인용 이메일
    String facebookEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        // 안드로이드 키해시를 구하기 위한 구문
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.teamTK.tracker", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(TAG, "KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        FacebookSdk.sdkInitialize(this);

        buttonSignin = (Button)findViewById(R.id.sign_in);
        buttonGoogleCustom = (Button)findViewById(R.id.btn_google_custom);
        buttonFacebookSignin = (LoginButton) findViewById(R.id.btn_facebook_login);
        buttonFacebookCustom = (Button) findViewById(R.id.btn_facebook_custom);
        buttonSignup = (TextView) findViewById(R.id.sign_up);

        buttonSignin.setOnClickListener(this);
        buttonGoogleCustom.setOnClickListener(this);
        buttonSignup.setOnClickListener(this);
        buttonFacebookCustom.setOnClickListener(this);

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null) {
            //이미 로그인 되었다면 이 액티비티를 종료함
            finish();
            //그리고 Home 액티비티를 연다.
            startActivity(new Intent(getApplicationContext(), HomeActivity.class)); //추가해 줄 ProfileActivity
        }

        //로그인 시도할 액티비티에서 유저데이터 요청
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // 페이스북 로그인
        mCallbackManager = CallbackManager.Factory.create();
        buttonFacebookSignin.setReadPermissions("email", "public_profile");
        buttonFacebookSignin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, "facebook:onSuccess");
                getUserEmail(loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG , "facebook:onError", error);
                // ...
            }
        });

        mAuth = FirebaseAuth.getInstance();

        getRegistrationId();
    }

    public void getRegistrationId() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // FCM 토큰값 획득
                        regToken = task.getResult().getToken();
                        Log.d(TAG, "FCM 토큰 값 : " + regToken);
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if(view == buttonSignin) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.putExtra("deviceToken", regToken);
            startActivityForResult(intent, REQUEST_CODE_MENU);
        }
        if(view == buttonSignup) {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.putExtra("deviceToken", regToken);
            startActivityForResult(intent, REQUEST_CODE_MENU);
        }
        if(view == buttonGoogleCustom) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        if(view == buttonFacebookCustom) {
            buttonFacebookSignin.performClick();
        }
    }

    // 구글 로그인 액티비티
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "결과 : " + result.isSuccess());
            Log.d(TAG, "시발 왜 안돼결과 : " + result.getStatus().toString());
            if (result.isSuccess()) {
                // 구글 로그인 성공. 파이어베이스 인증 등록
                GoogleSignInAccount accout = result.getSignInAccount();
                firebaseAuthWithGoogle(accout);
            }
            else {
                // 구글 로그인 실패
                Toast.makeText(MainActivity.this, "구글 로그인에 실패하였습니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();;
            }
        }
    }

    // 구글로그인 시 파이어베이스 인증 등록 및 계정 DB 저장
    private void firebaseAuthWithGoogle (GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "구글 로그인 인증에 실패하였습니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();;
                        } else {
                            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.d(TAG, "이 값은 ? : " + ObjectUtils.isEmpty(dataSnapshot.getValue(UserModel.class)));
                                    if (ObjectUtils.isEmpty(dataSnapshot.getValue(UserModel.class))) {
                                        UserModel userModel = new UserModel();
                                        userModel.setUid(uid);
                                        userModel.setUserid(acct.getEmail());
                                        userModel.setUsernm(acct.getDisplayName());
                                        userModel.setToken(regToken);
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(getApplicationContext(), "환영합니다!", Toast.LENGTH_LONG).show();
                                                finish();
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            }
                                        });
                                    } else {
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("token").setValue(regToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "환영합니다!", Toast.LENGTH_LONG).show();
                                                finish();
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
    }

    // 페이스북 로그인 이벤트
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.d(TAG, "이 값은 ? : " + ObjectUtils.isEmpty(dataSnapshot.getValue(UserModel.class)));
                                    if (ObjectUtils.isEmpty(dataSnapshot.getValue(UserModel.class))) {
                                        UserModel userModel = new UserModel();
                                        userModel.setUid(uid);
                                        userModel.setUserid(facebookEmail);
                                        userModel.setUsernm(Profile.getCurrentProfile().getName());
                                        userModel.setToken(regToken);
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(getApplicationContext(), "환영합니다!", Toast.LENGTH_LONG).show();
                                                finish();
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            }
                                        });
                                    } else {
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("token").setValue(regToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "환영합니다!", Toast.LENGTH_LONG).show();
                                                finish();
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            // 로그인 실패
                            Toast.makeText(MainActivity.this, "페이스북 로그인에 실패하였습니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 페이스북 로그인 이메일
    public void getUserEmail(LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            facebookEmail = response.getJSONObject().getString("email");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}