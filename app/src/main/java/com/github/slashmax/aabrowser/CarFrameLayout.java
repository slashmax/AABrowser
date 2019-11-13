package com.github.slashmax.aabrowser;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class CarFrameLayout extends FrameLayout
{
    private static final String TAG = "CarFrameLayout";

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

    @Override
    public void dispatchWindowVisibilityChanged(int visibility)
    {
        if (visibility != VISIBLE)
            return;
        super.dispatchWindowVisibilityChanged(visibility);
    }

    @Override
    protected void dispatchVisibilityChanged(View changedView, int visibility)
    {
        if (visibility != VISIBLE)
            return;
        super.dispatchVisibilityChanged(changedView, visibility);
    }
}
