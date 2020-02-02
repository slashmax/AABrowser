package com.github.slashmax.aabrowser;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON;
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

    private Context                     m_Context;
    private MediaSessionCompat.Callback m_Callback;

    private int                         m_State;
    private String                      m_Title;
    private Bitmap                      m_Icon;
    private long                        m_Position;
    private long                        m_Duration;
    private boolean                     m_Advertisement;

    private MediaBrowserCompat          m_MediaBrowserCompat;
    private MediaControllerCompat       m_MediaControllerCompat;

    void setContext(Context context)
    {
        m_Context = context;
    }

    void setCallback(MediaSessionCompat.Callback callback)
    {
        m_Callback = callback;
    }

    void onCreate()
    {
        m_State = STATE_NONE;
        m_Title = "";
        m_Position = 0;
        m_Duration = 0;
        m_Advertisement = false;

        if (m_Context != null)
            m_MediaBrowserCompat = new MediaBrowserCompat(m_Context,
                new ComponentName("com.github.slashmax.aabrowser.mediaservice", "com.github.slashmax.aabrowser.mediaservice.CarMediaService"),
                new CarCarMediaBrowserCallback(), null);

        if (!isConnected())
            connect();
    }

    void onDestroy()
    {
        setPlaybackState(STATE_NONE, 0.0f);
        if (isConnected())
            disconnect();

        m_Context = null;
        m_Callback = null;
        m_MediaBrowserCompat = null;
        m_MediaControllerCompat = null;
    }

    void setMetadataAdvertisement(boolean advertisement)
    {
        m_Advertisement = advertisement;
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

    boolean setPlaybackIcon(Bitmap icon, boolean force)
    {
        boolean ok = (icon != null);
        ok = ok && ((m_Icon == null) || (icon.getWidth() >= m_Icon.getWidth()));
        ok = ok && ((m_Icon == null) || (icon.getHeight() >= m_Icon.getHeight()));
        if (!ok && !force)
            return false;
        m_Icon = icon;
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
        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();

        builder.setActions(ACTION_PLAY | ACTION_PAUSE | ACTION_STOP | ACTION_SKIP_TO_NEXT | ACTION_SKIP_TO_PREVIOUS);
        builder.setState(m_State, m_Position, 1.0f);

        return sendCustomAction(PLAYBACK_STATE_COMPAT, builder.build());
    }

    private boolean sendMediaMetadata()
    {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, m_Title);
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, m_Duration);
        builder.putBitmap(METADATA_KEY_DISPLAY_ICON, m_Icon);
        if (m_Advertisement)
            builder.putLong(EXTRA_METADATA_ADVERTISEMENT, 1);

        return sendCustomAction(MEDIA_METADATA_COMPAT, builder.build());
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

        if (m_MediaBrowserCompat != null)
            m_MediaBrowserCompat.sendCustomAction(action, extras, null);

        return true;
    }

    private void connect()
    {
        if (m_MediaBrowserCompat != null)
            m_MediaBrowserCompat.connect();
    }

    private void disconnect()
    {
        if (m_MediaBrowserCompat != null)
            m_MediaBrowserCompat.disconnect();
    }

    private boolean isConnected()
    {
        return m_MediaBrowserCompat != null && m_MediaBrowserCompat.isConnected();
    }

    class CarCarMediaBrowserCallback extends MediaBrowserCompat.ConnectionCallback
    {

        @Override
        public void onConnected()
        {
            try
            {
                if (m_MediaBrowserCompat != null && m_Context != null)
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
