package com.github.slashmax.aabrowser.mediaservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import androidx.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import static android.app.NotificationManager.IMPORTANCE_LOW;

class CarMediaNotificationManager
{
    private static final String TAG = "CarMediaNotificationManager";

    private static final String CHANNEL_ID = "com.github.slashmax.aabrowser.mediachannel";
    private static final int REQUEST_CODE = 500;
    private static final int NOTIFICATION_ID = 600;

    private final CarMediaService           m_CarMediaService;
    private final NotificationCompat.Action m_PlayAction;
    private final NotificationCompat.Action m_PauseAction;
    private final NotificationCompat.Action m_NextAction;
    private final NotificationCompat.Action m_PrevAction;
    private final NotificationManager       m_NotificationManager;

    CarMediaNotificationManager(CarMediaService carMediaService)
    {
        m_CarMediaService = carMediaService;

        m_NotificationManager = (NotificationManager)m_CarMediaService.getSystemService(Context.NOTIFICATION_SERVICE);

        m_PlayAction =
                new NotificationCompat.Action(
                        R.drawable.ic_play_arrow_black_24dp,
                        m_CarMediaService.getString(R.string.label_play),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                m_CarMediaService, PlaybackStateCompat.ACTION_PLAY));
        m_PauseAction =
                new NotificationCompat.Action(
                        R.drawable.ic_pause_black_24dp,
                        m_CarMediaService.getString(R.string.label_pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                m_CarMediaService, PlaybackStateCompat.ACTION_PAUSE));
        m_PrevAction =
                new NotificationCompat.Action(
                        R.drawable.ic_skip_previous_black_24dp,
                        m_CarMediaService.getString(R.string.label_previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                m_CarMediaService, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        m_NextAction =
                new NotificationCompat.Action(
                        R.drawable.ic_skip_next_black_24dp,
                        m_CarMediaService.getString(R.string.label_next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                m_CarMediaService, PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        if (m_NotificationManager != null)
            m_NotificationManager.cancel(NOTIFICATION_ID);
    }

    void notify(Notification notification)
    {
        if (m_NotificationManager != null)
            m_NotificationManager.notify(NOTIFICATION_ID, notification);
    }

    void cancel()
    {
        if (m_NotificationManager != null)
            m_NotificationManager.cancel(NOTIFICATION_ID);
    }

    Notification getNotification(MediaMetadataCompat metadata, PlaybackStateCompat state, MediaSessionCompat.Token token)
    {
        if (metadata == null || state == null || token == null)
            return null;

        MediaDescriptionCompat description = metadata.getDescription();
        NotificationCompat.Builder builder = buildNotification(state, token, description);
        return builder.build();
    }

    private NotificationCompat.Builder buildNotification(PlaybackStateCompat state,
                                                         MediaSessionCompat.Token token,
                                                         MediaDescriptionCompat description)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel();

        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(m_CarMediaService, CHANNEL_ID);
        builder.setStyle(
                new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        m_CarMediaService, PlaybackStateCompat.ACTION_STOP)));

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setContentIntent(createContentIntent())
                .setOngoing(isPlaying);

        builder.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                m_CarMediaService, PlaybackStateCompat.ACTION_STOP));

        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0)
            builder.addAction(m_PrevAction);

        builder.addAction(isPlaying ? m_PauseAction : m_PlayAction);

        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0)
            builder.addAction(m_NextAction);

        return builder;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel()
    {
        if (m_NotificationManager != null && m_NotificationManager.getNotificationChannel(CHANNEL_ID) == null)
        {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "AA Browser", IMPORTANCE_LOW);
            channel.setDescription("AA Browser MediaSession");
            m_NotificationManager.createNotificationChannel(channel);
        }
    }

    private PendingIntent createContentIntent()
    {
        Intent openUI = new Intent();
        openUI.setClassName("com.github.slashmax.aabrowser", "com.github.slashmax.aabrowser.MainActivity");
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(m_CarMediaService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
