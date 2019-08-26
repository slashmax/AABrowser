package com.github.slashmax.aabrowser;

import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;

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
        }
    }
}
