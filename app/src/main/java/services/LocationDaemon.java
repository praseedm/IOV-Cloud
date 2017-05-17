package services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

import models.LocationObj;

public abstract class LocationDaemon implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private String TAG;
    private Context context;
    private String mUser;
    //Location track
    private Location mLastLocation;
    private String mLastTime;
    private LocationObj mLocationObj;
    private LocationRequest mLocationRequest;
    private GoogleApiClient ApiClient;
    private boolean mTrackStatus = false;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 10 sec
    private static int FATEST_INTERVAL = 3000; // 5 sec
    private static int DISPLACEMENT = 1; // 10 meters
    //Date
    public SimpleDateFormat myDateFormat = new SimpleDateFormat("h:mm:ss a");

    //Constructor with params context,TAG string
    public LocationDaemon(Context context, String TAG) {
        Log.d(TAG, "LocationDaemon: ");
        this.context = context;
        this.TAG = TAG;
        buildGoogleApiClient();
        createLocationRequest();

    }

    //GoogleApi Client
    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient: ");
        ApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    //Location Request
    public void createLocationRequest() {
        Log.d(TAG, "createLocationRequest: ");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    //GoogleApi connect
    public void connect() {
        ApiClient.connect();
        //String re = ApiClient.toString();
        Log.d(TAG, "connect: ");
    }

    //GoogleApi disconnect
    public void disconnect() {
        ApiClient.disconnect();
    }

    //Start Request
    public void startLocationUpdates() {
        Log.d(TAG, "LocationDeamon startLocationUpdates: ");
        // Toast.makeText(context, "LocarionDaemon UpdateStartED", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mTrackStatus = true;
        if (ApiClient.isConnected()) {
            Log.d(TAG, "LocarionDaemon CoNNected");
            // Toast.makeText(context, "LocarionDaemon CoNNected", Toast.LENGTH_SHORT).show();
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    ApiClient, mLocationRequest, this);
        }
        else {
            Log.d(TAG, " LocarionDaemon DisconnectED ; "+ApiClient.isConnecting());
            //Toast.makeText(context, " LocarionDaemon DisconnectED", Toast.LENGTH_SHORT).show();
        }
    }

    //Stop Request
    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                ApiClient, this);
        mTrackStatus = false;
        //Toast.makeText(context, "UpdateStoppED", Toast.LENGTH_SHORT).show();
    }

    //Return Location
    public LocationObj getLocation() {
        Log.d(TAG, "getLocation: ");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(ApiClient);
        mLastTime = myDateFormat.format(new Date());
        mLocationObj = new LocationObj(mUser,mLastLocation,mLastTime);
        return mLocationObj;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
