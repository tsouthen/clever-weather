package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

public class TwoPaneFragment extends Fragment {

    public TwoPaneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_two_pane, container, false);

        //add fragment to left panel
        ProvincesFragment provincesFragment = ProvincesFragment.newInstance();
        getFragmentManager().beginTransaction()
                .replace(R.id.left_panel, provincesFragment)
                .commit();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setChoiceMode();
    }

    private void setChoiceMode() {
        ProvincesFragment provincesFragment = (ProvincesFragment) getFragmentManager().findFragmentById(R.id.left_panel);
        if (provincesFragment != null)
            provincesFragment.setActivateOnItemClick(true);
    }
}
