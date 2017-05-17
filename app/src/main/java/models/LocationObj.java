package models;

import android.location.Location;

public class LocationObj {
    public String mUser;
    public String mLastTime;
    public double mLatitude , mLongitude;
    public float mAccuracy;

    public LocationObj() {
    }

    public LocationObj(String User , Location mLastLocation, String mLastTime) {
        this.mUser = User;
        this.mLastTime = mLastTime;
        this.mLatitude = mLastLocation.getLatitude();
        this.mLongitude = mLastLocation.getLongitude();
        this.mAccuracy = mLastLocation.getAccuracy();
    }

    public String getmUser() {
        return mUser;
    }

    public void setmUser(String mUser) {
        this.mUser = mUser;
    }

    public String getmLastTime() {
        return mLastTime;
    }

    public void setmLastTime(String mLastTime) {
        this.mLastTime = mLastTime;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public float getmAccuracy() {
        return mAccuracy;
    }

    public void setmAccuracy(float mAccuracy) {
        this.mAccuracy = mAccuracy;
    }
}

