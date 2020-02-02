package com.github.slashmax.aabrowser;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import org.adblockplus.libadblockplus.android.AdblockEngine;
import org.adblockplus.libadblockplus.android.AndroidHttpClientResourceWrapper;
import org.adblockplus.libadblockplus.android.settings.AdblockHelper;

import java.util.HashMap;
import java.util.Map;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class CarApplication extends Application
{
    private static final String TAG = "CarApplication";

    @Override
    public void onCreate()
    {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);

        if (!AdblockHelper.get().isInit())
        {
            String basePath = getDir(AdblockEngine.BASE_PATH_DIRECTORY, Context.MODE_PRIVATE).getAbsolutePath();

            Map<String, Integer> map = new HashMap<>();
            map.put(AndroidHttpClientResourceWrapper.EASYLIST, R.raw.easylist);
            map.put(AndroidHttpClientResourceWrapper.ACCEPTABLE_ADS, R.raw.exceptionrules);

            AdblockHelper
                    .get()
                    .init(this, basePath, false, AdblockHelper.PREFERENCE_NAME)
                    .preloadSubscriptions(AdblockHelper.PRELOAD_PREFERENCE_NAME, map) ;
        }
    }
}
