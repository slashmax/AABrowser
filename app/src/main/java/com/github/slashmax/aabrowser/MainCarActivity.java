package com.github.slashmax.aabrowser;

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
        this.c().getDecorView().setSystemUiVisibility(
                        SYSTEM_UI_FLAG_FULLSCREEN |
                        SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        SYSTEM_UI_FLAG_IMMERSIVE | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        m_CarController = new CarController();
        m_CarController.onCreate(this);
        m_CarController.InitCarUiController(getCarUiController());

        m_CarWebView = (CarWebView)findViewById(R.id.m_AAWebView);
        m_CarWebView.setAAFrameLayout((CarFrameLayout)findViewById(R.id.m_AAFrameLayout));
        m_CarWebView.setInputManager(new CarInputManager(a()));
        m_CarWebView.onCreate();

        ForegroundService.startForegroundService(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        m_CarWebView.onDestroy();
        m_CarController.onDestroy();
        ForegroundService.stopForegroundService(this);
    }

    @Override
    public void onBackPressed()
    {
        if (m_CarWebView.canGoBack())
            m_CarWebView.goBack();
        else
            super.onBackPressed();
    }
}
