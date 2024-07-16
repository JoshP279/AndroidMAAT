package com.radaee.docx;

import android.os.Bundle;

import com.radaee.comm.RDStream;

public class Document {
    private static native long open(String path, String password);

    private static native long openStream(RDStream stream, String password);

    private static native void close(long hand);

    private static native float[] getPagesMaxSize(long hand);

    private static native float getPageWidth(long hand, int pageno);

    private static native float getPageHeight(long hand, int pageno);

    private static native int getPageCount(long hand);

    private static native long getPage(long hand, int pageno);

    private static native boolean exportPDF(long hanf, long pdf);

    protected long hand_val;

    /**
     * open document.<br/>
     * first time, SDK try password as user password, and then try password as owner password.
     *
     * @param path     PDF file to be open.
     * @param password password or null.
     * @return error code:<br/>
     * 0:succeeded, and continue<br/>
     * -1:need input password<br/>
     * -2:unknown encryption<br/>
     * -3:damaged or invalid format<br/>
     * -10:access denied or invalid file path<br/>
     * others:unknown error
     */
    public int Open(String path, String password) {
        if (hand_val == 0) {
            int ret = 0;
            try {
                hand_val = open(path, password);
            } catch (Exception e) {
                e.printStackTrace();
                hand_val = -10;
            }
            if (hand_val <= 0 && hand_val >= -10)//error
            {
                ret = (int) hand_val;
                hand_val = 0;
            }
            return ret;
        }
        return 0;
    }

    public int OpenStream(RDStream stream, String password) {
        if (hand_val == 0) {
            int ret = 0;
            try {
                hand_val = openStream(stream, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (hand_val <= 0 && hand_val >= -10)//error
            {
                ret = (int) hand_val;
                hand_val = 0;
            }
            return ret;
        }
        return 0;
    }

    public void Close() {
        if (hand_val != 0)
            close(hand_val);
        hand_val = 0;
    }

    /**
     * get max width and max height of all pages.
     *
     * @return 2 elements container width and height values, or null if failed.
     */
    public float[] GetPagesMaxSize() {
        return getPagesMaxSize(hand_val);
    }

    /**
     * get page width by page NO.
     *
     * @param pageno 0 based page NO. range:[0, GetPageCount()-1]
     * @return width value.
     */
    public float GetPageWidth(int pageno) {
        float w = getPageWidth(hand_val, pageno);
        if (w <= 0) return 1;
        else return w;
    }

    /**
     * get page height by page NO.
     *
     * @param pageno 0 based page NO. range:[0, GetPageCount()-1]
     * @return height value.
     */
    public float GetPageHeight(int pageno) {
        float h = getPageHeight(hand_val, pageno);
        if (h <= 0) return 1;
        else return h;
    }

    public int GetPageCount() {
        return getPageCount(hand_val);
    }

    public Page GetPage(int pageno) {
        Page page = new Page(this, getPage(hand_val, pageno));
        if (page.hand != 0) return page;
        else return null;
    }

    /**
     * Export docx file to PDF file.<br/>
     * this function require premium license. and can only export 3 pages if demo package actived.<br/>
     * docx pages will append to tail of PDF file, if PDF file is not empty.<br/>
     * export to no premium license or readonly PDF Object will return false.
     *
     * @param doc PDF document object.
     * @return true or false.
     */
    public boolean ExportPDF(com.radaee.pdf.Document doc) {
        return exportPDF(hand_val, doc.get_hand());
    }

    public static void BundleSave(Bundle bundle, Document doc) {
        bundle.putLong("docx_doc_handle", doc.hand_val);
    }

    public static Document BundleRestore(Bundle bundle) {
        try {
            long hand = bundle.getLong("docx_doc_handle");
            if (hand != 0) {
                Document doc = new Document();
                doc.hand_val = hand;
                return doc;
            } else return null;
        } catch (Exception e) {
            return null;
        }
    }
}
