package com.birddevstudios.thefastforecast.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.birddevstudios.thefastforecast.R;
import com.birddevstudios.thefastforecast.fragments.CitySelectionFragment;
import com.birddevstudios.thefastforecast.fragments.WeatherDetailFragment;
import com.birddevstudios.thefastforecast.fragments.WeatherFragment;
import com.birddevstudios.thefastforecast.utilities.Methods;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements WeatherFragment.OnWeatherItemClickListener,
        WeatherFragment.OnAddCityClickListener{

    int REQUEST_INTERNET_ACCESS = 1;

    BroadcastReceiver broadcastReceiver;

    private ArrayList<HashMap<String, String>> cityArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityArrayList = Methods.sortCityNamesAndIds(this);

        FragmentTransaction mFragmentManager = getSupportFragmentManager().beginTransaction();
        mFragmentManager.replace(R.id.container, new WeatherFragment(), "WeatherFragment");
        mFragmentManager.commit();

    }


    @Override
    public void onWeatherItemClicked(HashMap<String, Object> cityWeatherHashMap) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        HashMap<String, Object> cityWeatherMap = cityWeatherHashMap;

        WeatherDetailFragment weatherDetailFragment = new WeatherDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("cityWeatherHashMap", cityWeatherMap);
        weatherDetailFragment.setArguments(args);

        fragmentTransaction.replace(R.id.container, weatherDetailFragment, "WeatherDetailFragment").addToBackStack("WeatherFragment").commit();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onAddCityClicked() {

        CitySelectionFragment citySelectionFragment = new CitySelectionFragment();
        Bundle args = new Bundle();
        args.putSerializable("cityArrayList", cityArrayList);
        citySelectionFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, citySelectionFragment, "CitySelectionFragment").addToBackStack("WeatherFragment").commit();


    }

    @Override
    protected void onResume() {
        super.onResume();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().compareTo(ConnectivityManager.CONNECTIVITY_ACTION) == 0) {
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting() == true) {


                    } else {
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
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
                }

            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(broadcastReceiver);
    }
}
