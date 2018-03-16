package com.github.slashmax.aabrowser;

import android.util.Log;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarActivityService;

public class CarService extends CarActivityService
{
    private static final String TAG = "CarService";

    @Override
    public Class<? extends CarActivity> getCarActivity()
    {
        Log.d(TAG, "getCarActivity");
        return MainCarActivity.class;
    }
}
