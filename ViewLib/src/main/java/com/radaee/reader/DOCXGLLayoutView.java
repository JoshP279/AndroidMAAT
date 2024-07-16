package com.radaee.reader;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.radaee.docx.Document;
import com.radaee.view.IDOCXLayoutView;

public class DOCXGLLayoutView extends RelativeLayout implements IDOCXLayoutView {
    public DOCXGLLayoutView(Context context) {
        super(context);
        init(context);
    }

    public DOCXGLLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private DOCXGLView m_view;
    private DOCXGLCanvas m_canvas;

    private void init(Context context) {
        m_view = new DOCXGLView(context);
        m_canvas = new DOCXGLCanvas(context);
        addView(m_view, 0);
        addView(m_canvas, 1);
    }

    public void DOCXOpen(Document doc, DOCXLayoutListener listener) {
        m_view.DOCXOpen(doc, listener, m_canvas, 4);
        m_canvas.vOpen(m_view);
    }

    public void DOCXSetView(int vmode) {
        m_view.DOCXSetView(vmode);
    }

    public void DOCXClose() {
        m_view.DOCXClose();
    }

    public void DOCXSetSelect() {
        m_view.DOCXSetSelect();
    }

    public final void DOCXFindStart(String key, boolean match_case, boolean whole_word) {
        m_view.DOCXFindStart(key, match_case, whole_word);
    }

    public final void DOCXFind(int dir) {
        m_view.DOCXFind(dir);
    }

    @Override
    public void DOCXFindEnd() {
        m_view.DOCXFindEnd();
    }

    public void BundleSavePos(Bundle bundle) {
        m_view.BundleSavePos(bundle);
    }

    public void BundleRestorePos(Bundle bundle) {
        m_view.BundleRestorePos(bundle);
    }

    public void DOCXGotoPage(int pageno) {
        m_view.DOCXGotoPage(pageno);
    }

    public void DOCXScrolltoPage(int pageno) {
        m_view.DOCXScrolltoPage(pageno);
    }

    public void DOCXUpdateCurrPage() {
        m_view.DOCXUpdateCurrPage();
    }

    @Override
    public Document DOCXGetDoc() {
        return m_view.DOCXGetDoc();
    }

    public int DOCXGetCurrPage() {
        return m_view.DOCXGetCurrPage();
    }

    public void DOCXSetBGColor(int color) {
        m_view.DOCXSetBGColor(color);
    }
}