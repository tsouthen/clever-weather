package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class ProvinceActivity extends BaseToolbarActivity {

    private static final String EXTRA_PROVINCE_ABBR = "EXTRA_PROVINCE_ABBR";
    private static final String EXTRA_PROVINCE_NAME = "EXTRA_PROVINCE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String provName = getIntent().getStringExtra(EXTRA_PROVINCE_NAME);
        if (provName != null)
            setActionBarTitle(provName);

        if (savedInstanceState == null) {
            String provAbbr = getIntent().getStringExtra(EXTRA_PROVINCE_ABBR);
            getFragmentManager().beginTransaction().add(R.id.content, CitiesFragment.newProvinceInstance(provAbbr)).commit();
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

    public static void start(Context context, ProvincesFragment.Province province) {
        Intent intent = new Intent(context, ProvinceActivity.class);
        intent.putExtra(ProvinceActivity.EXTRA_PROVINCE_ABBR, province.Abbreviation);
        intent.putExtra(ProvinceActivity.EXTRA_PROVINCE_NAME, province.Name);
        context.startActivity(intent);
    }
}
