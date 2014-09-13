package com.listotechnologies.cleverweather;

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

    public interface OnProvinceClickListener {
        void onProvinceClick(Province province);
    }

    public void setOnProvinceClickListener(OnProvinceClickListener listener) {
        mClickListener = listener;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter<Province> adapter = new ArrayAdapter<Province>(getActivity(), android.R.layout.simple_list_item_1, Province.GetProvinces(getActivity()));
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Province province = (Province) getListAdapter().getItem(position);

        if (mClickListener != null) {
            mClickListener.onProvinceClick(province);
        } else {
            ProvinceActivity.start(getActivity(), province);
        }
    }

    public static ProvincesFragment newInstance() {
        return new ProvincesFragment();
    }
}

