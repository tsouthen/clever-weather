package com.listotechnologies.cleverweather;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

public class CitiesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter m_adapter;
    private boolean m_onlyFavorites;
    private String m_province;

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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = null;
        if (m_onlyFavorites)
            selection = CleverWeatherProvider.CITY_ISFAVORITE_COLUMN + "=1";
        else if (m_province != null)
            selection = CleverWeatherProvider.CITY_PROVINCE_COLUMN + "='" + m_province + "'";

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
