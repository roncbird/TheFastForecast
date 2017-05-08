package com.mycompany.thefastforecast.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.mycompany.thefastforecast.R;
import com.mycompany.thefastforecast.activities.MainActivity;
import com.mycompany.thefastforecast.adapters.WeatherAdapter;
import com.mycompany.thefastforecast.utilities.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {

    public interface OnWeatherItemClickListener
    {
        void onWeatherItemClicked(HashMap<String, Object> cityWeatherHashMap);
    }

    public interface OnAddCityClickListener
    {
        void onAddCityClicked(ArrayList<String> selectedCityIds, ArrayList<String> userSelectedCityNames);
    }

    private ArrayList<HashMap<String, Object>> mCityWeatherList = new ArrayList<>();
    private ArrayList<String> userSelectedCityIds = new ArrayList<>();
    private ArrayList<String> userSelectedCityNames = new ArrayList<>();
    private ListView lv_city_weather;

    private Button btn_add_city;

    private WeatherAdapter mWeatherAdapter;

    private OnWeatherItemClickListener onWeatherItemClickListener;

    private OnAddCityClickListener onAddCityClickListener;

    private String selectedCityIDs = "";
    public static String mWeatherJsonString = "";


    public WeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        URL cityForcastUrl = null;
        try {
            if(userSelectedCityIds.size() > 0)
            {

                selectedCityIDs = "";

                for(int i = 0; i < userSelectedCityIds.size(); i++)
                {
                    if(i < userSelectedCityIds.size() - 1 )
                    {
                        selectedCityIDs += userSelectedCityIds.get(i) + ",";
                    }
                    else
                    {
                        selectedCityIDs += userSelectedCityIds.get(i);
                    }
                }
            }
            else
            {
                selectedCityIDs = "";
                selectedCityIDs = "5780993,5128638,5391959";
            }
            cityForcastUrl = new URL("http://api.openweathermap.org/data/2.5/group?id=" + selectedCityIDs + "&units=imperial&APPID=da65fafb6cb9242168b7724fb5ab75e7");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(!Methods.isOnline(getContext()))
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder
                    .setMessage(R.string.alert_no_network_connectivity)
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
            CityWeather cityWeather = new CityWeather();
            cityWeather.execute(cityForcastUrl);
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWeatherAdapter = new WeatherAdapter(getActivity(), 0, mCityWeatherList, userSelectedCityIds, userSelectedCityNames );
        lv_city_weather = (ListView)view.findViewById(R.id.lv_city_weather);
        lv_city_weather.setAdapter(mWeatherAdapter);
        lv_city_weather.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                onWeatherItemClickListener.onWeatherItemClicked(mCityWeatherList.get(position));

                mCityWeatherList.clear();

            }
        });

        btn_add_city = (Button)view.findViewById(R.id.btn_add_city);
        btn_add_city.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                onAddCityClickListener.onAddCityClicked(userSelectedCityIds, userSelectedCityNames);

                mCityWeatherList.clear();
            }
        });


    }



    public class CityWeather extends AsyncTask<URL, Void, String>{

        private ProgressDialog dialog = new ProgressDialog(getActivity());

        private HttpURLConnection httpUrlConnection = null;
        private BufferedReader bufferedReader = null;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading Weather Data");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(URL... params) {

            if(!Methods.retrieveString(getActivity(), "cityWeatherJsonString").equals("") && MainActivity.loadDataFromSharedPreference)
            {
                MainActivity.loadDataFromSharedPreference = false;
                mWeatherJsonString = Methods.retrieveString(getActivity(), "cityWeatherJsonString");
                loadWeatherData(Methods.retrieveString(getActivity(), "cityWeatherJsonString"));
                return Methods.retrieveString(getActivity(), "cityWeatherJsonString");
            }

            StringBuilder results = new StringBuilder();

            try
            {
                URL url = params[0];
                httpUrlConnection = (HttpURLConnection)url.openConnection();
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                if(httpUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStream inputStream = new BufferedInputStream(httpUrlConnection.getInputStream());
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = bufferedReader.readLine()) != null){
                        results.append(line);
                    }

                }

            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally{
                if(httpUrlConnection != null){
                    httpUrlConnection.disconnect();
                }

                if(bufferedReader != null){
                    try{
                        bufferedReader.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }

            }

            mWeatherJsonString = results.toString();

            loadWeatherData(mWeatherJsonString);

            return mWeatherJsonString;


        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            mWeatherAdapter.notifyDataSetChanged();
        }

        public void loadWeatherData(String jsonResults)
        {


            try {

                userSelectedCityIds.clear();
                userSelectedCityNames.clear();

                JSONObject cityWeatherJsonObject = new JSONObject(jsonResults);
                JSONArray cityWeatherJsonArray = cityWeatherJsonObject.getJSONArray("list");

                for(int i = 0; i < cityWeatherJsonArray.length(); i++)
                {
                    HashMap<String, Object> cityWeatherHashmap = new HashMap<>();

                    cityWeatherHashmap.put("cityName", cityWeatherJsonArray.getJSONObject(i).getString("name"));
                    cityWeatherHashmap.put("currentTemp", cityWeatherJsonArray.getJSONObject(i).getJSONObject("main").getString("temp"));
                    cityWeatherHashmap.put("highTemp", cityWeatherJsonArray.getJSONObject(i).getJSONObject("main").getString("temp_max"));
                    cityWeatherHashmap.put("lowTemp", cityWeatherJsonArray.getJSONObject(i).getJSONObject("main").getString("temp_min"));
                    cityWeatherHashmap.put("precipitation", cityWeatherJsonArray.getJSONObject(i).getJSONObject("clouds").getString("all"));

                    userSelectedCityIds.add(cityWeatherJsonArray.getJSONObject(i).getString("id"));
                    userSelectedCityNames.add(cityWeatherJsonArray.getJSONObject(i).getString("name"));

                    URL iconUrl = new URL("http://openweathermap.org/img/w/"
                            + cityWeatherJsonArray.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon") + ".png");

                    HttpURLConnection httpURLConnection = (HttpURLConnection)iconUrl.openConnection();

                    if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

                        Bitmap bitMapImage = BitmapFactory.decodeStream(inputStream);
                        cityWeatherHashmap.put("icon", bitMapImage);
                    }

                    mCityWeatherList.add(cityWeatherHashmap);

                }

                Methods.saveString(getActivity(), "cityWeatherJsonString", mWeatherJsonString);
                MainActivity.loadDataFromSharedPreference = false;


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof OnWeatherItemClickListener)
        {
            onWeatherItemClickListener = (OnWeatherItemClickListener)context;
        }
        else{
            throw new ClassCastException(context.toString() + "must implement"
                + WeatherFragment.OnWeatherItemClickListener.class.getSimpleName());

        }

        if(context instanceof OnAddCityClickListener)
        {
            onAddCityClickListener = (OnAddCityClickListener) context;
        }
        else{
            throw new ClassCastException(context.toString() + "must implement"
                    + WeatherFragment.OnAddCityClickListener.class.getSimpleName());

        }


    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void updateSelectedIdsArray(ArrayList<String> selectedCityIds, ArrayList<String> selectedCityNames)
    {
        userSelectedCityNames.clear();
        userSelectedCityIds.clear();
        userSelectedCityIds.addAll(selectedCityIds);
        userSelectedCityNames.addAll(selectedCityNames);
    }

    @Override
    public void onResume() {
        super.onResume();
        mWeatherAdapter.notifyDataSetChanged();
    }
}


