package com.listotechnologies.cleverweather;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class LocationHelper {
    private Handler mHandler;
    private LocationManager mLocMgr;
    private LocationResultListener mLocationResultListener;
    private boolean mNetworkOnly;
    private int mUpdateTimeout = 20;
    private int mLocationExpiry = 0;
    private Context mContext;

    public LocationHelper(Context context, boolean networkOnly) {
        mContext = context;
        mNetworkOnly = networkOnly;
    }

    private LocationListener mLocationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            cancelTimerAndRemoveListeners();
            mLocationResultListener.onGotLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private LocationListener mLocationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            cancelTimerAndRemoveListeners();
            mLocationResultListener.onGotLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private LocationManager getLocationManager() {
        if (mLocMgr == null)
            mLocMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        return mLocMgr;
    }

    public boolean isNetworkEnabled() {
        try {
            return getLocationManager().isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException ex) {
        }
        return false;
    }

    public boolean isGpsEnabled() {
        if (!mNetworkOnly) {
            try {
                return getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (SecurityException ex) {
            }
        }
        return false;
    }

    public static boolean isLocationExpired(Location location, int locationExpiryMinutes) {
        if (location != null && locationExpiryMinutes > 0) {
            long diff = System.currentTimeMillis() - location.getTime();
            return (diff > (locationExpiryMinutes * 60 * 1000));
        }
        return false;
    }

    public boolean isLocationExpired(Location location) {
        return isLocationExpired(location, mLocationExpiry);
    }

    public boolean getLocation(LocationResultListener result) {
        //see if last location is still valid
        Location location = getLastLocation();
        if (location != null && !isLocationExpired(location)) {
            result.onGotLocation(location);
            return true;
        }

        // don't start listeners if no provider is enabled
        if (!isGpsEnabled() && !isNetworkEnabled())
            return false;

        // use LocationResult callback class to pass location value from LocationHelper to user code.
        mLocationResultListener = result;

        Looper looper = Looper.myLooper();
        if (isGpsEnabled()) {
            mLocMgr.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListenerGps, looper);
        }
        
        if (isNetworkEnabled()) {
            mLocMgr.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListenerNetwork, looper);
        }

        mHandler = new Handler(looper);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeListeners();
                mLocationResultListener.onGotLocation(getLastLocation());
            }
        }, mUpdateTimeout * 1000);
        return true;
    }

    public void cancelTimerAndRemoveListeners() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        removeListeners();
    }

    private void removeListeners() {
        if (isGpsEnabled()) {
            mLocMgr.removeUpdates(mLocationListenerGps);
        }
        if (isNetworkEnabled()) {
            mLocMgr.removeUpdates(mLocationListenerNetwork);
        }
    }
    
    public interface LocationResultListener {
        public void onGotLocation(Location location);
    }
    
    public int getUpdateTimeout() {
        return mUpdateTimeout;
    }
    
    public void setUpdateTimeout(int timeoutInSeconds) {
        mUpdateTimeout = timeoutInSeconds;
    }

    public Location getLastLocation() {
        Location netLoc = null;
        Location gpsLoc = null;

        if (isGpsEnabled()) {
            gpsLoc = mLocMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        
        if (isNetworkEnabled()) {
            netLoc = mLocMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        // if we have both values use the latest one
        if (gpsLoc != null && netLoc != null) {
            if (gpsLoc.getTime() > netLoc.getTime())
                return gpsLoc;
            else
                return netLoc;
        } else if (gpsLoc != null) {
            return gpsLoc;
        } else {
            return netLoc;
        }
    }

    /* variation on above from stack exchange
    private Location getLastKnownLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        //Loop over the array backwards, and if you get an accurate location, then break
        for (int ii = providers.size()-1; ii >= 0; ii--) {
            Location location = lm.getLastKnownLocation(providers.get(ii));
            if (location != null)
                return location;
        }
        return null;
    } */

    public int getLocationExpiry() {
        return mLocationExpiry;
    }

    public void setLocationExpiry(int expiryInMinutes) {
        mLocationExpiry = expiryInMinutes;
    }
}
