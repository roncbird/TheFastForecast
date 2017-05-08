package com.mycompany.thefastforecast.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mycompany.thefastforecast.R;
import com.mycompany.thefastforecast.utilities.FontCache;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bird1 on 5/2/17.
 */

public class CitySelectionAdapter extends ArrayAdapter{

    private Context mContext;

    private ViewHolder viewHolder;

    private ArrayList<HashMap<String, String>> cityArrayList = new ArrayList<>();
    private ArrayList<String> userSelectedCityIdsList = new ArrayList<>();

    private Typeface fontawesome;

    public CitySelectionAdapter(@NonNull Context context, @LayoutRes int resource,
                                ArrayList<HashMap<String, String>> cityList, ArrayList<String> selectedCitysIdsList ) {
        super(context, resource);

        mContext = context;
        cityArrayList = cityList;
        userSelectedCityIdsList = selectedCitysIdsList;

        fontawesome = FontCache.get("fontawesome-webfont.ttf", mContext);
    }


    @Override
    public int getCount() {
        return cityArrayList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_city_selection_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.tv_us_city_name = (TextView)convertView.findViewById(R.id.tv_us_city_name);
            viewHolder.tv_city_selected_icon = (TextView)convertView.findViewById(R.id.tv_city_selected_icon);
            viewHolder.tv_city_selected_icon.setTypeface(fontawesome);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.tv_us_city_name.setText(cityArrayList.get(position).get("cityName"));

        for(int i = 0; i < userSelectedCityIdsList.size(); i++ )
        {
            if(userSelectedCityIdsList.get(i).equals(cityArrayList.get(position).get("id")))
            {
                viewHolder.tv_city_selected_icon.setVisibility(View.VISIBLE);
                break;
            }
            else
            {
                viewHolder.tv_city_selected_icon.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }

    public class ViewHolder
    {
        TextView tv_us_city_name;
        TextView tv_city_selected_icon;
    }
}
