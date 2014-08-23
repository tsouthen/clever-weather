package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.ViewConfiguration;
import android.widget.SearchView;

import com.example.android.common.view.SlidingTabLayout;

import java.lang.reflect.Field;
import java.util.Locale;

public class TabbedActivity extends Activity {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private static LocationGetter sLocationGetter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ForceOverflowMenu.overrideHasPermanentMenuKey(this);
        setContentView(R.layout.activity_tabbed);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        //setup the tabs
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);
        tabs.setSelectedIndicatorColors(getResources().getColor(R.color.indicator_color));
    }

    public static LocationGetter getLocationGetter(Context context) {
        if (sLocationGetter == null)
            sLocationGetter = new LocationGetter(context, 10, 15);
        return sLocationGetter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabbed, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
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
