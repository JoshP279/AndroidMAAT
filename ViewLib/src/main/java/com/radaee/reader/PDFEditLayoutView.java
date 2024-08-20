package com.radaee.reader;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.radaee.annotui.UIAnnotMenu;
import com.radaee.annotui.UIEditMenu;
import com.radaee.pdf.Document;
import com.radaee.pdf.Ink;
import com.radaee.view.IPDFLayoutView;

public class PDFEditLayoutView extends RelativeLayout implements IPDFLayoutView {
    public PDFEditLayoutView(Context context) {
        super(context);
        init(context);
    }

    public PDFEditLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private PDFEditView m_view;
    private PDFEditCanvas m_canvas;
    private UIAnnotMenu m_annot_menu;
    private UIEditMenu m_edit_menu;

    private void init(Context context) {
        m_view = new PDFEditView(context);
        m_canvas = new PDFEditCanvas(context);
        addView(m_view, 0);
        addView(m_canvas, 1);
        m_annot_menu = new UIAnnotMenu(this);
        m_edit_menu = new UIEditMenu(this);
    }

    public void PDFOpen(Document doc, PDFLayoutListener listener) {
        m_view.PDFOpen(doc, listener, m_canvas, 4);
        m_view.setAnnotMenu(m_annot_menu);
        m_view.setEditMenu(m_edit_menu);
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

    public void PDFSetAnnot(Ink ink, int page){
        m_view.PDFSetAnnot(ink, page);
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
        m_view.PDFSetEditbox(code);
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

    public void PDFSetFieldSign(int code) {
        m_view.PDFSetFieldSign(code);
    }

    public void PDFSetFieldCheckbox(int code) {
        m_view.PDFSetFieldCheckbox(code);
    }

    public void PDFSetFieldRadio(int code) {
        m_view.PDFSetFieldRadio(code);
    }

    public void PDFSetFieldEditbox(int code) {
        m_view.PDFSetFieldEditbox(code);
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

    public boolean PDFCanUndo() {
        return m_view.PDFCanUndo();
    }

    public void PDFRedo() {
        m_view.PDFRedo();
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
        return m_view.PDFEraseSel();
    }

    public void PDFSetEdit(int code) {
        m_view.PDFSetEdit(code);
    }

    public void PDFSaveView() {
        m_view.PDFSaveView();
    }

    public void PDFRestoreView() {
        m_view.PDFRestoreView();
    }

    public void PDFSetFTSRect(float[] rect, int pageno) {
        m_view.PDFSetFTSRect(rect, pageno);
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
