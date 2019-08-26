package com.github.slashmax.aabrowser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class CarFrameLayout extends FrameLayout
{
    private static final String TAG = "AAFrameLayout";

    public CarFrameLayout(@NonNull Context context)
    {
        super(context);
    }

    public CarFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CarFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public CarFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void dispatchVisibilityChanged(View changedView, int visibility)
    {
        if (visibility != GONE)
            super.dispatchVisibilityChanged(changedView, visibility);
    }

    @Override
    public void dispatchWindowVisibilityChanged(int visibility)
    {
        if (visibility != GONE)
            super.dispatchWindowVisibilityChanged(visibility);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility)
    {
        if (visibility != GONE)
            super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility)
    {
        if (visibility != GONE)
            super.onWindowVisibilityChanged(visibility);
    }
}
