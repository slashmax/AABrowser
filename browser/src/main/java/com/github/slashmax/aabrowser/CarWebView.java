package com.github.slashmax.aabrowser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.preference.PreferenceManager;
import com.google.android.material.appbar.AppBarLayout;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adblockplus.libadblockplus.android.settings.AdblockHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
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
        SwipeRefreshLayout.OnRefreshListener,
        BookmarksAdapter.OnBookmarkListener
{
    private static final String TAG = "CarWebView";

    private static final String DEFAULT_HOME    = "https://www.google.com";
    private static final String DEFAULT_SEARCH  = "https://www.google.com/search?q=";

    private boolean                     m_Moved;

    private CarInputManager             m_CarInputManager;
    private BookmarksAdapter            m_BookmarksAdapter;

    private CarFrameLayout              m_CarFrameLayout;

    private AppBarLayout                m_AppBarLayout;
    private SwipeRefreshLayout          m_SwipeRefreshLayout;
    private ViewGroup                   m_CustomViewGroup;
    private LinearLayout                m_BookmarksLayout;
    private RecyclerView                m_RecyclerView;

    private ImageButton                 m_BookmarksShowButton;
    private ImageButton                 m_BackButton;
    private ImageButton                 m_ForwardButton;
    private ImageButton                 m_ReloadButton;
    private ImageButton                 m_HomeButton;

    private CarEditText                 m_UrlEditText;

    private ImageButton                 m_BookmarksHideButton;
    private ImageButton                 m_BookmarksAddButton;
    private ImageButton                 m_BookmarksRemoveButton;
    private ImageButton                 m_DesktopButton;
    private ImageButton                 m_AdpSettingsButton;
    private ImageButton                 m_LogButton;

    private View                                m_CustomView;
    private WebChromeClient.CustomViewCallback  m_CustomViewCallback;

    private long                        m_MediaPrevTimestamp;

    private Bitmap                      m_DefaultVideoPoster;

    private String                      m_HomeUrl;
    private String                      m_LastUrl;

    private String                      m_UserAgentString;
    private String                      m_DesktopUserAgentString;
    private boolean                     m_DesktopUserAgent;

    private Runnable                    m_AppBarLayoutHider;

    private CarMediaBrowser             m_CarMediaBrowser;

    private String                      m_JsScript;

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

    public void setInputManager(CarInputManager inputManager)
    {
        m_CarInputManager = inputManager;
    }

    public void setCarFrameLayout(CarFrameLayout frameLayout)
    {
        m_CarFrameLayout = frameLayout;
    }

    public void setMetadataAdvertisement(boolean advertisement)
    {
        if (m_CarMediaBrowser != null)
            m_CarMediaBrowser.setMetadataAdvertisement(advertisement);
    }

    @SuppressLint("setJavaScriptEnabled")
    public void onCreate()
    {
        setProvider(AdblockHelper.get().getProvider());
        setFocusable(true);
        setFocusableInTouchMode(true);

        m_Moved = false;
        m_JsScript = readJavaScript(R.raw.media_functions);

        m_BookmarksAdapter = new BookmarksAdapter();
        m_BookmarksAdapter.setContext(getContext());
        m_BookmarksAdapter.setListener(this);
        m_BookmarksAdapter.onCreate();

        m_CarMediaBrowser = new CarMediaBrowser();
        m_CarMediaBrowser.setContext(getContext());
        m_CarMediaBrowser.setCallback(new MediaSessionCallback());
        m_CarMediaBrowser.onCreate();

        m_AppBarLayoutHider = new Runnable()
        {
            @Override
            public void run()
            {
                if (m_AppBarLayout != null && hasFocus())
                    m_AppBarLayout.setExpanded(false, true);
            }
        };

        InitUi();
        InitWebView();
        LoadSharedPreferences();
    }

    public void onDestroy()
    {
        mediaPause();
        onPause();
        SaveSharedPreferences();
        if (m_CarMediaBrowser != null)
            m_CarMediaBrowser.onDestroy();

        m_BookmarksAdapter.setListener(null);
        m_BookmarksAdapter.onDestroy();

        dispose(null);

        m_CarInputManager = null;
        m_BookmarksAdapter = null;
        m_CarFrameLayout = null;
        m_AppBarLayout = null;
        m_SwipeRefreshLayout = null;
        m_CustomViewGroup = null;
        m_BookmarksLayout = null;
        m_RecyclerView = null;
        m_BookmarksShowButton = null;
        m_BackButton = null;
        m_ForwardButton = null;
        m_ReloadButton = null;
        m_HomeButton = null;
        m_UrlEditText = null;
        m_BookmarksHideButton = null;
        m_BookmarksAddButton = null;
        m_BookmarksRemoveButton = null;
        m_DesktopButton = null;
        m_AdpSettingsButton = null;
        m_LogButton = null;
        m_CustomView = null;
        m_CustomViewCallback = null;
        m_AppBarLayoutHider = null;
        m_CarMediaBrowser = null;
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
            case R.id.m_BookmarksShowButton:    showBookmarks();break;
            case R.id.m_BackButton:             goBack();break;
            case R.id.m_ForwardButton:          goForward();break;
            case R.id.m_ReloadButton:           reload();break;
            case R.id.m_HomeButton:             goHome();break;
            case R.id.m_BookmarksHideButton:    hideBookmarks();break;
            case R.id.m_BookmarksAddButton:     addBookmark();break;
            case R.id.m_BookmarksRemoveButton:  removeBookmark();break;
            case R.id.m_DesktopButton:          toggleDesktopMode();break;
            case R.id.m_AdpSettingsButton:      showAdpSettings();break;
            case R.id.m_LogButton:              showLog();break;
        }
        refreshAppBar();
        removeHideAppBar();
        stopInput();
    }

    public void goUrl(String url)
    {
        if (url != null)
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
        return agent
                .replace("Android", "X11;")
                .replace("Mobile", "")
                .replace("wv", "")
                .replace("Version/4.0", "");
    }

    public boolean onBackPressed()
    {
        if (m_BookmarksLayout != null && m_BookmarksLayout.getVisibility() == VISIBLE)
        {
            hideBookmarks();
            return true;
        }
        if (m_CustomViewCallback != null)
        {
            m_CustomViewCallback.onCustomViewHidden();
            return true;
        }
        if (canGoBack())
        {
            goBack();
            return true;
        }
        return false;
    }

    void showBookmarks()
    {
        if (m_BookmarksLayout != null)
        {
            m_BookmarksLayout.bringToFront();
            m_BookmarksLayout.setVisibility(VISIBLE);
        }
    }

    void hideBookmarks()
    {
        if (m_BookmarksAdapter != null)
            m_BookmarksAdapter.SaveBookmarks();

        if (m_BookmarksLayout != null)
        {
            m_BookmarksLayout.setVisibility(INVISIBLE);
        }
    }

    void addBookmark()
    {
        m_BookmarksAdapter.Add(getTitle(), getUrl());
    }

    void removeBookmark()
    {
        m_BookmarksAdapter.Remove(getUrl());
    }

    void enableAdbSettings()
    {
        if (m_AdpSettingsButton != null)
            m_AdpSettingsButton.setVisibility(VISIBLE);
    }

    void showAdpSettings()
    {
        Intent intent = new Intent(getContext(), SettingsActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    void showLog()
    {
        loadData("", "text/html", "");
    }

    public void toggleDesktopMode()
    {
        SetDesktopMode(!m_DesktopUserAgent);
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
        m_DefaultVideoPoster = Bitmap.createBitmap(256, 256, ALPHA_8);
        WebChromeClient webChromeClient = new WebChromeClient()
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

                if (m_CarMediaBrowser != null)
                    m_CarMediaBrowser.setPlaybackTitle(title);
                refreshAppBar();
                mediaFunctions();
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon)
            {
                if (m_CarMediaBrowser != null)
                    m_CarMediaBrowser.setPlaybackIcon(icon, false);
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback)
            {
                m_CustomView = view;
                m_CustomViewCallback = callback;
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
                }
                m_CustomView = null;
                m_CustomViewCallback = null;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request)
            {
                String[] resources = request.getResources();
                for (int i = 0; i < resources.length; i++)
                {
                    if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID.equals(resources[i]))
                    {
                        request.grant(new String[] {PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID});
                        return;
                    }
                }
                super.onPermissionRequest(request);
            }

            @Override
            public Bitmap getDefaultVideoPoster()
            {
                return m_DefaultVideoPoster;
            }
        };

        setWebChromeClient(webChromeClient);
    }

    private void InitWebViewClient()
    {
        WebViewClient webViewClient = new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                refreshAppBar();
                removeHideAppBar();
                if (m_SwipeRefreshLayout != null)
                    m_SwipeRefreshLayout.setRefreshing(true);

                if (m_CarMediaBrowser != null)
                    m_CarMediaBrowser.setPlaybackIcon(favicon, true);
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
        setWebViewClient(webViewClient);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void InitWebView()
    {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setUseWideViewPort(true);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setMediaPlaybackRequiresUserGesture(false);

        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);

        getSettings().setAllowContentAccess(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setDomStorageEnabled(true);

        InitWebChromeClient();
        InitWebViewClient();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            getSettings().setOffscreenPreRaster(true);
            setRendererPriorityPolicy(RENDERER_PRIORITY_IMPORTANT, false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getSettings().setMixedContentMode(MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        m_UserAgentString = getSettings().getUserAgentString();
        m_DesktopUserAgentString = BuildDesktopUserAgentString(m_UserAgentString);

        addJavascriptInterface(new JavaScriptMediaCallbacks(), "m_JavaScriptMediaCallbacks");
        mediaFunctions();
    }

    private void InitUi()
    {
        if (m_CarFrameLayout != null)
        {
            m_AppBarLayout          = m_CarFrameLayout.findViewById(R.id.m_AppBarLayout);
            m_SwipeRefreshLayout    = m_CarFrameLayout.findViewById(R.id.m_SwipeRefreshLayout);
            m_CustomViewGroup       = m_CarFrameLayout.findViewById(R.id.m_CustomViewGroup);
            m_BookmarksLayout       = m_CarFrameLayout.findViewById(R.id.m_BookmarksLayout);
            m_RecyclerView          = m_CarFrameLayout.findViewById(R.id.m_RecyclerView);

            m_BookmarksShowButton   = m_CarFrameLayout.findViewById(R.id.m_BookmarksShowButton);
            m_BackButton            = m_CarFrameLayout.findViewById(R.id.m_BackButton);
            m_ForwardButton         = m_CarFrameLayout.findViewById(R.id.m_ForwardButton);
            m_ReloadButton          = m_CarFrameLayout.findViewById(R.id.m_ReloadButton);
            m_HomeButton            = m_CarFrameLayout.findViewById(R.id.m_HomeButton);
            m_BookmarksHideButton   = m_CarFrameLayout.findViewById(R.id.m_BookmarksHideButton);
            m_BookmarksAddButton    = m_CarFrameLayout.findViewById(R.id.m_BookmarksAddButton);
            m_BookmarksRemoveButton = m_CarFrameLayout.findViewById(R.id.m_BookmarksRemoveButton);
            m_DesktopButton         = m_CarFrameLayout.findViewById(R.id.m_DesktopButton);
            m_AdpSettingsButton     = m_CarFrameLayout.findViewById(R.id.m_AdpSettingsButton);
            m_LogButton             = m_CarFrameLayout.findViewById(R.id.m_LogButton);

            m_UrlEditText           = m_CarFrameLayout.findViewById(R.id.m_UrlEditText);
        }

        if (m_RecyclerView != null)
        {
            m_RecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            m_RecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT)
            {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target)
                {
                    final int fromPos = viewHolder.getAdapterPosition();
                    final int toPos = target.getAdapterPosition();
                    m_BookmarksAdapter.Move(fromPos, toPos);
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
                {
                    if (direction == ItemTouchHelper.RIGHT)
                        m_BookmarksAdapter.Remove(viewHolder.getAdapterPosition());
                }
            }).attachToRecyclerView(m_RecyclerView);

            m_RecyclerView.setAdapter(m_BookmarksAdapter);
        }

        if (m_BookmarksShowButton != null)      m_BookmarksShowButton.setOnClickListener(this);
        if (m_BackButton != null)               m_BackButton.setOnClickListener(this);
        if (m_ForwardButton != null)            m_ForwardButton.setOnClickListener(this);
        if (m_ReloadButton != null)             m_ReloadButton.setOnClickListener(this);
        if (m_HomeButton != null)               m_HomeButton.setOnClickListener(this);
        if (m_BookmarksHideButton != null)      m_BookmarksHideButton.setOnClickListener(this);
        if (m_BookmarksAddButton != null)       m_BookmarksAddButton.setOnClickListener(this);
        if (m_BookmarksRemoveButton != null)    m_BookmarksRemoveButton.setOnClickListener(this);
        if (m_DesktopButton != null)            m_DesktopButton.setOnClickListener(this);
        if (m_AdpSettingsButton != null)        m_AdpSettingsButton.setOnClickListener(this);
        if (m_LogButton != null)                m_LogButton.setOnClickListener(this);

        if (m_UrlEditText != null)
        {
            m_UrlEditText.setInputManager(m_CarInputManager);
            m_UrlEditText.setOnEditorActionListener(this);
        }

        if (m_SwipeRefreshLayout != null)
            m_SwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void postHideAppBar()
    {
        if (m_AppBarLayoutHider != null)
            postDelayed(m_AppBarLayoutHider,5000);
    }

    private void removeHideAppBar()
    {
        if (m_AppBarLayoutHider != null)
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

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
    {
        String text = view.getText().toString();
        String url = guessUrl(text);

        if (URLUtil.isValidUrl(url))
            goUrl(url);
        else
            doSearch(text);
        return true;
    }

    String guessUrl(String url)
    {
        if (url == null || url.isEmpty() || url.contains("://"))
            return url;

        if (!url.startsWith("www.") && !url.endsWith("/") && !url.contains("."))
            return url;

        return "https://" + url;
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
            Log.d(TAG, "readRawResource exception : " + e.toString());
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
        loadJavaScript("mediaResetEventListener();");
    }

    private void mediaSetEventListener()
    {
        loadJavaScript("mediaSetEventListener();");
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

    @Override
    public void onBookmark(String url)
    {
        goUrl(url);
        hideBookmarks();
    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback
    {
        @Override
        public void onPlay()
        {
            mediaPlay();
            mediaFunctions();
        }

        @Override
        public void onPause()
        {
            mediaPause();
            mediaFunctions();
        }

        @Override
        public void onStop()
        {
            mediaPause();
            mediaFunctions();
        }

        @Override
        public void onSkipToPrevious()
        {
            mediaSkipToPrev();
            mediaFunctions();
        }

        @Override
        public void onSkipToNext()
        {
            mediaSkipToNext();
            mediaFunctions();
        }
    }

    public class JavaScriptMediaCallbacks
    {
        @JavascriptInterface
        public void onMediaPlay(float time)
        {
            if (m_CarMediaBrowser != null)
                m_CarMediaBrowser.setPlaybackState(STATE_PLAYING, time);
        }

        @JavascriptInterface
        public void onMediaPause(float time)
        {
            if (m_CarMediaBrowser != null)
                m_CarMediaBrowser.setPlaybackState(STATE_PAUSED, time);
        }

        @JavascriptInterface
        public void onMediaStop(float time)
        {
            if (m_CarMediaBrowser != null)
                m_CarMediaBrowser.setPlaybackState(STATE_PAUSED, time);
        }

        @JavascriptInterface
        public void onMediaTimeUpdate(float time)
        {
            if (m_CarMediaBrowser != null)
                m_CarMediaBrowser.setPlaybackPosition(time);
        }

        @JavascriptInterface
        public void onMediaDurationChange(float duration)
        {
            if (m_CarMediaBrowser != null)
                m_CarMediaBrowser.setPlaybackDuration(duration);
        }
    }
}
