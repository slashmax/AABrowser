package com.github.slashmax.aabrowser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.graphics.Bitmap.Config.ALPHA_8;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE;

public class CarWebView
        extends
        NestedScrollWebView
        implements
        Runnable,
        View.OnClickListener,
        TextView.OnEditorActionListener,
        SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "CarWebView";

    private static final String DEFAULT_HOME = "https://www.google.com";
    private static final String DEFAULT_SEARCH = "https://www.google.com/search?q=";

    private boolean                     m_Moved;

    private CarInputManager             m_CarInputManager;

    private CarFrameLayout              m_CarFrameLayout;
    private AppBarLayout                m_AppBarLayout;
    private SwipeRefreshLayout          m_SwipeRefreshLayout;
    private ViewGroup                   m_CustomViewGroup;

    private View                        m_CustomView;

    private ImageButton                 m_BackButton;
    private ImageButton                 m_ForwardButton;
    private ImageButton                 m_ReloadButton;
    private ImageButton                 m_HomeButton;
    private ImageButton                 m_DesktopButton;
    private ImageButton                 m_LogButton;
    private CarEditText                 m_UrlEditText;
    private long                        m_MediaPrevTimestamp;

    private WebChromeClient             m_WebChromeClient;
    private WebViewClient               m_WebViewClient;
    private Bitmap                      m_DefaultVideoPoster;

    private String                      m_HomeUrl;
    private String                      m_LastUrl;

    private String                      m_UserAgentString;
    private String                      m_DesktopUserAgentString;
    private boolean                     m_DesktopUserAgent;

    private Runnable                    m_AppBarLayoutHider;

    private CarMediaBrowser             m_CarMediaBrowser;

    private String                      m_JsScript;

    private static String               m_Log;

    public CarWebView(Context context)
    {
        super(context);
    }

    public CarWebView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CarWebView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public void setAAFrameLayout(CarFrameLayout frameLayout)
    {
        m_CarFrameLayout = frameLayout;
    }

    public void setInputManager(CarInputManager inputManager)
    {
        m_CarInputManager = inputManager;
    }

    public void setMetadataAdvertisement(long val)
    {
        if (m_CarMediaBrowser != null)
            m_CarMediaBrowser.setMetadataAdvertisement(val);
    }

    @SuppressLint("setJavaScriptEnabled")
    public void onCreate()
    {
        m_DefaultVideoPoster = Bitmap.createBitmap(1, 1, ALPHA_8);
        m_Log = "";

        InitWebChromeClient();
        InitWebViewClient();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            getSettings().setOffscreenPreRaster(true);

//        setWebContentsDebuggingEnabled(true);
        getSettings().setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getSettings().setMixedContentMode(MIXED_CONTENT_COMPATIBILITY_MODE);

        getSettings().setUseWideViewPort(true);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setMediaPlaybackRequiresUserGesture(false);

        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setRendererPriorityPolicy(RENDERER_PRIORITY_IMPORTANT, false);

        setWebChromeClient(m_WebChromeClient);
        setWebViewClient(m_WebViewClient);

        m_UserAgentString = getSettings().getUserAgentString();
        m_DesktopUserAgentString = BuildDesktopUserAgentString(m_UserAgentString);

        Log(TAG, m_UserAgentString);
        Log(TAG, m_DesktopUserAgentString);

        if (m_CarFrameLayout != null)
        {
            m_AppBarLayout = m_CarFrameLayout.findViewById(R.id.m_AppBarLayout);
            m_SwipeRefreshLayout = m_CarFrameLayout.findViewById(R.id.m_SwipeRefreshLayout);
            m_CustomViewGroup = m_CarFrameLayout.findViewById(R.id.m_CustomViewGroup);
        }

        if (m_AppBarLayout != null)
        {
            m_BackButton = m_AppBarLayout.findViewById(R.id.m_BackButton);
            m_ForwardButton = m_AppBarLayout.findViewById(R.id.m_ForwardButton);
            m_ReloadButton = m_AppBarLayout.findViewById(R.id.m_ReloadButton);
            m_HomeButton = m_AppBarLayout.findViewById(R.id.m_HomeButton);
            m_DesktopButton = m_AppBarLayout.findViewById(R.id.m_DesktopButton);
            m_LogButton = m_AppBarLayout.findViewById(R.id.m_LogButton);

            if (m_BackButton != null) m_BackButton.setOnClickListener(this);
            if (m_ForwardButton != null) m_ForwardButton.setOnClickListener(this);
            if (m_ReloadButton != null) m_ReloadButton.setOnClickListener(this);
            if (m_HomeButton != null) m_HomeButton.setOnClickListener(this);
            if (m_DesktopButton != null) m_DesktopButton.setOnClickListener(this);
            if (m_LogButton != null) m_LogButton.setOnClickListener(this);

            m_UrlEditText = m_AppBarLayout.findViewById(R.id.m_UrlEditText);
            if (m_UrlEditText != null)
            {
                m_UrlEditText.setInputManager(m_CarInputManager);
                m_UrlEditText.setOnEditorActionListener(this);
            }
        }

        if (m_SwipeRefreshLayout != null)
            m_SwipeRefreshLayout.setOnRefreshListener(this);

        m_AppBarLayoutHider = new Runnable()
        {
            @Override
            public void run()
            {
                if (m_AppBarLayout != null && hasFocus())
                    m_AppBarLayout.setExpanded(false, true);
            }
        };

        m_CarMediaBrowser = new CarMediaBrowser(getContext(), new MediaSessionCallback());
        m_CarMediaBrowser.onCreate();

        addJavascriptInterface(new JavaScriptMediaCallbacks(), "m_JavaScriptMediaCallbacks");
        m_JsScript = readJavaScript(R.raw.media_functions);
        mediaFunctions();

        LoadSharedPreferences();
        goLast();
    }

    public void onDestroy()
    {
        SaveSharedPreferences();
        m_CarMediaBrowser.onDestroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        removeHideAppBar();
        removeCheckIsTextEditor();

        int actionMasked = event.getActionMasked();

        if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL)
            postHideAppBar();

        if (actionMasked == MotionEvent.ACTION_DOWN)
            m_Moved = false;

        if (actionMasked == MotionEvent.ACTION_MOVE)
            m_Moved = true;

        if (actionMasked == MotionEvent.ACTION_UP && !m_Moved)
            postCheckIsTextEditor();

        return super.onTouchEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect)
    {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused)
        {
            removeHideAppBar();
            removeCheckIsTextEditor();
            stopInput();
        }
    }

    @Override
    public void clearFocus()
    {
        if (isInputActive())
            return;
        super.clearFocus();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.m_BackButton:     goBack();break;
            case R.id.m_ForwardButton:  goForward();break;
            case R.id.m_ReloadButton:   reload();break;
            case R.id.m_HomeButton:     goHome();break;
            case R.id.m_DesktopButton:  toggleDesktopMode();break;
            case R.id.m_LogButton:      showLog();break;
        }
        refreshAppBar();
        removeHideAppBar();
        stopInput();
    }

    public void goUrl(String url)
    {
        if (!url.startsWith("https://"))
            url = "https://" + url;
        loadUrl(url);
        removeHideAppBar();
    }

    public void goHome()
    {
        if (m_HomeUrl != null && !m_HomeUrl.isEmpty())
            goUrl(m_HomeUrl);
    }

    public void goLast()
    {
        if (m_LastUrl != null && !m_LastUrl.isEmpty())
            goUrl(m_LastUrl);
        else
            goHome();
    }

    public void doSearch(String query)
    {
        goUrl(DEFAULT_SEARCH + query);
    }

    String BuildDesktopUserAgentString(String agent)
    {
        return agent.replace("Android", "").replace("Mobile", "").replace("wv", "");
    }

    public void showLog()
    {
        loadData(m_Log, "text/html", "");
    }

    public static void Log(String tag, String msg)
    {
        Log.d(tag, msg);
        m_Log = m_Log + tag + ": " + msg + "<br>\n";
    }

    public void toggleDesktopMode()
    {
        SetDesktopMode(!m_DesktopUserAgent);
        reload();
    }

    public void SetDesktopMode(boolean desktopMode)
    {
        m_DesktopUserAgent = desktopMode;
        if (m_DesktopUserAgent)
            getSettings().setUserAgentString(m_DesktopUserAgentString);
        else
            getSettings().setUserAgentString(m_UserAgentString);
    }

    private void LoadSharedPreferences()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        m_HomeUrl = sharedPref.getString("m_HomeUrl", DEFAULT_HOME);
        m_LastUrl = sharedPref.getString("m_LastUrl", DEFAULT_HOME);
        m_DesktopUserAgent = sharedPref.getBoolean("m_DesktopUserAgent", true);
        SetDesktopMode(m_DesktopUserAgent);
    }

    private void SaveSharedPreferences()
    {
        m_LastUrl = getUrl();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("m_HomeUrl", m_HomeUrl);
        editor.putString("m_LastUrl", m_LastUrl);
        editor.putBoolean("m_DesktopUserAgent", m_DesktopUserAgent);
        editor.apply();
    }

    private void InitWebChromeClient()
    {
        m_WebChromeClient = new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100)
                    mediaFunctions();
            }

            @Override
            public void onReceivedTitle(WebView view, String title)
            {
                super.onReceivedTitle(view, title);

                if (m_LastUrl == null || !m_LastUrl.equals(getUrl()))
                    SaveSharedPreferences();

                m_CarMediaBrowser.setPlaybackTitle(title);
                refreshAppBar();
                mediaFunctions();
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon)
            {
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback)
            {
                m_CustomView = view;
                if (m_CustomViewGroup != null && m_CustomView != null)
                {
                    m_CustomViewGroup.addView(m_CustomView);
                    m_CustomViewGroup.bringToFront();
                    m_CustomViewGroup.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onHideCustomView()
            {
                if (m_CustomView != null)
                {
                    if (m_CustomViewGroup != null)
                    {
                        m_CustomViewGroup.setVisibility(INVISIBLE);
                        m_CustomViewGroup.removeView(m_CustomView);
                        m_CustomViewGroup.removeAllViews();
                    }
                    m_CustomView = null;
                }
            }

            @Override
            public Bitmap getDefaultVideoPoster()
            {
                return m_DefaultVideoPoster;
            }
        };
    }

    private void InitWebViewClient()
    {
        m_WebViewClient = new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                refreshAppBar();
                removeHideAppBar();
                if (m_SwipeRefreshLayout != null)
                    m_SwipeRefreshLayout.setRefreshing(true);

                mediaFunctions();
                requestFocus();
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                refreshAppBar();
                postHideAppBar();
                if (m_SwipeRefreshLayout != null)
                    m_SwipeRefreshLayout.setRefreshing(false);

                mediaFunctions();
                requestFocus();
            }
        };
    }

    private void postHideAppBar()
    {
        postDelayed(m_AppBarLayoutHider,5000);
    }

    private void removeHideAppBar()
    {
        removeCallbacks(m_AppBarLayoutHider);
    }

    private void postCheckIsTextEditor()
    {
        if (m_CarInputManager != null)
            postDelayed(this, 1000);
    }

    private void removeCheckIsTextEditor()
    {
        if (m_CarInputManager != null)
            removeCallbacks(this);
    }

    @Override
    public void run()
    {
        if (!hasFocus())
            return;

        if (onCheckIsTextEditor())
            startInput();
        else
            stopInput();
    }

    boolean isValidUrl(String url)
    {
        return url.contains("://") || url.startsWith("www.") || url.endsWith("/");
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
    {
        String url = view.getText().toString();
        if (isValidUrl(url))
            goUrl(url);
        else
            doSearch(url);
        return true;
    }

    private void refreshAppBar()
    {
        if (m_BackButton != null)
        {
            m_BackButton.setEnabled(canGoBack());
        }
        if (m_ForwardButton != null)
        {
            m_ForwardButton.setVisibility(canGoForward() ? VISIBLE : GONE);
            m_ForwardButton.setEnabled(canGoForward());
        }
        if (m_DesktopButton != null)
        {
            m_DesktopButton.setSelected(m_DesktopUserAgent);
        }
        if (m_UrlEditText != null)
        {
            m_UrlEditText.setText(getUrl());
        }
    }

    void startInput()
    {
        if (m_CarInputManager != null)
            m_CarInputManager.startInput(this);
    }

    void stopInput()
    {
        if (m_CarInputManager != null)
            m_CarInputManager.stopInput();
    }

    boolean isInputActive()
    {
        return m_CarInputManager != null && m_CarInputManager.isInputActive();
    }

    @Override
    public void onRefresh()
    {
        reload();
    }

    private String readJavaScript(int id)
    {
        return readRawResource(id);
    }

    private String readRawResource(int id)
    {
        StringBuilder builder = new StringBuilder();
        InputStream inputStream = getResources().openRawResource(id);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try
        {
            for (;;)
            {
                String line = bufferedReader.readLine();
                if (line == null)
                    break;

                builder.append(line);
                builder.append("\n");
            }
        }
        catch (Exception e)
        {
            Log(TAG, "readRawResource exception : " + e.toString());
        }
        return builder.toString();
    }

    private void loadJavaScript(String javaScript)
    {
        if (javaScript != null)
            loadUrl("javascript:" + javaScript);
    }

    private void mediaFunctions()
    {
        mediaResetEventListener();
        loadJavaScript(m_JsScript);
        mediaSetEventListener();
    }

    private void mediaResetEventListener()
    {
        loadJavaScript("if (typeof mediaResetEventListener === 'function') {mediaResetEventListener();}");
    }

    private void mediaSetEventListener()
    {
        loadJavaScript("if (typeof mediaSetEventListener === 'function') {mediaSetEventListener();}");
    }

    private void mediaPlay()
    {
        loadJavaScript("mediaPlay();");
    }

    private void mediaPause()
    {
        loadJavaScript("mediaPause();");
    }

    private void mediaSkipToPrev()
    {
        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis - m_MediaPrevTimestamp >  1000)
        {
            loadJavaScript("mediaPlayPause();");
        }
        else
        {
            loadJavaScript("mediaSeekToStart();");
            loadJavaScript("mediaPlay();");
        }

        m_MediaPrevTimestamp = currentTimeMillis;
    }

    private void mediaSkipToNext()
    {
        loadJavaScript("mediaSeekToEnd();");
    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback
    {
        @Override
        public void onPlay()
        {
            mediaPlay();
        }

        @Override
        public void onPause()
        {
            mediaPause();
        }

        @Override
        public void onStop()
        {
            mediaPause();
        }

        @Override
        public void onSkipToPrevious()
        {
            mediaSkipToPrev();
        }

        @Override
        public void onSkipToNext()
        {
            mediaSkipToNext();
        }
    }

    public class JavaScriptMediaCallbacks
    {
        @JavascriptInterface
        public void onMediaPlay(float time)
        {
            m_CarMediaBrowser.setPlaybackState(STATE_PLAYING, time);
        }

        @JavascriptInterface
        public void onMediaPause(float time)
        {
            m_CarMediaBrowser.setPlaybackState(STATE_PAUSED, time);
        }

        @JavascriptInterface
        public void onMediaStop(float time)
        {
            m_CarMediaBrowser.setPlaybackState(STATE_PAUSED, time);
        }

        @JavascriptInterface
        public void onMediaTimeUpdate(float time)
        {
            m_CarMediaBrowser.setPlaybackPosition(time);
        }

        @JavascriptInterface
        public void onMediaDurationChange(float duration)
        {
            m_CarMediaBrowser.setPlaybackDuration(duration);
        }
    }
}
