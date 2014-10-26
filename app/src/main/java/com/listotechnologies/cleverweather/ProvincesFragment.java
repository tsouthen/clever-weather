package com.listotechnologies.cleverweather;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ProvincesFragment extends ListFragment {
    private OnProvinceClickListener mClickListener = null;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    public interface OnProvinceClickListener {
        void onProvinceClick(Province province);
    }

    public static class Province {
        public final String Name;
        public final String Abbreviation;
        private static ArrayList<Province> s_list;

        public Province (String name, String abbreviation) {
            Name = name;
            Abbreviation = abbreviation;
        }

        public static ArrayList<Province> GetProvinces(Context context) {
            if (s_list == null) {
                String[] abbr = context.getResources().getStringArray(R.array.province_abbr);
                String[] prov = context.getResources().getStringArray(R.array.provinces);

                s_list = new ArrayList<Province>();
                for (int ii=0; ii < abbr.length; ii++) {
                    s_list.add(new Province(prov[ii], abbr[ii]));
                }
            }
            return s_list;
        }

        @Override
        public String toString() {
            return Name;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnProvinceClickListener)
            mClickListener = (OnProvinceClickListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClickListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter<Province> adapter = new ArrayAdapter<Province>(getActivity(), android.R.layout.simple_list_item_activated_1, Province.GetProvinces(getActivity()));
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (mClickListener != null) {
            Province province = (Province) getListAdapter().getItem(position);
            mClickListener.onProvinceClick(province);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    public static ProvincesFragment newInstance() {
        return new ProvincesFragment();
    }
}

