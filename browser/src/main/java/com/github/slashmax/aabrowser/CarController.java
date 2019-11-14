package com.github.slashmax.aabrowser;

import android.content.Context;
import android.graphics.Color;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.media.CarAudioManager;
import android.util.Log;

import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.DayNightStyle;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchItem;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.support.car.media.CarAudioManager.CAR_AUDIO_USAGE_DEFAULT;

class CarController
{
    private static final String TAG = "CarController";

    private Car m_Car;

    void onCreate(Context context)
    {
        m_Car = Car.createCar(context, new CarConnectionCallback()
        {
            @Override
            public void onConnected(Car car)
            {
                RequestAudioFocus();
            }

            @Override
            public void onDisconnected(Car car)
            {
                AbandonAudioFocus();
            }
        });
        m_Car.connect();
    }

    void onDestroy()
    {
        if (m_Car.isConnected())
            m_Car.disconnect();
    }

    void InitCarUiController(CarUiController controller)
    {
        controller.getStatusBarController().setTitle("");
        controller.getStatusBarController().hideAppHeader();
        controller.getStatusBarController().setAppBarAlpha(0.0f);
        controller.getStatusBarController().setAppBarBackgroundColor(Color.WHITE);
        controller.getStatusBarController().setDayNightStyle(DayNightStyle.AUTO);
        controller.getMenuController().hideMenuButton();

        controller.getSearchController().setSearchCallback(new SearchCallback()
        {
            @Override
            public void onSearchItemSelected(SearchItem searchItem)
            {
            }

            @Override
            public boolean onSearchSubmitted(String s)
            {
                return true;
            }

            @Override
            public void onSearchTextChanged(String s)
            {
            }
        });
    }

    private void RequestAudioFocus()
    {
        try
        {
            CarAudioManager carAM = m_Car.getCarManager(CarAudioManager.class);
            carAM.requestAudioFocus(null, carAM.getAudioAttributesForCarUsage(CAR_AUDIO_USAGE_DEFAULT), AUDIOFOCUS_GAIN, 0);
        }
        catch (Exception e)
        {
            Log.d(TAG, "RequestAudioFocus exception : " + e.toString());
        }
    }

    private void AbandonAudioFocus()
    {
        try
        {
            CarAudioManager carAM = m_Car.getCarManager(CarAudioManager.class);
            carAM.abandonAudioFocus(null, carAM.getAudioAttributesForCarUsage(CAR_AUDIO_USAGE_DEFAULT));
        }
        catch (Exception e)
        {
            Log.d(TAG, "AbandonAudioFocus exception : " + e.toString());
        }
    }
}
