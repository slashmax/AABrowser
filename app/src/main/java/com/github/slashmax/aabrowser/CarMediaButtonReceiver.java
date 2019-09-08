package com.github.slashmax.aabrowser;

import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import android.util.Log;

public class CarMediaButtonReceiver extends MediaButtonReceiver
{
    private static final String TAG = "CarMediaButtonReceiver";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            super.onReceive(context, intent);
        }
        catch (Exception e)
        {
            Log.d(TAG, "onReceive exception : " + e.toString());
        }
    }
}
