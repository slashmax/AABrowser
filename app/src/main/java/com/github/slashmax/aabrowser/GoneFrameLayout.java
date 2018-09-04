package com.github.slashmax.aabrowser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class GoneFrameLayout extends FrameLayout
{
    private static final String TAG = "AAFrameLayout";

    public GoneFrameLayout(@NonNull Context context)
    {
        super(context);
    }

    public GoneFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public GoneFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchVisibilityChanged(View changedView, int visibility)
    {
        Log.d(TAG, "dispatchVisibilityChanged: " + visibility);
        if (visibility != GONE)
            super.dispatchVisibilityChanged(changedView, visibility);
    }

    @Override
    public void dispatchWindowVisibilityChanged(int visibility)
    {
        Log.d(TAG, "dispatchWindowVisibilityChanged: " + visibility);
        if (visibility != GONE)
            super.dispatchWindowVisibilityChanged(visibility);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility)
    {
        Log.d(TAG, "onVisibilityChanged: " + visibility);
        if (visibility != GONE)
            super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility)
    {
        Log.d(TAG, "onWindowVisibilityChanged: " + visibility);
        if (visibility != GONE)
            super.onWindowVisibilityChanged(visibility);
    }
}
