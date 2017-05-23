package com.listotechnologies.cleverweather;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.util.List;

public class LocationHelper {
    private Handler mHandler;
    private LocationManager mLocMgr;
    private LocationResultListener mLocationResultListener;
    private boolean mNetworkOnly;
    private int mUpdateTimeout = 20;
    private int mLocationExpiry = 0;
    private Context mContext;
    private boolean mOngoing = false;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public LocationHelper(Context context, boolean networkOnly) {
        mContext = context;
        mNetworkOnly = networkOnly;
    }

    public boolean getNetworkOnly() {
        return mNetworkOnly;
    }

    public void setNetworkOnly(boolean networkOnly) {
        mNetworkOnly = networkOnly;
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            if (!mOngoing)
                cancelTimerAndRemoveListeners();
            mLocationResultListener.onGotLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private LocationListener mLocationListenerGps = new MyLocationListener();
    private LocationListener mLocationListenerNetwork = new MyLocationListener();
    private LocationListener mLocationListenerPassive = new MyLocationListener();

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
            boolean expired = (diff > (locationExpiryMinutes * 60 * 1000));
            /*
            if (expired) {
                diff = SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos();
                expired = diff > (locationExpiryMinutes * 60 * 1e9);
            }*/
            return expired;
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
        //if (!isGpsEnabled() && !isNetworkEnabled())
        //    return false;

        // use LocationResult callback class to pass location value from LocationHelper to user code.
        mLocationResultListener = result;
        mOngoing = false;
        Looper looper = Looper.myLooper();
        if (isGpsEnabled()) {
            getLocationManager().requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListenerGps, looper);
        }
        
        if (isNetworkEnabled()) {
            getLocationManager().requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListenerNetwork, looper);
        }
        getLocationManager().requestSingleUpdate(LocationManager.PASSIVE_PROVIDER, mLocationListenerPassive, looper);

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

    public boolean requestLocationUpdates(long minTime, float minDistance, LocationResultListener result) {
        /*
        //see if last location is still valid
        Location location = getLastLocation();
        if (location != null && !isLocationExpired(location)) {
            result.onGotLocation(location);
            return true;
        }*/

        // don't start listeners if no provider is enabled
        //if (!isGpsEnabled() && !isNetworkEnabled())
        //    return false;

        // use LocationResult callback class to pass location value from LocationHelper to user code.
        mLocationResultListener = result;
        mOngoing = true;
        Looper looper = Looper.myLooper();
        mHandler = null;

        if (isNetworkEnabled())
            getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, mLocationListenerNetwork, looper);
        if (isGpsEnabled())
            getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, mLocationListenerGps, looper);

        getLocationManager().requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDistance, mLocationListenerPassive, looper);
        return true;
    }

    public void cancelLocationUpdates() {
        if (isNetworkEnabled())
            getLocationManager().removeUpdates(mLocationListenerNetwork);
        if (isGpsEnabled())
            getLocationManager().removeUpdates(mLocationListenerGps);

        getLocationManager().removeUpdates(mLocationListenerPassive);
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
            getLocationManager().removeUpdates(mLocationListenerGps);
        }
        if (isNetworkEnabled()) {
            getLocationManager().removeUpdates(mLocationListenerNetwork);
        }
        getLocationManager().removeUpdates(mLocationListenerPassive);
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

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public Location getLastLocation() {
        Location bestLoc = null;
        List<String> providers = getLocationManager().getProviders(true);
        for (String provider: providers) {
            Location loc = getLocationManager().getLastKnownLocation(provider);
            if (loc != null && isBetterLocation(loc, bestLoc)) {
                bestLoc = loc;
            }
        }
        return bestLoc;
    }

    public int getLocationExpiry() {
        return mLocationExpiry;
    }

    public void setLocationExpiry(int expiryInMinutes) {
        mLocationExpiry = expiryInMinutes;
    }
}
