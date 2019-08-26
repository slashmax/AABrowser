package com.github.slashmax.aabrowser;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.google.android.gms.car.input.CarEditable;
import com.google.android.gms.car.input.CarEditableListener;
import com.google.android.gms.car.input.InputManager;

public class CarInputManager implements CarEditable
{
    private static final String TAG = "CarInputManager";
    private InputManager m_InputManager;
    private View         m_TargetView;

    CarInputManager(InputManager inputManager)
    {
        m_InputManager = inputManager;
    }

    public boolean isCurrentCarEditable(CarEditable carEditable)
    {
        return m_InputManager != null && m_InputManager.isCurrentCarEditable(carEditable);
    }

    public boolean isInputActive()
    {
        return m_InputManager != null && m_InputManager.isInputActive();
    }

    public boolean isValid()
    {
        return m_InputManager != null && m_InputManager.isValid();
    }

    public void startInput(View TargetView)
    {
        if (!isInputActive())
        {
            m_TargetView = TargetView;
            m_InputManager.startInput(this);
        }
    }

    public void stopInput()
    {
        if (isInputActive())
        {
            m_InputManager.stopInput();
            m_TargetView = null;
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo)
    {
        if (m_TargetView == null)
            return null;

        InputConnection inputConnection = m_TargetView.onCreateInputConnection(editorInfo);
        if (inputConnection == null)
            return null;

        return new CarInputConnection(this, inputConnection);
    }

    @Override
    public void setCarEditableListener(CarEditableListener carEditableListener)
    {
    }

    @Override
    public void setInputEnabled(boolean b)
    {
    }
}
