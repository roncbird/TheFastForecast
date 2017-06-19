package com.birddevstudios.thefastforecast.fragments;


import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.birddevstudios.thefastforecast.activities.MainActivity;
import com.birddevstudios.thefastforecast.R;
import com.birddevstudios.thefastforecast.utilities.Methods;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherDetailFragment extends Fragment implements View.OnClickListener {

    HashMap<String, Object> mCityWeatherHashMap;

    private Typeface fontAwesome;

    public WeatherDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fontAwesome = Typeface.createFromAsset(getActivity().getAssets(), "fontawesome-webfont.ttf");

        if(getArguments().getSerializable("cityWeatherHashMap") != null)
        {
            mCityWeatherHashMap = (HashMap<String, Object>)getArguments().getSerializable("cityWeatherHashMap");
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.cloud_animation);

        RelativeLayout rl_weather_header = (RelativeLayout)view.findViewById(R.id.rl_weather_header);

        ((ImageView)view.findViewById(R.id.iv_weather_detail_icon)).setImageBitmap((Bitmap)mCityWeatherHashMap.get("icon"));

        ((TextView)view.findViewById(R.id.tv_back_icon)).setTypeface(fontAwesome);
        ((TextView)view.findViewById(R.id.tv_cloud)).setTypeface(fontAwesome);
        ((TextView)view.findViewById(R.id.tv_weather_detail_city_name)).setText(mCityWeatherHashMap.get("cityName").toString());
        ((TextView)view.findViewById(R.id.tv_weather_detail_current_temp)).setText( Methods.formatTemperature(mCityWeatherHashMap.get("currentTemp").toString()) + " \u2109");
        ((TextView)view.findViewById(R.id.tv_weather_detail_high_temp)).setText("Today's High - " +  Methods.formatTemperature(mCityWeatherHashMap.get("highTemp").toString()) + " \u2109");
        ((TextView)view.findViewById(R.id.tv_weather_detail_low_temp)).setText("Today's Low - " +  Methods.formatTemperature(mCityWeatherHashMap.get("lowTemp").toString()) + " \u2109");
        ((TextView)view.findViewById(R.id.tv_weather_detail_precipitation)).setText(mCityWeatherHashMap.get("precipitation").toString() + "\u0025" + " Chance of Precipitation");

        view.findViewById(R.id.ll_back).setOnClickListener(this);

        rl_weather_header.startAnimation(anim);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.ll_back:

                ((MainActivity)getActivity()).onBackPressed();

                break;
        }
    }
}
