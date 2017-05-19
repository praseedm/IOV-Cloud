package com.example.master.proto1;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {
    private String TAG = "SplashActivity";
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ChooseNext();
            }
        },SPLASH_TIME_OUT);
    }
    private void ChooseNext() {
        //mAuth = FirebaseAuth.getInstance();
       /* mFbUser = mAuth.getCurrentUser();
        if(mFbUser != null){
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            if(refreshedToken != null){
                mRootRef.child(Constants.tokkenRef).child(mFbUser.getUid()).child(Constants.fcmtokken).setValue(refreshedToken); }
            Intent main = new Intent(SplashActivity.this,MainActivity.class);
            main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(main);
        }
        else {
            Intent login = new Intent(SplashActivity.this,LoginActivity.class);
            startActivity(login);
        }*/
        Intent main = new Intent(SplashActivity.this,MainActivity.class);
        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(main);
        finish();
    }
}
