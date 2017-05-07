package com.listotechnologies.cleverweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.v4.content.ContextCompat;

public class LocationGetter {
    private Context mContext;
    private Location mLocation = null;
    private final Object mGotLocationLock = new Object();
    private LocationHelper mLocationHelper = null;
    private int mUpdateTimeout;
    private int mLocationExpiryMins;

    private final LocationHelper.LocationResultListener mLocationResult = new LocationHelper.LocationResultListener() {
        @Override
        public void onGotLocation(Location location) {
            synchronized (mGotLocationLock) {
                LocationGetter.this.mLocation = location;
                mGotLocationLock.notifyAll();
                Looper.myLooper().quit();
            }
        }
    };

    public LocationGetter(Context context, int updateTimeoutSecs, int locationExpiryMins) {
        if (context == null)
            throw new IllegalArgumentException("context == null");

        mContext = context;
        mUpdateTimeout = updateTimeoutSecs;
        mLocationExpiryMins = locationExpiryMins;
    }

    public void SetContext(Context context) {
        mContext = context;
    }

    public synchronized Location getLocation() {
        try {
            synchronized (mGotLocationLock) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        getLocationHelper().getLocation(mLocationResult);
                        Looper.loop();
                    }
                }.start();

                mGotLocationLock.wait((mUpdateTimeout + 1) * 1000);
            }
        } catch (InterruptedException e1) {
            //e1.printStackTrace();
        }
        return mLocation;
    }

    public LocationHelper getLocationHelper() {
        if (mLocationHelper == null) {
            mLocationHelper = new LocationHelper(mContext, false);
            mLocationHelper.setUpdateTimeout(mUpdateTimeout);
            mLocationHelper.setLocationExpiry(mLocationExpiryMins);
        }
        return mLocationHelper;
    }

    public boolean isLocationEnabled() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return false;

        return getLocationHelper().isGpsEnabled() || getLocationHelper().isNetworkEnabled();
    }
}
