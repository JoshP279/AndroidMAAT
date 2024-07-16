package com.radaee.reader;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.radaee.ofd.Document;
import com.radaee.view.IOFDLayoutView;

public class OFDGLLayoutView extends RelativeLayout implements IOFDLayoutView {
    public OFDGLLayoutView(Context context) {
        super(context);
        init(context);
    }

    public OFDGLLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private OFDGLView m_view;
    private OFDGLCanvas m_canvas;

    private void init(Context context) {
        m_view = new OFDGLView(context);
        m_canvas = new OFDGLCanvas(context);
        addView(m_view, 0);
        addView(m_canvas, 1);
    }

    public void OFDOpen(Document doc, OFDLayoutListener listener) {
        m_view.OFDOpen(doc, listener, m_canvas, 4);
        m_canvas.vOpen(m_view);
    }

    public void OFDSetView(int vmode) {
        m_view.OFDSetView(vmode);
    }

    public void OFDClose() {
        m_view.DOCXClose();
    }

    public void OFDSetSelect() {
        m_view.OFDSetSelect();
    }

    public final void OFDFindStart(String key, boolean match_case, boolean whole_word) {
        m_view.OFDFindStart(key, match_case, whole_word);
    }

    public final void OFDFind(int dir) {
        m_view.OFDFind(dir);
    }

    @Override
    public void OFDFindEnd() {
        m_view.OFDFindEnd();
    }

    public void BundleSavePos(Bundle bundle) {
        m_view.BundleSavePos(bundle);
    }

    public void BundleRestorePos(Bundle bundle) {
        m_view.BundleRestorePos(bundle);
    }

    public void OFDGotoPage(int pageno) {
        m_view.OFDGotoPage(pageno);
    }

    public void OFDScrolltoPage(int pageno) {
        m_view.OFDScrolltoPage(pageno);
    }

    public void OFDUpdateCurrPage() {
        m_view.OFDUpdateCurrPage();
    }

    @Override
    public Document OFDGetDoc() {
        return m_view.OFDGetDoc();
    }

    public int OFDGetCurrPage() {
        return m_view.OFDGetCurrPage();
    }

    public void OFDSetBGColor(int color) {
        m_view.OFDSetBGColor(color);
    }
}