package com.radaee.pen;

public class Pen {
    protected static native long createPen();
    protected static native long createColorPen();
    protected static native long createMarkPen();
    protected static native long createRubber();
    protected static native void destroy(long hpen);
    protected static native float getWidth(long hpen);
    protected static native int getColor(long hpen);
    protected static native int getType(long hpen);
    protected static native void setWidth(long hpen, float width);
    protected static native void setColor(long hpen, int clr);
    protected long m_hpen;
    protected boolean m_ref;
    public Pen(int type)
    {
        switch(type)
        {
            case 1:
                m_hpen = createPen();
                break;
            case 2:
                m_hpen = createColorPen();
                break;
            case 3:
                m_hpen = createMarkPen();
                break;
            case 100:
                m_hpen = createRubber();
                break;
            default:
                m_hpen = createPen();
                break;
        }
    }
    protected Pen(long hpen)
    {
        m_hpen = hpen;
        m_ref = true;
    }
    @Override
    protected void finalize() throws Throwable
    {
        Destroy();
        super.finalize();
    }
    public int GetType()
    {
        return getType(m_hpen);
    }
    public float GetWidth()
    {
        return getWidth(m_hpen);
    }
    public int GetColor()
    {
        return getColor(m_hpen);
    }
    public void SetWidth(float width)
    {
        setWidth(m_hpen, width);
    }
    public void SetColor(int color)
    {
        setColor(m_hpen, color);
    }
    public void Destroy()
    {
        if(m_hpen != 0)
        {
            if (!m_ref) destroy(m_hpen);
            m_hpen = 0;
            m_ref = false;
        }
    }
}
