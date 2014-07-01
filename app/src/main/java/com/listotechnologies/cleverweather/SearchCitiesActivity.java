package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

public class SearchCitiesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Search Cities"); //TODO: needed? If so, move to strings.xml

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            getFragmentManager().beginTransaction().add(android.R.id.content, CitiesFragment.newSearchInstance(query)).commit();
        }
    }
}
