package com.example.master.proto1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.Random;

import models.Constants;
import models.UserObj;
import services.ImageCompress;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    EditText vnumber;
    Button bLogin,bDone;
    ImageView vehicleImg;
    private FirebaseAuth mAuth;
    private FirebaseUser mFbUser;
    GoogleApiClient mGoogleApiClient;
    private String userName, userEmail, vNumber;
    private String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressBar progressBar;
    private ProgressDialog mProgressDialog;
    private static final int SELECT_PHOTO = 100;
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    private UploadTask uploadTask;
    private String downloadUrl = null;
    private Boolean status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        vnumber = (EditText) findViewById(R.id.editText);
        bLogin = (Button) findViewById(R.id.loginb);
        vehicleImg = (ImageView) findViewById(R.id.imageview);
        bDone = (Button) findViewById(R.id.bDone);

        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this /* OnConnectionFailedListener */)
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
                    // User is signed in
                    UserObj newUser = new UserObj(mFbUser.getDisplayName(), photoUri, mFbUser.getUid(), mFbUser.getEmail());
                    newUser.setvNumber(vNumber);
                    MyApp.setVehicleNum(vNumber);
                    mRootRef.child(Constants.vehicleRef).child(mFbUser.getUid()).setValue(newUser);
                    //startMainACtivity();
                    //finish();

                } else {
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
        if (mAuthListener != null) {
            Log.d(TAG, "onStop: removed");
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void login(View view) {
        vNumber = vnumber.getText().toString();
        if (vNumber.trim().length() > 4) {
            signin();
        } else Toast.makeText(this, "Vehicle number required", Toast.LENGTH_SHORT).show();
    }

    private void signin() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            progressBar.setVisibility(View.GONE);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        switch (requestCode) {
            case RC_SIGN_IN:
                progressBar.setVisibility(View.GONE);
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Bitmap bitmap;
                    try {
                        bitmap = ImageCompress.decodeSampledBitmapFromResource(this, selectedImage, 480, 480);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos);
                        byte[] dataupload = baos.toByteArray();
                        Random r = new Random();
                        String i = String.valueOf(r.nextInt(10));
                        StorageReference pic = storage.child(Constants.vehicleRef).child(i);
                        uploadTask = pic.putBytes(dataupload);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                downloadUrl = taskSnapshot.getDownloadUrl().toString();
                                status = true;
                                mRootRef.child(Constants.vehicleRef).child(mFbUser.getUid()).child(Constants.photoRef).setValue(downloadUrl);
                                Glide.with(LoginActivity.this)
                                        .load(downloadUrl)
                                        .centerCrop()
                                        .into(vehicleImg);
                                bDone.setVisibility(View.VISIBLE);
                            }
                        });

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {

            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d(TAG, "LoginA LoginSucess: " + acct);
            AuthWithGoogle(acct);
        } else {
            Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "LoginA LoginFailed: ");
        }
    }

    private void AuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "LoginA AuthWithGoogle: " + " ID:" + acct.getId());
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "LoginA Auth onComplete: ");
                if (!task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "LoginA Auth onComplete: Failed ");
                } else {

                    Log.d(TAG, "LoginA onComplete: ");

                }
                hideProgressDialog();
            }
        });
    }

    private void startMainACtivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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

    public void picUpload(View view) {
        Intent photoPickerIntent = new Intent();
        photoPickerIntent.setType("image/*");
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    public void registered(View view) {
        startMainACtivity();
        finish();
    }
}
