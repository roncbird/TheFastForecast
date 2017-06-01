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

import com.mycompany.thefastforecast.R;
import com.mycompany.thefastforecast.fragments.Constants;
import com.mycompany.thefastforecast.utilities.FontCache;
import com.mycompany.thefastforecast.utilities.Methods;

import org.json.JSONArray;
import org.json.JSONException;

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

    private Typeface fontAwesome;

    public WeatherAdapter(Context context, int resource, ArrayList<HashMap<String, Object>> weatherArrayList) {
        super(context, resource);

        fontAwesome = FontCache.get("fontawesome-webfont.ttf", getContext());

        mContext = context;

        mWeatherArrayList = weatherArrayList;
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
            viewHolder.tv_city_name_and_temp = (TextView)convertView.findViewById(R.id.tv_city_name_and_temp);
            viewHolder.iv_weather_icon = (ImageView) convertView.findViewById(R.id.iv_weather_icon);
            viewHolder.tv_delete_city = (TextView) convertView.findViewById(R.id.tv_delete_city);

            convertView.setTag(viewHolder);


        }
        else{

            viewHolder = (ViewHolder)convertView.getTag();
        }


        if(mWeatherArrayList.size() > 0) {


            String currentTemp = Methods.formatTemperature((String) mWeatherArrayList.get(position).get("currentTemp"));

            viewHolder.tv_city_name_and_temp.setText((String) mWeatherArrayList.get(position).get("cityName") + "     " + currentTemp + " \u2109");
            viewHolder.tv_city_name_and_temp.setSelected(true);
            viewHolder.iv_weather_icon.setImageBitmap((Bitmap) mWeatherArrayList.get(position).get("icon"));

            if (position <= 2) {
                viewHolder.tv_delete_city.setVisibility(View.VISIBLE);
                viewHolder.tv_delete_city.setTypeface(fontAwesome);
                viewHolder.tv_delete_city.setAlpha((float) .25);
            } else {
                viewHolder.tv_delete_city.setVisibility(View.VISIBLE);
                viewHolder.tv_delete_city.setTypeface(fontAwesome);
                viewHolder.tv_delete_city.setAlpha((float) 1);
                viewHolder.tv_delete_city.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        JSONArray selectedCityIdsJsonArray = null;
                        try {
                            selectedCityIdsJsonArray = new JSONArray(Methods.retrieveJSONString(getContext(),
                                    Constants.SharedPrefrenceKeys.SELECTED_CITY_IDS_JSON_STRING_KEY));
                            selectedCityIdsJsonArray.remove(position);
                            Methods.saveString(getContext(), Constants.SharedPrefrenceKeys.SELECTED_CITY_IDS_JSON_STRING_KEY, selectedCityIdsJsonArray.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        mWeatherArrayList.remove(position);
                        notifyDataSetChanged();

                    }
                });
            }
        }

        return convertView;
    }

    public class ViewHolder
    {

        TextView tv_city_name_and_temp;
        ImageView iv_weather_icon;
        TextView tv_delete_city;

    }


}
