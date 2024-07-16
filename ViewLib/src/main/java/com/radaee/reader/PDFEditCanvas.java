package com.radaee.reader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PDFEditCanvas extends View {
    public interface CanvasListener {
        void drawLayer(Canvas canvas);

        void onKeyDelete();

        void onKeyReturn();

        void onKeyBack();

        void onString(String sval);
    }

    private CanvasListener m_listener;

    public PDFEditCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PDFEditCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PDFEditCanvas(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setFocusable(true);
        setFocusableInTouchMode(true);
        m_imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void vOpen(CanvasListener listener) {
        m_listener = listener;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(Canvas canvas) {
        if (m_listener != null) m_listener.drawLayer(canvas);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    class RDInputConnection implements InputConnection {
        @Override
        public CharSequence getTextBeforeCursor(int n, int flags) {
            return null;
        }

        @Override
        public CharSequence getTextAfterCursor(int n, int flags) {
            return null;
        }

        @Override
        public CharSequence getSelectedText(int flags) {
            return null;
        }

        @Override
        public int getCursorCapsMode(int reqModes) {
            return 0;
        }

        @Override
        public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
            return null;
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (m_listener != null) m_listener.onKeyBack();
            return true;
        }

        @Override
        public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
            return false;
        }

        @Override
        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            return false;
        }

        @Override
        public boolean setComposingRegion(int start, int end) {
            return false;
        }

        @Override
        public boolean finishComposingText() {
            return false;
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            if (m_listener != null)
                m_listener.onString(text.toString());
            return true;
        }

        @Override
        public boolean commitCompletion(CompletionInfo text) {
            return false;
        }

        @Override
        public boolean commitCorrection(CorrectionInfo correctionInfo) {
            return false;
        }

        @Override
        public boolean setSelection(int start, int end) {
            return false;
        }

        @Override
        public boolean performEditorAction(int editorAction) {
            if (m_listener != null)
                m_listener.onKeyReturn();
            return true;
        }

        @Override
        public boolean performContextMenuAction(int id) {
            return false;
        }

        @Override
        public boolean beginBatchEdit() {
            return false;
        }

        @Override
        public boolean endBatchEdit() {
            return false;
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    if (m_listener != null)
                        m_listener.onKeyBack();
                } /*else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (m_listener != null)
                        m_listener.onKeyBack();
                }*/ else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (m_listener != null)
                        m_listener.onKeyReturn();
                }
            }
            postInvalidate();
            return true;
        }

        @Override
        public boolean clearMetaKeyStates(int states) {
            return false;
        }

        @Override
        public boolean reportFullscreenMode(boolean enabled) {
            return false;
        }

        @Override
        public boolean performPrivateCommand(String action, Bundle data) {
            return false;
        }

        @Override
        public boolean requestCursorUpdates(int cursorUpdateMode) {
            return false;
        }

        @Override
        public Handler getHandler() {
            return null;
        }

        @Override
        public void closeConnection() {
        }

        @Override
        public boolean commitContent(@NonNull InputContentInfo inputContentInfo, int flags, @Nullable Bundle opts) {
            return false;
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        outAttrs.inputType = InputType.TYPE_NULL;
        return new RDInputConnection();
    }

    private InputMethodManager m_imm;

    public void inputOpen() {
        /*
        requestFocus();
        //m_imm.showSoftInput(this, 0);
        //m_imm.toggleSoftInput(0,0);
        m_imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        m_imm.restartInput(this);
         */
        post(new Runnable() {
            @Override
            public void run() {
                requestFocus();
                m_imm.showSoftInput(PDFEditCanvas.this, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    public void inputClose() {
        m_imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }
}
