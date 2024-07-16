package com.radaee.docx;

import android.graphics.Bitmap;
import android.util.Log;

import com.radaee.comm.DIB;
import com.radaee.comm.Global;

public class Page {
    static private native void renderPrepare(long hand, long dib);

    static private native void renderCancel(long hand);

    static private native boolean renderIsFinished(long hand);

    static private native void close(long hand);

    static private native boolean render(long page, long dib, float scale, int orgx, int orgy, int quality);

    static private native boolean renderToBmp(long page, Bitmap bitmap, float scale, int orgx, int orgy, int quality);

    protected long hand;
    protected Document m_doc;

    protected Page(Document doc, long handle) {
        m_doc = doc;
        hand = handle;
    }

    /**
     * prepare to render. it reset dib pixels to white value, and reset page status.<br/>
     * if dib is null, only to reset page status.
     *
     * @param dib DIB object to render. get from Global.dibGet() or null.
     */
    final public void RenderPrepare(DIB dib) {
        if (dib == null)
            renderPrepare(hand, 0);
        else
            renderPrepare(hand, dib.get_hand());
    }

    public boolean Render(DIB dib, float scale, int orgx, int orgy) {
        if (dib == null || scale < 0.01f) return false;
        return render(hand, dib.get_hand(), scale, orgx, orgy, Global.g_render_quality);
    }

    public boolean RenderToBmp(Bitmap bitmap, float scale, int orgx, int orgy) {
        if (bitmap == null || scale < 0.01f) return false;
        return renderToBmp(hand, bitmap, scale, orgx, orgy, Global.g_render_quality);
    }

    /**
     * set page status to cancelled and cancel render function.
     */
    final public void RenderCancel() {
        renderCancel(hand);
    }

    /**
     * check if page rendering is finished.
     *
     * @return true or false
     */
    final public boolean RenderIsFinished() {
        return renderIsFinished(hand);
    }

    /**
     * Close page object and free memory.
     */
    final public void Close() {
        long hand_page = hand;
        hand = 0;
        if (m_doc != null) {
            if (m_doc.hand_val != 0)
                close(hand_page);
            else
                Log.e("Bad Coding", "Document object closed, but Page object not closed, will cause memory leaks.");
            m_doc = null;
        }
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
        return getHLink(hand, x, y);
    }
}
