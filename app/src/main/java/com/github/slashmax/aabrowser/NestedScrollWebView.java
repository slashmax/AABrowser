package com.github.slashmax.aabrowser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

public class NestedScrollWebView extends WebView implements NestedScrollingChild2
{
    private static final String TAG = "NestedScrollWebView";

    private NestedScrollingChildHelper  m_NestedScrollingChildHelper;
    private int                         m_PreviousYPosition;

    public NestedScrollWebView(Context context)
    {
        super(context);
        init();
    }

    public NestedScrollWebView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public NestedScrollWebView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean motionEventHandled;

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                m_PreviousYPosition = (int) event.getY();
                motionEventHandled = super.onTouchEvent(event);
                break;

            case MotionEvent.ACTION_MOVE:

                int currentYMotionPosition = (int) event.getY();
                int preScrollDeltaY = m_PreviousYPosition - currentYMotionPosition;
                int[] consumedScroll = new int[2];
                int[] offsetInWindow = new int[2];
                int webViewYPosition = getScrollY();
                int scrollDeltaY = preScrollDeltaY;

                if (dispatchNestedPreScroll(0, preScrollDeltaY, consumedScroll, offsetInWindow))
                {
                    scrollDeltaY = preScrollDeltaY - consumedScroll[1];
                }

                if ((webViewYPosition == 0) && (scrollDeltaY < 0))
                {
                    stopNestedScroll();
                }
                else
                {
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    dispatchNestedScroll(0, scrollDeltaY, 0, 0, offsetInWindow);
                    m_PreviousYPosition = m_PreviousYPosition - scrollDeltaY;
                }

                motionEventHandled = super.onTouchEvent(event);
                break;

            default:
                stopNestedScroll();
                motionEventHandled = super.onTouchEvent(event);
        }

        return motionEventHandled;
    }

    private void init()
    {
        m_NestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled)
    {
        m_NestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled()
    {
        return m_NestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean hasNestedScrollingParent()
    {
        return m_NestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean hasNestedScrollingParent(int type)
    {
        return m_NestedScrollingChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean startNestedScroll(int axes)
    {
        return m_NestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type)
    {
        return m_NestedScrollingChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll()
    {
        m_NestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public void stopNestedScroll(int type)
    {
        m_NestedScrollingChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow)
    {
        return m_NestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type)
    {
        return m_NestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow)
    {
        return m_NestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type)
    {
        return m_NestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed)
    {
        return m_NestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY)
    {
        return m_NestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void onDetachedFromWindow()
    {
        m_NestedScrollingChildHelper.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    public void onStopNestedScroll(@NonNull View child)
    {
        m_NestedScrollingChildHelper.onStopNestedScroll(child);
    }
}
