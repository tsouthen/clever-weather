package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class SearchCitiesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null)
                query = query.trim();

            //if we already know (via suggestions) that only one city will be returned, go straight to it
            if (query.equals(CleverWeatherProviderExtended.getLastSuggestionQuery()))
                viewForecasts(CleverWeatherProviderExtended.getLastSuggestion());
            else
                viewCities(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            viewForecasts(intent.getData().toString());
        }
    }

    private void viewCities(String query) {
        getFragmentManager().beginTransaction().add(android.R.id.content, CitiesFragment.newSearchInstance(query)).commit();
        setTitle(query);
    }

    private void viewForecasts(String query) {
        if (query != null) {
            ContentValues content = CleverWeatherProviderExtended.getSuggestionContent(query);
            if (content != null) {
                getFragmentManager()
                        .beginTransaction()
                        .add(android.R.id.content, ForecastsFragment.newInstance(content.getAsString(CleverWeatherProviderExtended.CITY_CODE_COLUMN),
                                                                                 content.getAsBoolean(CleverWeatherProviderExtended.CITY_ISFAVORITE_COLUMN)))
                        .commit();
                setTitle(content.getAsString(CleverWeatherProviderExtended.CITY_NAMEEN_COLUMN));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }
}
