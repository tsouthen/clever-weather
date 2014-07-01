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
    private SwipeRefreshLayout mSwipeRefresh;

    public static final String ARG_PROVINCE = "ARG_PROVINCE";
    public static final String ARG_FAVORITES = "ARG_FAVORITES";
    public static final String ARG_LOCATION = "ARG_LOCATION";
    public static final String ARG_SEARCH = "ARG_SEARCH";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    public static CitiesFragment newSearchInstance(String search) {
        CitiesFragment frag = new CitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SEARCH, search);
        frag.setArguments(bundle);
        return frag;
    }

    private enum FilterMode { None, Location, Favorites, Search, Province };
    private static class Filter {
        public FilterMode Mode;
        public String Arguments;
    }

    private Filter getFilterFromArguments() {
        Filter filter = new Filter();
        filter.Mode = FilterMode.None;

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(ARG_PROVINCE)) {
                filter.Arguments = bundle.getString(ARG_PROVINCE);
                filter.Mode = FilterMode.Province;
            } else if (bundle.containsKey(ARG_FAVORITES)) {
                filter.Mode = FilterMode.Favorites;
            } else if (bundle.containsKey(ARG_LOCATION)) {
                filter.Mode = FilterMode.Location;
            } else if (bundle.containsKey(ARG_SEARCH)) {
                filter.Mode = FilterMode.Search;
                filter.Arguments = bundle.getString(ARG_SEARCH);
            }
        }
        if (filter.Mode == FilterMode.None) {
            Log.d("CitiesFragment", "CitiesFragment created without any constraints!");
        }
        return filter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mSwipeRefresh.setRefreshing(true);
        String where = null;
        String orderBy = CleverWeatherProvider.CITY_NAMEEN_COLUMN + " COLLATE UNICODE";
        String[] projection = { CleverWeatherProvider.ROW_ID, CleverWeatherProvider.CITY_NAMEEN_COLUMN };

        Filter filter = getFilterFromArguments();
        switch (filter.Mode) {
            case Favorites:
                where = CleverWeatherProvider.CITY_ISFAVORITE_COLUMN + "=1";
                break;

            case Province:
                where = CleverWeatherProvider.CITY_PROVINCE_COLUMN + "='" + filter.Arguments + "'";
                break;

            case Location:
                String colName = "dist";
                projection = new String[] { CleverWeatherProvider.ROW_ID, CleverWeatherProvider.CITY_NAMEEN_COLUMN, getDistanceProjection(colName) };
                orderBy = colName + " limit 10";
                break;

            case Search:
                where = CleverWeatherProvider.CITY_NAMEEN_COLUMN + " LIKE '" + filter.Arguments + "%'";
                break;
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
