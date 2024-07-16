package com.radaee.pdf;

public class EditNode {
    static private native void destroy(long enode);
    static private native void delete(long enode);
    static private native int getType(long enode);
    static private native void updateRect(long enode);
    static private native float[] getRect(long enode);
    static private native void setRect(long enode, float[] rect);
    static private native long getCharPos(long enode, float pdfx, float pdfy);
    static private native float[] getCharRect(long enode, long pos);
    static private native String charGetString(long enode, long pos0, long pos1);
    static private native long getCharPrev(long enode, long pos);
    static private native long getCharNext(long enode, long pos);
    static private native void charDelete(long enode, long start, long end);
    static private native long charInsert(long enode, long pos, String val);
    static private native void charReturn(long enode, long pos);
    static private native void setDefFont(String fname);
    static private native void setDefCJKFont(String fname);
    private long hand;
    protected EditNode(long handle)
    {
        hand = handle;
    }
    @Override
    protected void finalize() throws Throwable
    {
        destroy(hand);
        hand = 0;
        super.finalize();
    }
    public void Destroy()
    {
        destroy(hand);
        hand = 0;
    }
    public void RemoveFromPage()
    {
        delete(hand);
        hand = 0;
    }

    /**
     * update text block size, after text deleted/text inserted/line returned.
     */
    public void UpdateSize()
    {
        updateRect(hand);
    }
    /**
     * get type of block
     * @return 1: text block, 3: vector path fill, 4: vector path stroke, 10: image
     */
    public int GetType()
    {
        return getType(hand);
    }

    /**
     * get rect area in PDF coordinate
     * @return
     */
    public float[] GetRect()
    {
        return getRect(hand);
    }

    /**
     * set rect area
     * @param rect rect area in PDF coordinate
     */
    public void SetRect(float[] rect)
    {
        setRect(hand, rect);
    }
    public long GetCharPos(float pdfx, float pdfy)
    {
        return getCharPos(hand, pdfx, pdfy);
    }
    /**
     * get relative rect area in Text block
     * @param pos return from GetCharPos
     * @return relative rect area in Text block
     */
    public float[] GetCharRect(long pos)
    {
        return getCharRect(hand, pos);
    }
    public long GetCharPrev(long pos)
    {
        return getCharPrev(hand, pos);
    }
    public long GetCharNext(long pos)
    {
        return getCharNext(hand, pos);
    }
    public void TextDelete(long pos_start, long pos_end)
    {
        charDelete(hand, pos_start, pos_end);
    }
    public long TextInsert(long pos, String sval)
    {
        return charInsert(hand, pos, sval);
    }
    public void TextReturn(long pos)
    {
        charReturn(hand, pos);
    }
    public String TextGetString(long pos0, long pos1)
    {
        return charGetString(hand, pos0, pos1);
    }
    static public boolean caret_is_end(long caret)
    {
        return (caret & 1) != 0;
    }
    static public boolean caret_is_vert(long caret)
    {
        return (caret & 2) != 0;
    }
    static public boolean caret_is_same(long pos0, long pos1)
    {
        if (pos0 == pos1) return true;
        int ic0 = (int)((pos0 >> 16) & 65535);
        int ic1 = (int)((pos1 >> 16) & 65535);
        return ((pos0 >> 32) == (pos1 >> 32) && ic0 + 1 == ic1 && !caret_is_end(pos0) && caret_is_end(pos1));
    }
    static public long caret_regular_end(long pos)
    {
        if(caret_is_end(pos))
        {
            int ic0 = ((int)((pos >> 16) & 65535)) + 1;
            int if0 = ((int)(pos & 65535)) & (~1);
            pos &= (~0xffffffffl);
            pos += (ic0 << 16) + if0;
        }
        return pos;
    }
    static public boolean caret_is_first(long pos)
    {
        return ((pos >> 32) == 0 && ((pos >> 16) & 65535) == 0 && (pos & 1) == 0);
    }
    public long caret_regular_start(long pos)
    {
        if(caret_is_end(pos))
        {
            pos &= (~1l);
            pos = getCharNext(hand, pos);
        }
        return pos;
    }
    static public void SetDefFont(String fname)
    {
        setDefFont(fname);
    }
    static public void SetDefCJKFont(String fname)
    {
        setDefCJKFont(fname);
    }
}
