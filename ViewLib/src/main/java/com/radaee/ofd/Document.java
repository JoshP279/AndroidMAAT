package com.radaee.ofd;

import android.os.Bundle;

import com.radaee.comm.RDStream;

public class Document {
    private static native void close(long hand);
    private static native int getPageCount(long hand);
    private static native long getPage(long hand, int pageno);
    private long hand_val;

    protected Document(long hand)
    {
        hand_val = hand;
    }
    public void Close() {
        if (hand_val != 0)
            close(hand_val);
        hand_val = 0;
    }

    public int GetPageCount() {
        return getPageCount(hand_val);
    }

    public Page GetPage(int pageno) {
        long handp = getPage(hand_val, pageno);
        if (handp == 0) return null;
        return new Page(handp);
    }
    private float[] m_pgsize;
    public float[] GetPagesMaxSize()
    {
        int cnt = getPageCount(hand_val);
        m_pgsize = new float[cnt << 1];
        float maxw = 0;
        float maxh = 0;
        for (int pgn = 0; pgn < cnt; pgn++)
        {
            Page page = GetPage(pgn);
            float pw = page.GetWidth();
            float ph = page.GetHeight();
            if (maxw < pw) maxw = pw;
            if (maxh < ph) maxh = ph;
            m_pgsize[(pgn << 1) + 0] = pw;
            m_pgsize[(pgn << 1) + 1] = ph;
            page.Close();
        }
        return new float[]{maxw, maxh};
    }

    public float GetPageWidth(int pno)
    {
        return m_pgsize[pno << 1];
    }
    public float GetPageHeight(int pno)
    {
        return m_pgsize[(pno << 1) + 1];
    }

    public static void BundleSave(Bundle bundle, Document doc) {
        bundle.putLong("docx_doc_handle", doc.hand_val);
    }

    public static Document BundleRestore(Bundle bundle) {
        try {
            long hand = bundle.getLong("docx_doc_handle");
            if (hand != 0) {
                return new Document(hand);
            } else return null;
        } catch (Exception e) {
            return null;
        }
    }
}
