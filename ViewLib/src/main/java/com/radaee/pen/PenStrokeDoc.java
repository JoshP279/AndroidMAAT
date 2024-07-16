package com.radaee.pen;

public class PenStrokeDoc {
    protected long m_hand;
    private static native long open(String path);
    private static native void save(long sdoc);
    private static native void close(long sdoc);
    public PenStrokeDoc(String path)
    {
        m_hand = open(path);
    }
    public void Save()
    {
        if (m_hand == 0) return;
        save(m_hand);
    }
    public void Close()
    {
        if (m_hand == 0) return;
        close(m_hand);
        m_hand = 0;
    }
    @Override
    protected void finalize() throws Throwable
    {
        Close();
        super.finalize();
    }
}
