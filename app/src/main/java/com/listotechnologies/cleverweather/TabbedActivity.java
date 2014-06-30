package com.listotechnologies.cleverweather;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TabbedActivity extends Activity {
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        try {
            copyDataBaseFromAssets();
        } catch (IOException ioe) {
            Log.e("TabbedActivity", "IOException", ioe);
        }
        */
        setContentView(R.layout.activity_tabbed);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        PagerTabStrip pts = (PagerTabStrip) mViewPager.findViewById(R.id.pager_tab_strip);
        pts.setTabIndicatorColor(0xff8800);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_search) {

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return FavoritesFragment.newInstance();

            if (position == 1)
                return ProvincesFragment.newInstance();

            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_favorites).toUpperCase(l);
                case 1:
                    return getString(R.string.title_browse).toUpperCase(l);
                case 2:
                    return getString(R.string.title_location).toUpperCase(l);
            }
            return null;
        }
    }

    public static class Province {
        public final String Name;
        public final String Abbreviation;
        private static ArrayList<Province> s_list;

        public Province (String name, String abbreviation) {
            Name = name;
            Abbreviation = abbreviation;
        }

        public static ArrayList<Province> GetProvinces() {
            if (s_list == null) {
                s_list = new ArrayList<Province>();
                s_list.add(new Province("Alberta", "AB"));
                s_list.add(new Province("British Columbia", "BC"));
                s_list.add(new Province("Manitoba", "MB"));
                s_list.add(new Province("New Brunswick", "NB"));
                s_list.add(new Province("Newfoundland & Labrador", "NL"));
                s_list.add(new Province("Northwest Territories", "NT"));
                s_list.add(new Province("Nova Scotia", "NS"));
                s_list.add(new Province("Nunavut", "NU"));
                s_list.add(new Province("Ontario", "ON"));
                s_list.add(new Province("Prince Edward Island", "PE"));
                s_list.add(new Province("Quebec", "QC"));
                s_list.add(new Province("Saskatchewan", "SK"));
                s_list.add(new Province("Yukon", "YT"));
            }
            return s_list;
        }

        @Override
        public String toString() {
            return Name;
        }
    }

    public static class ProvincesFragment extends ListFragment {
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            ArrayAdapter<Province> adapter = new ArrayAdapter<Province>(getActivity(), android.R.layout.simple_list_item_1, Province.GetProvinces());
            setListAdapter(adapter);
        }

        public static ProvincesFragment newInstance() {
            return new ProvincesFragment();
        }
    }

    public static class FavoritesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private SimpleCursorAdapter m_adapter;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            int resId = 0;
            String[] dataColumns = { CleverWeatherProvider.CITY_NAMEEN_COLUMN };
            int[] viewIds = { android.R.id.text1 };

            m_adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, dataColumns, viewIds, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            setListAdapter(m_adapter);
            getLoaderManager().initLoader(0, null, this);
        }

        public static FavoritesFragment newInstance() {
            return new FavoritesFragment();
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String selection = CleverWeatherProvider.CITY_ISFAVORITE_COLUMN + "=1";
            String[] projection = { CleverWeatherProvider.ROW_ID, CleverWeatherProvider.CITY_NAMEEN_COLUMN };
            return new CursorLoader(getActivity(), CleverWeatherProvider.CITY_URI, projection, selection, null, CleverWeatherProvider.CITY_NAMEEN_COLUMN);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            m_adapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            m_adapter.swapCursor(null);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
            TextView tv = (TextView) rootView.findViewById(R.id.section_label);
            tv.setText("Not yet implemented");
            return rootView;
        }
    }

    private void copyDataBaseFromAssets() throws IOException {
        // Path to the db
        String dbName = "CleverWeather.db";
        File dbPath = this.getDatabasePath(dbName);
        if (dbPath.exists())
            return;

        //Open your local db as the input stream
        InputStream myInput = this.getAssets().open(dbName);
        OutputStream myOutput = null;

        //create directory path if necessary
        File parentFile = dbPath.getParentFile();
        if (!parentFile.exists())
            parentFile.mkdirs();

        //Open the empty db as the output stream
        try {
            myOutput = new BufferedOutputStream(new FileOutputStream(dbPath));

            //transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
        } finally {
            myOutput.close();
            myInput.close();
        }
    }
}
