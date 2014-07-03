package com.listotechnologies.cleverweather;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class ForecastsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;

    private static final String FORCE_REFRESH = "ForceRefresh";
    public static final String ARG_CITY_CODE = "ARG_CITY_CODE";
    public static final String ARG_IS_FAVORITE = "ARG_IS_FAVORITE";

    public static ForecastsFragment newInstance(String cityCode, boolean isFavorite) {
        ForecastsFragment frag = new ForecastsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CITY_CODE, cityCode);
        bundle.putBoolean(ARG_IS_FAVORITE, isFavorite);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_refresh, container, false);
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.container);
        mSwipeRefresh.setColorScheme(R.color.swipe_color_4, R.color.swipe_color_3, R.color.swipe_color_2, R.color.swipe_color_1);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                restartLoaderForceRefresh();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] dataColumns = {
                CleverWeatherProvider.FORECAST_NAME_COLUMN,
                CleverWeatherProvider.FORECAST_SUMMARY_COLUMN,
                CleverWeatherProvider.FORECAST_HIGHTEMP_COLUMN,
                CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN,
                CleverWeatherProvider.FORECAST_ICONCODE_COLUMN,
        };
        int[] viewIds = {
                android.R.id.text1,
                android.R.id.text2,
                R.id.hightemp,
                R.id.lowtemp,
                R.id.icon
        };

        mAdapter = new ForecastAdapter(getActivity(), R.layout.forecast_item, null, dataColumns, viewIds, 0);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String orderBy = CleverWeatherProvider.ROW_ID;
        String[] projection = {
                CleverWeatherProvider.ROW_ID,
                CleverWeatherProvider.FORECAST_NAME_COLUMN,
                CleverWeatherProvider.FORECAST_SUMMARY_COLUMN,
                CleverWeatherProvider.FORECAST_HIGHTEMP_COLUMN,
                CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN,
                CleverWeatherProvider.FORECAST_ICONCODE_COLUMN,
                CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN,
        };

        String cityCode = "bogus";
        Bundle args = getArguments();
        if (args != null)
            cityCode = args.getString(ARG_CITY_CODE);
        String where = CleverWeatherProvider.FORECAST_CITYCODE_COLUMN + "=?";
        boolean forceRefresh = bundle != null && bundle.getBoolean(FORCE_REFRESH);
        //TODO: only show this when we're actually refreshing from internet
        mSwipeRefresh.setRefreshing(true);
        return new ForecastsLoader(getActivity(), CleverWeatherProvider.FORECAST_URI, projection, where, new String[] { cityCode }, orderBy, forceRefresh);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mSwipeRefresh.setRefreshing(false);
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mSwipeRefresh.setRefreshing(false);
        mAdapter.changeCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecasts, menu);
        MenuItem isFav = menu.findItem(R.id.menu_is_favorite);
        if (isFav != null)
            isFav.setChecked(getArguments().getBoolean(ARG_IS_FAVORITE));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            mSwipeRefresh.setRefreshing(true);
            restartLoaderForceRefresh();
        } else if (item.getItemId() == R.id.menu_is_favorite) {
            //toggle check state
            boolean isFav = !item.isChecked();
            item.setChecked(isFav);
            getArguments().putBoolean(ARG_IS_FAVORITE, isFav);
            //update City database in background thread
            setIsFavorite(isFav);
        }
        return super.onOptionsItemSelected(item);
    }

    private void restartLoaderForceRefresh() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        getLoaderManager().restartLoader(0, bundle, this);
    }

    private void setIsFavorite(final boolean isFavorite) {
        final String cityCode = getArguments().getString(ARG_CITY_CODE);
        AsyncTask<String, Integer, Long> task = new AsyncTask<String, Integer, Long>() {
            @Override
            protected Long doInBackground(String... strings) {
                ContentValues values = new ContentValues();
                values.put(CleverWeatherProvider.CITY_ISFAVORITE_COLUMN, isFavorite);
                String where = CleverWeatherProvider.CITY_CODE_COLUMN + "=?";
                getActivity().getContentResolver().update(CleverWeatherProvider.CITY_URI, values, where, new String[] {cityCode});
                return null;
            }
        };
        task.execute();
    }

    private static class ForecastAdapter extends SimpleCursorAdapter {
        private Locale mLocale;
        private Context mContext;

        public ForecastAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            setViewBinder(mViewBinder);
            mContext = context;
        }

        @Override
        public void setViewText(TextView v, String text) {
            switch (v.getId()) {
                case android.R.id.text1:
                    if (text == null || text.length() == 0)
                        text = mContext.getString(R.string.current_conditions);
                    break;
            }
            super.setViewText(v, text);
        }

        @Override
        public void setViewImage(ImageView v, String value) {
            int iconCode = 29;
            if (value != null) {
                try {
                    iconCode = Integer.parseInt(value);
                    if (iconCode > 39 || iconCode < 0)
                        iconCode = 29;
                } catch (NumberFormatException ex) {
                }
            }

            if (iconCode != 29) {
                v.setVisibility(View.VISIBLE);
                int id = R.drawable.cbc_white_00 + iconCode;
                v.setImageResource(id);
            } else {
                v.setVisibility(View.INVISIBLE);
            }
        }

        private final ViewBinder mViewBinder = new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                switch (view.getId()) {
                    case R.id.hightemp:
                        return bindHighTemp((TextView) view, cursor, i);
                    case android.R.id.text2:
                        return bindText2((TextView) view, cursor, i);
                }
                return false;
            }

            private boolean bindText2(TextView view, Cursor cursor, int i) {
                String name = cursor.getString(cursor.getColumnIndex(CleverWeatherProvider.FORECAST_NAME_COLUMN));
                if (name == null || name.isEmpty()) {
                    long timeStamp = cursor.getLong(cursor.getColumnIndex(CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN));
                    if (timeStamp > 0) {
                        String text = cursor.getString(i);
                        Date utc = new Date(timeStamp);
                        if (mLocale == null)
                            mLocale = mContext.getResources().getConfiguration().locale;
                        String asOf = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, mLocale).format(utc);
                        text += String.format("\n\nas of %s", asOf);
                        ((TextView) view).setText(text);
                        return true;
                    }
                }
                return false;
            }

            private boolean bindHighTemp(TextView view, Cursor cursor, int i) {
                String highText = cursor.getString(i);
                String lowText = cursor.getString(cursor.getColumnIndex(CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN));
                String name = cursor.getString(cursor.getColumnIndex(CleverWeatherProvider.FORECAST_NAME_COLUMN));
                String textVal = null;
                if (highText != null && lowText != null) {
                    textVal = String.format("%s° | %s°", highText, lowText);
                } else if (highText != null && name == null) {
                    textVal = String.format("%s°", highText);
                } else if (highText != null) {
                    textVal = String.format("↑ %s°", highText);
                } else if (lowText != null) {
                    textVal = String.format("↓ %s°", lowText);
                }
                if (textVal != null)
                    ((TextView) view).setText(textVal);
                return true;
            }
        };

    }

    private static class ForecastsLoader extends CursorLoader {
        private boolean mForceRefresh;

        public ForecastsLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, boolean forceRefresh) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
            mForceRefresh = forceRefresh;
        }

        @Override
        public Cursor loadInBackground() {
            if (mForceRefresh) {
                //Use the db directly instead of through the content provider as this didn't seem to
                // work, ended up with duplicate data. Perhaps the content provider wraps things in
                // a transaction
                int rowsDeleted = 0;
                CleverWeatherDbHelper helper = new CleverWeatherDbHelper(getContext());
                helper.open();
                rowsDeleted = helper.mDb.delete(CleverWeatherDbHelper.FORECAST_TABLE, getSelection(), getSelectionArgs());
                //rowsDeleted = getContext().getContentResolver().delete(getUri(), getSelection(), getSelectionArgs());
                helper.close();
                mForceRefresh = false;
            }
            return super.loadInBackground();
        }
    }
}