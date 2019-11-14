package com.github.slashmax.aabrowser;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class CarApplication extends Application
{
    private static final String TAG = "CarApplication";

    @Override
    public void onCreate()
    {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
    }
}
