package com.listotechnologies.cleverweather;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
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
    private SimpleCursorAdapter m_adapter;
    private SwipeRefreshLayout mSwipeRefresh;

    public static final String ARG_CITY_CODE = "ARG_CITY_CODE";

    public static ForecastsFragment newInstance(String cityCode) {
        ForecastsFragment frag = new ForecastsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CITY_CODE, cityCode);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_refresh, container, false);
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.container);
        mSwipeRefresh.setColorScheme(R.color.swipe_color_1, R.color.swipe_color_2, R.color.swipe_color_3, R.color.swipe_color_4);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(0, null, ForecastsFragment.this);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int resId = 0;
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

        m_adapter = new ForecastAdapter(getActivity(), R.layout.forecast_item, null, dataColumns, viewIds, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(m_adapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mSwipeRefresh.setRefreshing(true);
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
        return new CursorLoader(getActivity(), CleverWeatherProvider.FORECAST_URI, projection, where, new String[] { cityCode }, orderBy);
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

    private static class ForecastAdapter extends SimpleCursorAdapter {
        private Locale mLocale;

        public ForecastAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            setViewBinder(mViewBinder);
            mLocale = context.getResources().getConfiguration().locale;
        }

        @Override
        public void setViewText(TextView v, String text) {
            switch (v.getId()) {
                case android.R.id.text1:
                    if (text == null || text.length() == 0)
                        text = "Current Conditions";
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
}
