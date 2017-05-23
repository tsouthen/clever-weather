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
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewConfiguration;
import android.widget.SearchView;

import com.example.android.common.view.SlidingTabLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.lang.reflect.Field;
import java.util.Locale;

public class TabbedActivity extends BaseToolbarActivity implements ProvincesFragment.OnProvinceClickListener, CitiesFragment.OnCityClickListener, LocationHelper.LocationResultListener, GoogleApiClient.ConnectionCallbacks, LocationListener {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private static LocationGetter sLocationGetter = null;
    private long mMinTime = 1000 * 60 * 15; //15 mins in millisecs
    private float mMinDist = 20000; //20 kms in meters
    private boolean mStarted = false;
    private GoogleApiClient mGoogleApiClient = null;

    @Override
    protected int getContentId() {
        return R.layout.activity_tabbed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ForceOverflowMenu.overrideHasPermanentMenuKey(this);

        // Create the adapter that will return a fragment for each of the primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        //setup the tabs
        //SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        ////tabs.setDefaultTextColor(getResources().getColorStateList(R.color.tab_text));
        //tabs.setViewPager(mViewPager);
        //tabs.setSelectedIndicatorColors(getResources().getColor(R.color.indicator_color));

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
        mStarted = true;

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }

        /*
        LocationHelper locationHelper = getLocationGetter(this).getLocationHelper(false);
        Location location = locationHelper.getLastLocation();

        //mock location of Calgary to test location updates
        //location = new Location(LocationManager.NETWORK_PROVIDER);
        //location.setLatitude(51.0486);
        //location.setLongitude(-114.0708);

        //if we don't have a location, request it to return ASAP
        if (location != null) {
            onGotLocation(location);
        } else {
            locationHelper.cancelLocationUpdates();
            locationHelper.requestLocationUpdates(location != null ? mMinTime : 0, location != null ? mMinDist : 0, this);
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStarted = false;
        //getLocationGetter(this).getLocationHelper().cancelLocationUpdates();
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

    public static LocationGetter getLocationGetter(Context context) {
        if (sLocationGetter == null)
            sLocationGetter = new LocationGetter(context, 10, 15);
        else
            sLocationGetter.SetContext(context);

        return sLocationGetter;
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

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }
        return true;
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
    public void onGotLocation(Location location) {
        if (!mStarted)
            return;

        Location currLocation = mSectionsPagerAdapter.getLocation();
        LocationHelper locationHelper = getLocationGetter(this).getLocationHelper(false);
        if (locationHelper.isBetterLocation(location, currLocation)) {
            String currCityCode = null;
            if (currLocation != null)
                currCityCode = CleverWeatherProviderExtended.getClosestCity(this.getContentResolver(), currLocation);
            String newCityCode = CleverWeatherProviderExtended.getClosestCity(this.getContentResolver(), location);
            if (newCityCode != null && !newCityCode.equals(currCityCode)) {
                mSectionsPagerAdapter.setLocation(location);
            }
        }
        //now that we have received a location, make sure our location updates are less frequent
        if (mSectionsPagerAdapter.getLocation() != null) {
            locationHelper.cancelLocationUpdates();
            locationHelper.requestLocationUpdates(mMinTime, mMinDist, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null)
            mSectionsPagerAdapter.setLocation(lastLocation);

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
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
        String currCityCode = null;
        if (currLocation != null)
            currCityCode = CleverWeatherProviderExtended.getClosestCity(this.getContentResolver(), currLocation);
        String newCityCode = CleverWeatherProviderExtended.getClosestCity(this.getContentResolver(), location);
        if (newCityCode != null && !newCityCode.equals(currCityCode)) {
            mSectionsPagerAdapter.setLocation(location);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] mFragments;
        private Location mLocation;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new Fragment[getCount()];
        }

        public Location getLocation() {
            return mLocation;
        }

        public void setLocation(Location location) {
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

        public Fragment getFragmentAtPosition(int position) {
            return mFragments[position];
        }
    }

    public static class ForceOverflowMenu {
        private boolean m_done = false;
        private static ForceOverflowMenu s_instance = null;

        private void possiblyOverrideHasPermanentMenuKey(Context context) {
            if (m_done)
                return;

            m_done = true;

            //Code from: http://stackoverflow.com/a/11438245/453479
            try {
                ViewConfiguration config = ViewConfiguration.get(context);
                Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
                if (menuKeyField != null) {
                    menuKeyField.setAccessible(true);
                    if (menuKeyField.getBoolean(config))
                        menuKeyField.setBoolean(config, false);
                }
            } catch (Exception ex) {
                // Ignore
            }
        }

        public static void overrideHasPermanentMenuKey(Context context) {
            if (s_instance == null)
                s_instance = new ForceOverflowMenu();

            s_instance.possiblyOverrideHasPermanentMenuKey(context);
        }
    }
}
