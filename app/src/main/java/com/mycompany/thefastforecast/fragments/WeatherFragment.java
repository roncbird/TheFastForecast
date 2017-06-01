package com.mycompany.thefastforecast.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mycompany.thefastforecast.R;
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

import static com.mycompany.thefastforecast.fragments.Constants.SharedPrefrenceKeys.APP_LOADED_FOR_FIRST_TIME_KEY;
import static com.mycompany.thefastforecast.fragments.Constants.SharedPrefrenceKeys.SELECTED_CITY_IDS_JSON_STRING_KEY;


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
        void onAddCityClicked();
    }

    private ArrayList<HashMap<String, Object>> mCityWeatherList = new ArrayList<>();
    private ArrayList<String> userSelectedCityIds = new ArrayList<>();
    private ListView lv_city_weather;

    private Button btn_add_city;

    private WeatherAdapter mWeatherAdapter;

    private OnWeatherItemClickListener onWeatherItemClickListener;

    private OnAddCityClickListener onAddCityClickListener;

    private String selectedCityIDs = "";

    private boolean sendNetworkRequest = true;

    private URL cityForcastUrl = null;

    private ProgressBar listview_progressbar_footer;

    private int selectedCityIdsCurrentPage = 0;
    private int selectedCityIdsTotalPages = 0;
    private int currentArrayIndex = 0;
    private boolean isLoading = false;
    private boolean lastReached = false;
    public Handler mHandler;

    private View mView;


    public WeatherFragment() {
        // Required empty public constructors
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_weather, container, false);

        // Inflate the layout for this fragment
        return mView;
    }

    @Override
    public void onViewCreated(View view1, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view1, savedInstanceState);

        listview_progressbar_footer = (ProgressBar)view1.inflate(getContext(), R.layout.listview_progressbar_footer, null);

        mWeatherAdapter = new WeatherAdapter(getActivity(), 0, mCityWeatherList);
        lv_city_weather = (ListView)view1.findViewById(R.id.lv_city_weather);
        lv_city_weather.setAdapter(mWeatherAdapter);
        lv_city_weather.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                onWeatherItemClickListener.onWeatherItemClicked(mCityWeatherList.get(position));

                sendNetworkRequest = false;

            }
        });


        if(Methods.retrieveJSONString(getContext(), SELECTED_CITY_IDS_JSON_STRING_KEY) != null)
        {

            try
            {
                JSONArray selectedCityIdsJsonArray = new JSONArray(Methods.retrieveJSONString(getContext(), SELECTED_CITY_IDS_JSON_STRING_KEY));

                if((selectedCityIdsJsonArray.length() % 10) == 0)
                {
                    selectedCityIdsTotalPages = selectedCityIdsJsonArray.length() / 10;
                }
                else if( selectedCityIdsJsonArray.length() < 10)
                {
                    selectedCityIdsTotalPages = 1;
                }
                else if(selectedCityIdsJsonArray.length() > 10 && (selectedCityIdsJsonArray.length() % 10) != 0)
                {
                    selectedCityIdsTotalPages = selectedCityIdsJsonArray.length() / 10 + 1;
                }

                int numberOfCitiesToLoad = selectedCityIdsJsonArray.length() > 10 ? 10 : selectedCityIdsJsonArray.length();

                if (selectedCityIdsJsonArray != null)
                {
                    userSelectedCityIds.clear();

                    for (int i = 0; i < numberOfCitiesToLoad; i++)
                    {
                        userSelectedCityIds.add(selectedCityIdsJsonArray.getString(i));
                        currentArrayIndex = i;
                    }

                    selectedCityIdsCurrentPage = 1;

                }


            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }

        if(sendNetworkRequest)
        {

            if (!Methods.isOnline(getContext()))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder
                        .setMessage(R.string.alert_no_network_connectivity)
                        .setCancelable(false)
                        .setPositiveButton("Dismiss", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {

                                dialog.dismiss();

                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            else
            {

                if(Methods.retrieveBoolean(getContext(), APP_LOADED_FOR_FIRST_TIME_KEY))
                {
                    selectedCityIDs = "";
                    selectedCityIDs = "5780993,5128638,5391959";
                    try {
                        cityForcastUrl = new URL("http://api.openweathermap.org/data/2.5/group?id=" + selectedCityIDs + "&units=imperial&APPID=da65fafb6cb9242168b7724fb5ab75e7");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    userSelectedCityIds.add("5780993");
                    userSelectedCityIds.add("5128638");
                    userSelectedCityIds.add("5391959");

                    callCityWeatherTask();
                }
                else
                {

                    try {

                        if (userSelectedCityIds.size() > 0)
                        {

                            selectedCityIDs = "";

                            for (int i = 0; i < userSelectedCityIds.size(); i++)
                            {
                                if (i < userSelectedCityIds.size() - 1)
                                {
                                    selectedCityIDs += userSelectedCityIds.get(i) + ",";
                                }
                                else
                                {
                                    selectedCityIDs += userSelectedCityIds.get(i);
                                }
                            }

                            cityForcastUrl = new URL("http://api.openweathermap.org/data/2.5/group?id=" + selectedCityIDs + "&units=imperial&APPID=da65fafb6cb9242168b7724fb5ab75e7");
                        }
                        else
                        {
                            cityForcastUrl = null;
                        }


                    }
                    catch (MalformedURLException e)
                    {
                        e.printStackTrace();
                    }


                    callCityWeatherTask();

                }


            }


        }
        else
        {
            sendNetworkRequest = true;
        }


        btn_add_city = (Button)view1.findViewById(R.id.btn_add_city);
        btn_add_city.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                onAddCityClickListener.onAddCityClicked();
            }
        });


    }

    public void loadMoreCities(){

        String url = "";

        int numberOfCitiesToLoad = 0;

        try {
            JSONArray selectedCityIdsJsonArray = new JSONArray(Methods.retrieveJSONString(getContext(), SELECTED_CITY_IDS_JSON_STRING_KEY));

            if((currentArrayIndex + 10) <= selectedCityIdsJsonArray.length() - 1)
            {
                numberOfCitiesToLoad = 10;
            }
            else if ((currentArrayIndex + 10) > selectedCityIdsJsonArray.length() - 1)
            {
                numberOfCitiesToLoad = selectedCityIdsJsonArray.length() - (currentArrayIndex + 1);
            }

            if (selectedCityIdsJsonArray != null)
            {
                userSelectedCityIds.clear();

                for (int i = 0; i < numberOfCitiesToLoad; i++)
                {
                    userSelectedCityIds.add(selectedCityIdsJsonArray.getString(++currentArrayIndex));
                }


            }

            if (userSelectedCityIds.size() > 0)
            {

                selectedCityIDs = "";

                for (int i = 0; i < userSelectedCityIds.size(); i++)
                {
                    if (i < userSelectedCityIds.size() - 1)
                    {
                        selectedCityIDs += userSelectedCityIds.get(i) + ",";
                    }
                    else
                    {
                        selectedCityIDs += userSelectedCityIds.get(i);
                    }
                }

                cityForcastUrl = new URL("http://api.openweathermap.org/data/2.5/group?id=" + selectedCityIDs + "&units=imperial&APPID=da65fafb6cb9242168b7724fb5ab75e7");
            }
            else
            {
                cityForcastUrl = null;
            }

            callCityWeatherTask();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }


    private void callCityWeatherTask()
    {
        CityWeather cityWeather = new CityWeather();
        cityWeather.execute(cityForcastUrl);
    }



    public class CityWeather extends AsyncTask<URL, Void, ArrayList<HashMap<String,Object>>>{

        private ProgressDialog dialog = new ProgressDialog(getActivity());

        private HttpURLConnection httpUrlConnection = null;
        private BufferedReader bufferedReader = null;

        ArrayList<HashMap<String, Object>> cityWeatherList = new ArrayList<>();

        @Override
        protected void onPreExecute() {

            if(!isLoading)
            {
                dialog.setMessage("Loading Weather Data");
                dialog.show();
            }
        }

        @Override
        protected ArrayList<HashMap<String,Object>> doInBackground(URL... params) {


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

            if(Methods.retrieveBoolean(getContext(), APP_LOADED_FOR_FIRST_TIME_KEY))
            {
                cityWeatherList = loadWeatherData(results.toString());

                JSONArray jsonArray = new JSONArray();

                for(int i = 0; i < userSelectedCityIds.size(); i++)
                {
                    jsonArray.put(userSelectedCityIds.get(i));
                }

                Methods.saveJSONString(getContext(), SELECTED_CITY_IDS_JSON_STRING_KEY, jsonArray.toString());
                Methods.saveBoolean(getContext(), APP_LOADED_FOR_FIRST_TIME_KEY, false);
            }
            else
            {
              cityWeatherList = loadWeatherData(results.toString());
            }

            return cityWeatherList;


        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String,Object>> result){
            super.onPostExecute(result);

            if(isLoading)
            {
                lv_city_weather.removeFooterView(listview_progressbar_footer);
                mCityWeatherList.addAll(result);
                mWeatherAdapter.notifyDataSetChanged();
                isLoading = false;
            }
            else
            {
                mCityWeatherList.clear();
                mCityWeatherList.addAll(result);
                mWeatherAdapter.notifyDataSetChanged();

                lv_city_weather.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                        if((firstVisibleItem + visibleItemCount) == totalItemCount && isLoading == false
                                && selectedCityIdsTotalPages > 1 && selectedCityIdsCurrentPage <= selectedCityIdsTotalPages){

                            selectedCityIdsCurrentPage++;
                            isLoading = true;

                            Log.e("onScrollIf", "entered");

                            if (lastReached == false) {

                                lv_city_weather.addFooterView(listview_progressbar_footer);
                                loadMoreCities();
                                mWeatherAdapter.notifyDataSetChanged();

                            }
                        }

                    }
                });

            }


            if (dialog.isShowing()) {
                dialog.dismiss();
            }


        }


        public ArrayList<HashMap<String, Object>> loadWeatherData(String jsonResults)
        {

            ArrayList<HashMap<String, Object>> cityWeatherList = new ArrayList<>();

            try {


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

                    URL iconUrl = new URL("http://openweathermap.org/img/w/"
                            + cityWeatherJsonArray.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon") + ".png");

                    HttpURLConnection httpURLConnection = (HttpURLConnection)iconUrl.openConnection();

                    if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

                        Bitmap bitMapImage = BitmapFactory.decodeStream(inputStream);
                        cityWeatherHashmap.put("icon", bitMapImage);
                    }

                    cityWeatherList.add(cityWeatherHashmap);

                }



            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return cityWeatherList;

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

    @Override
    public void onResume() {
        super.onResume();
    }
}


