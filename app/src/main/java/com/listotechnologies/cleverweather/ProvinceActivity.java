package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class ProvinceActivity extends Activity {

    public static final String EXTRA_PROVINCE_ABBR = "EXTRA_PROVINCE_ABBR";
    public static final String EXTRA_PROVINCE_NAME = "EXTRA_PROVINCE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String provName = getIntent().getStringExtra(EXTRA_PROVINCE_NAME);
        if (provName != null)
            setTitle(provName);

        if (savedInstanceState == null) {
            String provAbbr = getIntent().getStringExtra(EXTRA_PROVINCE_ABBR);
            getFragmentManager().beginTransaction().add(android.R.id.content, CitiesFragment.newProvinceInstance(provAbbr)).commit();
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
