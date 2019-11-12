package com.github.slashmax.aabrowser;

import android.app.Notification;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;

public class CarMediaService extends MediaBrowserServiceCompat
{
    private static final String TAG = "CarMediaService";

    private static final String PLAYBACK_STATE_COMPAT   = "PlaybackStateCompat";
    private static final String MEDIA_METADATA_COMPAT   = "MediaMetadataCompat";
    private static final String PLAYBACK_ACTION         = "PlaybackAction";

    private CarMediaNotificationManager m_CarMediaNotificationManager;
    private MediaSessionCompat          m_MediaSessionCompat;
    private MediaControllerCompat       m_MediaControllerCompat;

    @Override
    public void onCreate()
    {
        super.onCreate();

        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();
        builder.setActions(ACTION_PLAY | ACTION_PAUSE | ACTION_STOP | ACTION_SKIP_TO_NEXT | ACTION_SKIP_TO_PREVIOUS);
        builder.setState(STATE_PAUSED, 0, 1.0f);

        m_CarMediaNotificationManager = new CarMediaNotificationManager(this);

        m_MediaSessionCompat = new MediaSessionCompat(this, "CarMediaService");
        m_MediaSessionCompat.setCallback(new MediaSessionCallback());
        m_MediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        m_MediaSessionCompat.setActive(true);
        m_MediaSessionCompat.setPlaybackState(builder.build());
        m_MediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder().build());

        m_MediaControllerCompat = m_MediaSessionCompat.getController();

        setSessionToken(m_MediaSessionCompat.getSessionToken());
    }

    @Override
    public void onDestroy()
    {
        m_CarMediaNotificationManager.cancel();
        m_MediaSessionCompat.release();
        super.onDestroy();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints)
    {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId, @NonNull final Result<List<MediaItem>> result)
    {
        result.sendResult(new ArrayList<MediaItem>());
    }

    @Override
    public void onCustomAction(@NonNull String action, Bundle extras, @NonNull Result<Bundle> result)
    {
        if (m_MediaSessionCompat != null && extras != null)
        {
            boolean update = false;
            boolean cancel = false;
            if (action.equals(PLAYBACK_STATE_COMPAT))
            {
                PlaybackStateCompat playbackStateCompat = extras.getParcelable(PLAYBACK_STATE_COMPAT);
                if (playbackStateCompat != null)
                {
                    update = stateChanged(playbackStateCompat);
                    cancel = (playbackStateCompat.getState() == STATE_NONE);
                    m_MediaSessionCompat.setPlaybackState(playbackStateCompat);
                }
            }
            if (action.equals(MEDIA_METADATA_COMPAT))
            {
                MediaMetadataCompat mediaMetadataCompat = extras.getParcelable(MEDIA_METADATA_COMPAT);
                if (mediaMetadataCompat != null)
                {
                    update = metaChanged(mediaMetadataCompat);
                    m_MediaSessionCompat.setMetadata(mediaMetadataCompat);
                }
            }
            if (cancel)
                m_CarMediaNotificationManager.cancel();
            else if (update)
                updateNotification();
        }

        result.sendResult(null);
    }

    private void broadcastPlaybackAction(long action)
    {
        Bundle bundle = new Bundle();
        bundle.putLong(PLAYBACK_ACTION, action);
        broadcastPlaybackAction(PLAYBACK_ACTION, bundle);
    }

    private void broadcastPlaybackAction(String event, Bundle extras)
    {
        if (m_MediaSessionCompat.isActive())
            m_MediaSessionCompat.sendSessionEvent(event, extras);
    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback
    {
        @Override
        public void onPlay()
        {
            broadcastPlaybackAction(ACTION_PLAY);
        }

        @Override
        public void onPause()
        {
            broadcastPlaybackAction(ACTION_PAUSE);
        }

        @Override
        public void onStop()
        {
            broadcastPlaybackAction(ACTION_STOP);
        }

        @Override
        public void onSkipToPrevious()
        {
            broadcastPlaybackAction(ACTION_SKIP_TO_PREVIOUS);
        }

        @Override
        public void onSkipToNext()
        {
            broadcastPlaybackAction(ACTION_SKIP_TO_NEXT);
        }
    }

    public Notification getNotification()
    {
        if (m_CarMediaNotificationManager == null || m_MediaControllerCompat == null)
            return null;

        return m_CarMediaNotificationManager.getNotification(m_MediaControllerCompat.getMetadata(), m_MediaControllerCompat.getPlaybackState(), getSessionToken());
    }
    private void updateNotification()
    {
        Notification notification = getNotification();
        if (notification != null && m_CarMediaNotificationManager != null)
            m_CarMediaNotificationManager.notify(notification);
    }
    boolean stateChanged(PlaybackStateCompat playbackStateCompat)
    {
        if (m_MediaControllerCompat == null || m_MediaControllerCompat.getPlaybackState() == null)
            return false;
        return m_MediaControllerCompat.getPlaybackState().getState() != playbackStateCompat.getState();
    }

    boolean metaChanged(MediaMetadataCompat mediaMetadataCompat)
    {
        if (m_MediaControllerCompat == null || m_MediaControllerCompat.getPlaybackState() == null || m_MediaControllerCompat.getMetadata() == null)
            return false;
        if (m_MediaControllerCompat.getPlaybackState().getState() != STATE_PLAYING)
            return false;
        return m_MediaControllerCompat.getMetadata().getDescription().getTitle() != mediaMetadataCompat.getDescription().getTitle();
    }
}
