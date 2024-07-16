package com.radaee.ofd;

import android.graphics.Bitmap;
import android.util.Log;

import com.radaee.comm.DIB;
import com.radaee.comm.Global;

public class Page {
    static private native void close(long hand);
    static private native boolean render(long page, long dib, int orgx, int orgy, float dpi);
    static private native boolean renderToBmp(long page, Bitmap bitmap, int orgx, int orgy, float dpi);
    static private native float getWidth(long page);
    static private native float getHeight(long page);

    protected long hand;
    protected Page(long handle) {
        hand = handle;
    }

    public boolean Render(DIB dib, int orgx, int orgy, float dpi) {
        if (dib == null || dpi < 0.01f) return false;
        return render(hand, dib.get_hand(), orgx, orgy, dpi);
    }

    public boolean RenderToBmp(Bitmap bitmap, int orgx, int orgy, float dpi) {
        if (bitmap == null || dpi < 0.01f) return false;
        return renderToBmp(hand, bitmap, orgx, orgy, dpi);
    }

    public float GetWidth()
    {
        return getWidth(hand);
    }
    public float GetHeight()
    {
        return getHeight(hand);
    }

    /**
     * Close page object and free memory.
     */
    final public void Close() {
        long hand_page = hand;
        hand = 0;
        close(hand_page);
    }


    static private native long findOpen(long hand, String str, boolean match_case, boolean whole_word);

    static private native long findOpen2(long hand, String str, boolean match_case, boolean whole_word, boolean skip_blank);

    static private native int findGetCount(long hand_finder);

    static private native int findGetFirstChar(long hand_finder, int index);

    static private native int findGetEndChar(long hand_finder, int index);

    static private native void findClose(long hand_finder);

    static private native void objsStart(long hand);

    static private native String objsGetString(long hand, int from, int to);

    static private native int objsAlignWord(long hand, int from, int dir);

    static private native void objsGetCharRect(long hand, int index, float[] vals);

    static private native int objsGetCharCount(long hand);

    static private native int objsGetCharIndex(long hand, float[] pt);

    static private native String getHLink(long hand, float x, float y);

    public class Finder {
        protected long hand;

        /**
         * get find count in this page.
         *
         * @return count or 0 if no found.
         */
        public final int GetCount() {
            return Page.findGetCount(hand);
        }

        /**
         * get first char index.
         *
         * @param index 0 based index value. range:[0, FindGetCount()-1]
         * @return the first char index of texts, see: ObjsGetString. range:[0, ObjsGetCharCount()-1]
         */
        public final int GetFirstChar(int index) {
            return Page.findGetFirstChar(hand, index);
        }

        public final int GetEndChar(int index) {
            return Page.findGetEndChar(hand, index);
        }

        /**
         * free memory of find session.
         */
        public final void Close() {
            Page.findClose(hand);
            hand = 0;
        }

        @Override
        protected void finalize() throws Throwable {
            Close();
            super.finalize();
        }
    }

    /**
     * get text objects to memory.<br/>
     * a standard license is required for this method
     */
    final public void ObjsStart() {
        objsStart(hand);
    }

    /**
     * get string from range. this can be invoked after ObjsStart
     *
     * @param from 0 based unicode index.
     * @param to   0 based unicode index.
     * @return string or null.
     */
    final public String ObjsGetString(int from, int to) {
        return objsGetString(hand, from, to);
    }

    /**
     * get index aligned by word. this can be invoked after ObjsStart
     *
     * @param from 0 based unicode index.
     * @param dir  if dir < 0,  get start index of the word. otherwise get last index of the word.
     * @return new index value.
     */
    final public int ObjsAlignWord(int from, int dir) {
        return objsAlignWord(hand, from, dir);
    }

    /**
     * get char's box in PDF coordinate system, this can be invoked after ObjsStart
     *
     * @param index 0 based unicode index.
     * @param vals  return 4 elements for PDF rectangle.
     */
    public final void ObjsGetCharRect(int index, float[] vals) {
        objsGetCharRect(hand, index, vals);
    }

    /**
     * get chars count in this page. this can be invoked after ObjsStart<br/>
     * a standard license is required for this method
     *
     * @return count or 0 if ObjsStart not invoked.
     */
    final public int ObjsGetCharCount() {
        return objsGetCharCount(hand);
    }

    /**
     * get char index nearest to point
     *
     * @param pt point as [x,y] in PDF coordinate.
     * @return char index or -1 failed.
     */
    public final int ObjsGetCharIndex(float[] pt) {
        return objsGetCharIndex(hand, pt);
    }

    /**
     * create a find session. this can be invoked after ObjsStart
     *
     * @param str        key string to find.
     * @param match_case match case?
     * @param whole_word match whole word?
     * @return handle of find session, or 0 if no found.
     */
    public Finder FindOpen(String str, boolean match_case, boolean whole_word) {
        long ret = findOpen(hand, str, match_case, whole_word);
        if (ret == 0) return null;
        Finder find = new Finder();
        find.hand = ret;
        return find;
    }

    /**
     * create a find session. this can be invoked after ObjsStart<br/>
     * this function treats line break as blank char.
     *
     * @param str        key string to find.
     * @param match_case match case?
     * @param whole_word match whole word?
     * @param skip_blank skip blank?
     * @return handle of find session, or 0 if no found.
     */
    public Finder FindOpen(String str, boolean match_case, boolean whole_word, boolean skip_blank) {
        long ret = findOpen2(hand, str, match_case, whole_word, skip_blank);
        if (ret == 0) return null;
        Finder find = new Finder();
        find.hand = ret;
        return find;
    }

    public String GetHyperLink(float x, float y) {
        //return getHLink(hand, x, y);
        return null;
    }
}
