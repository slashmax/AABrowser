package com.github.slashmax.aabrowser;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

public class NestedScrollWebView extends WebView implements NestedScrollingChild2
{
    private static final String TAG = "NestedScrollWebView";

    private NestedScrollingChildHelper  m_NestedScrollingChildHelper;
    private int                         m_LastMotionY;

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
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                m_LastMotionY = (int)event.getY();
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaY = m_LastMotionY - (int)event.getY();
                if (deltaY != 0)
                {
                    int[] scrollConsumed = new int[2];
                    int[] scrollOffset = new int[2];
                    if (dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset))
                        deltaY -= scrollConsumed[1];

                    if (deltaY != 0)
                    {
                        dispatchNestedScroll(0, deltaY, 0, 0, scrollOffset);
                        m_LastMotionY -= deltaY;
                    }
                }
                break;

            default:
                stopNestedScroll();
                break;
        }

        return super.onTouchEvent(event);
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
