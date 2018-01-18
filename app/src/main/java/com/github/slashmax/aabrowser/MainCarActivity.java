package com.github.slashmax.aabrowser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.ConsoleMessage;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.DayNightStyle;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchItem;
import com.google.android.gms.car.input.CarEditable;
import com.google.android.gms.car.input.CarEditableListener;

import static android.graphics.Bitmap.Config.ALPHA_8;
import static android.view.MotionEvent.ACTION_UP;

public class MainCarActivity extends CarActivity implements CarEditable , View.OnTouchListener
{
    private static final String TAG = "MainCarActivity";

    private static final String DEFAULT_HOME = "https://www.google.com";
    private static final String DEFAULT_SEARCH = "https://www.google.com/search?q=";

    private static final String DEFAULT_LINUX_AGENT     = "Mozilla/5.0 (Linux;) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.111 Safari/537.36";
    private static final String DEFAULT_WINDOWS_AGENT   = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.71 Safari/537.36";

    private WebView         m_WebView;
    private WebChromeClient m_WebChromeClient;
    private WebViewClient   m_WebViewClient;
    private Bitmap          m_DefaultVideoPoster;

    private View            m_CurrentEditable;

    private String          m_HomeUrl;
    private String          m_LastUrl;
    private String          m_OriginalUserAgent;
    private int             m_UserAgentIndex;

    @Override
    public void onCreate(Bundle bundle)
    {
        Log.d(TAG, "onCreate: " + (bundle != null ? bundle.toString() : "null"));

        setTheme(R.style.AppTheme);
        super.onCreate(bundle);
        setContentView(R.layout.activity_car_main);

        setIgnoreConfigChanges(0xFFFF);//0x200

        InitCarUiController(getCarUiController());

        InitWebChromeClient();
        InitWebViewClient();

        m_DefaultVideoPoster = Bitmap.createBitmap(1, 1, ALPHA_8);

        m_WebView = (WebView)findViewById(R.id.m_WebView);

        m_WebView.setWebContentsDebuggingEnabled(false);

        m_WebView.getSettings().setJavaScriptEnabled(true);

        m_WebView.getSettings().setUseWideViewPort(true);
        m_WebView.getSettings().setLoadWithOverviewMode(true);
        m_WebView.getSettings().setLoadsImagesAutomatically(true);
        m_WebView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        m_WebView.getSettings().setSupportZoom(true);
        m_WebView.getSettings().setBuiltInZoomControls(true);
        m_WebView.getSettings().setDisplayZoomControls(false);

        m_WebView.setFocusable(true);
        m_WebView.setFocusableInTouchMode(true);

        m_WebView.setWebChromeClient(m_WebChromeClient);
        m_WebView.setWebViewClient(m_WebViewClient);

        m_WebView.setOnTouchListener(this);

        m_OriginalUserAgent = m_WebView.getSettings().getUserAgentString();
        Log.d(TAG, "m_OriginalUserAgent: " + m_OriginalUserAgent);

        LoadSharedPreferences();

        UpdateConfiguration(getResources().getConfiguration());

        InitButtonsActions();

        if (bundle == null)
            goLast();
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        m_WebView = null;
        m_WebChromeClient = null;
        m_WebViewClient = null;
        m_DefaultVideoPoster = null;
    }

    private void go(String url)
    {
        Log.d(TAG, "go: " + url);
        if (m_WebView != null)
            m_WebView.loadUrl(url);
    }

    private void goHome()
    {
        Log.d(TAG, "goHome");
        go(m_HomeUrl);
    }

    private void goLast()
    {
        Log.d(TAG, "goCurrent");
        if (m_LastUrl == null || m_LastUrl.isEmpty())
            goHome();
        else
            go(m_LastUrl);
    }

    private void doSearch(String query)
    {
        Log.d(TAG, "doSearch : " + (query != null ? query : "null"));
        go(DEFAULT_SEARCH + query);
    }

    private void InitWebChromeClient()
    {
        Log.d(TAG, "InitWebChromeClient");
        m_WebChromeClient = new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                Log.d(TAG, "onProgressChanged: " + newProgress);
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title)
            {
                Log.d(TAG, "onReceivedTitle: " + title);
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon)
            {
                Log.d(TAG, "onReceivedIcon");
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback)
            {
                Log.d(TAG, "onShowCustomView");
                FrameLayout m_Fullscreen = (FrameLayout) findViewById(R.id.m_Fullscreen);
                if (m_Fullscreen != null)
                {
                    m_Fullscreen.addView(view);
                    m_Fullscreen.setBackgroundColor(Color.BLACK);
                    m_Fullscreen.bringToFront();
                }
            }

            @Override
            public void onHideCustomView()
            {
                Log.d(TAG, "onHideCustomView");
                FrameLayout m_Fullscreen = (FrameLayout) findViewById(R.id.m_Fullscreen);
                if (m_Fullscreen != null)
                    m_Fullscreen.removeAllViewsInLayout();
                LinearLayout m_MainLayout = (LinearLayout) findViewById(R.id.m_MainLayout);
                if (m_MainLayout != null)
                    m_MainLayout.bringToFront();
            }

            @Override
            public void onRequestFocus(WebView view)
            {
                Log.d(TAG, "onRequestFocus");
                super.onRequestFocus(view);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result)
            {
                Log.d(TAG, "onJsAlert: " + url + " message: " + message);
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result)
            {
                Log.d(TAG, "onJsConfirm: " + url + " message: " + message);
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result)
            {
                Log.d(TAG, "onJsPrompt: " + url + " message: " + message);
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result)
            {
                Log.d(TAG, "onJsBeforeUnload: " + url + " message: " + message);
                return super.onJsBeforeUnload(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage)
            {
                if (consoleMessage != null)
                    Log.d(TAG, "onConsoleMessage: " + consoleMessage.message() + " (" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public Bitmap getDefaultVideoPoster()
            {
                Log.d(TAG, "getDefaultVideoPoster");
                return m_DefaultVideoPoster;
            }

            @Override
            public View getVideoLoadingProgressView()
            {
                Log.d(TAG, "getVideoLoadingProgressView");
                return super.getVideoLoadingProgressView();
            }
        };
    }

    private void InitWebViewClient()
    {
        Log.d(TAG, "InitWebViewClient");
        m_WebViewClient = new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
            {
                Log.d(TAG, "shouldOverrideUrlLoading");
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                Log.d(TAG, "onPageStarted: " + url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                Log.d(TAG, "onPageFinished: " + url);
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
            {
                if (request != null)
                    Log.d(TAG, "onReceivedError: " + request.getUrl() + " (" + request.getMethod() + ")");
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
            {
                Log.d(TAG, "onReceivedHttpError: " + (errorResponse != null ? errorResponse.toString() : "null"));
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
            {
                Log.d(TAG, "onReceivedSslError: " + (error != null ? error.toString() : "null"));
                super.onReceivedSslError(view, handler, error);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm)
            {
                Log.d(TAG, "onReceivedHttpAuthRequest");
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
            }

            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event)
            {
                Log.d(TAG, "shouldOverrideKeyEvent: " + (event != null ? event.toString() : "null"));
                return super.shouldOverrideKeyEvent(view, event);
            }

            @Override
            public void onUnhandledKeyEvent(WebView view, KeyEvent event)
            {
                Log.d(TAG, "onUnhandledKeyEvent: " + (event != null ? event.toString() : "null"));
                super.onUnhandledKeyEvent(view, event);
            }

            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale)
            {
                Log.d(TAG, "onScaleChanged");
                super.onScaleChanged(view, oldScale, newScale);
            }

            @Override
            public void onReceivedLoginRequest(WebView view, String realm, String account, String args)
            {
                Log.d(TAG, "onReceivedLoginRequest");
                super.onReceivedLoginRequest(view, realm, account, args);
            }
        };
    }

    private void InitCarUiController(CarUiController controller)
    {
        Log.d(TAG, "InitCarUiController");
        controller.getStatusBarController().setTitle("");
        controller.getStatusBarController().hideAppHeader();
        controller.getStatusBarController().setAppBarAlpha(0.0f);
        controller.getStatusBarController().setAppBarBackgroundColor(Color.WHITE);
        controller.getStatusBarController().setDayNightStyle(DayNightStyle.AUTO);
        controller.getMenuController().hideMenuButton();

        controller.getSearchController().setSearchCallback(new SearchCallback()
        {
            @Override
            public void onSearchItemSelected(SearchItem searchItem)
            {
                Log.d(TAG, "onSearchItemSelected");
            }

            @Override
            public void onSearchStart()
            {
                Log.d(TAG, "onSearchStart");
                super.onSearchStart();
            }

            @Override
            public void onSearchStop()
            {
                Log.d(TAG, "onSearchStop");
                super.onSearchStop();
            }

            @Override
            public boolean onSearchSubmitted(String s)
            {
                Log.d(TAG, "onSearchSubmitted: " + (s != null ? s : "null"));
                doSearch(s);
                return true;
            }

            @Override
            public void onSearchTextChanged(String s)
            {
                Log.d(TAG, "onSearchTextChanged: " + (s != null ? s : "null"));
            }
        });
    }

    private void InitButtonsActions()
    {
        Log.d(TAG, "InitButtonsActions");

        ImageButton back = (ImageButton)findViewById(R.id.m_Back);
        if (back != null)
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_Back.onClick");
                    m_WebView.goBack();
                }
            });

        ImageButton reload = (ImageButton)findViewById(R.id.m_Reload);
        if (reload != null)
            reload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_Reload.onClick");
                    m_WebView.reload();
                }
            });

        ImageButton home = (ImageButton)findViewById(R.id.m_Home);
        if (home != null)
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_HomeUrl.onClick");
                    goHome();
                }
            });


        ImageButton browse = (ImageButton)findViewById(R.id.m_Browse);
        if (browse != null)
            browse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_Browse.onClick");
                    getCarUiController().getSearchController().startSearch("");
                }
            });

        ImageButton desktop =(ImageButton)findViewById(R.id.m_DesktopMode);
        if (desktop != null)
            desktop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_DesktopMode.onClick");

                    if (m_UserAgentIndex == 0)
                        SetUserAgentIndex(1);
                    else
                        SetUserAgentIndex(0);

                    m_WebView.reload();
                }
            });

        ImageButton keyboard = (ImageButton)findViewById(R.id.m_Keyboard);
        if (keyboard != null)
            keyboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_Keyboard.onClick");
                    startInput(m_WebView);
                }
            });
    }

    @Nullable
    @Override
    public View onCreateView(String s, @NonNull Context context, @NonNull AttributeSet attributeSet)
    {
        Log.d(TAG, "onCreateView: " + s + " (" + (context != null ? context.toString() : "null") + ")");
        return super.onCreateView(s, context, attributeSet);
    }

    @Override
    public View findViewById(int i)
    {
        Log.d(TAG, "findViewById: " + i);
        return super.findViewById(i);
    }

    @Override
    public boolean onKeyDown(int i, KeyEvent keyEvent)
    {
        Log.d(TAG, "onKeyDown: " + (keyEvent != null ? keyEvent.toString() : "null"));
        return super.onKeyDown(i, keyEvent);
    }

    @Override
    public boolean onKeyLongPress(int i, KeyEvent keyEvent) {
        Log.d(TAG, "onKeyLongPress: " + (keyEvent != null ? keyEvent.toString() : "null"));
        return super.onKeyLongPress(i, keyEvent);
    }

    @Override
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        Log.d(TAG, "onKeyUp: " + (keyEvent != null ? keyEvent.toString() : "null"));
        return super.onKeyUp(i, keyEvent);
    }

    @Override
    public void onBackPressed()
    {
        Log.d(TAG, "onBackPressed");
        super.onBackPressed();
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        Log.d(TAG, "onNewIntent: " + (intent != null ? intent.toString() : "null"));
        super.onNewIntent(intent);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle)
    {
        Log.d(TAG, "onRestoreInstanceState: " + (bundle != null ? bundle.toString() : "null"));
        super.onRestoreInstanceState(bundle);
        if(m_WebView != null && bundle != null)
            m_WebView.restoreState(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        Log.d(TAG, "onSaveInstanceState: " + (bundle != null ? bundle.toString() : "null"));
        super.onSaveInstanceState(bundle);
        if(m_WebView != null && bundle != null)
            m_WebView.saveState(bundle);

        SaveSharedPreferences();
    }

    @Override
    public void onStart()
    {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onStop()
    {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onPause()
    {
        Log.d(TAG, "onPause");
        super.onPause();
        if(m_WebView != null)
            m_WebView.onPause();
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume");
        super.onResume();
        if(m_WebView != null)
            m_WebView.onResume();
    }

    @Override
    public void onPostResume()
    {
        Log.d(TAG, "onPostResume");
        super.onPostResume();
    }

    @Override
    public void onWindowFocusChanged(boolean b, boolean b1)
    {
        Log.d(TAG, "onWindowFocusChanged");
        super.onWindowFocusChanged(b, b1);
    }

    private void SetImageButtonColorState(int buttonId, ColorStateList foreTint, ColorStateList backTint)
    {
        Log.d(TAG, "SetImageButtonColorState");
        ImageButton button = (ImageButton)findViewById(buttonId);
        if (button != null)
        {
            button.setForegroundTintList(foreTint);
            button.setBackgroundTintList(backTint);
        }
    }

    private void UpdateConfiguration(Configuration configuration)
    {
        if (configuration == null)
            return;

        Log.d(TAG, "UpdateConfiguration: " + configuration.toString());

        int backgroundColor;
        if ((configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
            backgroundColor = getColor(R.color.colorCarBackgroundNight);
        else
            backgroundColor = getColor(R.color.colorCarBackgroundDay);

        int textColor = getColor(R.color.colorCarText);

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed
        };

        int[] foreColors = new int[] {textColor, textColor, textColor, textColor};

        int[] backColors = new int[] {backgroundColor, backgroundColor, backgroundColor, backgroundColor};

        ColorStateList foreTint = new ColorStateList(states, foreColors);
        ColorStateList backTint = new ColorStateList(states, backColors);

        LinearLayout buttonsLayout = (LinearLayout)findViewById(R.id.m_ButtonsLayout);
        if (buttonsLayout != null)
            buttonsLayout.setBackgroundColor(backgroundColor);

        SetImageButtonColorState(R.id.m_Back, foreTint, backTint);
        SetImageButtonColorState(R.id.m_Reload, foreTint, backTint);
        SetImageButtonColorState(R.id.m_Home, foreTint, backTint);
        SetImageButtonColorState(R.id.m_Browse, foreTint, backTint);
        SetImageButtonColorState(R.id.m_DesktopMode, foreTint, backTint);
        SetImageButtonColorState(R.id.m_Keyboard, foreTint, backTint);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration)
    {
        Log.d(TAG, "onConfigurationChanged: " + (configuration != null ? configuration.toString() : "null"));
        super.onConfigurationChanged(configuration);
        UpdateConfiguration(configuration);
    }

    @Override
    public void onLowMemory()
    {
        Log.d(TAG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onFrameRateChange(int i)
    {
        Log.d(TAG, "onFrameRateChange: " + i);
        super.onFrameRateChange(i);
    }

    @Override
    public void onPowerStateChange(int i)
    {
        Log.d(TAG, "onPowerStateChange: " + i);
        super.onPowerStateChange(i);
    }

    @Override
    public Intent getIntent()
    {
        Log.d(TAG, "getIntent");
        return super.getIntent();
    }

    @Override
    public void setIntent(Intent intent)
    {
        Log.d(TAG, "setIntent: " + (intent != null ? intent.toString() : "null"));
        super.setIntent(intent);
    }

    @Override
    public void startCarActivity(Intent intent)
    {
        Log.d(TAG, "startCarActivity: " + (intent != null ? intent.toString() : "null"));
        super.startCarActivity(intent);
    }

    @Override
    public void onAccessibilityScanRequested(IBinder iBinder)
    {
        Log.d(TAG, "onAccessibilityScanRequested: " + (iBinder != null ? iBinder.toString() : "null"));
        super.onAccessibilityScanRequested(iBinder);
    }

    @Override
    public ComponentName startService(Intent service)
    {
        Log.d(TAG, "startService: " + (service != null ? service.toString() : "null"));
        return super.startService(service);
    }

    @Override
    public boolean stopService(Intent name)
    {
        Log.d(TAG, "stopService: " + (name != null ? name.toString() : "null"));
        return super.stopService(name);
    }

    @Override
    public Object getSystemService(String name)
    {
        Log.d(TAG, "getSystemService: " + name);
        return super.getSystemService(name);
    }

    @Override
    public String getSystemServiceName(Class<?> serviceClass)
    {
        Log.d(TAG, "getSystemServiceName: " + (serviceClass != null ? serviceClass.toString() : "null"));
        return super.getSystemServiceName(serviceClass);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        Log.d(TAG, "onTouch: " + (event != null ? event.toString() : "null"));
        if (event != null && event.getAction() == ACTION_UP)
        {
            WebView.HitTestResult hitTest = m_WebView.getHitTestResult();
            if (hitTest != null && hitTest.getType() == WebView.HitTestResult.EDIT_TEXT_TYPE)
            {
                if (event.getY() > 170)
                    m_WebView.scrollBy(0, (int) event.getY() - 100);

                startInput(m_WebView);
            }
            else
            {
                stopInput();
            }
        }
        return false;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo)
    {
        Log.d(TAG, "onCreateInputConnection");
        InputConnection result = null;
        if (m_CurrentEditable != null)
        {
            //leak ???
            result = new BaseInputConnection(m_CurrentEditable, false)
            {
                public boolean sendKeyEvent(KeyEvent event)
                {
                    boolean ok = m_CurrentEditable.dispatchKeyEvent(event);
                    if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        stopInput();
                    return ok;
                }

                public boolean deleteSurroundingText(int beforeLength, int afterLength)
                {
                    boolean ok = m_CurrentEditable.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    ok = ok && m_CurrentEditable.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                    return ok;
                }
            };
        }
        return result;
    }

    @Override
    public void setCarEditableListener(CarEditableListener carEditableListener)
    {
        Log.d(TAG, "setCarEditableListener");
    }

    @Override
    public void setInputEnabled(boolean b)
    {
        Log.d(TAG, "setInputEnabled: " + b);
    }

    private void startInput(View view)
    {
        Log.d(TAG, "startInput");
        m_CurrentEditable = view;
        a().startInput(this);
    }

    private void stopInput()
    {
        Log.d(TAG, "stopInput");
        if (a().isInputActive())
            a().stopInput();
        m_CurrentEditable = null;
    }

    private void SetUserAgentIndex(int index)
    {
        Log.d(TAG, "SetUserAgentIndex: " + index);
        m_UserAgentIndex = index;
        ImageButton desktop =(ImageButton)findViewById(R.id.m_DesktopMode);

        if (m_UserAgentIndex == 0)
        {
            m_WebView.getSettings().setUserAgentString(m_OriginalUserAgent);
            desktop.setImageDrawable(getDrawable(R.drawable.ic_tablet_android_black));
        }
        else if (m_UserAgentIndex == 1)
        {
            m_WebView.getSettings().setUserAgentString(DEFAULT_LINUX_AGENT);
            desktop.setImageDrawable(getDrawable(R.drawable.ic_desktop_windows_black));
        }
    }

    private void LoadSharedPreferences()
    {
        Log.d(TAG, "LoadSharedPreferences");

        SharedPreferences sharedPref = getSharedPreferences("com.github.slashmax.aabrowser.preferences", MODE_PRIVATE);
        m_HomeUrl = sharedPref.getString("m_HomeUrl", DEFAULT_HOME);
        m_LastUrl = sharedPref.getString("m_LastUrl", DEFAULT_HOME);
        m_UserAgentIndex = sharedPref.getInt("m_UserAgentIndex", 0);

        SetUserAgentIndex(m_UserAgentIndex);
    }

    private void SaveSharedPreferences()
    {
        Log.d(TAG, "SaveSharedPreferences");

        m_LastUrl = m_WebView.getUrl();

        SharedPreferences sharedPref = getSharedPreferences("com.github.slashmax.aabrowser.preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("m_HomeUrl", m_HomeUrl);
        editor.putString("m_LastUrl", m_LastUrl);
        editor.putInt("m_UserAgentIndex", m_UserAgentIndex);
        editor.commit();
    }
}
