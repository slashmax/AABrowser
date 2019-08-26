package com.github.slashmax.aabrowser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

@SuppressLint("AppCompatCustomView")
public class CarEditText extends EditText
{
    private static final String TAG = "CarEditText";

    private CarInputManager m_CarInputManager;

    public CarEditText(Context context)
    {
        super(context);
    }

    public CarEditText(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }

    public CarEditText(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public void setInputManager(CarInputManager inputManager)
    {
        m_CarInputManager = inputManager;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect)
    {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused)
            startInput();
        else
            stopInput();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getActionMasked() == MotionEvent.ACTION_UP && hasFocus())
            startInput();
        return super.onTouchEvent(event);
    }

    @Override
    public void clearFocus()
    {
        if (isInputActive())
            return;
        super.clearFocus();
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
}
