package com.radaee.reader;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.radaee.annotui.UIAnnotMenu;
import com.radaee.pdf.Document;
import com.radaee.view.IPDFLayoutView;

public class PDFGLLayoutView extends RelativeLayout implements IPDFLayoutView {
    public PDFGLLayoutView(Context context) {
        super(context);
        init(context);
    }

    public PDFGLLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private PDFGLView m_view;
    private PDFGLCanvas m_canvas;
    private UIAnnotMenu m_annot_menu;

    private void init(Context context) {
        m_view = new PDFGLView(context);
        m_canvas = new PDFGLCanvas(context);
        addView(m_view, 0);
        addView(m_canvas, 1);
        m_annot_menu = new UIAnnotMenu(this);
    }

    public void PDFOpen(Document doc, IPDFLayoutView.PDFLayoutListener listener) {
        m_view.PDFOpen(doc, listener, m_canvas, 4);
        m_view.setAnnotMenu(m_annot_menu);
        m_canvas.vOpen(m_view);
    }

    public void PDFSetView(int vmode) {
        m_view.PDFSetView(vmode);
    }

    public void PDFCloseOnUI() {
        m_view.PDFCloseOnUI();
    }
    public void PDFClose() {
        m_view.PDFClose();
    }

    public void PDFSetInk(int code) {
        m_view.PDFSetInk(code);
    }

    @Override
    public void PDFSetPolygon(int code) {
        m_view.PDFSetPolygon(code);
    }

    @Override
    public void PDFSetPolyline(int code) {
        m_view.PDFSetPolyline(code);
    }

    public void PDFSetRect(int code) {
        m_view.PDFSetRect(code);
    }

    public void PDFSetEllipse(int code) {
        m_view.PDFSetEllipse(code);
    }

    public void PDFSetEditbox(int code) {
    }

    public void PDFSetSelect() {
        m_view.PDFSetSelect();
    }

    public void PDFSetNote(int code) {
        m_view.PDFSetNote(code);
    }

    public void PDFSetLine(int code) {
        m_view.PDFSetLine(code);
    }

    public void PDFSetStamp(int code) {
        m_view.PDFSetStamp(code);
    }

    @Override
    public boolean PDFSetAttachment(String attachmentPath) { //TODO
        return false;
    }

    public void PDFCancelAnnot() {
        m_view.PDFCancelAnnot();
    }

    public void PDFRemoveAnnot() {
        m_view.PDFRemoveAnnot();
    }

    public void PDFEndAnnot() {
        m_view.PDFEndAnnot();
    }

    public void PDFEditAnnot() {
        m_view.PDFEditAnnot();
    }

    public void PDFPerformAnnot() {
        m_view.PDFPerformAnnot();
    }

    public final void PDFFindStart(String key, boolean match_case, boolean whole_word) {
        m_view.PDFFindStart(key, match_case, whole_word);
    }

    public final void PDFFind(int dir) {
        m_view.PDFFind(dir);
    }

    @Override
    public void PDFFindEnd() {
        m_view.PDFFindEnd();
    }

    @Override
    public void PDFSetScale(float scale) {
        m_view.PDFSetScale(scale);
    }

    public boolean PDFSetSelMarkup(int type) {
        return m_view.PDFSetSelMarkup(type);
    }

    public Document PDFGetDoc() {
        return m_view.PDFGetDoc();
    }

    public void BundleSavePos(Bundle bundle) {
        m_view.BundleSavePos(bundle);
    }

    public void BundleRestorePos(Bundle bundle) {
        m_view.BundleRestorePos(bundle);
    }

    public void PDFGotoPage(int pageno) {
        m_view.PDFGotoPage(pageno);
    }

    public void PDFScrolltoPage(int pageno) {
        m_view.PDFScrolltoPage(pageno);
    }

    public void PDFUndo() {
        m_view.PDFUndo();
    }

    public void PDFRedo() {
        m_view.PDFRedo();
    }
    public boolean PDFCanUndo() {
        return m_view.PDFCanUndo();
    }
    public boolean PDFCanRedo() {
        return m_view.PDFCanRedo();
    }

    public boolean PDFCanSave() {
        return m_view.PDFCanSave();
    }

    public boolean PDFSave() {
        return m_view.PDFSave();
    }

    public void PDFUpdatePage(int pageno) {
        m_view.PDFUpdatePage(pageno);
    }

    public void PDFSetBGColor(int color) {
        m_view.PDFSetBGColor(color);
    }

    public boolean PDFEraseSel() {
        return false;
    }

    public void PDFSaveView() {
        m_view.PDFSaveView();
    }

    public void PDFRestoreView() {
        m_view.PDFRestoreView();
    }

    public void PDFSetEdit(int code) {
    }

    public void onPause()
    {
        m_view.onPause();
    }
    public void onResume()
    {
        m_view.onResume();
    }
}
