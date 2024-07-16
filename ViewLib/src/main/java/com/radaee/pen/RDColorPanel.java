package com.radaee.pen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.radaee.viewlib.R;

public class RDColorPanel extends RelativeLayout
{
    private RDColorPanelView m_panel;
    private RDColorPickerView m_picker;
    private RDColorRectView m_rect;
    private SeekBar m_alpha;
    public interface OnColorChangeListener
    {
        void OnColorChange(int color);
    }
    public RDColorPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        m_panel = findViewById(R.id.vw_panel);
        m_picker = findViewById(R.id.vw_picker);
        m_rect = findViewById(R.id.vw_rect);
        m_alpha = findViewById(R.id.seek_alpha);
        m_alpha.setMax(255);
        m_alpha.setProgress(255);
        m_picker.SetListener(new RDColorPickerView.OnColorChangeListener() {
            @Override
            public void OnColorChange(int color) {
                m_panel.SetColor(color);
            }
        });
        m_panel.SetListener(new RDColorPanelView.OnColorChangeListener() {
            @Override
            public void OnColorChange(int color) {
                m_rect.SetColor(color);
            }
        });
    }

    public void SetColor(int color)
    {
        m_alpha.setProgress((color >> 24) & 255);
        int cr = (color >> 16) & 0xff;
        int cg = (color >> 8) & 0xff;
        int cb = color & 0xff;
        int white = cr;
        int black = cr;
        if(white > cg) white = cg;
        if(white > cb) white = cb;
        if(black < cg) black = cg;
        if(black < cb) black = cb;
        black = 255 - black;

        cr -= white;
        cg -= white;
        cb -= white;
        int cval = cr;
        if(cval < cg) cval = cg;
        if(cval < cb) cval = cb;
        if (cr == cg && cr == cb)
        {
            //gray color.
            int icolor = 0xFF000000 + (white<<16) + (white<<8) + white;
            m_picker.LocColor(255, 0, 0);
            m_panel.SetColor(icolor);
            m_panel.LocColor(cr, cg, cb);
        }
        else
        {
            cr = cr * 255 / cval;
            cg = cg * 255 / cval;
            cb = cb * 255 / cval;
            int icolor = 0xFF000000 + (cr<<16) + (cg<<8) + cb;
            m_picker.LocColor(cr, cg, cb);
            m_panel.SetColor(icolor);
            m_panel.LocColor(cr, cg, cb);
        }
        m_rect.SetColor(color);
    }
    public int GetColor()
    {
        int alpha = m_alpha.getProgress();
        return (m_rect.GetColor() & 0xFFFFFF) | (alpha << 24);
    }
    private OnColorChangeListener m_listener;
    public void SetListener(OnColorChangeListener listener)
    {
        m_listener = listener;
    }
    public void dismiss()
    {
        setVisibility(View.GONE);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (m_listener != null)
            m_listener.OnColorChange(GetColor());
        setVisibility(View.GONE);
        return true;
    }
}
