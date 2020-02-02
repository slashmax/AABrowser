package com.github.slashmax.aabrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private CarWebView m_CarWebView;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_car_main);

        ForegroundService.startForegroundService(this);

        m_CarWebView = findViewById(R.id.m_AAWebView);
        m_CarWebView.setCarFrameLayout((CarFrameLayout)findViewById(R.id.m_CarFrameLayout));
        m_CarWebView.onCreate();
        m_CarWebView.goLast();
        m_CarWebView.enableAdbSettings();

        requestIgnoreBatteryOptimizations();
    }

    @Override
    protected void onDestroy()
    {
        if (m_CarWebView != null)
        {
            m_CarWebView.onDestroy();
            m_CarWebView = null;
        }

        ForegroundService.stopForegroundService(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        if (m_CarWebView != null && m_CarWebView.onBackPressed())
            return;
        super.onBackPressed();
    }

    void requestIgnoreBatteryOptimizations()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName()))
            {
                @SuppressLint("BatteryLife") Intent intent = new Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1001);
            }
        }
    }
}
