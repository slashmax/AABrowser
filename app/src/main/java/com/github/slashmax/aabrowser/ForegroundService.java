package com.github.slashmax.aabrowser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class ForegroundService extends Service
{
    private static final String TAG = "ForegroundService";
    private static final String CHANNEL_ID = "com.github.slashmax.aabrowser";
    private static final int    ONGOING_NOTIFICATION_ID = 1;

    public ForegroundService()
    {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        startNotification();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("STOP"))
            stopSelf();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void startNotification()
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                CharSequence channelName = getString(R.string.notification_channel_name);
                String channelDescription = getString(R.string.notification_channel_description);

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(channelDescription);
                channel.setShowBadge(false);

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
            }

            Intent intent = new Intent(this, getClass());
            intent.setAction("STOP");
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

            Notification.Builder builder;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                builder = new Notification.Builder(this);
            else
                builder = new Notification.Builder(this, CHANNEL_ID);

            Notification notification = builder
                    .setContentTitle(getText(R.string.notification_title))
                    .setContentText(getText(R.string.notification_text))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setShowWhen(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();

            startForeground(ONGOING_NOTIFICATION_ID, notification);
        }
        catch (Exception e)
        {
            Log.d(TAG, "startNotification exception : " + e.toString());
        }
    }

    private void stopNotification()
    {
        try
        {
            stopForeground(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null)
                    notificationManager.deleteNotificationChannel(CHANNEL_ID);
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "stopNotification exception : " + e.toString());
        }
    }

    static void startForegroundService(Context context)
    {
        Intent intentService = new Intent(context, ForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intentService);
        else
            context.startService(intentService);
    }

    static void stopForegroundService(Context context)
    {
        context.stopService(new Intent(context, ForegroundService.class));
    }
}
