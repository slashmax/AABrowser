package com.github.slashmax.aabrowser;

import android.content.Context;
import android.graphics.Color;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.media.CarAudioManager;
import android.util.Log;

import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.DayNightStyle;
import com.google.android.apps.auto.sdk.MenuController;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchController;
import com.google.android.apps.auto.sdk.SearchItem;
import com.google.android.apps.auto.sdk.StatusBarController;

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
        if (m_Car != null)
            m_Car.connect();
    }

    void onDestroy()
    {
        if (m_Car != null)
        {
            if (m_Car.isConnected())
                m_Car.disconnect();
            m_Car = null;
        }
    }

    void InitCarUiController(CarUiController controller)
    {
        if (controller != null)
        {
            StatusBarController statusBarController = controller.getStatusBarController();
            if (statusBarController != null)
            {
                statusBarController.setTitle("");
                statusBarController.hideAppHeader();
                statusBarController.setAppBarAlpha(0.0f);
                statusBarController.setAppBarBackgroundColor(Color.WHITE);
                statusBarController.setDayNightStyle(DayNightStyle.AUTO);
            }

            MenuController menuController = controller.getMenuController();
            if (menuController != null)
                menuController.hideMenuButton();

            SearchController searchController = controller.getSearchController();
            if (searchController != null)
            {
                searchController.setSearchCallback(new SearchCallback()
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
        }
    }

    private void RequestAudioFocus()
    {
        try
        {
            if (m_Car != null)
            {
                CarAudioManager carAM = m_Car.getCarManager(CarAudioManager.class);
                if (carAM != null)
                    carAM.requestAudioFocus(null, carAM.getAudioAttributesForCarUsage(CAR_AUDIO_USAGE_DEFAULT), AUDIOFOCUS_GAIN, 0);
            }
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
            if (m_Car != null)
            {
                CarAudioManager carAM = m_Car.getCarManager(CarAudioManager.class);
                if (carAM != null)
                    carAM.abandonAudioFocus(null, carAM.getAudioAttributesForCarUsage(CAR_AUDIO_USAGE_DEFAULT));
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "AbandonAudioFocus exception : " + e.toString());
        }
    }
}
