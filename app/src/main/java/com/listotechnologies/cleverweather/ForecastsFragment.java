package com.listotechnologies.cleverweather;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.StaleDataException;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForecastsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private ForecastAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private MenuItem mFavoriteMenu = null;
    private View mEmptyView = null;
    private long mLastLoad;
    private boolean mRefreshing = false;

    private static final String FORCE_REFRESH = "ForceRefresh";
    private static final String ARG_CITY_CODE = "ARG_CITY_CODE";
    private static final String ARG_IS_FAVORITE = "ARG_IS_FAVORITE";
    private static final String ARG_CITY_NAME = "ARG_CITY_NAME";
    private static final String ARG_BY_LOCATION = "ARG_BY_LOCATION";
    private static final String ARG_LOCATION = "ARG_LOCATION";

    public static ForecastsFragment newInstance(String cityCode, boolean isFavorite) {
        ForecastsFragment frag = new ForecastsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CITY_CODE, cityCode);
        bundle.putBoolean(ARG_IS_FAVORITE, isFavorite);
        frag.setArguments(bundle);
        return frag;
    }

    public static ForecastsFragment newClosestInstance(Location location) {
        ForecastsFragment frag = new ForecastsFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_BY_LOCATION, true);
        if (location != null) {
            bundle.putParcelable(ARG_LOCATION, location);
        }
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
        View view = inflater.inflate(R.layout.fragment_list_refresh, container, false);
        mEmptyView = view.findViewById(android.R.id.empty);
        setErrorText();
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.container);
        mSwipeRefresh.setColorScheme(R.color.swipe_color_4, R.color.swipe_color_3, R.color.swipe_color_2, R.color.swipe_color_1);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mRefreshing)
                    restartLoaderForceRefresh();
            }
        });
        return view;
    }

    private void setErrorText() {
        int id ;
        if (getArguments().containsKey(ARG_BY_LOCATION)) {
            id = R.string.location_error;
        } else {
            id = R.string.forecast_error;
        }

        if (mEmptyView != null) {
            TextView errorText = (TextView) mEmptyView.findViewById(R.id.error_text);
            if (errorText != null)
                errorText.setText(id);
        }
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
        String title = getArguments().getString(ARG_CITY_NAME);
        if (title != null && !title.isEmpty())
            mAdapter.setTitleString(title);
        getLoaderManager().initLoader(0, null, this);
        mLastLoad = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        possiblyRefresh();
    }

    private void possiblyRefresh() {
        if (!mRefreshing && mLastLoad != 0) {
            long nowTime = new Date().getTime();
            //if difference > half hour, initiate reload
            if ((nowTime - mLastLoad) > (30 * 60 * 1000)) {
                mLastLoad = 0;
                //Toast.makeText(getActivity(), "Refreshing Forecast", Toast.LENGTH_SHORT).show();
                restartLoaderForceRefresh();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        setUnsetEmptyView(false);
        String orderBy = CleverWeatherProvider.FORECAST_ID_COLUMN;
        String[] projection = {
                CleverWeatherProvider.FORECAST_ID_COLUMN,
                CleverWeatherProvider.FORECAST_NAME_COLUMN,
                CleverWeatherProvider.FORECAST_SUMMARY_COLUMN,
                CleverWeatherProvider.FORECAST_HIGHTEMP_COLUMN,
                CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN,
                CleverWeatherProvider.FORECAST_ICONCODE_COLUMN,
                CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN,
                CleverWeatherProvider.CITY_CODE_COLUMN,
                CleverWeatherProvider.CITY_NAMEEN_COLUMN,
                CleverWeatherProvider.CITY_ISFAVORITE_COLUMN
        };

        boolean forceRefresh = bundle != null && bundle.getBoolean(FORCE_REFRESH);

        String cityCode = "bogus";
        Bundle args = getArguments();
        if (args != null)
            cityCode = args.getString(ARG_CITY_CODE);
        String where = CleverWeatherProvider.FORECAST_CITYCODE_COLUMN + "=?";

        //TODO: only show this when we're actually refreshing from internet?
        mSwipeRefresh.setRefreshing(true);
        mRefreshing = true;

        if (args != null && args.getBoolean(ARG_BY_LOCATION, false))
            return new NearestCityForecastsLoader((Location) getArguments().getParcelable(ARG_LOCATION), getActivity(), CleverWeatherProvider.FORECAST_URI, projection, where, new String[] { cityCode }, orderBy, forceRefresh);

        return new ForecastsLoader(getActivity(), CleverWeatherProvider.FORECAST_URI, projection, where, new String[] { cityCode }, orderBy, forceRefresh);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        setUnsetEmptyView(true);
        mSwipeRefresh.setRefreshing(false);
        mRefreshing = false;

        boolean byLocation = getArguments().getBoolean(ARG_BY_LOCATION);
        if (byLocation) {
            if (cursor.isBeforeFirst() && !cursor.isAfterLast())
                cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                String cityCode = cursor.getString(7);
                String cityName = cursor.getString(8);
                boolean isFav = cursor.getInt(9) == 1;

                Bundle bundle = getArguments();
                bundle.putString(ARG_CITY_CODE, cityCode);
                bundle.putString(ARG_CITY_NAME, cityName);
                bundle.putBoolean(ARG_IS_FAVORITE, isFav);

                mAdapter.setTitleString(cityName);
                if (mFavoriteMenu != null) {
                    setActionBarCheckboxChecked(mFavoriteMenu, isFav);
                }
            }
        }

        mAdapter.changeCursor(cursor);
        Exception ex = CleverWeatherProviderExtended.getLastQueryException();
        if (ex != null)
            Toast.makeText(getActivity(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        mLastLoad = new Date().getTime();

        if (byLocation && mFavoriteMenu != null) {
            mFavoriteMenu.setVisible(!mAdapter.isEmpty());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mSwipeRefresh.setRefreshing(false);
        mRefreshing = false;
        mAdapter.changeCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        boolean checked = getArguments().getBoolean(ARG_IS_FAVORITE);
        inflater.inflate(R.menu.forecasts, menu);
        mFavoriteMenu = menu.findItem(R.id.menu_is_favorite);
        mFavoriteMenu.setChecked(checked);
        CheckBox favoriteView = (CheckBox) mFavoriteMenu.getActionView();
        if (favoriteView != null) {
            Drawable drawable = getResources().getDrawable(R.drawable.favorite_selector);
            TabbedActivity.setDrawableWhite(getActivity(), drawable);
            favoriteView.setButtonDrawable(drawable);
            favoriteView.setChecked(checked);
            favoriteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onOptionsItemSelected(mFavoriteMenu);
                }
            });
        }
        TabbedActivity.setIconsWhite(getActivity(), menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setActionBarCheckboxChecked(MenuItem it, boolean checked) {
        if (it == null)
            return;

        it.setChecked(checked);

        CheckBox cb = (CheckBox)it.getActionView();
        if (cb != null)
            cb.setChecked(checked);
        getArguments().putBoolean(ARG_IS_FAVORITE, checked);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_is_favorite) {
            //toggle check state
            boolean isFav = !item.isChecked();
            setActionBarCheckboxChecked(item, isFav);
            getArguments().putBoolean(ARG_IS_FAVORITE, isFav);

            //update City database in background thread
            setIsFavorite(isFav);

            //TODO: get called back by above call so we know the database has been updated
            //reload current data (from database only, no location redo)
            Loader<Cursor> cursorLoader = getLoaderManager().getLoader(0);
            NearestCityForecastsLoader loader = (NearestCityForecastsLoader) cursorLoader;
            if (loader != null) {
                getLoaderManager().restartLoader(0, null, this);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLocation(Location location) {
        if (location != null && getArguments().getBoolean(ARG_BY_LOCATION)) {
            getArguments().putParcelable(ARG_LOCATION, location);
            mSwipeRefresh.setRefreshing(true);
            restartLoaderForceRefresh();
        }
    }

    private void setUnsetEmptyView(boolean set) {
        if (mEmptyView == null || getView() == null)
            return;

        if (set) {
            getListView().setEmptyView(mEmptyView);
        } else {
            mEmptyView.setVisibility(View.GONE);
            getListView().setEmptyView(null);
        }
    }

    private void restartLoaderForceRefresh() {
        setUnsetEmptyView(false);
        //close the cursor so it isn't notified of changes we're about to make
        if (mAdapter.getCursor() != null)
            mAdapter.getCursor().close();
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        mRefreshing = true;
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
        private String mTitleString;

        ForecastAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            setViewBinder(mViewBinder);
            mContext = context;
            mExpanded = null;
            if (c != null) {
                mExpanded = new boolean[c.getCount()];
                if (mExpanded.length > 0)
                    mExpanded[0] = true;
            }
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            if (c != null) {
                mExpanded = new boolean[c.getCount()];
                if (mExpanded.length > 0)
                    mExpanded[0] = true;
            } else {
                mExpanded = null;
            }
            return super.swapCursor(c);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            //not sure why this is sometimes happening in the wild
            try {
                view = super.getView(position, convertView, parent);
            } catch (IllegalStateException isEx) {
                return null;
            }
            boolean expanded = false;
            if (mExpanded != null && position >= 0 && position < mExpanded.length)
                expanded = mExpanded[position];

            setVisibilities(view, expanded);
            return view;
        }

        private void setVisibilities(View view, boolean expanded) {
            setVisibility(view, android.R.id.text2, expanded ? View.VISIBLE : View.GONE);
            setVisibility(view, R.id.time_stamp, expanded ? View.VISIBLE : View.GONE);
            setVisibility(view, R.id.expand, expanded ? View.GONE : View.VISIBLE);
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
                    if (text == null || text.length() == 0) {
                        if (mTitleString != null)
                            text = mTitleString;
                        else
                            text = mContext.getString(R.string.now);
                    }
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
                } catch (NumberFormatException ex) {
                }
            }

            int id = 0;
            switch (iconCode) {
                case 0: //sun
                    id = R.drawable.sun;
                    break;

                case 1: //little clouds
                    id = R.drawable.sun_cloud;
                    break;

                case 4: //increasing cloud
                    id = R.drawable.sun_cloud_increasing;
                    break;

                case 5: //decreasing cloud
                case 20: //decreasing cloud
                    id = R.drawable.sun_cloud_decreasing;
                    break;

                case 2: //big cloud with sun
                case 3: //sun behind big cloud
                case 22: //big cloud with sun
                    id = R.drawable.cloud_sun;
                    break;

                case 6: //rain with sun behind cloud
                    id = R.drawable.cloud_drizzle_sun_alt;
                    break;

                case 7: //rain and snow with sun behind cloud
                case 8: //snow with sun behind cloud
                    id = R.drawable.cloud_snow_sun_alt;
                    break;

                case 9: //cloud rain lightning
                    id = R.drawable.cloud_lightning_sun;
                    break;

                case 10: //cloud
                    id = R.drawable.cloud;
                    break;

                case 11:
                case 28:
                    id = R.drawable.cloud_drizzle_alt;
                    break;

                case 12:
                    id = R.drawable.cloud_drizzle;
                    break;

                case 13:
                    id = R.drawable.cloud_rain;
                    break;

                case 15:
                case 16:
                case 17:
                case 18:
                    id = R.drawable.cloud_snow_alt;
                    break;

                case 19:
                    id = R.drawable.cloud_lightning;
                    break;

                case 23:
                case 24:
                case 44:
                    id = R.drawable.cloud_fog;
                    break;

                case 25:
                    id = R.drawable.cloud_wind;
                    break;

                case 14: //freezing rain
                case 26: //ice
                case 27: //hail
                    id = R.drawable.cloud_hail;
                    break;

                case 30:
                    id = R.drawable.moon;
                    break;

                case 31:
                case 32:
                case 33:
                    id = R.drawable.cloud_moon;
                    break;

                case 21:
                case 34:
                    id = R.drawable.cloud_moon_increasing;
                    break;

                case 35:
                    id = R.drawable.cloud_moon_decreasing;
                    break;

                case 36:
                    id = R.drawable.cloud_drizzle_moon_alt;
                    break;

                case 37:
                case 38:
                    id = R.drawable.cloud_snow_moon_alt;
                    break;

                case 39:
                    id = R.drawable.cloud_lightning_moon;
                    break;

                default:
                    iconCode = 29;
                    break;
            }

            if (iconCode == 29) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
                v.setImageResource(id);
            }
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
                    case android.R.id.text2:
                        return bindText2((TextView) view, cursor, i);
                }
                return false;
            }

            private boolean bindTimeStamp(TextView view, Cursor cursor, int i) {
                String text = "";
                long timeStamp = cursor.getLong(i);
                if (timeStamp > 0) {
                    Date utc = new Date(timeStamp);
                    if (mLocale == null)
                        mLocale = mContext.getResources().getConfiguration().locale;
                    if (mTimeStampFmt == null)
                        mTimeStampFmt = new SimpleDateFormat("MMM d, h:mm a", mLocale);
                    String asOf = mTimeStampFmt.format(utc);
                    if (cursor.getPosition() == 0)
                        text = String.format("conditions as of %s", asOf);
                    else
                        text = String.format("forecast as of %s", asOf);
                }
                view.setText(text);
                return true;
            }

            private String getLowTempHtml(String lowTemp) {
                int color = mContext.getResources().getColor(R.color.low_temp_color);
                String html = String.format("<font color=\"#%06X\">%s°</font>", color & 0x0FFFFFF, lowTemp);
                return html;
            }

            private String getHighTempHtml(String highTemp) {
                return String.format("<b>%s°</b>", highTemp);
            }

            private boolean bindHighTemp(TextView view, Cursor cursor, int i) {
                String highText = cursor.getString(i);
                String lowText = cursor.getString(cursor.getColumnIndex(CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN));
                String name = cursor.getString(cursor.getColumnIndex(CleverWeatherProvider.FORECAST_NAME_COLUMN));
                Spanned textVal = null;
                if (highText != null && lowText != null) {
                    textVal = Html.fromHtml(String.format("%s %s", getHighTempHtml(highText), getLowTempHtml(lowText)));
                } else if (highText != null) {
                    textVal = Html.fromHtml(getHighTempHtml(highText));
                } else if (lowText != null) {
                    textVal = Html.fromHtml(getLowTempHtml(lowText));
                } else {
                    textVal = new SpannedString("");
                }
                view.setText(textVal);
                return true;
            }

            private boolean bindText2(TextView view, Cursor cursor, int i) {
                String text = cursor.getString(i);
                if (text.contains("<a")) {
                    view.setText(Html.fromHtml(text));
                    view.setLinkTextColor(mContext.getResources().getColor(R.color.link_color));
                    view.setMovementMethod(LinkMovementMethod.getInstance());
                    setExpanded(cursor.getPosition(), true);
                    setVisibilities((View) view.getParent(), true);
                } else {
                    view.setText(text);
                }
                return true;
            }
        };

        public String getTitleString() {
            return mTitleString;
        }

        public void setTitleString(String mTitleString) {
            this.mTitleString = mTitleString;
        }

        @Override
        public long getItemId(int position) {
            //have a weird situation where during a refresh a touch event on the list can cause an
            //exception. Trying to avoid it by catching the exceptions and returning a default value.
            try {
                return super.getItemId(position);
            } catch (StaleDataException sdEx) {
                return 0;
            } catch (IllegalStateException isEx) {
                return 0;
            }
        }
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

    private static class NearestCityForecastsLoader extends ForecastsLoader {
        private Location mLocation = null;

        public NearestCityForecastsLoader (Location location, Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, boolean forceRefresh) {
            super(context, uri, projection, selection, selectionArgs, sortOrder, forceRefresh);
            mLocation = location;
        }

        @Override
        public Cursor loadInBackground() {
            String cityCode = null;
            if (mLocation != null) {
                cityCode = CleverWeatherProviderExtended.getClosestCity(getContext().getContentResolver(), mLocation);
            }
            if (cityCode == null) {
                cityCode = "bogus";
            }
            setSelectionArgs(new String [] {cityCode});
            return super.loadInBackground();
        }
    }
}
