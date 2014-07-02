package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.os.Bundle;

public class ForecastsActivity extends Activity {
    public static final String EXTRA_CITY_CODE = "EXTRA_CITY_CODE";
    public static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String cityName = getIntent().getStringExtra(EXTRA_CITY_NAME);
        if (cityName != null)
            setTitle(cityName);

        if (savedInstanceState == null) {
            String cityCode = getIntent().getStringExtra(EXTRA_CITY_CODE);
            getFragmentManager().beginTransaction().add(android.R.id.content, ForecastsFragment.newInstance(cityCode)).commit();
        }
    }
}
