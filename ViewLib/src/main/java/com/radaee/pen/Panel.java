package com.radaee.pen;

import android.graphics.Bitmap;

public class Panel
{
    protected static native long create(int w, int h);
    protected static native void destroy(long hpanel);
    protected static native long getPen(long hpanel);
    protected static native void setPen(long hpanel, long hpen);
    protected static native void onTDown(long hpanel, float x, float y, float p);
    protected static native void onTMove(long hpanel, float x, float y, float p);
    protected static native void onTEnd(long hpanel);
    protected static native boolean endStroke(long hpanel, Bitmap bitmap);
    protected static native boolean getDirty(long hpanel, int[] rect);
    protected static native boolean getRect(long hpanel, int[] rect);
    protected static native boolean update(long hpanel, Bitmap bitmap);
    protected static native boolean updatePanel(long hpanel, Bitmap bitmap);
    protected static native boolean updateBmp(long hpanel, Bitmap bitmap);
    protected static native boolean saveStroke(long hpanel, long sdoc);
    protected static native boolean canUndo(long hpanel, long sdoc);
    protected static native boolean undo(long hpanel, long sdoc, Bitmap bmp);
    protected static native boolean canRedo(long hpanel, long sdoc);
    protected static native boolean redo(long hpanel, long sdoc, Bitmap bmp);
    protected long m_hpanel;
    public Panel(int w, int h)
    {
        m_hpanel = create(w, h);
    }
    @Override
    protected void finalize() throws Throwable
    {
        Destroy();
        super.finalize();
    }
    public void Destroy()
    {
        if(m_hpanel != 0)
        {
            destroy(m_hpanel);
            m_hpanel = 0;
        }
    }
    public Pen GetPen()
    {
        long hpen = getPen(m_hpanel);
        if (hpen == 0) return null;
        return new Pen(hpen);
    }
    public void SetPen(Pen pen)
    {
        setPen(m_hpanel, pen.m_hpen);
    }
    public void OnTouchDown(float x, float y, float p)
    {
        onTDown(m_hpanel, x, y, p);
    }
    public void OnTouchMove(float x, float y, float p)
    {
        onTMove(m_hpanel, x, y, p);
    }
    public void OnTouchEnd()
    {
        onTEnd(m_hpanel);
    }
    public void EndStroke(Bitmap bitmap)
    {
        endStroke(m_hpanel, bitmap);
    }
    public boolean GetStrokeDirty(int[] rect)
    {
        return getDirty(m_hpanel, rect);
    }
    public boolean GetStrokeRect(int[] rect)
    {
        return getRect(m_hpanel, rect);
    }
    public boolean Update(Bitmap bitmap)
    {
        return update(m_hpanel, bitmap);
    }
    public boolean UpdatePanel(Bitmap bitmap)
    {
        return updatePanel(m_hpanel, bitmap);
    }
    public boolean UpdateBmp(Bitmap bitmap)
    {
        return updateBmp(m_hpanel, bitmap);
    }
    public boolean SaveStroke(PenStrokeDoc sdoc)
    {
        return saveStroke(m_hpanel, sdoc.m_hand);
    }
    public boolean CanUndo(PenStrokeDoc sdoc)
    {
        return canUndo(m_hpanel, sdoc.m_hand);
    }
    public boolean Undo(PenStrokeDoc sdoc, Bitmap bmp)
    {
        return undo(m_hpanel, sdoc.m_hand, bmp);
    }
    public boolean CanRedo(PenStrokeDoc sdoc)
    {
        return canRedo(m_hpanel, sdoc.m_hand);
    }
    public boolean Redo(PenStrokeDoc sdoc, Bitmap bmp)
    {
        return redo(m_hpanel, sdoc.m_hand, bmp);
    }
}
