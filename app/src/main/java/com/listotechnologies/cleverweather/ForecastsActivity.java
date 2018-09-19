package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class ForecastsActivity extends BaseToolbarActivity {
    private static final String EXTRA_CITY_CODE = "EXTRA_CITY_CODE";
    private static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME";
    private static final String EXTRA_IS_FAVORITE = "EXTRA_IS_FAVORITE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String cityName = getIntent().getStringExtra(EXTRA_CITY_NAME);
        if (cityName != null)
            setActionBarTitle(cityName);

        if (savedInstanceState == null) {
            String cityCode = getIntent().getStringExtra(EXTRA_CITY_CODE);
            boolean isFavorite = getIntent().getBooleanExtra(EXTRA_IS_FAVORITE, false);
            ForecastsFragment fragment = ForecastsFragment.newInstance(cityCode, isFavorite);
            getFragmentManager().beginTransaction().add(R.id.content, fragment).commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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

    public static void start(Context context, String cityCode, String cityName, boolean isFavorite) {
        Intent intent = new Intent(context, ForecastsActivity.class);
        intent.putExtra(ForecastsActivity.EXTRA_CITY_CODE, cityCode);
        intent.putExtra(ForecastsActivity.EXTRA_CITY_NAME, cityName);
        intent.putExtra(ForecastsActivity.EXTRA_IS_FAVORITE, isFavorite);
        context.startActivity(intent);
    }
}
