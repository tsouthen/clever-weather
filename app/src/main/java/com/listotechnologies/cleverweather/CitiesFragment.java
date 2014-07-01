package com.listotechnologies.cleverweather;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

public class CitiesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter m_adapter;
    private boolean m_onlyFavorites;
    private String m_province;
    private boolean m_byLocation;
    private SwipeRefreshLayout mSwipeRefresh;

    public static final String ARG_PROVINCE = "ARG_PROVINCE";
    public static final String ARG_FAVORITES = "ARG_FAVORITES";
    public static final String ARG_LOCATION = "ARG_LOCATION";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(ARG_PROVINCE)) {
                m_province = bundle.getString(ARG_PROVINCE);
            } else if (bundle.containsKey(ARG_FAVORITES)) {
                m_onlyFavorites = bundle.getBoolean(ARG_FAVORITES);
            } else if (bundle.containsKey(ARG_LOCATION)) {
                m_byLocation = bundle.getBoolean(ARG_LOCATION);
            }
        }
        if (!m_onlyFavorites && !m_byLocation && m_province == null) {
            Log.d("CitiesFragment", "CitiesFragment created without any constraints!");
        }

        View view = inflater.inflate(R.layout.swipe_refresh, container, false);
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.container);
        mSwipeRefresh.setColorScheme(R.color.swipe_color_1, R.color.swipe_color_2, R.color.swipe_color_3, R.color.swipe_color_4);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(0, null, CitiesFragment.this);
            }
        });
        return view;
    }

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
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_FAVORITES, true);
        frag.setArguments(bundle);
        return frag;
    }

    public static CitiesFragment newProvinceInstance(String province) {
        CitiesFragment frag = new CitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PROVINCE, province);
        frag.setArguments(bundle);
        return frag;
    }

    public static CitiesFragment newLocationInstance() {
        CitiesFragment frag = new CitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_LOCATION, true);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mSwipeRefresh.setRefreshing(true);
        String where = null;
        String orderBy = CleverWeatherProvider.CITY_NAMEEN_COLUMN + " COLLATE UNICODE";
        String[] projection = { CleverWeatherProvider.ROW_ID, CleverWeatherProvider.CITY_NAMEEN_COLUMN };

        if (m_onlyFavorites) {
            where = CleverWeatherProvider.CITY_ISFAVORITE_COLUMN + "=1";
        } else if (m_province != null) {
            where = CleverWeatherProvider.CITY_PROVINCE_COLUMN + "='" + m_province + "'";
        } else if (m_byLocation) {
            String colName = "dist";
            projection = new String[] { CleverWeatherProvider.ROW_ID, CleverWeatherProvider.CITY_NAMEEN_COLUMN, getDistanceProjection(colName) };
            orderBy = colName + " limit 10";
        }

        return new CursorLoader(getActivity(), CleverWeatherProvider.CITY_URI, projection, where, null, orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mSwipeRefresh.setRefreshing(false);
        m_adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mSwipeRefresh.setRefreshing(false);
        m_adapter.swapCursor(null);
    }

    String getDistanceProjection(String colName) {
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
