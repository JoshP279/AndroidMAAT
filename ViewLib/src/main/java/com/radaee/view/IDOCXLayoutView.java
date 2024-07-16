package com.radaee.view;

import android.graphics.Canvas;
import android.os.Bundle;

import com.radaee.docx.Document;

public interface IDOCXLayoutView {
    interface DOCXLayoutListener {
        /**
         * call when page scrolling.
         *
         * @param pageno
         */
        void OnDOCXPageChanged(int pageno);

        /**
         * call when blank tapped on page, this mean not annotation tapped.
         */
        void OnDOCXBlankTapped();

        void OnDOCXSelectEnd();
        /**
         * call select status end.
         *
         * @param text selected text string
         * @param x the x-coordination of end point
         * @param y the x-coordination of end point
         */
        void OnDOCXTextSelected(String text, float x, float y);

        void OnDOCXOpenURI(String uri);

        /**
         * call when zoom start.
         */
        void OnDOCXZoomStart();

        /**
         * call when zoom end
         */
        void OnDOCXZoomEnd();

        boolean OnDOCXDoubleTapped(float x, float y);

        void OnDOCXLongPressed(float x, float y);

        /**
         * call when search finished. each search shall call back each time.
         *
         * @param found
         */
        void OnDOCXSearchFinished(boolean found);

        /**
         * call when page displayed on screen.
         *
         * @param canvas
         * @param vpage
         */
        void OnDOCXPageDisplayed(Canvas canvas, IVPage vpage);

        /**
         * call when page is rendered by backing thread.
         *
         * @param vpage
         */
        void OnDOCXPageRendered(IVPage vpage);
    }

    interface IVPage {
        public int GetPageNo();

        public int GetVX(float docxx);

        public int GetVY(float docy);

        public float ToDOCXX(float x, float scrollx);

        public float ToDOCXY(float y, float scrolly);

        public float ToDIBX(float x);

        public float ToDIBY(float y);

        public float ToDOCXSize(float val);
    }

    /**
     * attach DOCX document object to reader. and initialize reader
     *
     * @param doc      DOCX Document object
     * @param listener callback listener.
     */
    void DOCXOpen(Document doc, DOCXLayoutListener listener);

    /**
     * close reader.
     */
    void DOCXClose();

    /**
     * set view mode, it sam as Global.def_mode.
     *
     * @param vmode view mode
     *              0:vertical<br/>
     *              1:horizon<br/>
     *              2:curl effect(opengl only)<br/>
     *              3:single<br/>
     *              4:SingleEx<br/>
     *              5:Reflow(opengl only)<br/>
     *              6:show 2 page as 1 page in land scape mode
     */
    void DOCXSetView(int vmode);

    /**
     * set select status or end status.<br/>
     * if current status is select status, set status to none.<br/>
     * if current status is none status, set status to select.
     */
    void DOCXSetSelect();

    void DOCXFindStart(String key, boolean match_case, boolean whole_word);

    void DOCXFind(int dir);

    void DOCXFindEnd();

    void BundleSavePos(Bundle bundle);

    void BundleRestorePos(Bundle bundle);

    void DOCXGotoPage(int pageno);

    void DOCXScrolltoPage(int pageno);

    Document DOCXGetDoc();

    int DOCXGetCurrPage();
}
