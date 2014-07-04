package com.listotechnologies.cleverweather;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class CitiesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter m_adapter;
    private SwipeRefreshLayout mSwipeRefresh;

    public static final String ARG_PROVINCE = "ARG_PROVINCE";
    public static final String ARG_FAVORITES = "ARG_FAVORITES";
    public static final String ARG_LOCATION = "ARG_LOCATION";
    public static final String ARG_SEARCH = "ARG_SEARCH";

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int viewId = R.layout.fragment_cities;
        if (getArguments().containsKey(ARG_LOCATION))
            viewId = R.layout.fragment_cities_refresh;

        View view = inflater.inflate(viewId, container, false);
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.container);
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setColorScheme(R.color.swipe_color_1, R.color.swipe_color_2, R.color.swipe_color_3, R.color.swipe_color_4);
            mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getLoaderManager().restartLoader(0, null, CitiesFragment.this);
                }
            });
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(getListView());

        int resId = 0;
        String[] dataColumns = { CleverWeatherProvider.CITY_NAMEEN_COLUMN };
        int[] viewIds = { android.R.id.text1 };

        m_adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, dataColumns, viewIds, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(m_adapter);
        getLoaderManager().initLoader(0, null, this);
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
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(true);
        String where = null;
        String orderBy = CleverWeatherProvider.CITY_NAMEEN_COLUMN + " COLLATE UNICODE";
        ArrayList<String> projection = new ArrayList<String>();
        projection.add(CleverWeatherProvider.ROW_ID);
        projection.add(CleverWeatherProvider.CITY_CODE_COLUMN);
        projection.add(CleverWeatherProvider.CITY_NAMEEN_COLUMN);
        projection.add(CleverWeatherProvider.CITY_ISFAVORITE_COLUMN);

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
                projection.add(getDistanceProjection(colName));
                orderBy = colName + " limit 10";
                break;

            case Search:
                where = CleverWeatherProvider.CITY_NAMEEN_COLUMN + " LIKE '" + filter.Arguments + "%'";
                break;
        }
        String[] projectionArray = projection.toArray(new String[projection.size()]);
        return new CursorLoader(getActivity(), CleverWeatherProvider.CITY_URI, projectionArray, where, null, orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(false);
        m_adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (mSwipeRefresh != null)
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) m_adapter.getItem(position);
        Intent intent = new Intent(getActivity(), ForecastsActivity.class);
        intent.putExtra(ForecastsActivity.EXTRA_CITY_CODE, cursor.getString(1));
        intent.putExtra(ForecastsActivity.EXTRA_CITY_NAME, cursor.getString(2));
        boolean isFavorite = cursor.getInt(3) != 0;
        intent.putExtra(ForecastsActivity.EXTRA_IS_FAVORITE, isFavorite);
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == android.R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            Cursor cursor = (Cursor) m_adapter.getItem(info.position);
            boolean isFavorite = cursor.getInt(3) != 0;
            menu.setHeaderTitle(cursor.getString(2));
            if (isFavorite)
                menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, getResources().getString(R.string.remove_from_favorites));
            else
                menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, getResources().getString(R.string.add_to_favorites));
            return;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == Menu.FIRST || item.getItemId() == Menu.FIRST + 1) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            Cursor cursor = (Cursor) m_adapter.getItem(info.position);
            String cityCode = cursor.getString(1);
            boolean isFavorite = item.getItemId() != Menu.FIRST;
            setIsFavorite(getActivity().getContentResolver(), cityCode, isFavorite);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    public static void setIsFavorite(final ContentResolver contentResolver, final String cityCode, final boolean isFavorite) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues values = new ContentValues();
                values.put(CleverWeatherProvider.CITY_ISFAVORITE_COLUMN, isFavorite);
                String where = CleverWeatherProvider.CITY_CODE_COLUMN + "=?";
                contentResolver.update(CleverWeatherProvider.CITY_URI, values, where, new String[] {cityCode});
                return null;
            }
        };
        task.execute();
    }
}
