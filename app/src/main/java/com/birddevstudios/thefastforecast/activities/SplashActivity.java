package com.birddevstudios.thefastforecast.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.birddevstudios.thefastforecast.R;
import com.birddevstudios.thefastforecast.utilities.FontCache;

public class SplashActivity extends AppCompatActivity {

    private RelativeLayout rl_splash_logo;

    private TextView tv_splash_cloud;
    private TextView tv_splash_app_name;

    private Typeface fontAwesome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        fontAwesome = FontCache.get("fontawesome-webfont.ttf", this);

        tv_splash_cloud = (TextView)findViewById(R.id.tv_splash_cloud);
        tv_splash_cloud.setTypeface(fontAwesome);

//        tv_splash_app_name = (TextView)findViewById(R.id.tv_splash_app_name);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);


    }
}
