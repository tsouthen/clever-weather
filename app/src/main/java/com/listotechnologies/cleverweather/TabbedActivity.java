package com.listotechnologies.cleverweather;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

public class TabbedActivity extends BaseToolbarActivity implements ProvincesFragment.OnProvinceClickListener, CitiesFragment.OnCityClickListener, GoogleApiClient.ConnectionCallbacks, LocationListener {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private GoogleApiClient mGoogleApiClient = null;
    private City mClosestCity = null;

    @Override
    protected int getContentId() {
        return R.layout.activity_tabbed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the adapter that will return a fragment for each of the primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //do nothing
            }

            @Override
            public void onPageSelected(int position) {
                int titleId = -1;
                switch (position) {
                    case 0:
                        titleId = R.string.title_nearby;
                        break;
                    case 1:
                        titleId = R.string.title_location;
                        if (mClosestCity != null) {
                            getSupportActionBar().setTitle(mClosestCity.NameEn);
                            return;
                        }
                        break;
                    case 2:
                        titleId = R.string.title_favorites;
                        break;
                    case 3:
                        titleId = R.string.title_browse;
                        break;
                }
                if (titleId >= 0) {
                    getSupportActionBar().setTitle(getString(titleId));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //do nothing
            }
        };
        mViewPager.addOnPageChangeListener(onPageChangeListener );
        mViewPager.setCurrentItem(1);
        onPageChangeListener.onPageSelected(1);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);

        tabs.getTabAt(0).setIcon(R.drawable.ic_map_markers);
        tabs.getTabAt(1).setIcon(R.drawable.ic_map_marker);
        tabs.getTabAt(2).setIcon(R.drawable.ic_star_filled);
        tabs.getTabAt(3).setIcon(R.drawable.ic_canada);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Location location = LocationUtility.getLastLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE));
            if (location != null) {
                mSectionsPagerAdapter.setLocation(location);
                mClosestCity = CleverWeatherProviderExtended.getClosestCity(this.getContentResolver(), location);
                if (mClosestCity != null)
                    getSupportActionBar().setTitle(mClosestCity.NameEn);
            }
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected())
            startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //restart the activity
            finish();
            startActivity(getIntent());
        }
    }

    public static void setDrawableWhite(Context context, Drawable drawable) {
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(context.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void setIconsWhite(Context context, Menu menu) {
        for (int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            setDrawableWhite(context, drawable);
        }
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabbed, menu);
        setIconsWhite(this, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
            searchView.setImeOptions(IME_ACTION_SEARCH);
            //searchView.setFocusable(true);
            //searchView.setIconified(true);
            //searchView.requestFocusFromTouch();
            searchView.clearFocus();

            //searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            //    @Override
            //    public void onFocusChange(View view, boolean hasFocus) {
            //        if (hasFocus) {
            //            showInputMethod(view.findFocus());
            //        }
            //    }
            //});
        }
        return true;
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    @Override
    public void onProvinceClick(ProvincesFragment.Province province) {
        ProvinceActivity.start(TabbedActivity.this, province);
        //TODO: dynamically determine if we're in the two-pane layout or not
        //CitiesFragment fragment = CitiesFragment.newProvinceInstance(province.Abbreviation);
        //getFragmentManager().beginTransaction()
        //        .replace(R.id.right_panel, fragment)
        //        .commit();
    }

    @Override
    public void onCityClick(String cityCode, String cityName, boolean isFavorite) {
        ForecastsActivity.start(this, cityCode, cityName, isFavorite);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null)
                mSectionsPagerAdapter.setLocation(lastLocation);
        }
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1000 * 60 * 15); //15 mins
            locationRequest.setFastestInterval(1000 * 60 * 5); //5 mins
            locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //do nothing
    }

    @Override
    public void onLocationChanged(Location location) {
        Location currLocation = mSectionsPagerAdapter.getLocation();
        City currCity = null;
        if (currLocation != null)
            currCity = CleverWeatherProviderExtended.getClosestCity(this.getContentResolver(), currLocation);
        City newCity = CleverWeatherProviderExtended.getClosestCity(this.getContentResolver(), location);
        if (newCity != null && (currCity == null || !newCity.Code.equals(currCity.Code))) {
            mSectionsPagerAdapter.setLocation(location);
            mClosestCity = newCity;
            if (mViewPager.getCurrentItem() == 1)
                getSupportActionBar().setTitle(mClosestCity.NameEn);
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] mFragments;
        private Location mLocation;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new Fragment[getCount()];
        }

        Location getLocation() {
            return mLocation;
        }

        void setLocation(Location location) {
            mLocation = location;
            if (mFragments[0] != null) {
                CitiesFragment citiesFragment = (CitiesFragment) mFragments[0];
                citiesFragment.setLocation(location);
            }
            if (mFragments[1] != null) {
                ForecastsFragment forecastsFragment = (ForecastsFragment) mFragments[1];
                forecastsFragment.setLocation(location);
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mFragments[position] = CitiesFragment.newLocationInstance(mLocation);
                    break;
                case 1:
                    mFragments[position] = ForecastsFragment.newClosestInstance(mLocation);
                    break;
                case 2:
                    mFragments[position] = CitiesFragment.newFavoritesInstance();
                    break;
                case 3:
                    mFragments[position] = ProvincesFragment.newInstance();
                    //mFragments[position] = new TwoPaneFragment();
                    break;
            }
            if (position > 3)
                return null;
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return 4;
        }

        //@Override
        //public CharSequence getPageTitle(int position) {
        //    Locale l = Locale.getDefault();
        //    switch (position) {
        //        case 0:
        //            return getString(R.string.title_nearby).toUpperCase(l);
        //        case 1:
        //            return getString(R.string.title_location).toUpperCase(l);
        //        case 2:
        //            return getString(R.string.title_favorites).toUpperCase(l);
        //        case 3:
        //            return getString(R.string.title_browse).toUpperCase(l);
        //    }
        //    return null;
        //}

        public Fragment getFragment(int position) {
            return mFragments[position];
        }
    }

    public static class LocationUtility {
        private static final int TWO_MINUTES = 1000 * 60 * 2;

        /** Determines whether one Location reading is better than the current Location fix
         * @param location  The new Location that you want to evaluate
         * @param currentBestLocation  The current Location fix, to which you want to compare the new one
         */
        public static boolean isBetterLocation(Location location, Location currentBestLocation) {
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
        private static boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            }
            return provider1.equals(provider2);
        }

        public static Location getLastLocation(LocationManager locationManager) throws SecurityException {
            Location bestLoc = null;
            List<String> providers = locationManager.getProviders(true);
            for (String provider: providers) {
                Location loc = locationManager.getLastKnownLocation(provider);
                if (loc != null && isBetterLocation(loc, bestLoc)) {
                    bestLoc = loc;
                }
            }
            return bestLoc;
        }
    }
}
