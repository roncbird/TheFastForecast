package com.mycompany.thefastforecast.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mycompany.thefastforecast.activities.MainActivity;
import com.mycompany.thefastforecast.fragments.WeatherFragment;
import com.mycompany.thefastforecast.R;
import com.mycompany.thefastforecast.utilities.FontCache;
import com.mycompany.thefastforecast.utilities.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bird1 on 4/29/17.
 */

public class WeatherAdapter extends ArrayAdapter {

    private Context mContext;

    private ViewHolder viewHolder = null;

    private ArrayList<HashMap<String, Object>> mWeatherArrayList = new ArrayList<>();
    private ArrayList<String> userSelectedCityIds;
    private ArrayList<String> userSelectedCityNames;

    private Typeface fontAwesome;

    public WeatherAdapter(Context context, int resource, ArrayList<HashMap<String, Object>> weatherArrayList,
                          ArrayList<String> selectedCityIds, ArrayList<String> selectedCityNames) {
        super(context, resource);

        fontAwesome = FontCache.get("fontawesome-webfont.ttf", getContext());

        mContext = context;

        mWeatherArrayList = weatherArrayList;

        userSelectedCityIds = selectedCityIds;
        userSelectedCityNames = selectedCityNames;
    }

    @Override
    public int getCount() {
        return mWeatherArrayList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_weather_item, parent, false );

            viewHolder = new ViewHolder();
            viewHolder.tv_city_name = (TextView)convertView.findViewById(R.id.tv_city_name);
            viewHolder.tv_current_temperature = (TextView)convertView.findViewById(R.id.tv_current_temperature);
            viewHolder.iv_weather_icon = (ImageView) convertView.findViewById(R.id.iv_weather_icon);
            viewHolder.tv_delete_city = (TextView) convertView.findViewById(R.id.tv_delete_city);

            convertView.setTag(viewHolder);


        }
        else{

            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.tv_city_name.setText((String)mWeatherArrayList.get(position).get("cityName"));


        String currentTemp = Methods.formatTemperature((String)mWeatherArrayList.get(position).get("currentTemp"));

        viewHolder.tv_current_temperature.setText(currentTemp + " \u2109");
        viewHolder.iv_weather_icon.setImageBitmap((Bitmap)mWeatherArrayList.get(position).get("icon"));

        if(position <= 2)
        {
            viewHolder.tv_delete_city.setVisibility(View.VISIBLE);
            viewHolder.tv_delete_city.setTypeface(fontAwesome);
            viewHolder.tv_delete_city.setAlpha((float).25);
        }
        else
        {
            viewHolder.tv_delete_city.setVisibility(View.VISIBLE);
            viewHolder.tv_delete_city.setTypeface(fontAwesome);
            viewHolder.tv_delete_city.setAlpha((float)1);
            viewHolder.tv_delete_city.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    JSONObject cityWeatherJsonObject = null;
                    try {
                        cityWeatherJsonObject = new JSONObject(WeatherFragment.mWeatherJsonString);
                        JSONArray cityWeatherJsonArray = cityWeatherJsonObject.getJSONArray("list");
                        cityWeatherJsonArray.remove(position);
                        cityWeatherJsonObject.put("list", cityWeatherJsonArray);
                        WeatherFragment.mWeatherJsonString = cityWeatherJsonObject.toString();
                        Methods.saveString(getContext(), "cityWeatherJsonString", WeatherFragment.mWeatherJsonString);
                        MainActivity.loadDataFromSharedPreference = false;
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }


                    mWeatherArrayList.remove(position);
                    userSelectedCityIds.remove(position);
                    userSelectedCityNames.remove(position);
                    notifyDataSetChanged();

                }
            });
        }

        return convertView;
    }

    public class ViewHolder
    {

        TextView tv_city_name;
        TextView tv_current_temperature;
        ImageView iv_weather_icon;
        TextView tv_delete_city;

    }


}
