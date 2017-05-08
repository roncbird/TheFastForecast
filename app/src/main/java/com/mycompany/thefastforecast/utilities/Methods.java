package com.mycompany.thefastforecast.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bird1 on 5/2/17.
 */

public class Methods {


    public static String parseUSCities(Context context)
    {

        String cityJSONString = loadJsonFile(context);

        String citiesUSJSONString = "";

        Pattern pattern1 = Pattern.compile("\\{([\\s\\S]*?)\\}([\\s\\S]*?)\\}");
        Matcher matcher1 = pattern1.matcher(cityJSONString);

        while(matcher1.find())
        {
            Pattern pattern2 = Pattern.compile("\"country\":([\\s\\S]*?)\"US\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher2 = pattern2.matcher(matcher1.group(0));

            Log.e("matcher1", matcher1.group(0) + "");

            while(matcher2.find()) {
                Log.e("matcher2", matcher1.group(0) + "");

                citiesUSJSONString += matcher1.group(0) + ",";
                Log.e("citiesUSJSONString", citiesUSJSONString);
            }


        }

        return citiesUSJSONString;
    }


    public static String loadJsonFile(Context context) {

        String jsonString = null;
        try {
//            InputStream inputStream = context.getAssets().open("city.list.json");
            InputStream inputStream = context.getAssets().open("us.cities.list.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return jsonString;
    }


    public static ArrayList<HashMap<String, String>> sortCityNamesAndIds(Context context)
    {
        ArrayList<String> unsortedCityNamesList = new ArrayList<>();
        ArrayList<String> sortedCityNamesList = new ArrayList<>();
        ArrayList<String> sortedCityIdList = new ArrayList<>();
        ArrayList<HashMap<String, String>> cityArrayList = new ArrayList<>();
        String cityJSONString;

        cityJSONString = Methods.loadJsonFile(context);

        try {

            JSONArray jsonArray = new JSONArray(cityJSONString);

            for(int i = 0; i < jsonArray.length(); i++)
            {
                unsortedCityNamesList.add(jsonArray.getJSONObject(i).getString("name") + " " + jsonArray.getJSONObject(i).getString("id"));
            }

            Collections.sort(unsortedCityNamesList, String.CASE_INSENSITIVE_ORDER);

            for(int i = 0; i < unsortedCityNamesList.size(); i++)
            {
                String cityNameAndId = unsortedCityNamesList.get(i);
                sortedCityNamesList.add(cityNameAndId.substring(0, cityNameAndId.lastIndexOf(" ")));
                sortedCityIdList.add(cityNameAndId.substring(cityNameAndId.lastIndexOf(" ") + 1, cityNameAndId.length()));

                HashMap<String, String> cityListHashMap = new HashMap<>();
                cityListHashMap.put("cityName", sortedCityNamesList.get(i));
                cityListHashMap.put("id", sortedCityIdList.get(i));
                cityArrayList.add(cityListHashMap);
            }


        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        return cityArrayList;
    }


    public static String formatTemperature(String unformattedTemp)
    {

        String unformattedCurrentTemp;
        unformattedCurrentTemp = unformattedTemp;
        String currentTemp = unformattedCurrentTemp.split("\\.", 2)[0];

        return currentTemp;
    }


    public static void saveString(Context mContext, String key, String value){
        SharedPreferences settings = mContext.getSharedPreferences("forcastBlast", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String retrieveString(Context mContext, String key){
        SharedPreferences settings = mContext.getSharedPreferences("forcastBlast", 0);
        return settings.getString(key, "");
    }

    public static boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}