package com.mycompany.thefastforecast.fragments;


import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.TextView;

import com.mycompany.thefastforecast.R;
import com.mycompany.thefastforecast.activities.MainActivity;
import com.mycompany.thefastforecast.adapters.CitySelectionAdapter;
import com.mycompany.thefastforecast.utilities.FontCache;
import com.mycompany.thefastforecast.utilities.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class CitySelectionFragment extends Fragment implements View.OnClickListener {

    public interface OnDoneClickListener
    {
        void onDoneClicked(ArrayList<String> selectedCityIds, ArrayList<String> selectedCityNames, ArrayList<String> selectedCityIdsForUrl);
    }

    private Typeface fontAwesome;

    private CitySelectionAdapter citySelectionAdapter;
    private ListView lv_city_selection_list;

    private ArrayList<String> userSelectedCityNames = new ArrayList<>();
    private ArrayList<String> userSelectedCityIDs = new ArrayList<>();
    private ArrayList<String> selectedCityIDsForUrl = new ArrayList<>();
    private ArrayList<HashMap<String, String>> cityArrayList = new ArrayList<>();

    private TextView tv_done_icon;
    private TextView tv_cancel_icon;

    private String oldWeatherJsonString;

    private OnDoneClickListener onDoneClickListener;

    private boolean doneButtonClicked = false;

    private int citySelectionCount = 0;


    public CitySelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fontAwesome = FontCache.get("fontawesome-webfont.ttf", getContext());

        oldWeatherJsonString = WeatherFragment.mWeatherJsonString;

        if(getArguments() != null)
        {
            if(getArguments().getStringArrayList("selectedCityNames") != null)
            {
                userSelectedCityNames.clear();
                userSelectedCityNames.addAll(getArguments().getStringArrayList("selectedCityNames"));
            }

            if(getArguments().getStringArrayList("selectedCityIds") != null)
            {
                userSelectedCityIDs.clear();
                userSelectedCityIDs.addAll(getArguments().getStringArrayList("selectedCityIds"));
            }

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

        citySelectionAdapter = new CitySelectionAdapter(getContext(), 0, cityArrayList, userSelectedCityIDs);
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
                        userSelectedCityNames.add(cityArrayList.get(position).get("cityName"));
                        userSelectedCityIDs.add(cityArrayList.get(position).get("id"));
                        selectedCityIDsForUrl.add(cityArrayList.get(position).get("id"));
                    }


                }
                else
                {
                    if(citySelectionCount > 0)
                    {
                        citySelectionCount--;
                    }

                    tv_city_selected_icon.setVisibility(View.INVISIBLE);
                    userSelectedCityNames.remove(cityArrayList.get(position).get("cityName"));
                    userSelectedCityIDs.remove(cityArrayList.get(position).get("id"));
                    selectedCityIDsForUrl.remove(cityArrayList.get(position).get("id"));

                    JSONObject cityWeatherJsonObject = null;
                    try {
                        cityWeatherJsonObject = new JSONObject(WeatherFragment.mWeatherJsonString);
                        JSONArray cityWeatherJsonArray = cityWeatherJsonObject.getJSONArray("list");
                        for(int i = 0; i < cityWeatherJsonArray.length(); i++)
                        {
                            JSONObject jsonObject = cityWeatherJsonArray.getJSONObject(i);
                            if(jsonObject.getString("id").equals(cityArrayList.get(position).get("id")))
                            {
                                cityWeatherJsonArray.remove(i);
                            }
                        }
                        cityWeatherJsonObject.put("list", cityWeatherJsonArray);
                        WeatherFragment.mWeatherJsonString = cityWeatherJsonObject.toString();
                        Methods.saveString(getContext(), "cityWeatherJsonString", WeatherFragment.mWeatherJsonString);
                        MainActivity.loadDataFromSharedPreference = false;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
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
                        userSelectedCityNames.clear();
                        userSelectedCityIDs.clear();
                        selectedCityIDsForUrl.clear();

                        WeatherFragment.mWeatherJsonString = oldWeatherJsonString;
                        Methods.saveString(getContext(), "cityWeatherJsonString", WeatherFragment.mWeatherJsonString);

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

                doneButtonClicked = true;
                onDoneClickListener.onDoneClicked(userSelectedCityIDs, userSelectedCityNames, selectedCityIDsForUrl);

                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof OnDoneClickListener)
        {
            onDoneClickListener = (OnDoneClickListener)context;
        }
        else {

            throw new ClassCastException(context.toString() + "must implement"
                    + CitySelectionFragment.OnDoneClickListener.class.getSimpleName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(!doneButtonClicked)
        {
            WeatherFragment.mWeatherJsonString = oldWeatherJsonString;
            Methods.saveString(getContext(), "cityWeatherJsonString", WeatherFragment.mWeatherJsonString);
        }
    }
}
