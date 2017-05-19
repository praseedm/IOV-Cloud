package com.example.master.proto1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import models.Constants;

public class LoginActivity extends AppCompatActivity  implements GoogleApiClient.OnConnectionFailedListener{
    EditText vnumber;
    Button bLogin;
    private FirebaseAuth mAuth ;
    private FirebaseUser mFbUser;
    GoogleApiClient mGoogleApiClient;
    private String userName , userEmail,vNumber;
    private String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressBar progressBar;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        vnumber = (EditText) findViewById(R.id.editText);
        bLogin = (Button) findViewById(R.id.loginb);

        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "onAuthStateChanged: ");

                mFbUser = mAuth.getCurrentUser();
                if (mFbUser != null) {
                    Log.d(TAG, "onAuthStateChanged: loggedIn");
                    String photoUri = Constants.PhotoUrl;
                    if(mFbUser.getPhotoUrl() != null){photoUri = mFbUser.getPhotoUrl().toString();}
                    // User is signed in
                    UserObj newUser = new UserObj(mFbUser.getDisplayName(), photoUri, mFbUser.getUid(), mFbUser.getEmail());
                    int randomPIN = (int)(Math.random()*9000)+1000;
                    newUser.setPin(""+randomPIN);
                    mRootRef.child(Constants.userRef).child(mFbUser.getUid()).setValue(newUser);
                    UserSingleton.getInstance().setVayaUser(newUser);
                    startMainACtivity();
                    finish();

                }
                else {
                    Log.d(TAG, "onAuthStateChanged: nologin");
                    // User is signed out
                }
            }
        };
        ///Text watcher
        vnumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 4) {
                    bLogin.setVisibility(View.VISIBLE);
                } else {
                    bLogin.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null){
            Log.d(TAG, "onStop: removed");
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void login(View view) {
    }

    private void startMainACtivity() {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            Log.d(TAG, "LoginA showProgressDialog: ");
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Signing In...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        Log.d(TAG, "LoginA hideProgressDialog: ");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
