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
import android.widget.SearchView;

import com.example.android.common.view.SlidingTabLayout;

import java.util.Locale;

public class TabbedActivity extends Activity {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private static LocationGetter sLocationGetter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return CitiesFragment.newLocationInstance();
                case 1:
                    return ForecastsFragment.newClosestInstance();
                case 2:
                    return CitiesFragment.newFavoritesInstance();
                case 3:
                    return ProvincesFragment.newInstance();
            }
            return null;
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
    }
}
