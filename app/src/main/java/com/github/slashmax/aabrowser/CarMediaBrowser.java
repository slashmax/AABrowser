package com.github.slashmax.aabrowser;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;

class CarMediaBrowser
{
    private static final String TAG = "CarMediaBrowser";

    private static final String PLAYBACK_STATE_COMPAT           = "PlaybackStateCompat";
    private static final String MEDIA_METADATA_COMPAT           = "MediaMetadataCompat";
    private static final String PLAYBACK_ACTION                 = "PlaybackAction";
    private static final String EXTRA_METADATA_ADVERTISEMENT    = "android.media.metadata.ADVERTISEMENT";

    private int                         m_State;
    private String                      m_Title;
    private long                        m_Position;
    private long                        m_Duration;

    private PlaybackStateCompat.Builder m_StateBuilder;
    private MediaMetadataCompat.Builder m_MetaBuilder;

    private Context                     m_Context;
    private MediaSessionCompat.Callback m_Callback;
    private MediaBrowserCompat          m_MediaBrowserCompat;
    private MediaControllerCompat       m_MediaControllerCompat;

    CarMediaBrowser(Context context, MediaSessionCompat.Callback callback)
    {
        m_State = STATE_NONE;
        m_Title = "";
        m_Position = 0;
        m_Duration = 0;

        m_StateBuilder = new PlaybackStateCompat.Builder();
        m_StateBuilder.setActions(ACTION_PLAY | ACTION_PAUSE | ACTION_STOP | ACTION_SKIP_TO_NEXT | ACTION_SKIP_TO_PREVIOUS);
        m_StateBuilder.setState(m_State, m_Position, 1.0f);

        m_MetaBuilder = new MediaMetadataCompat.Builder();
        m_MetaBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, m_Title);
        m_MetaBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, m_Duration);

        m_Context = context;
        m_Callback = callback;

        m_MediaBrowserCompat = new MediaBrowserCompat(m_Context, new ComponentName(m_Context, CarMediaService.class), new CarCarMediaBrowserCallback(), null);
    }

    void setMetadataAdvertisement(long val)
    {
        if (m_MetaBuilder != null)
            m_MetaBuilder.putLong(EXTRA_METADATA_ADVERTISEMENT, val);
    }

    void onCreate()
    {
        if (!isConnected())
            connect();
    }

    void onDestroy()
    {
        setPlaybackState(STATE_NONE, 0.0f);
        if (isConnected())
            disconnect();
    }

    private long floatToMs(float time)
    {
        return (Float.isNaN(time) ? 0 : (long)(time * 1000));
    }

    boolean setPlaybackState(int state, float time)
    {
        long pos = floatToMs(time);
        if (m_State == state && m_Position == pos)
            return true;

        m_State = state;
        m_Position = pos;
        return sendPlaybackState();
    }

    boolean setPlaybackPosition(float time)
    {
        long pos = floatToMs(time);
        if (m_Position == pos)
            return true;

        m_Position = pos;
        return true;
    }

    boolean setPlaybackTitle(String title)
    {
        if (m_Title.equals(title))
            return true;

        m_Title = title;
        return sendMediaMetadata();
    }

    boolean setPlaybackDuration(float duration)
    {
        long dur = floatToMs(duration);
        if (m_Duration == dur)
            return true;

        m_Duration = dur;
        return sendMediaMetadata();
    }

    private boolean sendPlaybackState()
    {
        m_StateBuilder.setState(m_State, m_Position, 1.0f, SystemClock.elapsedRealtime());
        return sendCustomAction(PLAYBACK_STATE_COMPAT, m_StateBuilder.build());
    }

    private boolean sendMediaMetadata()
    {
        m_MetaBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, m_Title);
        m_MetaBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, m_Duration);
        return sendCustomAction(MEDIA_METADATA_COMPAT, m_MetaBuilder.build());
    }

    private boolean sendCustomAction(String action, Parcelable value)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(action, value);
        return sendCustomAction(action, bundle);
    }

    private boolean sendCustomAction(String action, Bundle extras)
    {
        if (!isConnected())
            return false;
        m_MediaBrowserCompat.sendCustomAction(action, extras, null);
        return true;
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
            try
            {
                if (m_MediaBrowserCompat != null)
                {
                    m_MediaControllerCompat = new MediaControllerCompat(m_Context, m_MediaBrowserCompat.getSessionToken());
                    m_MediaControllerCompat.registerCallback(new MediaControllerCompat.Callback()
                    {
                        @Override
                        public void onSessionEvent(String event, Bundle extras)
                        {
                            if (event != null && event.equals(PLAYBACK_ACTION) && extras != null && m_Callback != null)
                            {
                                switch ((int)extras.getLong(PLAYBACK_ACTION))
                                {
                                    case (int)ACTION_PLAY:              m_Callback.onPlay();            break;
                                    case (int)ACTION_PAUSE:             m_Callback.onPause();           break;
                                    case (int)ACTION_STOP:              m_Callback.onStop();            break;
                                    case (int)ACTION_SKIP_TO_PREVIOUS:  m_Callback.onSkipToPrevious();  break;
                                    case (int)ACTION_SKIP_TO_NEXT:      m_Callback.onSkipToNext();      break;
                                }
                            }
                            super.onSessionEvent(event, extras);
                        }
                    });
                }
            }
            catch (Exception e)
            {
                Log.d(TAG, e.toString());
            }

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
