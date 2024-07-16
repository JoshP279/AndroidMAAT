package com.radaee.view;

import android.graphics.Canvas;
import android.os.Bundle;

import com.radaee.ofd.Document;

public interface IOFDLayoutView {
    interface OFDLayoutListener {
        /**
         * call when page scrolling.
         *
         * @param pageno
         */
        void OnOFDPageChanged(int pageno);

        /**
         * call when blank tapped on page, this mean not annotation tapped.
         */
        void OnOFDBlankTapped();


        void OnOFDSelectEnd();

        /**
         * call select status end.
         *
         * @param text selected text string
         */
        void OnOFDTextSelected(String text, float x, float y);

        void OnOFDOpenURI(String uri);

        /**
         * call when zoom start.
         */
        void OnOFDZoomStart();

        /**
         * call when zoom end
         */
        void OnOFDZoomEnd();

        boolean OnOFDDoubleTapped(float x, float y);

        void OnOFDLongPressed(float x, float y);

        /**
         * call when search finished. each search shall call back each time.
         *
         * @param found
         */
        void OnOFDSearchFinished(boolean found);

        /**
         * call when page displayed on screen.
         *
         * @param canvas
         * @param vpage
         */
        void OnOFDPageDisplayed(Canvas canvas, IVPage vpage);

        /**
         * call when page is rendered by backing thread.
         *
         * @param vpage
         */
        void OnOFDPageRendered(IVPage vpage);
    }

    interface IVPage {
        public int GetPageNo();

        public int GetVX(float docxx);

        public int GetVY(float docy);

        public float ToOFDX(float x, float scrollx);

        public float ToOFDY(float y, float scrolly);

        public float ToDIBX(float x);

        public float ToDIBY(float y);

        public float ToOFDSize(float val);
    }

    /**
     * attach OFD document object to reader. and initialize reader
     *
     * @param doc      OFD Document object
     * @param listener callback listener.
     */
    void OFDOpen(Document doc, OFDLayoutListener listener);

    /**
     * close reader.
     */
    void OFDClose();

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
    void OFDSetView(int vmode);

    /**
     * set select status or end status.<br/>
     * if current status is select status, set status to none.<br/>
     * if current status is none status, set status to select.
     */
    void OFDSetSelect();

    void OFDFindStart(String key, boolean match_case, boolean whole_word);

    void OFDFind(int dir);

    void OFDFindEnd();

    void BundleSavePos(Bundle bundle);

    void BundleRestorePos(Bundle bundle);

    void OFDGotoPage(int pageno);

    void OFDScrolltoPage(int pageno);

    Document OFDGetDoc();

    int OFDGetCurrPage();
}
