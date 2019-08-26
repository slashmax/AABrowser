package com.github.slashmax.aabrowser;

import android.app.Application;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatDelegate;

import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_YES;

public class CarApplication extends Application
{
    private static final String TAG = "CarApplication";

    @Override
    public void onCreate()
    {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }
}
