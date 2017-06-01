package com.example.master.proto1;


import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.firebase.database.FirebaseDatabase;

public class MyApp extends Application {
    static SharedPreferences pref;
    // Editor for Shared preferences
    static SharedPreferences.Editor editor;

    private static final String PREF_NAME = "iovtrackerPref";
    // Vnumber Key
    public static final String KEY_vnum = "vnum";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        pref = this.getSharedPreferences(PREF_NAME,0);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public static void setVehicleNum(String vehicleNum) {
        editor = pref.edit();
        editor.putString(KEY_vnum,vehicleNum);
        editor.commit();
    }

    public static String getVehicleNum() {
        return pref.getString(KEY_vnum,null);
    }
}
