package com.example.master.proto1;


import android.app.Application;
import android.content.SharedPreferences;

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
