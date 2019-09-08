package com.github.slashmax.aabrowser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

class CarMediaNotificationManager
{
    private static final String TAG = "CarMediaNotificationManager";

    private static final String CHANNEL_ID = "com.github.slashmax.aabrowser.mediachannel";
    private static final int REQUEST_CODE = 500;
    static final int NOTIFICATION_ID = 600;

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

    Notification getNotification(MediaMetadataCompat metadata, PlaybackStateCompat state, MediaSessionCompat.Token token)
    {
        if (metadata == null || state == null || token == null)
            return null;

        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        MediaDescriptionCompat description = metadata.getDescription();
        NotificationCompat.Builder builder = buildNotification(state, token, isPlaying, description);
        return builder.build();
    }

    private NotificationCompat.Builder buildNotification(PlaybackStateCompat state,
                                                         MediaSessionCompat.Token token,
                                                         boolean isPlaying,
                                                         MediaDescriptionCompat description)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(m_CarMediaService, CHANNEL_ID);
        builder.setStyle(
                new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        m_CarMediaService, PlaybackStateCompat.ACTION_STOP)))
                .setColor(ContextCompat.getColor(m_CarMediaService, R.color.notification_bg))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        m_CarMediaService, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

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
        if (m_NotificationManager.getNotificationChannel(CHANNEL_ID) == null)
        {
            CharSequence name = "AA Browser";
            String description = "AA Browser MediaSession";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

            mChannel.setDescription(description);
            mChannel.enableLights(true);

            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            m_NotificationManager.createNotificationChannel(mChannel);
        }
    }

    private PendingIntent createContentIntent()
    {
        Intent openUI = new Intent(m_CarMediaService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(m_CarMediaService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
