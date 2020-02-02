package com.github.slashmax.aabrowser;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.apps.auto.sdk.CarActivity;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

public class MainCarActivity extends CarActivity
{
    private static final String TAG = "MainCarActivity";

    private CarController   m_CarController;
    private CarWebView      m_CarWebView;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_car_main);

        setIgnoreConfigChanges(0xFFFF);
        c().getDecorView().setSystemUiVisibility(
                        SYSTEM_UI_FLAG_FULLSCREEN |
                        SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        SYSTEM_UI_FLAG_IMMERSIVE |
                        SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        m_CarController = new CarController();
        m_CarController.onCreate(this);
        m_CarController.InitCarUiController(getCarUiController());

        ForegroundService.startForegroundService(this);

        m_CarWebView = (CarWebView)findViewById(R.id.m_AAWebView);
        m_CarWebView.setInputManager(new CarInputManager(a()));
        m_CarWebView.setCarFrameLayout((CarFrameLayout)findViewById(R.id.m_CarFrameLayout));
        m_CarWebView.onCreate();
        m_CarWebView.goLast();
    }

    @Override
    public void onDestroy()
    {
        if (m_CarWebView != null)
        {
            m_CarWebView.onDestroy();
            m_CarWebView = null;
        }

        if (m_CarController != null)
        {
            m_CarController.onDestroy();
            m_CarController = null;
        }

        ForegroundService.stopForegroundService(this);
        super.onDestroy();
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        setMetadataAdvertisement(false);
    }

    @Override
    public void onResume()
    {
        setMetadataAdvertisement(true);
        super.onResume();
    }

    @Override
    public void onBackPressed()
    {
        if (m_CarWebView != null && m_CarWebView.onBackPressed())
            return;
        super.onBackPressed();
    }

    void setMetadataAdvertisement(boolean advertisement)
    {
        if (m_CarWebView != null)
            m_CarWebView.setMetadataAdvertisement(advertisement);
    }
}
