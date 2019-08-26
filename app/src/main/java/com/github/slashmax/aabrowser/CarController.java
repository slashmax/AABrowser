package com.github.slashmax.aabrowser;

import android.content.Context;
import android.graphics.Color;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.media.CarAudioManager;

import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.DayNightStyle;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchItem;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.support.car.media.CarAudioManager.CAR_AUDIO_USAGE_DEFAULT;

public class CarController
{
    private static final String TAG = "CarController";

    private Car m_Car;

    public void onCreate(Context context)
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

    public void onDestroy()
    {
        if (m_Car.isConnected())
            m_Car.disconnect();
    }

    public void InitCarUiController(CarUiController controller)
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
        }
    }
}
