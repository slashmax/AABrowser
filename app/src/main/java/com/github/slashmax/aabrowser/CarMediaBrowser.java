package com.github.slashmax.aabrowser;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class CarMediaBrowser
{
    public static final String LOCAL_INTENT_FILTER   = "CarMediaBrowser.IntentFilter";

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
        m_StateBuilder = new PlaybackStateCompat.Builder();
        m_State = PlaybackStateCompat.STATE_PAUSED;
        m_Title = "";
        m_Position = 0;
        m_Duration = 0;
        m_StateBuilder.setActions(ACTION_PLAY | ACTION_PAUSE | ACTION_SKIP_TO_NEXT | ACTION_SKIP_TO_PREVIOUS);
        m_StateBuilder.setState(m_State, m_Position * 1000, 1);

        m_MetaBuilder = new MediaMetadataCompat.Builder();
        m_MetaBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, m_Title);
        m_MetaBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, m_Duration * 1000);
    }

    void setCallback(MediaSessionCompat.Callback callback)
    {
        m_Callback = callback;
    }

    public void onCreate()
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

    public void onDestroy()
    {
        if (isConnected())
            disconnect();

        LocalBroadcastManager.getInstance(m_Context).unregisterReceiver(m_LocalIntentReceiver);
    }

    public boolean setPlaybackState(int state)
    {
        if (m_State == state)
            return true;
        m_State = state;
        m_StateBuilder.setState(m_State, m_Position * 1000, 1, SystemClock.elapsedRealtime());
        return setPlaybackState(m_StateBuilder.build());
    }

    public boolean setPlaybackPosition(long pos)
    {
        if (m_Position == pos)
            return true;
        m_Position = pos;
        m_StateBuilder.setState(m_State, m_Position * 1000, 1, SystemClock.elapsedRealtime());
        return setPlaybackState(m_StateBuilder.build());
    }

    public boolean setPlaybackTitle(String title)
    {
        if (title == null || m_Title.equals(title))
            return true;

        m_Title = title;
        m_MetaBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, m_Title);
        m_MetaBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, m_Duration * 1000);
        return setMetadata(m_MetaBuilder.build());
    }

    public boolean setPlaybackDuration(long duration)
    {
        if (m_Duration == duration)
            return true;

        m_Duration = duration;
        m_MetaBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, m_Title);
        m_MetaBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, m_Duration * 1000);
        return setMetadata(m_MetaBuilder.build());
    }

    public boolean setPlaybackState(PlaybackStateCompat state)
    {
        Intent intent = new Intent(CarMediaService.LOCAL_INTENT_FILTER);
        intent.putExtra("PlaybackStateCompat", state);
        return broadcastLocalIntent(intent);
    }

    public boolean setMetadata(MediaMetadataCompat metadata)
    {
        Intent intent = new Intent(CarMediaService.LOCAL_INTENT_FILTER);
        intent.putExtra("MediaMetadataCompat", metadata);
        return broadcastLocalIntent(intent);
    }

    private boolean broadcastLocalIntent(Intent intent)
    {
        return LocalBroadcastManager.getInstance(m_Context).sendBroadcast(intent);
    }

    public void connect()
    {
        m_MediaBrowserCompat.connect();
    }

    public void disconnect()
    {
        m_MediaBrowserCompat.disconnect();
    }

    public boolean isConnected()
    {
        return m_MediaBrowserCompat.isConnected();
    }

    @NonNull
    public ComponentName getServiceComponent()
    {
        return m_MediaBrowserCompat.getServiceComponent();
    }

    @NonNull
    public String getRoot()
    {
        return m_MediaBrowserCompat.getRoot();
    }

    @Nullable
    public Bundle getExtras()
    {
        return m_MediaBrowserCompat.getExtras();
    }

    @NonNull
    public MediaSessionCompat.Token getSessionToken()
    {
        return m_MediaBrowserCompat.getSessionToken();
    }

    public void subscribe(@NonNull String parentId, @NonNull MediaBrowserCompat.SubscriptionCallback callback)
    {
        m_MediaBrowserCompat.subscribe(parentId, callback);
    }

    public void subscribe(@NonNull String parentId, @NonNull Bundle options, @NonNull MediaBrowserCompat.SubscriptionCallback callback)
    {
        m_MediaBrowserCompat.subscribe(parentId, options, callback);
    }

    public void unsubscribe(@NonNull String parentId)
    {
        m_MediaBrowserCompat.unsubscribe(parentId);
    }

    public void unsubscribe(@NonNull String parentId, @NonNull MediaBrowserCompat.SubscriptionCallback callback)
    {
        m_MediaBrowserCompat.unsubscribe(parentId, callback);
    }

    public void getItem(@NonNull String mediaId, @NonNull MediaBrowserCompat.ItemCallback cb)
    {
        m_MediaBrowserCompat.getItem(mediaId, cb);
    }

    public void search(@NonNull String query, Bundle extras, @NonNull MediaBrowserCompat.SearchCallback callback)
    {
        m_MediaBrowserCompat.search(query, extras, callback);
    }

    public void sendCustomAction(@NonNull String action, Bundle extras, @Nullable MediaBrowserCompat.CustomActionCallback callback)
    {
        m_MediaBrowserCompat.sendCustomAction(action, extras, callback);
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
