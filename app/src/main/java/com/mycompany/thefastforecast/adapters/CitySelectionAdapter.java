package com.mycompany.thefastforecast.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.mycompany.thefastforecast.R;
import com.mycompany.thefastforecast.utilities.FontCache;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bird1 on 5/2/17.
 */

public class CitySelectionAdapter extends ArrayAdapter implements Filterable{

    public interface OnUpdateCityArrayListener
    {
        void onUpdateCityArray(ArrayList<HashMap<String, String>> cityArrayList);
    }

    private OnUpdateCityArrayListener onUpdateCityArrayListener;

    private Context mContext;

    private ViewHolder viewHolder;

    private ArrayList<HashMap<String, String>> cityArrayList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> originalCityArrayList = new ArrayList<>();
    private ArrayList<String> userSelectedCityIdsList = new ArrayList<>();

    private Typeface fontawesome;

    public CitySelectionAdapter(@NonNull Context context, @LayoutRes int resource,
                                ArrayList<HashMap<String, String>> cityList, ArrayList<String> selectedCitysIdsList,
                                OnUpdateCityArrayListener arrayUpdateListener ) {
        super(context, resource);

        mContext = context;
        cityArrayList = cityList;
        originalCityArrayList = cityList;
        userSelectedCityIdsList = selectedCitysIdsList;

        onUpdateCityArrayListener = arrayUpdateListener;

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

        viewHolder.tv_us_city_name.setText(cityArrayList.get(position).get("cityName") + "," + " " +
                cityArrayList.get(position).get("state"));
        viewHolder.tv_us_city_name.setSelected(true);

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


    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                cityArrayList = (ArrayList<HashMap<String, String>>) results.values;
                onUpdateCityArrayListener.onUpdateCityArray(cityArrayList);
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();

                if(constraint == null || constraint.length() == 0)
                {
                    results.values = originalCityArrayList;
                    results.count = originalCityArrayList.size();
                }
                else
                {
                    ArrayList<HashMap<String, String>> filteredCityNames = new ArrayList();

                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < originalCityArrayList.size(); i++) {
                        String cityName = originalCityArrayList.get(i).get("cityName");
                        if (cityName.toLowerCase().contains(constraint.toString()))  {
                            filteredCityNames.add(originalCityArrayList.get(i));
                        }
                    }

                    results.count = filteredCityNames.size();
                    results.values = filteredCityNames;
                }

                return results;
            }
        };


        return filter;
    }

}
