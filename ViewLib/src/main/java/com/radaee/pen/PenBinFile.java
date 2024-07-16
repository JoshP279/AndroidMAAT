package com.radaee.pen;

public class PenBinFile {
    private static native long open(String path);
    private static native int readInt(long file);
    private static native long readLong(long file);
    private static native String readString(long file);
    private static native void writeInt(long file, int val);
    private static native void writeLong(long file, long val);
    private static native void writeString(long file, String val);
    private static native void close(long file);

    protected long m_hand;
    public PenBinFile(String path)
    {
        m_hand = open(path);
    }
    public int ReadInt()
    {
        return readInt(m_hand);
    }
    public long ReadLong()
    {
        return readLong(m_hand);
    }
    public String ReadString()
    {
        return readString(m_hand);
    }
    public void WriteInt(int val)
    {
        writeInt(m_hand, val);
    }
    public void WriteLong(long val)
    {
        writeLong(m_hand, val);
    }
    public void WriteString(String val)
    {
        writeString(m_hand, val);
    }
    public void Close()
    {
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
