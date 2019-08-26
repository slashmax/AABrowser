package com.github.slashmax.aabrowser;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
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

        m_CarWebView = findViewById(R.id.m_AAWebView);
        m_CarWebView.setAAFrameLayout((CarFrameLayout)findViewById(R.id.m_AAFrameLayout));
        m_CarWebView.onCreate();

        requestIgnoreBatteryOptimizations();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        m_CarWebView.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        if (m_CarWebView.canGoBack())
            m_CarWebView.goBack();
        else
            super.onBackPressed();
    }

    void requestIgnoreBatteryOptimizations()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName()))
            {
                Intent intent = new Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1001);
            }
        }
    }
}
