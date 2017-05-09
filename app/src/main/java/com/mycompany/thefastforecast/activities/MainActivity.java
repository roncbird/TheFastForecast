package com.mycompany.thefastforecast.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.mycompany.thefastforecast.R;
import com.mycompany.thefastforecast.fragments.CitySelectionFragment;
import com.mycompany.thefastforecast.fragments.WeatherDetailFragment;
import com.mycompany.thefastforecast.fragments.WeatherFragment;
import com.mycompany.thefastforecast.utilities.Methods;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements WeatherFragment.OnWeatherItemClickListener,
        WeatherFragment.OnAddCityClickListener, CitySelectionFragment.OnDoneClickListener{

    int REQUEST_INTERNET_ACCESS = 1;

    public static boolean loadDataFromSharedPreference;

    BroadcastReceiver broadcastReceiver;

    private ArrayList<HashMap<String, String>> cityArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadDataFromSharedPreference = true;

        cityArrayList = Methods.sortCityNamesAndIds(this);

        checkPermissions();

        FragmentTransaction mFragmentManager = getSupportFragmentManager().beginTransaction();
        mFragmentManager.replace(R.id.container, new WeatherFragment(), "WeatherFragment");
        mFragmentManager.commit();

    }

    private void checkPermissions() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {


                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.INTERNET)) {


                } else {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.INTERNET},
                            REQUEST_INTERNET_ACCESS);

                }
            }
        }
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
    public void onAddCityClicked(ArrayList<String> selectedCityIds, ArrayList<String> selectedCityNames) {

        if(selectedCityNames.size() > 0 && selectedCityIds.size() > 0)
        {
            CitySelectionFragment citySelectionFragment = new CitySelectionFragment();
            Bundle args = new Bundle();
            args.putStringArrayList("selectedCityNames", selectedCityNames );
            args.putStringArrayList("selectedCityIds", selectedCityIds );
            args.putSerializable("cityArrayList", cityArrayList);
            citySelectionFragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, citySelectionFragment, "CitySelectionFragment").addToBackStack("WeatherFragment").commit();
        }
        else
        {
            CitySelectionFragment citySelectionFragment = new CitySelectionFragment();
            Bundle args = new Bundle();
            args.putSerializable("cityArrayList", cityArrayList);
            citySelectionFragment.setArguments(args);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, citySelectionFragment, "CitySelectionFragment").addToBackStack("WeatherFragment").commit();

        }


    }

    @Override
    public void onDoneClicked(ArrayList<String> selectedCityIds, ArrayList<String> selectedCityNames, ArrayList<String> selectedCityIdsForUrl ) {

        WeatherFragment weatherFragment = (WeatherFragment)getSupportFragmentManager().findFragmentByTag("WeatherFragment");
        weatherFragment.updateSelectedIdsArray(selectedCityIds, selectedCityNames, selectedCityIdsForUrl);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }


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
