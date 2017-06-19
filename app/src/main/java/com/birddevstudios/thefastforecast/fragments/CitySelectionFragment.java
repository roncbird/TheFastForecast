package com.birddevstudios.thefastforecast.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.birddevstudios.thefastforecast.R;
import com.birddevstudios.thefastforecast.adapters.CitySelectionAdapter;
import com.birddevstudios.thefastforecast.utilities.FontCache;
import com.birddevstudios.thefastforecast.utilities.Methods;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import static com.birddevstudios.thefastforecast.utilities.Constants.SharedPrefrenceKeys.SELECTED_CITY_IDS_JSON_STRING_KEY;


/**
 * A simple {@link Fragment} subclass.
 */
public class CitySelectionFragment extends Fragment implements View.OnClickListener, CitySelectionAdapter.OnUpdateCityArrayListener {


    private Typeface fontAwesome;

    private CitySelectionAdapter citySelectionAdapter;
    private ListView lv_city_selection_list;

    private ArrayList<String> userSelectedCityIDs = new ArrayList<>();
    private ArrayList<HashMap<String, String>> cityArrayList = new ArrayList<>();

    private TextView tv_done_icon;
    private TextView tv_cancel_icon;

    private int citySelectionCount = 0;

    private SearchView sv_city_list;


    public CitySelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fontAwesome = FontCache.get("fontawesome-webfont.ttf", getContext());

        JSONArray selectedCityIdsJsonArray = null;
        try {

            if(Methods.retrieveJSONString(getContext(), SELECTED_CITY_IDS_JSON_STRING_KEY) != null)
            {

                selectedCityIdsJsonArray = new JSONArray(Methods.retrieveJSONString(getContext(), SELECTED_CITY_IDS_JSON_STRING_KEY));

                if (selectedCityIdsJsonArray != null)
                {
                    userSelectedCityIDs.clear();

                    for (int i = 0; i < selectedCityIdsJsonArray.length(); i++)
                    {
                        userSelectedCityIDs.add(selectedCityIdsJsonArray.getString(i));
                    }

                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }


        if(getArguments() != null)
        {

            if(getArguments().getStringArrayList("cityArrayList") != null)
            {
                cityArrayList.clear();
                cityArrayList.addAll((ArrayList<HashMap<String, String>>)getArguments().getSerializable("cityArrayList"));
            }


        }


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_city_selection, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        citySelectionAdapter = new CitySelectionAdapter(getContext(), 0, cityArrayList, userSelectedCityIDs, this);
        lv_city_selection_list = (ListView)view.findViewById(R.id.lv_city_selection_list);
        lv_city_selection_list.setTextFilterEnabled(true);
        lv_city_selection_list.setAdapter(citySelectionAdapter);
        lv_city_selection_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tv_city_selected_icon = (TextView)view.findViewById(R.id.tv_city_selected_icon);

                if(tv_city_selected_icon.getVisibility() == View.INVISIBLE)
                {

                    if(citySelectionCount > 19)
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder
                                .setMessage(R.string.alert_select_only_20_cities)
                                .setCancelable(false)
                                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        dialog.dismiss();

                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                    else
                    {
                        citySelectionCount++;
                        tv_city_selected_icon.setVisibility(View.VISIBLE);
                        userSelectedCityIDs.add(cityArrayList.get(position).get("id"));
                    }


                }
                else
                {
                    if(citySelectionCount > 0)
                    {
                        citySelectionCount--;
                    }

                    tv_city_selected_icon.setVisibility(View.INVISIBLE);
                    userSelectedCityIDs.remove(cityArrayList.get(position).get("id"));

                }
            }


        });

        sv_city_list = (SearchView)view.findViewById(R.id.sv_city_list);

        sv_city_list.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                citySelectionAdapter.getFilter().filter(newText);

                return false;
            }
        });


        tv_done_icon = (TextView)view.findViewById(R.id.tv_done_icon);
        tv_done_icon.setTypeface(fontAwesome);
        tv_cancel_icon = (TextView)view.findViewById(R.id.tv_cancel_icon);
        tv_cancel_icon.setTypeface(fontAwesome);

        view.findViewById(R.id.ll_cancel_button).setOnClickListener(this);
        view.findViewById(R.id.ll_done_button).setOnClickListener(this);



    }

    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.ll_cancel_button:

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setMessage(R.string.alert_do_you_want_to_quit);
                alert.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Methods.hideKeyboard(getActivity());
                        getActivity().onBackPressed();
                    }
                });
                alert.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                alert.show();


                break;
            case R.id.ll_done_button:

                JSONArray jsonArray = new JSONArray();

                for(int i = 0; i < userSelectedCityIDs.size(); i++)
                {
                    jsonArray.put(userSelectedCityIDs.get(i));
                }

                Methods.hideKeyboard(getActivity());
                Methods.saveJSONString(getContext(), SELECTED_CITY_IDS_JSON_STRING_KEY, jsonArray.toString());

                getActivity().onBackPressed();

                break;
        }
    }


    @Override
    public void onUpdateCityArray(ArrayList<HashMap<String, String>> cityArrayList) {

        this.cityArrayList = cityArrayList;

    }
}
