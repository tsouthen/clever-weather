package com.listotechnologies.cleverweather;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForecastsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private ForecastAdapter mAdapter;
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
        View view = inflater.inflate(R.layout.fragment_forecasts, container, false);
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
                CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN,
        };
        int[] viewIds = {
                android.R.id.text1,
                android.R.id.text2,
                R.id.hightemp,
                R.id.lowtemp,
                R.id.icon,
                R.id.time_stamp,
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
        //close the cursor so it isn't notified of changes we're about to make
        mAdapter.getCursor().close();
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        getLoaderManager().restartLoader(0, bundle, this);
    }

    private void setIsFavorite(final boolean isFavorite) {
        final String cityCode = getArguments().getString(ARG_CITY_CODE);
        CitiesFragment.setIsFavorite(getActivity().getContentResolver(), cityCode, isFavorite);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //toggle visibility of text2 and expand
        View text2 = v.findViewById(android.R.id.text2);
        swapVisibility(text2);
        swapVisibility(v.findViewById(R.id.expand));
        TextView timeStamp = (TextView) v.findViewById(R.id.time_stamp);
        if (timeStamp.getText().length() > 0)
            timeStamp.setVisibility(text2.getVisibility());
        mAdapter.setExpanded(position, text2.getVisibility() == View.VISIBLE);
        super.onListItemClick(l, v, position, id);
    }

    private void swapVisibility(View view) {
        int vis = view.getVisibility();
        if (vis == View.GONE || vis == View.VISIBLE) {
            if (vis == View.GONE)
                vis = View.VISIBLE;
            else
                vis = View.GONE;
            view.setVisibility(vis);
        }
    }

    private static class ForecastAdapter extends SimpleCursorAdapter {
        private Locale mLocale;
        private Context mContext;
        private SimpleDateFormat mTimeStampFmt;
        private boolean mExpanded[];

        public ForecastAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            setViewBinder(mViewBinder);
            mContext = context;
            mExpanded = null;
            if (c != null)
                mExpanded = new boolean[c.getCount()];
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            mExpanded = new boolean[c.getCount()];
            return super.swapCursor(c);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            boolean expanded = false;
            if (mExpanded != null && position >= 0 && position < mExpanded.length)
                expanded = mExpanded[position];

            setVisibility(view, android.R.id.text2, expanded ? View.VISIBLE : View.GONE);
            setVisibility(view, R.id.time_stamp, expanded ? View.VISIBLE : View.GONE);
            setVisibility(view, R.id.expand, expanded ? View.GONE : View.VISIBLE);

            return view;
        }

        private void setVisibility(View parent, int viewId, int visibility) {
            if (parent != null) {
                View view = parent.findViewById(viewId);
                if (view != null)
                    view.setVisibility(visibility);
            }
        }

        @Override
        public void setViewText(TextView v, String text) {
            switch (v.getId()) {
                case android.R.id.text1:
                    if (text == null || text.length() == 0)
                        text = mContext.getString(R.string.now);
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

            v.setVisibility(View.VISIBLE);
            int id = R.drawable.cbc_white_00 + iconCode;
            v.setImageResource(id);
        }

        public boolean getExpanded(int position) {
            if (mExpanded == null || position < 0 || position >= mExpanded.length)
                return false;

            return mExpanded[position];
        }

        public void setExpanded(int position, boolean expanded) {
            if (mExpanded != null && position >= 0 && position < mExpanded.length)
                mExpanded[position] = expanded;
        }

        private final ViewBinder mViewBinder = new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                switch (view.getId()) {
                    case R.id.hightemp:
                        return bindHighTemp((TextView) view, cursor, i);
                    case R.id.time_stamp:
                        return bindTimeStamp((TextView) view, cursor, i);
                }
                return false;
            }

            private boolean bindTimeStamp(TextView view, Cursor cursor, int i) {
                long timeStamp = cursor.getLong(i);
                if (timeStamp > 0) {
                    Date utc = new Date(timeStamp);
                    if (mLocale == null)
                        mLocale = mContext.getResources().getConfiguration().locale;
                    if (mTimeStampFmt == null)
                        mTimeStampFmt = new SimpleDateFormat("MMM d, h::mm a", mLocale);
                    String asOf = mTimeStampFmt.format(utc);
                    String text;
                    if (cursor.getPosition() == 0)
                        text = String.format("conditions as of %s", asOf);
                    else
                        text = String.format("forecast as of %s", asOf);
                    ((TextView) view).setText(text);
                }
                return true;
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
                    textVal = String.format("%s°", highText);
                } else if (lowText != null) {
                    textVal = String.format("low %s°", lowText);
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
