package com.github.slashmax.aabrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;

public class CarMediaService extends MediaBrowserServiceCompat
{
    public static final String LOCAL_INTENT_FILTER  = "CarMediaService.IntentFilter";
    public static final String PLAYBACK_ACTION      = "PlaybackAction";

    private static final String TAG = "CarMediaService";

    private BroadcastReceiver   m_LocalIntentReceiver;
    private MediaSessionCompat  m_MediaSessionCompat;

    @Override
    public void onCreate()
    {
        super.onCreate();

        m_LocalIntentReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (intent != null && intent.getAction() != null && intent.getAction().equals(LOCAL_INTENT_FILTER))
                {
                    if (intent.hasExtra("PlaybackStateCompat"))
                    {
                        PlaybackStateCompat playbackStateCompat = intent.getParcelableExtra("PlaybackStateCompat");
                        if (playbackStateCompat != null && m_MediaSessionCompat != null)
                            m_MediaSessionCompat.setPlaybackState(playbackStateCompat);
                    }
                    if (intent.hasExtra("MediaMetadataCompat"))
                    {
                        MediaMetadataCompat mediaMetadataCompat = intent.getParcelableExtra("MediaMetadataCompat");
                        if (mediaMetadataCompat != null && m_MediaSessionCompat != null)
                            m_MediaSessionCompat.setMetadata(mediaMetadataCompat);
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(m_LocalIntentReceiver, new IntentFilter(LOCAL_INTENT_FILTER));

        m_MediaSessionCompat = new MediaSessionCompat(this, "CarMediaService");
        setSessionToken(m_MediaSessionCompat.getSessionToken());
        m_MediaSessionCompat.setCallback(new MediaSessionCallback());
        m_MediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        m_MediaSessionCompat.setActive(true);

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(ACTION_PLAY | ACTION_PAUSE | ACTION_SKIP_TO_NEXT | ACTION_SKIP_TO_PREVIOUS);
        stateBuilder.setState(STATE_PAUSED, 0, 1, SystemClock.elapsedRealtime());
        m_MediaSessionCompat.setPlaybackState(stateBuilder.build());

        MediaMetadataCompat.Builder metaBuilder = new MediaMetadataCompat.Builder();
        m_MediaSessionCompat.setMetadata(metaBuilder.build());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        m_MediaSessionCompat.release();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(m_LocalIntentReceiver);
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid,
                                 Bundle rootHints)
    {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result)
    {
        result.sendResult(new ArrayList<MediaItem>());
    }

    private boolean broadcastLocalIntent(Intent intent)
    {
        return LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private boolean broadcastPlaybackAction(long action)
    {
        Intent intent = new Intent(CarMediaBrowser.LOCAL_INTENT_FILTER);
        intent.putExtra(PLAYBACK_ACTION, action);
        return broadcastLocalIntent(intent);
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
}
