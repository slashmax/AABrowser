package com.github.slashmax.aabrowser;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;

class CarMediaBrowser
{
    static final String LOCAL_INTENT_FILTER   = "CarMediaBrowser.IntentFilter";

    private static final String TAG = "CarMediaService";

    private Context                     m_Context;
    private BroadcastReceiver           m_LocalIntentReceiver;
    private MediaBrowserCompat          m_MediaBrowserCompat;
    private MediaSessionCompat.Callback m_Callback;
    private PlaybackStateCompat.Builder m_StateBuilder;
    private MediaMetadataCompat.Builder m_MetaBuilder;
    private int                         m_State;
    private String                      m_Title;
    private long                        m_Position;
    private long                        m_Duration;

    CarMediaBrowser(Context context)
    {
        m_Context = context;
        m_State = STATE_NONE;
        m_Title = "";
        m_Position = 0;
        m_Duration = 0;

        m_StateBuilder = new PlaybackStateCompat.Builder();
        m_StateBuilder.setActions(ACTION_PLAY | ACTION_PAUSE | ACTION_SKIP_TO_NEXT | ACTION_SKIP_TO_PREVIOUS);
        m_StateBuilder.setState(m_State, m_Position, 1.0f);

        m_MetaBuilder = new MediaMetadataCompat.Builder();
        m_MetaBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, m_Title);
        m_MetaBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, m_Duration);
    }

    void setCallback(MediaSessionCompat.Callback callback)
    {
        m_Callback = callback;
    }

    void onCreate()
    {
        m_LocalIntentReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (intent != null && intent.getAction() != null && intent.getAction().equals(LOCAL_INTENT_FILTER) && m_Callback != null)
                {
                    switch ((int)intent.getLongExtra(CarMediaService.PLAYBACK_ACTION, 0))
                    {
                        case (int)ACTION_PLAY:              m_Callback.onPlay();            break;
                        case (int)ACTION_PAUSE:             m_Callback.onPause();           break;
                        case (int)ACTION_STOP:              m_Callback.onStop();            break;
                        case (int)ACTION_SKIP_TO_PREVIOUS:  m_Callback.onSkipToPrevious();  break;
                        case (int)ACTION_SKIP_TO_NEXT:      m_Callback.onSkipToNext();      break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(m_Context).registerReceiver(m_LocalIntentReceiver, new IntentFilter(LOCAL_INTENT_FILTER));

        m_MediaBrowserCompat = new MediaBrowserCompat(m_Context, new ComponentName(m_Context, CarMediaService.class), new CarCarMediaBrowserCallback(), null);

        connect();
    }

    void onDestroy()
    {
        if (isConnected())
            disconnect();

        LocalBroadcastManager.getInstance(m_Context).unregisterReceiver(m_LocalIntentReceiver);
    }

    private long floatToMs(float time)
    {
        if (Float.isNaN(time))
            return 0;
        else
            return (long)(time * 1000);
    }

    boolean setPlaybackState(int state, float time)
    {
        long pos = floatToMs(time);
        if (m_State == state && m_Position == pos)
            return true;

        m_State = state;
        m_Position = pos;
        return broadcastPlaybackState();
    }

    boolean setPlaybackPosition(float time)
    {
        long pos = floatToMs(time);
        if (m_Position == pos)
            return true;

        m_Position = pos;
        return broadcastPlaybackState();
    }

    boolean setPlaybackTitle(String title)
    {
        if (m_Title.equals(title))
            return true;

        m_Title = title;
        return broadcastMediaMetadata();
    }

    boolean setPlaybackDuration(float duration)
    {
        long dur = floatToMs(duration);
        if (m_Duration == dur)
            return true;

        m_Duration = dur;
        return broadcastMediaMetadata();
    }

    private boolean broadcastPlaybackState()
    {
        m_StateBuilder.setState(m_State, m_Position, 1.0f, SystemClock.elapsedRealtime());
        return broadcastLocalIntent(m_StateBuilder.build());
    }

    private boolean broadcastMediaMetadata()
    {
        m_MetaBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, m_Title);
        m_MetaBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, m_Duration);
        return broadcastLocalIntent(m_MetaBuilder.build());
    }

    private boolean broadcastLocalIntent(PlaybackStateCompat playbackStateCompat)
    {
        Intent intent = new Intent(CarMediaService.LOCAL_INTENT_FILTER);
        intent.putExtra("PlaybackStateCompat", playbackStateCompat);
        return broadcastLocalIntent(intent);
    }

    private boolean broadcastLocalIntent(MediaMetadataCompat mediaMetadataCompat)
    {
        Intent intent = new Intent(CarMediaService.LOCAL_INTENT_FILTER);
        intent.putExtra("MediaMetadataCompat", mediaMetadataCompat);
        return broadcastLocalIntent(intent);
    }

    private boolean broadcastLocalIntent(Intent intent)
    {
        return LocalBroadcastManager.getInstance(m_Context).sendBroadcast(intent);
    }

    private void connect()
    {
        m_MediaBrowserCompat.connect();
    }

    private void disconnect()
    {
        m_MediaBrowserCompat.disconnect();
    }

    private boolean isConnected()
    {
        return m_MediaBrowserCompat.isConnected();
    }

    class CarCarMediaBrowserCallback extends MediaBrowserCompat.ConnectionCallback
    {

        @Override
        public void onConnected()
        {
            super.onConnected();
        }

        @Override
        public void onConnectionSuspended()
        {
            super.onConnectionSuspended();
        }

        @Override
        public void onConnectionFailed()
        {
            super.onConnectionFailed();
        }
    }
}
