package com.github.slashmax.aabrowser;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;

public class CarInputConnection implements InputConnection
{
    private static final String TAG = "CarInputConnection";

    private CarInputManager m_CarInputManager;
    private InputConnection m_InputConnection;

    CarInputConnection(CarInputManager carInputManager, InputConnection inputConnection)
    {
        m_CarInputManager = carInputManager;
        m_InputConnection = inputConnection;
    }

    @Override
    public CharSequence getTextBeforeCursor(int n, int flags)
    {
        return m_InputConnection.getTextBeforeCursor(n, flags);
    }

    @Override
    public CharSequence getTextAfterCursor(int n, int flags)
    {
        return m_InputConnection.getTextAfterCursor(n, flags);
    }

    @Override
    public CharSequence getSelectedText(int flags)
    {
        return m_InputConnection.getSelectedText(flags);
    }

    @Override
    public int getCursorCapsMode(int reqModes)
    {
        return m_InputConnection.getCursorCapsMode(reqModes);
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags)
    {
        return m_InputConnection.getExtractedText(request, flags);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength)
    {
        return m_InputConnection.deleteSurroundingText(beforeLength, afterLength);
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Override
    public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength)
    {
        return m_InputConnection.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition)
    {
        return m_InputConnection.setComposingText(text, newCursorPosition);
    }

    @Override
    public boolean setComposingRegion(int start, int end)
    {
        return m_InputConnection.setComposingRegion(start, end);
    }

    @Override
    public boolean finishComposingText()
    {
        return m_InputConnection.finishComposingText();
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition)
    {
        return m_InputConnection.commitText(text, newCursorPosition);
    }

    @Override
    public boolean commitCompletion(CompletionInfo text)
    {
        return m_InputConnection.commitCompletion(text);
    }

    @Override
    public boolean commitCorrection(CorrectionInfo correctionInfo)
    {
        return m_InputConnection.commitCorrection(correctionInfo);
    }

    @Override
    public boolean setSelection(int start, int end)
    {
        return m_InputConnection.setSelection(start, end);
    }

    @Override
    public boolean performEditorAction(int editorAction)
    {
        boolean result = m_InputConnection.performEditorAction(editorAction);
        if (m_CarInputManager != null)
            m_CarInputManager.stopInput();
        return result;
    }

    @Override
    public boolean performContextMenuAction(int id)
    {
        return m_InputConnection.performContextMenuAction(id);
    }

    @Override
    public boolean beginBatchEdit()
    {
        return m_InputConnection.beginBatchEdit();
    }

    @Override
    public boolean endBatchEdit()
    {
        return m_InputConnection.endBatchEdit();
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event)
    {
        return m_InputConnection.sendKeyEvent(event);
    }

    @Override
    public boolean clearMetaKeyStates(int states)
    {
        return m_InputConnection.clearMetaKeyStates(states);
    }

    @Override
    public boolean reportFullscreenMode(boolean enabled)
    {
        return m_InputConnection.reportFullscreenMode(enabled);
    }

    @Override
    public boolean performPrivateCommand(String action, Bundle data)
    {
        return m_InputConnection.performPrivateCommand(action, data);
    }

    @Override
    public boolean requestCursorUpdates(int cursorUpdateMode)
    {
        return m_InputConnection.requestCursorUpdates(cursorUpdateMode);
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Override
    public Handler getHandler()
    {
        return m_InputConnection.getHandler();
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Override
    public void closeConnection()
    {
        m_InputConnection.closeConnection();
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    @Override
    public boolean commitContent(@NonNull InputContentInfo inputContentInfo, int flags, @Nullable Bundle opts)
    {
        return m_InputConnection.commitContent(inputContentInfo, flags, opts);
    }
}
