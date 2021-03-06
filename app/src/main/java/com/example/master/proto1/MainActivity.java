package com.example.master.proto1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import models.Constants;
import models.LocationObj;
import models.UserObj;
import services.LocationDaemon;

public class MainActivity extends AppCompatActivity {
    //Tracker
    private LocationObj mLastlocationObj;
    private LocationDaemon mLocationDaemon;
    private boolean trackStatus = false;
    //Check Location Acess
    LocationManager lm;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    private Context context;

    private final String TAG = "mainactivity";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private String userName,vNumber;
    TextView msg, textId,textvnum;
    ImageView vehicleImg;
    Button trackB;
    private FirebaseAuth mAuth ;
    private FirebaseUser mFbUser;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference(),announRef,
            dataRef = database.child(Constants.dataRef), singleRef = database.child(Constants.singleRef);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msg = (TextView) findViewById(R.id.announ);
        textId = (TextView) findViewById(R.id.textName);
        textvnum = (TextView) findViewById(R.id.textVnum);
        vehicleImg = (ImageView) findViewById(R.id.vehicleImg);
        trackB = (Button) findViewById(R.id.trackb);
        announRef = database.child("Announcement");
        mAuth = FirebaseAuth.getInstance();
        mFbUser = mAuth.getCurrentUser();
        checkPlayServices();
        vNumber = MyApp.getVehicleNum();
        mLocationDaemon = new LocationDaemon(this,TAG) {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: ");
                UpdateUI();
            }
        };

        mLocationDaemon.connect();
    }

    private void loadVehicelimage() {
        database.child(Constants.vehicleRef).child(mFbUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserObj vehicle = dataSnapshot.getValue(UserObj.class);
                if(vehicle != null){
                    textId.setText(vehicle.getName());
                    textvnum.setText(vehicle.getvNumber());
                    Glide.with(MainActivity.this)
                            .load(vehicle.getPhotoUri())
                            .centerCrop()
                            .into(vehicleImg);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadVehicelimage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationAcess();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationDaemon.stopLocationUpdates();
        mLocationDaemon.disconnect();
    }

    private void UpdateUI() {
        Log.d(TAG, "UpdateUI: ");
        mLastlocationObj = mLocationDaemon.getLocation();
        Toast.makeText(this, "Updated!!!", Toast.LENGTH_SHORT).show();
        String dis = mLastlocationObj.getmLongitude() + "," + mLastlocationObj.getmLatitude();
        msg.setText(dis);
        mLastlocationObj.setmVehicleNum(vNumber);
        dataRef.child(mFbUser.getUid()).push().setValue(mLastlocationObj);
        singleRef.child(mFbUser.getUid()).setValue(mLastlocationObj);
    }

    //Tracker fn
    private void tracker() {
        if (!trackStatus) {
            trackStatus = true;
            Log.d(TAG, "track: Starting");
            mLocationDaemon.startLocationUpdates();
            //tracker_Button.setText("ON");

        } else {
            Log.d(TAG, "track: Stopping");
            trackStatus = false;
            mLocationDaemon.stopLocationUpdates();
            //tracker_Button.setText("OFF");
        }
    }

    //Function to checkLocationAcess Location Acess
    //Toast when GPS disabled , AlertDialog when NetworkProvider disabled .
    protected void checkLocationAcess(){
        Log.d(TAG, "checkLocationAcess: ");
        context = this;
        lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}


        if(!gps_enabled){
            Log.d(TAG, "checkLocationAcess: gps disabled ");
            Toast.makeText(context,"Enable GPS for best result",Toast.LENGTH_LONG).show();
        }
        if(gps_enabled){
            Log.d(TAG, "checkLocationAcess: gps Enabled ");
        }
        if(network_enabled)
        {Log.d(TAG, "checkLocationAcess: network Enabled ");}

        if(!network_enabled) {
            Log.d(TAG, "checkLocationAcess: network disabled ");
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Location Acess Disabled");
            dialog.setMessage("Enable Location Acess");
            dialog.setCancelable(false);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    dialog.dismiss();
                }
            });
            dialog.setNegativeButton("cancel",null);
            dialog.create().show();
            return;
        }
    }

    public void clicked(View view) {
        tracker();
        String displayMsg = "Stopped";
        if(trackStatus){
            displayMsg = "tracking";
        }
        trackB.setText(displayMsg);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }
        Log.d(TAG, "checkPlayServices: ");
        return true;
    }
}
