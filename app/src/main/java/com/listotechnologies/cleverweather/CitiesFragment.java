package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class CitiesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private View mEmptyView = null;
    private boolean mRefreshing = false;
    private OnCityClickListener mClickListener = null;

    public static final String ARG_PROVINCE = "ARG_PROVINCE";
    public static final String ARG_FAVORITES = "ARG_FAVORITES";
    public static final String ARG_BY_LOCATION = "ARG_BY_LOCATION";
    public static final String ARG_LOCATION = "ARG_LOCATION";
    public static final String ARG_SEARCH = "ARG_SEARCH";

    public interface OnCityClickListener {
        void onCityClick(String cityCode, String cityName, boolean isFavorite);
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

    public static CitiesFragment newLocationInstance(Location location) {
        CitiesFragment frag = new CitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_BY_LOCATION, true);
        if (location != null) {
            bundle.putParcelable(ARG_LOCATION, location);
        }
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnCityClickListener)
            mClickListener = (OnCityClickListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClickListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int viewId = R.layout.fragment_list;
        if (getArguments().containsKey(ARG_BY_LOCATION))
            viewId = R.layout.fragment_list_refresh;

        View view = inflater.inflate(viewId, container, false);
        mEmptyView = view.findViewById(android.R.id.empty);
        setErrorText();
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.container);
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setColorScheme(R.color.swipe_color_4, R.color.swipe_color_3, R.color.swipe_color_2, R.color.swipe_color_1);
            mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!mRefreshing)
                        restartLoader();
                }
            });
        }
        return view;
    }

    private void setUnsetEmptyView(boolean set) {
        if (mEmptyView == null)
            return;

        if (set) {
            getListView().setEmptyView(mEmptyView);
        } else {
            if (mEmptyView != null)
                mEmptyView.setVisibility(View.GONE);
            getListView().setEmptyView(null);
        }
    }

    private void restartLoader() {
        mRefreshing = true;
        setUnsetEmptyView(false);
        getLoaderManager().restartLoader(0, null, this);
    }

    private void setErrorText() {
        int id = 0;
        if (getArguments().containsKey(ARG_BY_LOCATION)) {
            id = R.string.location_error;
        } else if (getArguments().containsKey(ARG_SEARCH)) {
            id = R.string.search_error;
        } else if (getArguments().containsKey(ARG_FAVORITES)) {
            id = R.string.favorites_error;
        }

        if (id != 0 && mEmptyView != null) {
            TextView errorText = (TextView) mEmptyView.findViewById(R.id.error_text);
            if (errorText != null)
                errorText.setText(id);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(getListView());

        int resId = 0;
        String[] dataColumns = {CleverWeatherProvider.CITY_NAMEEN_COLUMN};
        int[] viewIds = {android.R.id.text1};

        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, dataColumns, viewIds, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    private enum FilterMode {None, Location, Favorites, Search, Province};

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
            } else if (bundle.containsKey(ARG_BY_LOCATION)) {
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
        setUnsetEmptyView(false);
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(true);
        mRefreshing = true;
        String where = null;
        String orderBy = CleverWeatherProvider.CITY_NAMEEN_COLUMN + " COLLATE UNICODE";
        ArrayList<String> projection = new ArrayList<String>();
        projection.add(CleverWeatherProvider.CITY_ID_COLUMN);
        projection.add(CleverWeatherProvider.CITY_CODE_COLUMN);
        projection.add(CleverWeatherProvider.CITY_NAMEEN_COLUMN);
        projection.add(CleverWeatherProvider.CITY_ISFAVORITE_COLUMN);

        Filter filter = getFilterFromArguments();
        if (filter.Mode == FilterMode.Location) {
            return new ClosestCitiesLoader((Location)getArguments().getParcelable(ARG_LOCATION), getActivity(), CleverWeatherProvider.CITY_URI, projection.toArray(new String[projection.size()]), null, null, null);
        }

        switch (filter.Mode) {
            case Favorites:
                /*
                String nonFavs = String.format("%s not in (select %s from %s where %s=1)",
                        CleverWeatherProvider.FORECAST_CITYCODE_COLUMN,
                        CleverWeatherProvider.CITY_CODE_COLUMN,
                        CleverWeatherProvider.CITY_TABLE,
                        CleverWeatherProvider.CITY_ISFAVORITE_COLUMN);
                String recents = String.format("%s in (select %s from %s where %s is not null and %s group by %s order by %s desc limit 20)",
                        CleverWeatherProvider.CITY_CODE_COLUMN,
                        CleverWeatherProvider.FORECAST_CITYCODE_COLUMN,
                        CleverWeatherProvider.FORECAST_TABLE,
                        CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN,
                        nonFavs,
                        CleverWeatherProvider.FORECAST_CITYCODE_COLUMN,
                        CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN);
                where = CleverWeatherProvider.CITY_ISFAVORITE_COLUMN + "=1 or " + recents;
                orderBy = CleverWeatherProvider.CITY_ISFAVORITE_COLUMN + " DESC," + CleverWeatherProvider.CITY_NAMEEN_COLUMN + " COLLATE UNICODE";
                */
                where = CleverWeatherProvider.CITY_ISFAVORITE_COLUMN + "=1";
                break;

            case Province:
                where = CleverWeatherProvider.CITY_PROVINCE_COLUMN + "='" + filter.Arguments + "'";
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
        mRefreshing = false;
        mAdapter.swapCursor(cursor);
        setUnsetEmptyView(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(false);
        mRefreshing = false;
        mAdapter.swapCursor(null);
    }

    private void onCityClick(int position) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        String cityCode = cursor.getString(1);
        String cityName = cursor.getString(2);
        boolean isFavorite = cursor.getInt(3) != 0;

        if (mClickListener != null) {
            mClickListener.onCityClick(cityCode, cityName, isFavorite);
        } else {
            ForecastsActivity.start(getActivity(), cityCode, cityName, isFavorite);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        onCityClick(position);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == android.R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Cursor cursor = (Cursor) mAdapter.getItem(info.position);
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
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Cursor cursor = (Cursor) mAdapter.getItem(info.position);
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
                contentResolver.update(CleverWeatherProvider.CITY_URI, values, where, new String[]{cityCode});
                return null;
            }
        };
        task.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getArguments().containsKey(ARG_BY_LOCATION))
            inflater.inflate(R.menu.refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            if (!mRefreshing) {
                mSwipeRefresh.setRefreshing(true);
                restartLoader();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLocation(Location location) {
        if (location != null && getArguments().getBoolean(ARG_BY_LOCATION)) {
            getArguments().putParcelable(ARG_LOCATION, location);
            mSwipeRefresh.setRefreshing(true);
            restartLoader();
        }
    }

    private static class ClosestCitiesLoader extends CursorLoader {
        private Location mLocation = null;

        public ClosestCitiesLoader(Location location, Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
            mLocation = location;
        }

        @Override
        public Cursor loadInBackground() {
            //get current location
            Location location = mLocation;
            if (location == null) {
                LocationGetter locationGetter = TabbedActivity.getLocationGetter(getContext());
                if (locationGetter.isLocationEnabled())
                    location = locationGetter.getLocation(true);
            }

            if (location != null) {
                mLocation = location;

                //add distance projection to projection
                String colName = "dist";
                String distProjection = CleverWeatherProviderExtended.getDistanceSquaredProjection(location, colName);
                ArrayList<String> projection = new ArrayList<String>(Arrays.asList(getProjection()));
                projection.add(distProjection);
                setProjection(projection.toArray(new String[projection.size()]));

                //set order by
                setSortOrder(colName + " limit 10");
                //don't return HEF entries
                setSelection(CleverWeatherProvider.CITY_PROVINCE_COLUMN + "<>'HEF'");
            } else {
                //set the where clause to return nothing
                setSelection(CleverWeatherProvider.CITY_CODE_COLUMN + "='bogus'");
            }

            return super.loadInBackground();
        }
    }
}
