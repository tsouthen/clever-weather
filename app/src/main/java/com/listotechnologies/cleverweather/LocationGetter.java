package com.listotechnologies.cleverweather;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

public class LocationGetter {
    private final Context mContext;
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
        return getLocationHelper().isGpsEnabled() || getLocationHelper().isNetworkEnabled();
    }
}
