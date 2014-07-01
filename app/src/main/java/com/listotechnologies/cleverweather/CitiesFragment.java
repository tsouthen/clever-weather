package com.listotechnologies.cleverweather;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import com.google.android.gms.location.LocationClient;

public class CitiesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter m_adapter;
    private boolean m_onlyFavorites;
    private String m_province;
    private boolean m_byLocation;

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

    public static CitiesFragment newFavoritesInstance() {
        CitiesFragment frag = new CitiesFragment();
        frag.m_onlyFavorites = true;
        return frag;
    }

    public static CitiesFragment newProvinceInstance(String province) {
        CitiesFragment frag = new CitiesFragment();
        frag.m_province = province;
        return frag;
    }

    public static CitiesFragment newLocationInstance() {
        CitiesFragment frag = new CitiesFragment();
        frag.m_byLocation = true;
        return frag;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String where = null;
        String orderBy = CleverWeatherProvider.CITY_NAMEEN_COLUMN + " COLLATE UNICODE";
        String[] projection = { CleverWeatherProvider.ROW_ID, CleverWeatherProvider.CITY_NAMEEN_COLUMN };

        if (m_onlyFavorites) {
            where = CleverWeatherProvider.CITY_ISFAVORITE_COLUMN + "=1";
        } else if (m_province != null) {
            where = CleverWeatherProvider.CITY_PROVINCE_COLUMN + "='" + m_province + "'";
        } else if (m_byLocation) {
            String colName = "dist";
            projection = new String[] { CleverWeatherProvider.ROW_ID, CleverWeatherProvider.CITY_NAMEEN_COLUMN, getDistanceSelection(colName) };
            orderBy = colName + " limit 10";
        }

        return new CursorLoader(getActivity(), CleverWeatherProvider.CITY_URI, projection, where, null, orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        m_adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        m_adapter.swapCursor(null);
    }

    String getDistanceSelection(String colName) {
        String selection = null;
        double lat = 49.68;
        double lon = -124.93;
        Location location = getCurrentLocation();
        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }
        return String.format("(((%.3f-longitude)*(%.3f-longitude))+((%.3f-latitude)*(%.3f-latitude))) as %s", lon, lon, lat, lat, colName);
    }

    Location getCurrentLocation() {
        TabbedActivity activity = (TabbedActivity) getActivity();
        return activity.getCurrentLocation();
    }
}
