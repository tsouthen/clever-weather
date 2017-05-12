package com.listotechnologies.cleverweather;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.lang.reflect.Field;
import java.util.Locale;

public class TabbedActivity extends BaseToolbarActivity implements ProvincesFragment.OnProvinceClickListener, CitiesFragment.OnCityClickListener {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private static LocationGetter sLocationGetter = null;

    @Override
    protected int getContentId() {
        return R.layout.activity_tabbed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ForceOverflowMenu.overrideHasPermanentMenuKey(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        //setup the tabs
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        //tabs.setDefaultTextColor(getResources().getColorStateList(R.color.tab_text));
        tabs.setViewPager(mViewPager);
        tabs.setSelectedIndicatorColors(getResources().getColor(R.color.indicator_color));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
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

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabbed, menu);

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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] mFragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new Fragment[getCount()];
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mFragments[position] = CitiesFragment.newLocationInstance();
                    break;
                case 1:
                    mFragments[position] = ForecastsFragment.newClosestInstance();
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

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_nearby).toUpperCase(l);
                case 1:
                    return getString(R.string.title_location).toUpperCase(l);
                case 2:
                    return getString(R.string.title_favorites).toUpperCase(l);
                case 3:
                    return getString(R.string.title_browse).toUpperCase(l);
            }
            return null;
        }

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
