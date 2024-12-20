package com.radaee.reader;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.radaee.annotui.UIAnnotDlgSign;
import com.radaee.annotui.UIAnnotDlgSignProp;
import com.radaee.annotui.UIAnnotMenu;
import com.radaee.annotui.UIAnnotPopCombo;
import com.radaee.annotui.UIAnnotPopEdit;
import com.radaee.comm.Global;
import com.radaee.comm.Matrix;
import com.radaee.pdf.Document;
import com.radaee.pdf.Ink;
import com.radaee.pdf.Page;
import com.radaee.pdf.Path;
import com.radaee.util.CommonUtil;
import com.radaee.view.IPDFLayoutView;
import com.radaee.view.PDFEditLayoutDual;
import com.radaee.view.PDFEditLayoutVert;
import com.radaee.view.PDFGLLayout;
import com.radaee.view.PDFGLLayoutCurl;
import com.radaee.view.PDFGLLayoutDual;
import com.radaee.view.PDFGLLayoutHorz;
import com.radaee.view.PDFGLLayoutReflow;
import com.radaee.view.PDFGLLayoutVert;
import com.radaee.view.PDFGLPage;
import com.radaee.view.PDFSel;
import com.radaee.viewlib.R;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PDFGLView extends GLSurfaceView implements PDFGLCanvas.CanvasListener {
    protected interface IPopupShow {
        void OnPopupEdit();
        void OnPopupCombo();
    }

    static final public int STA_NONE = 0;
    static final public int STA_ZOOM = 1;
    static final public int STA_SELECT = 2;
    static final public int STA_INK = 3;
    static final public int STA_RECT = 4;
    static final public int STA_ELLIPSE = 5;
    static final public int STA_NOTE = 6;
    static final public int STA_LINE = 7;
    static final public int STA_STAMP = 8;
    static final public int STA_EDITBOX = 9;
    static final public int STA_POLYGON = 10;
    static final public int STA_POLYLINE = 11;
    static final public int STA_FIELD_SIGNATURE = 20;
    static final public int STA_FIELD_EDITBOX = 21;
    static final public int STA_FIELD_CHECK = 22;
    static final public int STA_FIELD_RADIO = 23;
    static final public int STA_ANNOT = 100;
    private int m_status = STA_NONE;
    private PDFGLLayout m_layout;
    private GestureDetector m_gesture;
    private IPDFLayoutView.PDFLayoutListener m_listener;
    private PDFGLCanvas m_canvas;
    private int m_w;
    private int m_h;
    private int m_back_color = 0xFFC0C0C0;

    private interface IAnnotOP
    {
        void onDraw(Canvas canvas);
        void onTouch(MotionEvent event);
    }
    static private Paint NewPaintBorder()
    {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(0x80000000);
        return paint;
    }
    static private Paint NewPaintLine()
    {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(0x400000FF);
        return paint;
    }
    static private Paint NewPaintFill()
    {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x80FFFFFF);
        return paint;
    }
    private void MovRect(float dx, float dy)
    {
        m_annot_rect[0] = m_annot_rect0[0] + dx;
        m_annot_rect[1] = m_annot_rect0[1] + dy;
        m_annot_rect[2] = m_annot_rect0[2] + dx;
        m_annot_rect[3] = m_annot_rect0[3] + dy;
    }
    private final IAnnotOP m_annot_op_normal = new IAnnotOP() {
        private final Paint m_paint = NewPaintBorder();
        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawRect(m_annot_rect[0],
                    m_annot_rect[1],
                    m_annot_rect[2],
                    m_annot_rect[3], m_paint);
        }

        @Override
        public void onTouch(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    m_annot_x0 = event.getX();
                    m_annot_y0 = event.getY();
                    if (m_annot_x0 > m_annot_rect[0] && m_annot_y0 > m_annot_rect[1] &&
                            m_annot_x0 < m_annot_rect[2] && m_annot_y0 < m_annot_rect[3]) {
                        m_annot_rect0 = new float[4];
                        m_annot_rect0[0] = m_annot_rect[0];
                        m_annot_rect0[1] = m_annot_rect[1];
                        m_annot_rect0[2] = m_annot_rect[2];
                        m_annot_rect0[3] = m_annot_rect[3];
                    } else
                        m_annot_rect0 = null;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (m_annot_rect0 != null) {
                        float x = event.getX();
                        float y = event.getY();
                        MovRect(x - m_annot_x0, y - m_annot_y0);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (m_annot_rect0 != null) {
                        float x = event.getX();
                        float y = event.getY();
                        PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) x, (int) y);
                        MovRect(x - m_annot_x0, y - m_annot_y0);
                        if (m_annot_page.GetPageNo() == pos.pageno) {
                            m_annot_rect0[0] = m_annot_page.ToPDFX(m_annot_rect[0], m_layout.vGetX());
                            m_annot_rect0[1] = m_annot_page.ToPDFY(m_annot_rect[3], m_layout.vGetY());
                            m_annot_rect0[2] = m_annot_page.ToPDFX(m_annot_rect[2], m_layout.vGetX());
                            m_annot_rect0[3] = m_annot_page.ToPDFY(m_annot_rect[1], m_layout.vGetY());
                            //add to redo/undo stack.
                            float[] rect = m_annot.GetRect();
                            m_opstack.push(new OPMove(pos.pageno, rect, pos.pageno, m_annot.GetIndexInPage(), m_annot_rect0));
                            m_annot.SetRect(m_annot_rect0[0], m_annot_rect0[1], m_annot_rect0[2], m_annot_rect0[3]);
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            m_layout.gl_render(m_annot_page);
                            if (m_listener != null)
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                        } else {
                            PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                            Page page = m_doc.GetPage(vpage.GetPageNo());
                            if (page != null) {
                                page.ObjsStart();
                                m_annot_rect0[0] = vpage.ToPDFX(m_annot_rect[0], m_layout.vGetX());
                                m_annot_rect0[1] = vpage.ToPDFY(m_annot_rect[3], m_layout.vGetY());
                                m_annot_rect0[2] = vpage.ToPDFX(m_annot_rect[2], m_layout.vGetX());
                                m_annot_rect0[3] = vpage.ToPDFY(m_annot_rect[1], m_layout.vGetY());
                                //add to redo/undo stack.
                                float[] rect = m_annot.GetRect();
                                m_opstack.push(new OPMove(m_annot_page.GetPageNo(), rect, pos.pageno, page.GetAnnotCount(), m_annot_rect0));
                                m_annot.MoveToPage(page, m_annot_rect0);
                                m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                                //page.CopyAnnot(m_annot, m_annot_rect0);
                                page.Close();
                                m_layout.gl_render(m_annot_page);
                            }
                            m_layout.gl_render(vpage, m_annot_rect0);
                            if (m_listener != null) {
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                                m_listener.OnPDFPageModified(vpage.GetPageNo());
                            }
                        }
                        requestRender();
                    }
                    PDFEndAnnot();
                    break;
            }
        }
    };
    private final IAnnotOP m_annot_op_resize = new IAnnotOP() {
        private final Paint m_paint_s = NewPaintBorder();
        private final Paint m_paint_f = NewPaintFill();
        private int m_gap = 0;
        private int m_node = 0;
        @Override
        public void onDraw(Canvas canvas) {
            if (m_gap == 0) m_gap = dp2px(getContext(), 4);
            float left = m_annot_rect[0];
            float top = m_annot_rect[1];
            float right = m_annot_rect[2];
            float bot = m_annot_rect[3];
            float xmid = (left + right) * 0.5f;
            float ymid = (top + bot) * 0.5f;

            canvas.drawRect(left, top, right, bot, m_paint_s);

            canvas.drawRect(left - m_gap, top - m_gap, left + m_gap, top + m_gap, m_paint_f);
            canvas.drawRect(left - m_gap, top - m_gap, left + m_gap, top + m_gap, m_paint_s);

            canvas.drawRect(xmid - m_gap, top - m_gap, xmid + m_gap, top + m_gap, m_paint_f);
            canvas.drawRect(xmid - m_gap, top - m_gap, xmid + m_gap, top + m_gap, m_paint_s);

            canvas.drawRect(right - m_gap, top - m_gap, right + m_gap, top + m_gap, m_paint_f);
            canvas.drawRect(right - m_gap, top - m_gap, right + m_gap, top + m_gap, m_paint_s);

            canvas.drawRect(right - m_gap, ymid - m_gap, right + m_gap, ymid + m_gap, m_paint_f);
            canvas.drawRect(right - m_gap, ymid - m_gap, right + m_gap, ymid + m_gap, m_paint_s);

            canvas.drawRect(right - m_gap, bot - m_gap, right + m_gap, bot + m_gap, m_paint_f);
            canvas.drawRect(right - m_gap, bot - m_gap, right + m_gap, bot + m_gap, m_paint_s);

            canvas.drawRect(xmid - m_gap, bot - m_gap, xmid + m_gap, bot + m_gap, m_paint_f);
            canvas.drawRect(xmid - m_gap, bot - m_gap, xmid + m_gap, bot + m_gap, m_paint_s);

            canvas.drawRect(left - m_gap, bot - m_gap, left + m_gap, bot + m_gap, m_paint_f);
            canvas.drawRect(left - m_gap, bot - m_gap, left + m_gap, bot + m_gap, m_paint_s);

            canvas.drawRect(left - m_gap, ymid - m_gap, left + m_gap, ymid + m_gap, m_paint_f);
            canvas.drawRect(left - m_gap, ymid - m_gap, left + m_gap, ymid + m_gap, m_paint_s);
        }

        @Override
        public void onTouch(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    if (m_gap == 0) m_gap = dp2px(getContext(), 4);
                    float left = m_annot_rect[0];
                    float top = m_annot_rect[1];
                    float right = m_annot_rect[2];
                    float bot = m_annot_rect[3];
                    float xmid = (left + right) * 0.5f;
                    float ymid = (top + bot) * 0.5f;

                    m_annot_x0 = event.getX();
                    m_annot_y0 = event.getY();
                    m_annot_rect0 = new float[4];
                    m_annot_rect0[0] = m_annot_rect[0];
                    m_annot_rect0[1] = m_annot_rect[1];
                    m_annot_rect0[2] = m_annot_rect[2];
                    m_annot_rect0[3] = m_annot_rect[3];
                    if (m_annot_y0 >= top - m_gap && m_annot_y0 <= top + m_gap) {
                        if (m_annot_x0 >= left - m_gap && m_annot_x0 <= left + m_gap) m_node = 1;
                        else if (m_annot_x0 >= xmid - m_gap && m_annot_x0 <= xmid + m_gap) m_node = 2;
                        else if (m_annot_x0 >= right - m_gap && m_annot_x0 <= right + m_gap) m_node = 3;
                        else m_node = 0;
                    }
                    else if (m_annot_y0 >= ymid - m_gap && m_annot_y0 <= ymid + m_gap)
                    {
                        if (m_annot_x0 >= left - m_gap && m_annot_x0 <= left + m_gap) m_node = 8;
                        else if (m_annot_x0 >= right - m_gap && m_annot_x0 <= right + m_gap) m_node = 4;
                        else m_node = 0;
                    }
                    else if (m_annot_y0 >= bot - m_gap && m_annot_y0 <= bot + m_gap)
                    {
                        if (m_annot_x0 >= left - m_gap && m_annot_x0 <= left + m_gap) m_node = 7;
                        else if (m_annot_x0 >= xmid - m_gap && m_annot_x0 <= xmid + m_gap) m_node = 6;
                        else if (m_annot_x0 >= right - m_gap && m_annot_x0 <= right + m_gap) m_node = 5;
                        else m_node = 0;
                    }
                    else if (m_annot_x0 > m_annot_rect[0] && m_annot_y0 > m_annot_rect[1] &&
                            m_annot_x0 < m_annot_rect[2] && m_annot_y0 < m_annot_rect[3]) {
                        m_node = 9;
                    } else
                    {
                        m_node = 0;
                        m_annot_rect0 = null;
                    }
                }
                break;
                case MotionEvent.ACTION_MOVE:
                    if (m_node > 0) {
                        float x = event.getX();
                        float y = event.getY();

                        switch(m_node)
                        {
                            case 1://left,top
                                m_annot_rect[0] = m_annot_rect0[0] + x - m_annot_x0;
                                m_annot_rect[1] = m_annot_rect0[1] + y - m_annot_y0;
                                break;
                            case 2://xmid,top
                                m_annot_rect[1] = m_annot_rect0[1] + y - m_annot_y0;
                                break;
                            case 3://right,top
                                m_annot_rect[1] = m_annot_rect0[1] + y - m_annot_y0;
                                m_annot_rect[2] = m_annot_rect0[2] + x - m_annot_x0;
                                break;
                            case 4://right,ymid
                                m_annot_rect[2] = m_annot_rect0[2] + x - m_annot_x0;
                                break;
                            case 5://right,bottom
                                m_annot_rect[2] = m_annot_rect0[2] + x - m_annot_x0;
                                m_annot_rect[3] = m_annot_rect0[3] + y - m_annot_y0;
                                break;
                            case 6://xmid,bottom
                                m_annot_rect[3] = m_annot_rect0[3] + y - m_annot_y0;
                                break;
                            case 7://left,bottom
                                m_annot_rect[0] = m_annot_rect0[0] + x - m_annot_x0;
                                m_annot_rect[3] = m_annot_rect0[3] + y - m_annot_y0;
                                break;
                            case 8://left,ymid
                                m_annot_rect[0] = m_annot_rect0[0] + x - m_annot_x0;
                                break;
                            default:
                                MovRect(x - m_annot_x0, y - m_annot_y0);
                                break;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (m_node > 0) {
                        float x = event.getX();
                        float y = event.getY();
                        PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) x, (int) y);
                        if (m_node != 9 || m_annot_page.GetPageNo() == pos.pageno) {
                            m_annot_rect0[0] = m_annot_page.ToPDFX(m_annot_rect[0], m_layout.vGetX());
                            m_annot_rect0[1] = m_annot_page.ToPDFY(m_annot_rect[3], m_layout.vGetY());
                            m_annot_rect0[2] = m_annot_page.ToPDFX(m_annot_rect[2], m_layout.vGetX());
                            m_annot_rect0[3] = m_annot_page.ToPDFY(m_annot_rect[1], m_layout.vGetY());
                            //add to redo/undo stack.
                            float[] rect = m_annot.GetRect();
                            m_opstack.push(new OPMove(pos.pageno, rect, pos.pageno, m_annot.GetIndexInPage(), m_annot_rect0));
                            m_annot.SetRect(m_annot_rect0[0], m_annot_rect0[1], m_annot_rect0[2], m_annot_rect0[3]);
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            m_layout.gl_render(m_annot_page);
                            if (m_listener != null)
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                        } else {
                            PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                            Page page = m_doc.GetPage(vpage.GetPageNo());
                            if (page != null) {
                                page.ObjsStart();
                                m_annot_rect0[0] = vpage.ToPDFX(m_annot_rect[0], m_layout.vGetX());
                                m_annot_rect0[1] = vpage.ToPDFY(m_annot_rect[3], m_layout.vGetY());
                                m_annot_rect0[2] = vpage.ToPDFX(m_annot_rect[2], m_layout.vGetX());
                                m_annot_rect0[3] = vpage.ToPDFY(m_annot_rect[1], m_layout.vGetY());
                                //add to redo/undo stack.
                                float[] rect = m_annot.GetRect();
                                m_opstack.push(new OPMove(m_annot_page.GetPageNo(), rect, pos.pageno, page.GetAnnotCount(), m_annot_rect0));
                                m_annot.MoveToPage(page, m_annot_rect0);
                                m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                                //page.CopyAnnot(m_annot, m_annot_rect0);
                                page.Close();
                            }
                            m_layout.gl_render(m_annot_page);
                            m_layout.gl_render(vpage, m_annot_rect0);
                            if (m_listener != null) {
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                                m_listener.OnPDFPageModified(vpage.GetPageNo());
                            }
                        }
                        requestRender();
                    }
                    PDFEndAnnot();
                    break;
            }
        }
    };
    private final IAnnotOP m_annot_op_line = new IAnnotOP() {
        private final Paint m_paint_s = NewPaintBorder();
        private final Paint m_paint_l = NewPaintLine();
        private final Paint m_paint_f = NewPaintFill();
        private int m_gap = 0;
        private int m_node = 0;
        private float[] m_pts0;
        private float[] m_pte0;
        private float[] m_pts;
        private float[] m_pte;
        @Override
        public void onDraw(Canvas canvas) {
            if (m_gap == 0) m_gap = dp2px(getContext(), 4);
            canvas.drawRect(m_annot_rect[0], m_annot_rect[1], m_annot_rect[2], m_annot_rect[3], m_paint_s);

            if (m_pts == null)
            {
                m_pts = m_annot.GetLinePoint(0);
                m_pts[0] = m_annot_page.GetVX(m_pts[0]) - m_layout.vGetX();
                m_pts[1] = m_annot_page.GetVY(m_pts[1]) - m_layout.vGetY();
            }
            if (m_pte == null)
            {
                m_pte = m_annot.GetLinePoint(1);
                m_pte[0] = m_annot_page.GetVX(m_pte[0]) - m_layout.vGetX();
                m_pte[1] = m_annot_page.GetVY(m_pte[1]) - m_layout.vGetY();
            }
            canvas.drawLine(m_pts[0], m_pts[1], m_pte[0], m_pte[1], m_paint_l);

            canvas.drawRect(m_pts[0] - m_gap, m_pts[1] - m_gap, m_pts[0] + m_gap, m_pts[1] + m_gap, m_paint_f);
            canvas.drawRect(m_pts[0] - m_gap, m_pts[1] - m_gap, m_pts[0] + m_gap, m_pts[1] + m_gap, m_paint_s);

            canvas.drawRect(m_pte[0] - m_gap, m_pte[1] - m_gap, m_pte[0] + m_gap, m_pte[1] + m_gap, m_paint_f);
            canvas.drawRect(m_pte[0] - m_gap, m_pte[1] - m_gap, m_pte[0] + m_gap, m_pte[1] + m_gap, m_paint_s);
        }

        @Override
        public void onTouch(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (m_gap == 0) m_gap = dp2px(getContext(), 4);
                    if (m_pts0 == null)
                    {
                        m_pts0 = m_annot.GetLinePoint(0);
                        m_pts0[0] = m_annot_page.GetVX(m_pts0[0]) - m_layout.vGetX();
                        m_pts0[1] = m_annot_page.GetVY(m_pts0[1]) - m_layout.vGetY();
                        m_pts = new float[2];
                        m_pts[0] = m_pts0[0];
                        m_pts[1] = m_pts0[1];
                    }
                    if (m_pte0 == null)
                    {
                        m_pte0 = m_annot.GetLinePoint(1);
                        m_pte0[0] = m_annot_page.GetVX(m_pte0[0]) - m_layout.vGetX();
                        m_pte0[1] = m_annot_page.GetVY(m_pte0[1]) - m_layout.vGetY();
                        m_pte = new float[2];
                        m_pte[0] = m_pte0[0];
                        m_pte[1] = m_pte0[1];
                    }

                    m_annot_x0 = event.getX();
                    m_annot_y0 = event.getY();
                    m_annot_rect0 = new float[4];
                    m_annot_rect0[0] = m_annot_rect[0];
                    m_annot_rect0[1] = m_annot_rect[1];
                    m_annot_rect0[2] = m_annot_rect[2];
                    m_annot_rect0[3] = m_annot_rect[3];
                    if (m_annot_x0 >= m_pts[0] - m_gap && m_annot_x0 <= m_pts[0] + m_gap &&
                            m_annot_y0 >= m_pts[1] - m_gap && m_annot_y0 <= m_pts[1] + m_gap)
                        m_node = 1;
                    else if (m_annot_x0 >= m_pte[0] - m_gap && m_annot_x0 <= m_pte[0] + m_gap &&
                            m_annot_y0 >= m_pte[1] - m_gap && m_annot_y0 <= m_pte[1] + m_gap)
                        m_node = 2;
                    else if (m_annot_x0 > m_annot_rect[0] && m_annot_y0 > m_annot_rect[1] &&
                            m_annot_x0 < m_annot_rect[2] && m_annot_y0 < m_annot_rect[3])
                        m_node = 3;
                    else
                    {
                        m_node = 0;
                        m_annot_rect0 = null;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (m_node > 0) {
                        float x = event.getX();
                        float y = event.getY();
                        switch(m_node)
                        {
                            case 1://start point
                                m_pts[0] = m_pts0[0] + x - m_annot_x0;
                                m_pts[1] = m_pts0[1] + y - m_annot_y0;
                                if (m_pts[0] > m_pte[0])
                                {
                                    m_annot_rect[0] = m_pte[0];
                                    m_annot_rect[2] = m_pts[0];
                                }
                                else
                                {
                                    m_annot_rect[0] = m_pts[0];
                                    m_annot_rect[2] = m_pte[0];
                                }
                                if (m_pts[1] > m_pte[1])
                                {
                                    m_annot_rect[1] = m_pte[1];
                                    m_annot_rect[3] = m_pts[1];
                                }
                                else
                                {
                                    m_annot_rect[1] = m_pts[1];
                                    m_annot_rect[3] = m_pte[1];
                                }
                                break;
                            case 2://end point
                                m_pte[0] = m_pte0[0] + x - m_annot_x0;
                                m_pte[1] = m_pte0[1] + y - m_annot_y0;
                                if (m_pts[0] > m_pte[0])
                                {
                                    m_annot_rect[0] = m_pte[0];
                                    m_annot_rect[2] = m_pts[0];
                                }
                                else
                                {
                                    m_annot_rect[0] = m_pts[0];
                                    m_annot_rect[2] = m_pte[0];
                                }
                                if (m_pts[1] > m_pte[1])
                                {
                                    m_annot_rect[1] = m_pte[1];
                                    m_annot_rect[3] = m_pts[1];
                                }
                                else
                                {
                                    m_annot_rect[1] = m_pts[1];
                                    m_annot_rect[3] = m_pte[1];
                                }
                                break;
                            default:
                                MovRect(x - m_annot_x0, y - m_annot_y0);
                                break;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (m_node > 0) {
                        float x = event.getX();
                        float y = event.getY();
                        PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) x, (int) y);
                        if (m_node != 3)
                        {
                            m_pts[0] = m_annot_page.ToPDFX(m_pts[0], m_layout.vGetX());
                            m_pts[1] = m_annot_page.ToPDFY(m_pts[1], m_layout.vGetY());
                            m_pte[0] = m_annot_page.ToPDFX(m_pte[0], m_layout.vGetX());
                            m_pte[1] = m_annot_page.ToPDFY(m_pte[1], m_layout.vGetY());
                            m_annot.SetLinePoint(m_pts[0], m_pts[1], m_pte[0], m_pte[1]);
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            m_layout.gl_render(m_annot_page);
                            if (m_listener != null)
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                        }
                        else if (m_annot_page.GetPageNo() == pos.pageno) {
                            m_annot_rect0[0] = m_annot_page.ToPDFX(m_annot_rect[0], m_layout.vGetX());
                            m_annot_rect0[1] = m_annot_page.ToPDFY(m_annot_rect[3], m_layout.vGetY());
                            m_annot_rect0[2] = m_annot_page.ToPDFX(m_annot_rect[2], m_layout.vGetX());
                            m_annot_rect0[3] = m_annot_page.ToPDFY(m_annot_rect[1], m_layout.vGetY());
                            if (m_annot_rect0[0] > m_annot_rect0[2])
                            {
                                float tmp = m_annot_rect0[0];
                                m_annot_rect0[0] = m_annot_rect0[2];
                                m_annot_rect0[2] = tmp;
                            }
                            if (m_annot_rect0[1] > m_annot_rect0[3])
                            {
                                float tmp = m_annot_rect0[1];
                                m_annot_rect0[1] = m_annot_rect0[3];
                                m_annot_rect0[3] = tmp;
                            }
                            //add to redo/undo stack.
                            float[] rect = m_annot.GetRect();
                            m_opstack.push(new OPMove(pos.pageno, rect, pos.pageno, m_annot.GetIndexInPage(), m_annot_rect0));
                            m_annot.SetRect(m_annot_rect0[0], m_annot_rect0[1], m_annot_rect0[2], m_annot_rect0[3]);
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            m_layout.gl_render(m_annot_page);
                            if (m_listener != null)
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                        } else {
                            PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                            Page page = m_doc.GetPage(vpage.GetPageNo());
                            if (page != null) {
                                page.ObjsStart();
                                m_annot_rect0[0] = vpage.ToPDFX(m_annot_rect[0], m_layout.vGetX());
                                m_annot_rect0[1] = vpage.ToPDFY(m_annot_rect[3], m_layout.vGetY());
                                m_annot_rect0[2] = vpage.ToPDFX(m_annot_rect[2], m_layout.vGetX());
                                m_annot_rect0[3] = vpage.ToPDFY(m_annot_rect[1], m_layout.vGetY());
                                //add to redo/undo stack.
                                float[] rect = m_annot.GetRect();
                                m_opstack.push(new OPMove(m_annot_page.GetPageNo(), rect, pos.pageno, page.GetAnnotCount(), m_annot_rect0));
                                m_annot.MoveToPage(page, m_annot_rect0);
                                m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                                //page.CopyAnnot(m_annot, m_annot_rect0);
                                page.Close();
                            }
                            m_layout.gl_render(m_annot_page);
                            m_layout.gl_render(vpage, m_annot_rect0);
                            if (m_listener != null) {
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                                m_listener.OnPDFPageModified(vpage.GetPageNo());
                            }
                        }
                    }
                    requestRender();
                    PDFEndAnnot();
                    m_pts = null;
                    m_pte = null;
                    m_pts0 = null;
                    m_pte0 = null;
                    m_node = 0;
                    break;
            }
        }
    };
    private final IAnnotOP m_annot_op_poly = new IAnnotOP() {
        private final Paint m_paint_s = NewPaintBorder();
        private final Paint m_paint_l = NewPaintLine();
        private final Paint m_paint_f = NewPaintFill();
        private float[] m_pts;
        private float[] m_pts0;
        private int m_atype;
        private int m_gap;
        private int m_node = -1;
        private final android.graphics.Path m_path = new android.graphics.Path();
        private float[] getPoints()
        {
            m_atype = m_annot.GetType();
            Path path = null;
            int cnt = 0;
            if (m_atype == 7) {
                path = m_annot.GetPolygonPath();
                cnt = path.GetNodeCount() - 1;
            }
            else if (m_atype == 8) {
                path = m_annot.GetPolylinePath();
                cnt = path.GetNodeCount();
            }
            if (path == null) return null;
            float[] pts = new float[cnt * 2];
            float[] pt1 = new float[2];
            int idx = 0;
            for (int ipt = 0; ipt < cnt; ipt++)
            {
                path.GetNode(ipt, pt1);
                pt1[0] = m_annot_page.GetVX(pt1[0]) - m_layout.vGetX();
                pt1[1] = m_annot_page.GetVY(pt1[1]) - m_layout.vGetY();
                pts[idx] = pt1[0];
                pts[idx + 1] = pt1[1];
                idx += 2;
            }
            return pts;
        }
        private void drawPts(Canvas canvas)
        {
            int cnt = m_pts.length;
            m_path.reset();
            m_path.moveTo(m_pts[0], m_pts[1]);
            for (int ipt = 2; ipt < cnt; ipt += 2)
                m_path.lineTo(m_pts[ipt], m_pts[ipt + 1]);
            canvas.drawPath(m_path, m_paint_l);
            for (int ipt = 0; ipt < cnt; ipt += 2)
            {
                canvas.drawRect(m_pts[ipt] - m_gap, m_pts[ipt + 1] - m_gap,
                        m_pts[ipt] + m_gap, m_pts[ipt + 1] + m_gap, m_paint_f);
                canvas.drawRect(m_pts[ipt] - m_gap, m_pts[ipt + 1] - m_gap,
                        m_pts[ipt] + m_gap, m_pts[ipt + 1] + m_gap, m_paint_s);
            }
        }
        private int getNode(float x, float y)
        {
            int cnt = m_pts.length;
            for (int ipt = 0; ipt < cnt; ipt += 2)
            {
                if (x >= m_pts[ipt] - m_gap && x < m_pts[ipt] + m_gap && y >= m_pts[ipt + 1] - m_gap && y < m_pts[ipt + 1] + m_gap)
                    return (ipt >> 1);
            }
            return -1;
        }
        private Path getPath()
        {
            int cnt = m_pts.length;
            Path path = new Path();
            path.MoveTo(m_annot_page.ToPDFX(m_pts[0], m_layout.vGetX()),
                    m_annot_page.ToPDFY(m_pts[1], m_layout.vGetY()));
            for (int ipt = 2; ipt < cnt; ipt += 2)
            {
                path.LineTo(m_annot_page.ToPDFX(m_pts[ipt], m_layout.vGetX()),
                        m_annot_page.ToPDFY(m_pts[ipt + 1], m_layout.vGetY()));
            }
            return path;
        }
        private void updateRect()
        {
            int cnt = m_pts.length;
            m_annot_rect[0] = m_pts[0];
            m_annot_rect[1] = m_pts[1];
            m_annot_rect[2] = m_pts[0];
            m_annot_rect[3] = m_pts[1];
            for (int ipt = 2; ipt < cnt; ipt += 2)
            {
                if (m_annot_rect[0] > m_pts[ipt]) m_annot_rect[0] = m_pts[ipt];
                else if(m_annot_rect[2] < m_pts[ipt]) m_annot_rect[2] = m_pts[ipt];

                if (m_annot_rect[1] > m_pts[ipt + 1]) m_annot_rect[1] = m_pts[ipt + 1];
                else if(m_annot_rect[3] < m_pts[ipt + 1]) m_annot_rect[3] = m_pts[ipt + 1];
            }
        }
        @Override
        public void onDraw(Canvas canvas) {
            if (m_gap == 0) m_gap = dp2px(getContext(), 4);
            if (m_pts == null) m_pts = getPoints();
            canvas.drawRect(m_annot_rect[0], m_annot_rect[1], m_annot_rect[2], m_annot_rect[3], m_paint_s);
            drawPts(canvas);
        }

        @Override
        public void onTouch(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (m_gap == 0) m_gap = dp2px(getContext(), 4);
                    if (m_pts == null) m_pts = getPoints();

                    m_annot_x0 = event.getX();
                    m_annot_y0 = event.getY();
                    m_annot_rect0 = new float[4];
                    m_annot_rect0[0] = m_annot_rect[0];
                    m_annot_rect0[1] = m_annot_rect[1];
                    m_annot_rect0[2] = m_annot_rect[2];
                    m_annot_rect0[3] = m_annot_rect[3];
                    m_node = getNode(m_annot_x0, m_annot_y0);
                    if (m_node >= 0) {
                        m_pts0 = new float[m_pts.length];
                        System.arraycopy(m_pts, 0, m_pts0, 0, m_pts.length);
                    }
                    else
                    {
                        if (m_annot_x0 > m_annot_rect[0] && m_annot_y0 > m_annot_rect[1] &&
                                m_annot_x0 < m_annot_rect[2] && m_annot_y0 < m_annot_rect[3])
                            m_node = 0;
                        else m_node = -1;
                        m_pts0 = null;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (m_pts != null) {
                        float x = event.getX();
                        float y = event.getY();
                        if (m_pts0 != null)
                        {
                            m_pts[m_node << 1] = m_pts0[m_node << 1] + x - m_annot_x0;
                            m_pts[(m_node << 1) + 1] = m_pts0[(m_node << 1) + 1] + y - m_annot_y0;
                            updateRect();
                        }
                        else
                            MovRect(x - m_annot_x0, y - m_annot_y0);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (m_node >= 0) {
                        float x = event.getX();
                        float y = event.getY();
                        PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) x, (int) y);
                        if (m_pts0 != null)
                        {
                            Path path = getPath();
                            if (m_atype == 7)
                            {
                                path.ClosePath();
                                m_annot.SetPolygonPath(path);
                            }
                            else if(m_atype == 8)
                                m_annot.SetPolylinePath(path);
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            m_layout.gl_render(m_annot_page);
                            if (m_listener != null)
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                        }
                        else if (m_annot_page.GetPageNo() == pos.pageno) {
                            m_annot_rect0[0] = m_annot_page.ToPDFX(m_annot_rect[0], m_layout.vGetX());
                            m_annot_rect0[1] = m_annot_page.ToPDFY(m_annot_rect[3], m_layout.vGetY());
                            m_annot_rect0[2] = m_annot_page.ToPDFX(m_annot_rect[2], m_layout.vGetX());
                            m_annot_rect0[3] = m_annot_page.ToPDFY(m_annot_rect[1], m_layout.vGetY());
                            if (m_annot_rect0[0] > m_annot_rect0[2])
                            {
                                float tmp = m_annot_rect0[0];
                                m_annot_rect0[0] = m_annot_rect0[2];
                                m_annot_rect0[2] = tmp;
                            }
                            if (m_annot_rect0[1] > m_annot_rect0[3])
                            {
                                float tmp = m_annot_rect0[1];
                                m_annot_rect0[1] = m_annot_rect0[3];
                                m_annot_rect0[3] = tmp;
                            }
                            //add to redo/undo stack.
                            float[] rect = m_annot.GetRect();
                            m_opstack.push(new OPMove(pos.pageno, rect, pos.pageno, m_annot.GetIndexInPage(), m_annot_rect0));
                            m_annot.SetRect(m_annot_rect0[0], m_annot_rect0[1], m_annot_rect0[2], m_annot_rect0[3]);
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            m_layout.gl_render(m_annot_page);
                            if (m_listener != null)
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                        } else {
                            PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                            Page page = m_doc.GetPage(vpage.GetPageNo());
                            if (page != null) {
                                page.ObjsStart();
                                m_annot_rect0[0] = vpage.ToPDFX(m_annot_rect[0], m_layout.vGetX());
                                m_annot_rect0[1] = vpage.ToPDFY(m_annot_rect[3], m_layout.vGetY());
                                m_annot_rect0[2] = vpage.ToPDFX(m_annot_rect[2], m_layout.vGetX());
                                m_annot_rect0[3] = vpage.ToPDFY(m_annot_rect[1], m_layout.vGetY());
                                //add to redo/undo stack.
                                float[] rect = m_annot.GetRect();
                                m_opstack.push(new OPMove(m_annot_page.GetPageNo(), rect, pos.pageno, page.GetAnnotCount(), m_annot_rect0));
                                m_annot.MoveToPage(page, m_annot_rect0);
                                m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                                //page.CopyAnnot(m_annot, m_annot_rect0);
                                page.Close();
                            }
                            m_layout.gl_render(m_annot_page);
                            m_layout.gl_render(vpage);
                            if (m_listener != null) {
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                                m_listener.OnPDFPageModified(vpage.GetPageNo());
                            }
                        }
                    }
                    requestRender();
                    PDFEndAnnot();
                    m_pts = null;
                    m_pts0 = null;
                    m_node = 0;
                    break;
            }
        }
    };
    private IAnnotOP m_annot_op;

    class PDFGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (m_status == STA_NONE && m_hold) {
                final float dx = e2.getX() - e1.getX();
                final float dy = e2.getY() - e1.getY();
                final float vx = velocityX;
                final float vy = velocityY;
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        m_layout.gl_fling(m_hold_docx, m_hold_docy, dx, dy, vx, vy);
                        requestRender();
                    }
                });
                return true;
            } else return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (m_status == STA_NONE && e.getActionMasked() == MotionEvent.ACTION_UP) {
                //remove comment mark, and comment "return false" to enable zoom when double tap.
                /*
                final int x = (int)e.getX();
                final int y = (int)e.getY();
                final float z = m_layout.vGetZoom();
                final GLLayout.PDFPos pos = m_layout.vGetPos(x, y);
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        m_layout.gl_zoom_start(m_gl10);
                        m_layout.gl_zoom_set(z * 1.2f);
                        m_layout.vSetPos(x, y, pos);
                        m_layout.gl_zoom_confirm(m_gl10);
                        requestRender();
                    }
                });
                if (m_listener != null) {
                    m_listener.OnPDFDoubleTapped(x, y);
                    return true;
                }
                */
                //if double tap is enabled for zooming, these 2 lines need to comment.
                return (m_listener != null && m_listener.OnPDFDoubleTapped(e.getX(), e.getY()));
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //remove comment mark to enable selection when long press.
            /*
            if(m_status != STA_NONE) return;
            m_sel_icon1 = BitmapFactory.decodeResource(GLView.this.getResources(), R.drawable.pt_start);
            m_sel_icon2 = BitmapFactory.decodeResource(GLView.this.getResources(), R.drawable.pt_end);
            m_status = STA_SELECT;
            m_hold_x = e.getX();
            m_hold_y = e.getY();
            if (m_sel != null) {
                m_sel.Clear();
                m_sel = null;
            }
            m_annot_pos = m_layout.vGetPos((int) m_hold_x, (int) m_hold_y);
            m_annot_page = m_layout.vGetPage(m_annot_pos.pageno);
            m_sel = new VSel(m_doc.GetPage(m_annot_pos.pageno));
            if (m_canvas != null) m_canvas.UpdateLayer();
            */
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (m_layout == null || m_status != STA_NONE && m_status != STA_ANNOT) return false;
            float x = e.getX();
            float y = e.getY();
            m_annot_pos = m_layout.vGetPos((int)x, (int)y);
            switch (m_layout.gl_click((int)x, (int)y)) {
                case 1://not annot event fired.
                    if (m_listener != null) m_listener.OnPDFBlankTapped(m_annot_pos.pageno);
                    return true;
                case 2://fired by native
                    return true;
                default://same as gone.
                    break;
            }
            if (m_annot_pg != null) {
                m_annot_pg.Close();
                m_annot_pg = null;
            }
            m_annot_page = m_layout.vGetPage(m_annot_pos.pageno);
            m_annot_pg = m_doc.GetPage(m_annot_page.GetPageNo());
            if (m_annot_pg == null) m_annot = null;
            else {
                m_annot_pg.ObjsStart();
                m_annot = m_annot_pg.GetAnnotFromPoint(m_annot_pos.x, m_annot_pos.y);
            }
            if (m_annot == null) {
                m_annot_page = null;
                m_annot_rect = null;
                if (m_listener != null) {
                    if (m_status == STA_ANNOT)
                        m_listener.OnPDFAnnotTapped(m_annot_pos.pageno, null);
                    else
                        m_listener.OnPDFBlankTapped(m_annot_pos.pageno);
                }
                m_annot_pos = null;
                m_annot_pg.Close();
                m_annot_pg = null;
                PDFEndAnnot();
                m_status = STA_NONE;
            } else {
                m_annot_rect = m_annot.GetRect();
                float tmp = m_annot_rect[1];
                m_annot_rect[0] = m_annot_page.GetVX(m_annot_rect[0]) - m_layout.vGetX();
                m_annot_rect[1] = m_annot_page.GetVY(m_annot_rect[3]) - m_layout.vGetY();
                m_annot_rect[2] = m_annot_page.GetVX(m_annot_rect[2]) - m_layout.vGetX();
                m_annot_rect[3] = m_annot_page.GetVY(tmp) - m_layout.vGetY();
                m_status = STA_ANNOT;
                int atype = m_annot.GetType();
                if (atype == 5 || atype == 6 || atype == 15)//it can resize
                    m_annot_op = m_annot_op_resize;
                else if (atype == 4)//it is line annotation
                    m_annot_op = m_annot_op_line;
                else if (atype == 7 || atype == 8)//it is polygon or polyline annotation
                    m_annot_op = m_annot_op_poly;
                else
                    m_annot_op = m_annot_op_normal;

                int check = m_annot.GetCheckStatus();
                if (PDFCanSave() && check >= 0) {
                    switch (check) {
                        case 0:
                            m_annot.SetCheckValue(true);
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            if (Global.g_exec_js && executeAnnotJS())
                                m_layout.gl_render(m_annot_page);
                            else m_layout.gl_render(m_annot_page, m_annot.GetRect());
                            break;
                        case 1:
                            m_annot.SetCheckValue(false);
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            if (Global.g_exec_js && executeAnnotJS())
                                m_layout.gl_render(m_annot_page);
                            else m_layout.gl_render(m_annot_page, m_annot.GetRect());
                            break;
                        case 2:
                        case 3:
                            m_annot.SetRadio();
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            if (Global.g_exec_js) executeAnnotJS();
                            m_layout.gl_render(m_annot_page, m_annot.GetRect());
                            break;
                    }
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                    PDFEndAnnot();
                } else if (PDFCanSave() && m_annot.GetEditType() > 0) { //if form edit-box.
                    if (m_pEdit == null) m_pEdit = new UIAnnotPopEdit(PDFGLView.this);
                    m_pEdit.update(m_annot, m_annot_rect, m_annot_page.GetScale());
                    m_edit_type = 1;

                    m_pEdit.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            if (m_annot != null) {
                                if (!m_annot.SetEditText(m_pEdit.getEditText())) {
                                    Log.e("RDERR", "set EditText failed.");
                                }
                                m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                                if (Global.g_exec_js && executeAnnotJS())
                                    m_layout.gl_render(m_annot_page);
                                else m_layout.gl_render(m_annot_page, m_annot.GetRect());
                                requestRender();
                                if (m_listener != null)
                                    m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                                PDFEndAnnot();
                                m_edit_type = 0;
                            }
                        }
                    });
                    int[] location = new int[2];
                    getLocationOnScreen(location);
                    m_pEdit.show(PDFGLView.this, (int) m_annot_rect[0] + location[0], (int) (m_annot_rect[1] + location[1]));
                } else if (PDFCanSave() && m_annot.GetComboItemCount() >= 0)//if form choice
                {
                    try {
                        if (m_pCombo == null) m_pCombo = new UIAnnotPopCombo(PDFGLView.this);
                        m_pCombo.update(m_annot, m_annot_rect);

                        m_edit_type = 2;
                        m_pCombo.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                if (m_edit_type == 2)//combo
                                {
                                    int selItem = m_pCombo.getSelItem();
                                    if (selItem >= 0) {
                                        m_annot.SetComboItem(selItem);
                                        m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                                        if (Global.g_exec_js && executeAnnotJS())
                                            m_layout.gl_render(m_annot_page);
                                        else m_layout.gl_render(m_annot_page, m_annot.GetRect());
                                        requestRender();
                                        if (m_listener != null)
                                            m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                                    }
                                    PDFEndAnnot();
                                }
                                m_edit_type = 0;

                            }
                        });

                        int[] location = new int[2];
                        getLocationOnScreen(location);
                        m_pCombo.show(PDFGLView.this, (int) m_annot_rect[0] + location[0], (int) (m_annot_rect[3] + location[1]));
                    } catch (Exception ignored) {
                    }
                } else if (PDFCanSave() && m_annot.GetListItemCount() >= 0)  //if list choice
                    onListAnnot();
                else if (PDFCanSave() && m_annot.GetFieldType() == 4 && Global.g_hand_signature)  //signature field
                    handleSignatureField();
                else if (m_annot.GetURI() != null && m_listener != null) { // launch link automatically
                    m_listener.OnPDFOpenURI(m_annot.GetURI());
                    PDFEndAnnot();
                } else if (m_listener != null) {
                    m_listener.OnPDFAnnotTapped(m_annot_pos.pageno, m_annot);
                    if (PDFCanSave() && m_aMenu != null) {
                        m_aMenu.show(m_annot, m_annot_rect, new UIAnnotMenu.IMemnuCallback() {
                            //the update need new operator in OPStack
                            @Override
                            public void onUpdate() {
                                m_layout.gl_render(m_annot_page, m_annot.GetRect());
                                requestRender();
                                if (m_listener != null && m_annot_page != null)
                                    m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                                PDFEndAnnot();
                            }

                            @Override
                            public void onRemove() {
                                PDFRemoveAnnot();
                            }

                            @Override
                            public void onPerform() {
                                PDFPerformAnnot();
                            }

                            @Override
                            public void onCancel() {
                                PDFCancelAnnot();
                            }

                            @Override
                            public void onAddCommonAnnotation(int pageNo) {

                            }
                        });
                    }
                }
                if (m_canvas != null) m_canvas.invalidate();
            }
            return true;
        }

        boolean[] mCheckedItems;

        private void onListAnnot() {
            try {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                String[] items = new String[m_annot.GetListItemCount()];
                int cur = 0;
                while (cur < items.length) {
                    items[cur] = m_annot.GetListItem(cur);
                    cur++;
                }
                final int[] selectedItems = m_annot.GetListSels();
                mCheckedItems = new boolean[items.length];
                for (int item : selectedItems)
                    mCheckedItems[item] = true;

                if (m_annot.IsListMultiSel()) {
                    alertBuilder.setMultiChoiceItems(items, mCheckedItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                            mCheckedItems[which] = isChecked;
                        }
                    });
                } else {
                    alertBuilder.setSingleChoiceItems(items, selectedItems[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mCheckedItems[i] = true;
                            mCheckedItems[selectedItems[0]] = false;
                        }
                    });
                }
                AlertDialog alert = alertBuilder.create();
                alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        List<Integer> listSels = new ArrayList<>();
                        for (int i = 0; i < mCheckedItems.length; i++)
                            if (mCheckedItems[i]) listSels.add(i);
                        int[] sels = new int[listSels.size()];
                        for (int i = 0; i < listSels.size(); i++)
                            sels[i] = listSels.get(i);
                        m_annot.SetListSels(sels);
                        m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                        if (Global.g_exec_js && executeAnnotJS())
                            m_layout.gl_render(m_annot_page);
                        else m_layout.gl_render(m_annot_page, m_annot.GetRect());
                        requestRender();
                        if (m_listener != null)
                            m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                        PDFEndAnnot();
                    }
                });
                alert.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*
        private void updateSignature(Bitmap signature, boolean remove) {
            if (m_annot != null) {
                float[] annotRect = m_annot.GetRect();
                float annotWidth = annotRect[2] - annotRect[0];
                float annotHeight = annotRect[3] - annotRect[1];

                if (remove)
                    signature = Bitmap.createBitmap((int) annotWidth, (int) annotHeight, Bitmap.Config.ARGB_8888);

                if (signature != null) {
                    Document.DocForm form = CommonUtil.createImageForm(m_doc, signature, annotWidth, annotHeight);
                    if (form != null && m_annot.SetIcon("Signature", form)) {
                        m_layout.gl_render(m_annot_page, m_annot.GetRect());
                        requestRender();
                        if (m_listener != null)
                            m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                        PDFEndAnnot();
                    }
                    signature.recycle();
                }
            }
        }
        */
        private int tmp_idx = 0;

        private void runJS(String js) {
            try {
                m_doc.RunJS(js, new Document.PDFJSDelegate() {
                    @Override
                    public void OnConsole(int cmd, String para) {
                        //cmd-> 0:clear, 1:hide, 2:println, 3:show
                    }

                    @Override
                    public int OnAlert(int btn, String msg, String title) {
                        Log.d(PDFGLLayoutView.class.getSimpleName(), "Alert {title:\"" + title + "\",message:\"" + msg + "\",button:" + btn + ",return:1}\r\n");
                        return 1;
                    }

                    @Override
                    public boolean OnDocClose() {
                        return false;
                    }

                    @Override
                    public String OnTmpFile() {
                        tmp_idx++;
                        return Global.tmp_path + "/" + tmp_idx + ".tmp";
                    }

                    @Override
                    public void OnUncaughtException(int code, String msg) {
                        Log.d(PDFGLLayoutView.class.getSimpleName(), "code = " + code + ", msg = " + msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean executeAnnotJS() {
            int cnt = 0;
            if (!TextUtils.isEmpty(m_annot.GetJS())) {
                runJS(m_annot.GetJS());
                cnt++;
            }
            if (!TextUtils.isEmpty(m_annot.GetAdditionalJS(1))) {
                runJS(m_annot.GetAdditionalJS(1));
                cnt++;
            }
            return cnt > 0;
        }

        private void handleSignatureField() {
            if (m_annot.GetSignStatus() == 1) {
                UIAnnotDlgSignProp dlg = new UIAnnotDlgSignProp(getContext());
                dlg.show(m_annot, m_doc, new UIAnnotMenu.IMemnuCallback() {
                    @Override
                    public void onUpdate() {
                    }

                    @Override
                    public void onRemove() {
                    }

                    @Override
                    public void onPerform() {
                    }

                    @Override
                    public void onCancel() {}

                    @Override
                    public void onAddCommonAnnotation(int pageNo) {

                    }
                });
            } else {
                UIAnnotDlgSign dlg = new UIAnnotDlgSign(getContext());
                dlg.show(m_annot, m_doc, new UIAnnotMenu.IMemnuCallback() {
                    @Override
                    public void onUpdate() {
                        m_layout.gl_render(m_annot_page, m_annot.GetRect());
                        requestRender();
                        if (m_listener != null)
                            m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                        PDFEndAnnot();
                    }

                    @Override
                    public void onRemove() {
                    }

                    @Override
                    public void onPerform() {
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onAddCommonAnnotation(int pageNo) {

                    }

                });
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
    }

    public PDFGLView(Context context) {
        super(context);
        init(context);
    }

    public PDFGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private Document m_doc;
    private int m_page_gap = 4;
    private GL10 m_gl10;
    private int m_cur_pageno = 0;

    private void init(Context context) {
        m_gesture = new GestureDetector(context, new PDFGestureListener());
        //getHolder().setFormat(PixelFormat.RGBA_8888);
        //setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        m_doc = null;
        setRenderer(new Renderer() {
            private boolean m_prepared = false;
            @Override
            public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
                setRenderMode(RENDERMODE_WHEN_DIRTY);//faster way.
                if (!m_prepared) {
                    Looper.prepare();
                    m_prepared = true;
                }
                gl10.glEnable(GL10.GL_BLEND);
                gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                if (m_layout != null) m_layout.gl_surface_create(gl10);
            }

            @Override
            public void onSurfaceChanged(GL10 gl10, int width, int height) {
                Log.e("GLView", "onSurfaceChanged");
                m_w = width;
                m_h = height;
                gl10.glViewport(0, 0, m_w, m_h);
                gl10.glMatrixMode(GL10.GL_PROJECTION);
                gl10.glLoadIdentity();
                gl10.glOrthof(0, m_w, m_h, 0, 1, -1);
                gl10.glEnable(GL10.GL_TEXTURE_2D);
                gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                gl10.glEnable(GL10.GL_BLEND);
                gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                if (m_layout == null) return;

                if (m_gl10 != gl10) {
                    if (m_gl10 != null) m_layout.gl_reset(m_gl10);
                    m_layout.gl_resize(m_w, m_h);
                    m_gl10 = gl10;
                } else {
                    m_layout.gl_zoom_start(gl10);
                    m_layout.gl_resize(m_w, m_h);
                    m_layout.gl_zoom_confirm(gl10);
                }
                requestRender();
            }

            private boolean darkmode = Global.g_dark_mode;
            @Override
            public void onDrawFrame(GL10 gl10) {
                m_gl10 = gl10;
                if (m_layout == null)
                {
                    //show background color.
                    gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
                    return;
                }
                if (darkmode != Global.g_dark_mode)
                {
                    darkmode = Global.g_dark_mode;
                    m_layout.gl_reset(gl10);
                    m_layout.gl_sync();
                    if (darkmode)
                        gl10.glClearColor(1.0f - ((m_back_color >> 16) & 0xff) / 255.0f, 1.0f - ((m_back_color >> 8) & 0xff) / 255.0f, 1.0f - (m_back_color & 0xff) / 255.0f, ((m_back_color >> 24) & 0xff) / 255.0f);
                    else
                        gl10.glClearColor(((m_back_color >> 16) & 0xff) / 255.0f, ((m_back_color >> 8) & 0xff) / 255.0f, (m_back_color & 0xff) / 255.0f, ((m_back_color >> 24) & 0xff) / 255.0f);
                }
                if (m_goto_pos != null) {
                    m_layout.vSetPos(0, 0, m_goto_pos);
                    m_goto_pos = null;
                }
                gl10.glClearColor(((m_back_color >> 16) & 0xff) / 255.0f, ((m_back_color >> 8) & 0xff) / 255.0f, (m_back_color & 0xff) / 255.0f, ((m_back_color >> 24) & 0xff) / 255.0f);
                gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
                m_layout.gl_draw(gl10);
                /*if (Global.g_dark_mode) {
                    gl10.glEnable(GL10.GL_COLOR_LOGIC_OP);
                    gl10.glLogicOp(GL10.GL_XOR);
                    m_layout.gl_fill_color(gl10, 0, 0, m_w, m_h, 1, 1, 1);
                    gl10.glDisable(GL10.GL_COLOR_LOGIC_OP);
                }*/
                final int pgno = m_layout.vGetPage(m_w >> 2, m_h >> 2);
                if (pgno != m_cur_pageno && m_listener != null) {
                    m_cur_pageno = pgno;
                    PDFGLView.this.post(new Runnable() {
                        @Override
                        public void run() {
                            m_listener.OnPDFPageChanged(pgno);
                        }
                    });
                }
                if ((Global.debug_mode || m_layout.vHasFind() || mFTSRect != null) && m_canvas != null)
                    m_canvas.postInvalidate();
                if (!m_layout.gl_is_scroll_finished())
                    requestRender();
            }
        });
    }

    /**
     * fired when resume.
     * @param holder SurfaceHolder object
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("GLView", "surfaceCreated");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (m_gl10 != null && m_layout != null)
                    m_layout.gl_surface_create(m_gl10);
            }
        });
        super.surfaceCreated(holder);
    }

    //fired when pause.
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("GLView", "surfaceDestroyed");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (m_gl10 != null && m_layout != null)
                    m_layout.gl_surface_destroy(m_gl10);
            }
        });
        super.surfaceDestroyed(holder);
    }

    public void PDFCloseOnUI()
    {
        PDFCancelAnnot();
    }

    public void PDFClose() {
        is_notified = false;
        is_waitting = false;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (m_layout != null) {
                    PDFGLLayout layout = m_layout;
                    GL10 gl10 = m_gl10;
                    m_layout = null;
                    m_gl10 = null;
                    layout.gl_close(gl10);
                    m_doc = null;
                }
                notify_init();
            }
        });
        wait_init();
    }

    public void setAnnotMenu(UIAnnotMenu aMenu) {
        m_aMenu = aMenu;
    }

    public void PDFOpen(Document doc, IPDFLayoutView.PDFLayoutListener listener, PDFGLCanvas canvas, int page_gap) {
        m_doc = doc;
        m_listener = listener;
        m_canvas = canvas;
        m_page_gap = (page_gap + 1) & -2;
        PDFSetView(Global.g_view_mode);
    }

    public void PDFSaveView() {
        if (m_layout != null) m_save_pos = m_layout.vGetPos(m_w >> 1, m_h >> 1);
        else m_save_pos = null;
        //PDFSetEdit(2);//cancel
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (m_layout != null) {
                    PDFGLLayout layout = m_layout;
                    m_layout = null;
                    layout.gl_close(m_gl10);
                }
                synchronized (PDFGLView.this) {
                    PDFGLView.this.notify();
                }
            }
        });
        synchronized (this) {
            try {
                wait();
            } catch (Exception ignored) {
            }
        }
    }

    public void PDFRestoreView() {
        //reset stack.
        m_opstack = new PDFLayoutOPStack();
        queueEvent(new Runnable() {
            @Override
            public void run() {
                PDFGLLayout layout;
                switch (m_view_mode) {
                    case 1:
                        layout = new PDFGLLayoutHorz(getContext(), false);
                        break;
                    case 3://single
                        layout = new PDFGLLayoutDual(getContext(), PDFEditLayoutDual.ALIGN_CENTER, PDFEditLayoutDual.SCALE_SAME_HEIGHT, false, null, null);
                        break;
                    case 4:
                    case 6://dual when landscape
                    {
                        int pcnt = m_doc.GetPageCount();
                        boolean[] bval = new boolean[pcnt];
                        int position = 0;
                        for (int pcur = 0; pcur < pcnt; pcur++) {
                            float pw = m_doc.GetPageWidth(pcur);
                            float ph = m_doc.GetPageHeight(pcur);
                            if (pw > ph) {
                                bval[position] = false;
                            } else if (pcur < pcnt - 1) {
                                float pw_next = m_doc.GetPageWidth(pcur + 1);
                                float ph_next = m_doc.GetPageHeight(pcur + 1);
                                if (pw_next > ph_next) bval[position] = false;
                                else {
                                    bval[position] = true;
                                    pcur++;
                                }
                            }
                            position++;
                        }
                        layout = new PDFGLLayoutDual(getContext(), PDFEditLayoutDual.ALIGN_CENTER, PDFEditLayoutDual.SCALE_FIT, false, bval, null);
                    }
                    break;
                    default://vertical.
                        layout = new PDFGLLayoutVert(getContext(), PDFEditLayoutVert.ALIGN_CENTER, false);
                        break;
                }
                layout.vOpen(m_doc, new PDFGLLayout.GLListener() {
                    @Override
                    public void OnBlockRendered(int pageno) {
                        requestRender();
                        if (m_listener != null)
                            m_listener.OnPDFPageRendered(m_layout.vGetPage(pageno));
                    }
                    @Override
                    public void OnFound(boolean found) {
                        requestRender();
                        if (m_listener != null) m_listener.OnPDFSearchFinished(found);
                        if (found && m_canvas != null) invalidate();
                    }
                    @Override
                    public void OnRedraw() {
                        requestRender();
                    }
                }, m_page_gap);
                if (m_layout != null && m_gl10 != null)
                    m_layout.gl_close(m_gl10);
                m_layout = layout;
                if (m_gl10 != null) {
                    m_layout.gl_surface_create(m_gl10);
                    m_layout.gl_resize(m_w, m_h);
                    if (m_save_pos != null) {
                        if (m_save_pos.pageno >= m_doc.GetPageCount())//after page deleted, page count may changed.
                            m_save_pos.pageno = m_doc.GetPageCount() - 1;
                        if (m_view_mode == 3 || m_view_mode == 4 || m_view_mode == 6) {
                            m_layout.vGotoPage(m_save_pos.pageno);
                        } else {
                            m_layout.vSetPos(0, 0, m_save_pos);
                            m_layout.gl_move_end();
                        }
                    }
                    requestRender();
                }
                m_save_pos = null;
            }
        });
    }

    private int m_view_mode;
    private PDFGLLayout.PDFPos m_save_pos;
    private boolean is_notified = false;
    private boolean is_waitting = false;

    private synchronized void wait_init() {
        try {
            if (is_notified)
                is_notified = false;
            else {
                is_waitting = true;
                wait();
                is_waitting = false;
            }
        } catch (Exception ignored) {
        }
    }

    private synchronized void notify_init() {
        if (is_waitting)
            notify();
        else
            is_notified = true;
    }

    public void PDFSetView(final int view_mode) {
        is_waitting = false;
        is_notified = false;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                PDFGLLayout layout;
                PDFGLLayout.PDFPos pos = null;
                m_view_mode = view_mode;
                if (m_layout != null) pos = m_layout.vGetPos(m_w >> 1, m_h >> 1);
                switch (view_mode) {
                    case 1://horz
                        layout = new PDFGLLayoutHorz(getContext(), Global.g_layout_rtol);
                        break;
                    case 2://curl
                        layout = new PDFGLLayoutCurl(getContext());
                        break;
                    case 3://single
                        layout = new PDFGLLayoutDual(getContext(), PDFGLLayoutDual.ALIGN_CENTER, PDFGLLayoutDual.SCALE_SAME_HEIGHT, Global.g_layout_rtol, null, null);
                        break;
                    case 5:
                        layout = new PDFGLLayoutReflow(getContext());
                        break;
                    case 4:
                    case 6://dual when landscape
                    {
                        int pcnt = m_doc.GetPageCount();
                        boolean[] bval = new boolean[pcnt];
                        /*
                        //following codes make first page as single, and others are dual.
                        bval[0] = false;
                        int position = 1;
                        for (int pcur = 1; pcur < pcnt; pcur++) {
                            float pw = m_doc.GetPageWidth(pcur);
                            float ph = m_doc.GetPageHeight(pcur);
                            if (pw > ph) {
                                bval[position] = false;
                            } else if(pcur < pcnt - 1) {
                                float pw_next = m_doc.GetPageWidth(pcur + 1);
                                float ph_next = m_doc.GetPageHeight(pcur + 1);
                                if (pw_next > ph_next) bval[position] = false;
                                else {
                                    bval[position] = true;
                                    pcur++;
                                }
                            }
                            position++;
                        }
                         */
                        int position = 0;
                        for (int pcur = 0; pcur < pcnt; pcur++) {
                            float pw = m_doc.GetPageWidth(pcur);
                            float ph = m_doc.GetPageHeight(pcur);
                            if (pw > ph) {
                                bval[position] = false;
                            } else if (pcur < pcnt - 1) {
                                float pw_next = m_doc.GetPageWidth(pcur + 1);
                                float ph_next = m_doc.GetPageHeight(pcur + 1);
                                if (pw_next > ph_next) bval[position] = false;
                                else {
                                    bval[position] = true;
                                    pcur++;
                                }
                            }
                            position++;
                        }
                        layout = new PDFGLLayoutDual(getContext(), PDFGLLayoutDual.ALIGN_CENTER, PDFGLLayoutDual.SCALE_FIT, Global.g_layout_rtol, bval, null);
                    }
                    break;
                    default://vertical.
                        layout = new PDFGLLayoutVert(getContext(), PDFGLLayoutVert.ALIGN_CENTER, false);
                        break;
                }
                layout.vOpen(m_doc, new PDFGLLayout.GLListener() {
                    @Override
                    public void OnBlockRendered(int pageno) {
                        if (m_listener != null)
                            m_listener.OnPDFPageRendered(m_layout.vGetPage(pageno));
                        requestRender();
                    }
                    @Override
                    public void OnFound(boolean found) {
                        if (m_listener != null) m_listener.OnPDFSearchFinished(found);
                        if (found && m_canvas != null) invalidate();
                        requestRender();
                    }
                    @Override
                    public void OnRedraw() {
                        requestRender();
                    }
                }, m_page_gap);
                if (m_layout != null && m_gl10 != null)
                    m_layout.gl_close(m_gl10);
                m_layout = layout;
                if (m_gl10 != null) {
                    m_layout.gl_surface_create(m_gl10);
                    m_layout.gl_resize(m_w, m_h);
                    if (pos != null) {
                        if (view_mode == 3 || view_mode == 4 || view_mode == 6) {
                            m_layout.vGotoPage(pos.pageno);
                        } else {
                            m_layout.vSetPos(0, 0, pos);
                            m_layout.gl_move_end();
                        }
                    }
                    requestRender();
                }
                notify_init();
            }
        });
        wait_init();
    }

    private boolean m_hold = false;
    private float m_hold_x;
    private float m_hold_y;
    private float m_end_x;
    private float m_end_y;
    private float m_sel_start_icon_x;
    private float m_sel_start_icon_y;
    private float m_sel_end_icon_x;
    private float m_sel_end_icon_y;
    private boolean SelectFromStart = false;
    private int m_hold_docx;
    private int m_hold_docy;
    private PDFGLLayout.PDFPos m_zoom_pos;
    private float m_zoom_dis0;
    private float m_zoom_scale;

    static abstract class TouchRunnable implements Runnable
    {
        private final float[] pts;
        private int pts_cur;
        private int pts_end;
        TouchRunnable()
        {
            pts = new float[256];
            pts_cur = 0;
            pts_end = 0;
        }
        public void new_point(float x, float y)
        {
            pts[pts_end] = x;
            pts[pts_end + 1] = y;
            pts_end += 2;
            pts_end &= 255;
        }
        protected float get_x()
        {
            return pts[pts_cur];
        }
        protected float get_y()
        {
            return pts[pts_cur + 1];
        }
        protected void next()
        {
            pts_cur += 2;
            pts_cur &= 255;
        }
    }


    private final Runnable OnNoneDown = new Runnable(){
        @Override
        public void run() {
            m_layout.gl_abort_scroll();
            m_layout.gl_down((int) m_hold_x, (int) m_hold_y);
            m_hold_docx = m_layout.vGetX();
            m_hold_docy = m_layout.vGetY();
        }
    };
    private final TouchRunnable OnNoneMove = new TouchRunnable(){
        @Override
        public void run() {
            float nx = get_x();
            float ny = get_y();
            next();
            m_layout.gl_move((int) nx, (int) ny);
            m_layout.vSetX((int) (m_hold_docx + m_hold_x - nx));
            m_layout.vSetY((int) (m_hold_docy + m_hold_y - ny));
            requestRender();
        }
    };
    private final TouchRunnable OnNoneUp = new TouchRunnable(){
        @Override
        public void run() {
            float nx = get_x();
            float ny = get_y();
            next();
            m_layout.vSetX((int) (m_hold_docx + m_hold_x - nx));
            m_layout.vSetY((int) (m_hold_docy + m_hold_y - ny));
            m_layout.gl_move_end();
            requestRender();
        }
    };
    private final Runnable OnZoomStart = new Runnable(){
        @Override
        public void run() {
            m_zoom_pos = m_layout.vGetPos((int) m_hold_x, (int) m_hold_y);
            m_layout.gl_zoom_start(m_gl10);
        }
    };
    private boolean onTouchNone(MotionEvent event) {
        if (m_status != STA_NONE) return false;
        if (m_gesture.onTouchEvent(event)) return true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                m_hold_x = event.getX();
                m_hold_y = event.getY();
                m_hold = true;
                queueEvent(OnNoneDown);
                break;
            case MotionEvent.ACTION_MOVE:
                if (m_hold) {
                    OnNoneMove.new_point(event.getX(), event.getY());
                    queueEvent(OnNoneMove);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (m_hold) {
                    m_hold = false;
                    OnNoneUp.new_point(event.getX(), event.getY());
                    queueEvent(OnNoneUp);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (m_layout.vSupportZoom() && event.getPointerCount() >= 2) {
                    m_status = STA_ZOOM;
                    m_hold_x = (event.getX(0) + event.getX(1)) * 0.5f;
                    m_hold_y = (event.getY(0) + event.getY(1)) * 0.5f;
                    float dx = event.getX(0) - event.getX(1);
                    float dy = event.getY(0) - event.getY(1);
                    m_zoom_dis0 = Global.sqrtf(dx * dx + dy * dy);
                    m_zoom_scale = m_layout.vGetZoom();
                    queueEvent(OnZoomStart);
                    if (m_listener != null)
                        m_listener.OnPDFZoomStart();
                }
                break;
        }
        return true;
    }

    private final TouchRunnable OnZoomMove = new TouchRunnable(){
        @Override
        public void run() {
            float dis = get_x();
            next();
            m_layout.gl_zoom_set(m_zoom_scale * dis / m_zoom_dis0);
            m_layout.vSetPos((int) m_hold_x, (int) m_hold_y, m_zoom_pos);
            requestRender();
        }
    };
    private final Runnable OnZoomEnd = new Runnable(){
        @Override
        public void run() {
            m_layout.gl_zoom_confirm(m_gl10);
            m_hold_x = -10000;
            m_hold_y = -10000;
            requestRender();
        }
    };
    private boolean onTouchZoom(MotionEvent event) {
        if (m_status != STA_ZOOM) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (m_status == STA_ZOOM && event.getPointerCount() >= 2) {
                    float dx = event.getX(0) - event.getX(1);
                    float dy = event.getY(0) - event.getY(1);
                    OnZoomMove.new_point(Global.sqrtf(dx * dx + dy * dy), 0);
                    queueEvent(OnZoomMove);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (m_status == STA_ZOOM && event.getPointerCount() == 2) {
                    m_status = STA_NONE;
                    m_hold = false;
                    queueEvent(OnZoomEnd);
                    if (m_listener != null)
                        m_listener.OnPDFZoomEnd();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (m_status == STA_ZOOM) {
                    m_status = STA_NONE;
                    m_hold = false;
                    queueEvent(OnZoomEnd);
                    if (m_listener != null)
                        m_listener.OnPDFZoomEnd();
                }
                break;
        }
        return true;
    }

    private boolean onTouchSelect(MotionEvent event) {
        if (m_status != STA_SELECT) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                float dist_start = (m_sel_start_icon_x - x) * (m_sel_start_icon_x - x) + (m_sel_start_icon_y - y) * (m_sel_start_icon_y - y);
                float dist_end = (m_sel_end_icon_x - x) * (m_sel_end_icon_x - x) + (m_sel_end_icon_y - y) * (m_sel_end_icon_y - y);
                float dist_threshold = 5000f;
                if (dist_start < dist_threshold || dist_end < dist_threshold) {
                    SelectFromStart = dist_start < dist_end;
                } else {
                    m_hold_x = event.getX();
                    m_hold_y = event.getY();
                    SelectFromStart = false;
                    PDFSetSelect();
                }
                /*m_hold_x = event.getX();
                m_hold_y = event.getY();
                if (m_sel != null) {
                    m_sel.Clear();
                    m_sel = null;
                }
                m_annot_pos = m_layout.vGetPos((int) m_hold_x, (int) m_hold_y);
                m_annot_page = m_layout.vGetPage(m_annot_pos.pageno);
                m_sel = new PDFSel(m_doc.GetPage(m_annot_pos.pageno));*/
                break;
            case MotionEvent.ACTION_MOVE:
                if (m_sel != null && m_annot_pos != null && m_annot_page != null && m_layout != null) {
                    if (!SelectFromStart)
                        m_sel.SetSel(m_annot_pos.x, m_annot_pos.y,
                                m_annot_page.ToPDFX(event.getX(), m_layout.vGetX()),
                                m_annot_page.ToPDFY(event.getY(), m_layout.vGetY()));
                    else {
                        m_annot_pos = m_layout.vGetPos((int) event.getX(), (int) event.getY());
                        m_sel.SetSel(m_annot_pos.x, m_annot_pos.y,
                                m_annot_page.ToPDFX(m_end_x, m_layout.vGetX()),
                                m_annot_page.ToPDFY(m_end_y, m_layout.vGetY())
                        );
                    }
                    if (m_canvas != null) m_canvas.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (m_sel != null && m_annot_pos != null && m_annot_page != null && m_layout != null) {
                    if (!SelectFromStart) {
                        m_sel.SetSel(m_annot_pos.x, m_annot_pos.y,
                                m_annot_page.ToPDFX(event.getX(), m_layout.vGetX()),
                                m_annot_page.ToPDFY(event.getY(), m_layout.vGetY()));
                        m_end_x = event.getX();
                        m_end_y = event.getY();
                    } else {
                        m_annot_pos = m_layout.vGetPos((int) event.getX(), (int) event.getY());
                        m_sel.SetSel(m_annot_pos.x, m_annot_pos.y,
                                m_annot_page.ToPDFX(m_end_x, m_layout.vGetX()),
                                m_annot_page.ToPDFY(m_end_y, m_layout.vGetY())
                        );
                        m_hold_x = event.getX();
                        m_hold_y = event.getY();
                    }
                    if (m_canvas != null) m_canvas.invalidate();
                    if (m_listener != null)
                        m_listener.OnPDFTextSelected(m_sel.GetSelString(), event.getX(), event.getY());
                }
                break;
        }
        return true;
    }

    private boolean onTouchInk(MotionEvent event) {
        if (m_status != STA_INK) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (m_annot_page == null) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) event.getX(), (int) event.getY());
                    m_annot_page = m_layout.vGetPage(pos.pageno);
                }
                m_ink.OnDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                m_ink.OnMove(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                m_ink.OnUp(event.getX(), event.getY());
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchPolygon(MotionEvent event) {
        if (m_status != STA_POLYGON) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (m_annot_page == null) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) event.getX(), (int) event.getY());
                    m_annot_page = m_layout.vGetPage(pos.pageno);
                }
                if (m_polygon.GetNodeCount() < 1)
                    m_polygon.MoveTo(event.getX(), event.getY());
                else
                    m_polygon.LineTo(event.getX(), event.getY());
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchPolyline(MotionEvent event) {
        if (m_status != STA_POLYLINE) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (m_annot_page == null) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) event.getX(), (int) event.getY());
                    m_annot_page = m_layout.vGetPage(pos.pageno);
                }
                if (m_polygon.GetNodeCount() < 1)
                    m_polygon.MoveTo(event.getX(), event.getY());
                else
                    m_polygon.LineTo(event.getX(), event.getY());
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchRect(MotionEvent event) {
        if (m_status != STA_RECT) return false;
        int len = 0;
        if (m_rects != null) len = m_rects.length;
        int cur;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float[] rects = new float[len + 4];
                for (cur = 0; cur < len; cur++)
                    rects[cur] = m_rects[cur];
                rects[cur    ] = event.getX();
                rects[cur + 1] = event.getY();
                rects[cur + 2] = event.getX();
                rects[cur + 3] = event.getY();
                m_rects = rects;
                break;
            case MotionEvent.ACTION_MOVE:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchEllipse(MotionEvent event) {
        if (m_status != STA_ELLIPSE) return false;
        int len = 0;
        if (m_rects != null) len = m_rects.length;
        int cur;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float[] rects = new float[len + 4];
                for (cur = 0; cur < len; cur++)
                    rects[cur] = m_rects[cur];
                rects[cur    ] = event.getX();
                rects[cur + 1] = event.getY();
                rects[cur + 2] = event.getX();
                rects[cur + 3] = event.getY();
                m_rects = rects;
                break;
            case MotionEvent.ACTION_MOVE:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchAnnot(MotionEvent event) {
        if (m_status != STA_ANNOT || !PDFCanSave()) return false;
        if ((Global.g_annot_lock && m_annot.IsLocked()) || (Global.g_annot_readonly && m_annot.IsReadOnly()) ||
                m_annot.GetType() == 2 ||
                m_annot.GetType() == 9 ||
                m_annot.GetType() == 10 ||
                m_annot.GetType() == 11 ||
                m_annot.GetType() == 12 ||
                m_annot.GetType() == 20) {
            PDFEndAnnot();
            return false;
        }
        m_annot_op.onTouch(event);
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchLine(MotionEvent event) {
        if (m_status != STA_LINE) return false;
        int len = 0;
        if (m_rects != null) len = m_rects.length;
        int cur;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float[] rects = new float[len + 4];
                for (cur = 0; cur < len; cur++)
                    rects[cur] = m_rects[cur];
                rects[cur    ] = event.getX();
                rects[cur + 1] = event.getY();
                rects[cur + 2] = event.getX();
                rects[cur + 3] = event.getY();
                m_rects = rects;
                break;
            case MotionEvent.ACTION_MOVE:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchStamp(MotionEvent event) {
        if (m_status != STA_STAMP) return false;
        int len = 0;
        if (m_rects != null) len = m_rects.length;
        int cur;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float[] rects = new float[len + 4];
                for (cur = 0; cur < len; cur++)
                    rects[cur] = m_rects[cur];
                rects[cur    ] = event.getX();
                rects[cur + 1] = event.getY();
                rects[cur + 2] = event.getX();
                rects[cur + 3] = event.getY();
                m_rects = rects;
                break;
            case MotionEvent.ACTION_MOVE:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchEditbox(MotionEvent event) {
        if (m_status != STA_EDITBOX) return false;
        int len = 0;
        if (m_rects != null) len = m_rects.length;
        PDFGLLayout.PDFPos pos;
        int cur;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float[] rects = new float[len + 4];
                for (cur = 0; cur < len; cur++)
                    rects[cur] = m_rects[cur];
                rects[cur    ] = event.getX();
                rects[cur + 1] = event.getY();
                rects[cur + 2] = event.getX();
                rects[cur + 3] = event.getY();
                m_rects = rects;
                break;
            case MotionEvent.ACTION_MOVE:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
            case MotionEvent.ACTION_UP://when touch up, it shall popup editbox on page
            case MotionEvent.ACTION_CANCEL:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                pos = m_layout.vGetPos((int) m_rects[0], (int) m_rects[1]);
                m_annot_page = m_layout.vGetPage(pos.pageno);
                m_annot_pg = m_doc.GetPage(m_annot_page.GetPageNo());
                PDFSetEditbox(1);//end editbox.
            {//popup editbox from UI.
                m_annot_pg.ObjsStart();
                m_annot = m_annot_pg.GetAnnot(m_annot_pg.GetAnnotCount() - 1);
                m_annot_rect = m_annot.GetRect();
                float tmp = m_annot_rect[1];
                m_annot_rect[0] = m_annot_page.GetVX(m_annot_rect[0]) - m_layout.vGetX();
                m_annot_rect[1] = m_annot_page.GetVY(m_annot_rect[3]) - m_layout.vGetY();
                m_annot_rect[2] = m_annot_page.GetVX(m_annot_rect[2]) - m_layout.vGetX();
                m_annot_rect[3] = m_annot_page.GetVY(tmp) - m_layout.vGetY();
                if (m_listener != null)
                    m_listener.OnPDFAnnotTapped(m_annot_page.GetPageNo(), m_annot);
                m_status = STA_ANNOT;
                m_annot_op = m_annot_op_normal;
                if (m_pEdit == null) m_pEdit = new UIAnnotPopEdit(PDFGLView.this);
                m_pEdit.update(m_annot, m_annot_rect, m_annot_page.GetScale());
                m_edit_type = 1;
                m_pEdit.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        if (m_annot != null) {
                            if (!m_annot.SetEditText(m_pEdit.getEditText())) {
                                Log.e("RDERR", "set EditText failed.");
                            }
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            //if (m_annot != null && Global.sExecuteAnnotJS) executeAnnotJS();//there is JS on free text annotation.
                            m_layout.gl_render(m_annot_page, m_annot.GetRect());
                            requestRender();
                            if (m_listener != null)
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                            PDFEndAnnot();
                            m_edit_type = 0;
                        }
                    }
                });
                post(new Runnable() {
                    @Override
                    public void run() {
                        int[] location = new int[2];
                        getLocationOnScreen(location);
                        m_pEdit.show(PDFGLView.this, (int) m_annot_rect[0] + location[0], (int) (m_annot_rect[1] + location[1]));
                    }
                });
            }
            break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchFieldEditbox(MotionEvent event) {
        if (m_status != STA_FIELD_EDITBOX) return false;
        int len = 0;
        if (m_rects != null) len = m_rects.length;
        PDFGLLayout.PDFPos pos;
        int cur;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float[] rects = new float[len + 4];
                for (cur = 0; cur < len; cur++)
                    rects[cur] = m_rects[cur];
                rects[cur    ] = event.getX();
                rects[cur + 1] = event.getY();
                rects[cur + 2] = event.getX();
                rects[cur + 3] = event.getY();
                m_rects = rects;
                break;
            case MotionEvent.ACTION_MOVE:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
            case MotionEvent.ACTION_UP://when touch up, it shall popup editbox on page
            case MotionEvent.ACTION_CANCEL:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                pos = m_layout.vGetPos((int) m_rects[0], (int) m_rects[1]);
                m_annot_page = m_layout.vGetPage(pos.pageno);
                m_annot_pg = m_doc.GetPage(m_annot_page.GetPageNo());
                PDFSetFieldEditbox(1);//end editbox.
            {//popup editbox from UI.
                m_annot_pg.ObjsStart();
                m_annot = m_annot_pg.GetAnnot(m_annot_pg.GetAnnotCount() - 1);
                m_annot_rect = m_annot.GetRect();
                float tmp = m_annot_rect[1];
                m_annot_rect[0] = m_annot_page.GetVX(m_annot_rect[0]) - m_layout.vGetX();
                m_annot_rect[1] = m_annot_page.GetVY(m_annot_rect[3]) - m_layout.vGetY();
                m_annot_rect[2] = m_annot_page.GetVX(m_annot_rect[2]) - m_layout.vGetX();
                m_annot_rect[3] = m_annot_page.GetVY(tmp) - m_layout.vGetY();
                if (m_listener != null)
                    m_listener.OnPDFAnnotTapped(m_annot_page.GetPageNo(), m_annot);
                m_status = STA_ANNOT;
                if (m_pEdit == null) m_pEdit = new UIAnnotPopEdit(PDFGLView.this);
                m_pEdit.update(m_annot, m_annot_rect, m_annot_page.GetScale());
                m_edit_type = 1;
                m_pEdit.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        if (m_annot != null) {
                            if (!m_annot.SetEditText(m_pEdit.getEditText())) {
                                Log.e("RDERR", "set EditText failed.");
                            }
                            m_annot.SetModifyDate(CommonUtil.getCurrentDate());
                            //if (m_annot != null && Global.sExecuteAnnotJS) executeAnnotJS();//there is JS on free text annotation.
                            m_layout.gl_render(m_annot_page, m_annot.GetRect());
                            requestRender();
                            if (m_listener != null)
                                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                            PDFEndAnnot();
                            m_edit_type = 0;
                        }
                    }
                });
                int[] location = new int[2];
                getLocationOnScreen(location);
                m_pEdit.show(PDFGLView.this, (int) m_annot_rect[0] + location[0], (int) (m_annot_rect[1] + location[1]));
            }
            break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchFieldSignature(MotionEvent event) {
        if (m_status != STA_FIELD_SIGNATURE) return false;
        int len = 0;
        if (m_rects != null) len = m_rects.length;
        PDFGLLayout.PDFPos pos;
        int cur;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float[] rects = new float[len + 4];
                for (cur = 0; cur < len; cur++)
                    rects[cur] = m_rects[cur];
                rects[cur    ] = event.getX();
                rects[cur + 1] = event.getY();
                rects[cur + 2] = event.getX();
                rects[cur + 3] = event.getY();
                m_rects = rects;
                break;
            case MotionEvent.ACTION_MOVE:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
            case MotionEvent.ACTION_UP://when touch up, it shall popup editbox on page
            case MotionEvent.ACTION_CANCEL:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                pos = m_layout.vGetPos((int) m_rects[0], (int) m_rects[1]);
                m_annot_page = m_layout.vGetPage(pos.pageno);
                m_annot_pg = m_doc.GetPage(m_annot_page.GetPageNo());
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchFieldCheck(MotionEvent event) {
        if (m_status != STA_FIELD_CHECK) return false;
        int len = 0;
        if (m_rects != null) len = m_rects.length;
        PDFGLLayout.PDFPos pos;
        int cur;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float[] rects = new float[len + 4];
                for (cur = 0; cur < len; cur++)
                    rects[cur] = m_rects[cur];
                rects[cur    ] = event.getX();
                rects[cur + 1] = event.getY();
                rects[cur + 2] = event.getX();
                rects[cur + 3] = event.getY();
                m_rects = rects;
                break;
            case MotionEvent.ACTION_MOVE:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
            case MotionEvent.ACTION_UP://when touch up, it shall popup editbox on page
            case MotionEvent.ACTION_CANCEL:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                pos = m_layout.vGetPos((int) m_rects[0], (int) m_rects[1]);
                m_annot_page = m_layout.vGetPage(pos.pageno);
                m_annot_pg = m_doc.GetPage(m_annot_page.GetPageNo());
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchFieldRadio(MotionEvent event) {
        if (m_status != STA_FIELD_RADIO) return false;
        int len = 0;
        if (m_rects != null) len = m_rects.length;
        PDFGLLayout.PDFPos pos;
        int cur;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                float[] rects = new float[len + 4];
                for (cur = 0; cur < len; cur++)
                    rects[cur] = m_rects[cur];
                rects[cur    ] = event.getX();
                rects[cur + 1] = event.getY();
                rects[cur + 2] = event.getX();
                rects[cur + 3] = event.getY();
                m_rects = rects;
                break;
            case MotionEvent.ACTION_MOVE:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                break;
            case MotionEvent.ACTION_UP://when touch up, it shall popup editbox on page
            case MotionEvent.ACTION_CANCEL:
                m_rects[len - 2] = event.getX();
                m_rects[len - 1] = event.getY();
                pos = m_layout.vGetPos((int) m_rects[0], (int) m_rects[1]);
                m_annot_page = m_layout.vGetPage(pos.pageno);
                m_annot_pg = m_doc.GetPage(m_annot_page.GetPageNo());
                break;
        }
        if (m_canvas != null) m_canvas.invalidate();
        return true;
    }

    private boolean onTouchNote(MotionEvent event) {
        if (m_status != STA_NOTE) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) event.getX(), (int) event.getY());
                PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                Page page = m_doc.GetPage(vpage.GetPageNo());
                if (page != null) {
                    page.ObjsStart();
                    if (m_note_pages == null) {
                        m_note_pages = new PDFGLPage[1];
                        m_note_indecs = new int[1];
                        m_note_pages[0] = vpage;
                        m_note_indecs[0] = page.GetAnnotCount();
                    } else {
                        int cur = 0;
                        int cnt = m_note_pages.length;
                        while (cur < cnt) {
                            if (m_note_pages[cur] == vpage) break;
                            cur++;
                        }
                        if (cur >= cnt)//append 1 page
                        {
                            PDFGLPage[] pages = new PDFGLPage[cnt + 1];
                            int[] indecs = new int[cnt + 1];
                            for (cur = 0; cur < cnt; cur++) {
                                pages[cur] = m_note_pages[cur];
                                indecs[cur] = m_note_indecs[cur];
                            }
                            pages[cnt] = vpage;
                            indecs[cnt] = page.GetAnnotCount();
                            m_note_pages = pages;
                            m_note_indecs = indecs;
                        }
                    }
                    float[] pt = new float[2];
                    pt[0] = pos.x;
                    pt[1] = pos.y;
                    page.AddAnnotText(pt);
                    //add to redo/undo stack.
                    int aidx = page.GetAnnotCount() - 1;
                    m_opstack.push(new OPAdd(pos.pageno, page, aidx));
                    m_layout.gl_render(vpage, page.GetAnnot(aidx).GetRect());
                    requestRender();
                    if (m_canvas != null) m_canvas.invalidate();
                    page.Close();

                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (m_layout == null) return false;
        if (onTouchNone(event)) return true;
        if (onTouchZoom(event)) return true;
        if (onTouchSelect(event)) return true;
        if (onTouchInk(event)) return true;
        if (onTouchRect(event)) return true;
        if (onTouchEllipse(event)) return true;
        if (onTouchNote(event)) return true;
        if (onTouchLine(event)) return true;
        if (onTouchStamp(event)) return true;
        if (onTouchAnnot(event)) return true;
        if (onTouchEditbox(event)) return true;
        if (onTouchPolygon(event)) return true;
        if (onTouchPolyline(event)) return true;
        if (onTouchFieldEditbox(event)) return true;
        if (onTouchFieldSignature(event)) return true;
        if (onTouchFieldCheck(event)) return true;
        if (onTouchFieldRadio(event)) return true;
        return true;
    }

    private void onDrawSelect(Canvas canvas) {
        if (m_status == STA_SELECT && m_sel != null && m_annot_page != null) {
            int orgx = m_annot_page.GetVX(0) - m_layout.vGetX();
            int orgy = m_annot_page.GetVY(m_doc.GetPageHeight(m_annot_page.GetPageNo())) - m_layout.vGetY();
            float scale = m_annot_page.GetScale();
            float pheight = m_doc.GetPageHeight(m_annot_page.GetPageNo());
            m_sel.DrawSel(canvas, scale, pheight, orgx, orgy);
            int[] rect1 = m_sel.GetRect1(scale, pheight, orgx, orgy);
            int[] rect2 = m_sel.GetRect2(scale, pheight, orgx, orgy);
            if (rect1 != null && rect2 != null) {
                canvas.drawBitmap(m_sel_icon1, rect1[0] - m_sel_icon1.getWidth(), rect1[1] - m_sel_icon1.getHeight(), null);
                canvas.drawBitmap(m_sel_icon2, rect2[2], rect2[3], null);
                m_sel_start_icon_x = rect1[0] - m_sel_icon1.getWidth();
                m_sel_start_icon_y = rect1[1] - m_sel_icon1.getHeight();
                m_sel_end_icon_x = rect2[2];
                m_sel_end_icon_y = rect2[3];
            }
        }
    }

    private void onDrawAnnot(Canvas canvas) {
        if (m_status == STA_ANNOT) {
            m_annot_op.onDraw(canvas);
        }
    }

    private void onDrawRect(Canvas canvas) {
        if (m_status == STA_RECT && m_rects != null) {
            int len = m_rects.length;
            int cur;
            Paint paint1 = new Paint();
            Paint paint2 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(Global.g_rect_annot_width);
            paint1.setColor(Global.g_rect_annot_color);
            paint2.setStyle(Paint.Style.FILL);
            paint2.setColor(Global.g_rect_annot_fill_color);
            float rad = Global.g_rect_annot_width * 0.5f;
            for (cur = 0; cur < len; cur += 4) {
                float[] rect = new float[4];
                if (m_rects[cur] > m_rects[cur + 2]) {
                    rect[0] = m_rects[cur + 2];
                    rect[2] = m_rects[cur];
                } else {
                    rect[0] = m_rects[cur];
                    rect[2] = m_rects[cur + 2];
                }
                if (m_rects[cur + 1] > m_rects[cur + 3]) {
                    rect[1] = m_rects[cur + 3];
                    rect[3] = m_rects[cur + 1];
                } else {
                    rect[1] = m_rects[cur + 1];
                    rect[3] = m_rects[cur + 3];
                }
                canvas.drawRect(rect[0], rect[1], rect[2], rect[3], paint1);
                canvas.drawRect(rect[0] + rad, rect[1] + rad, rect[2] - rad, rect[3] - rad, paint2);
            }
        }
    }

    private void onDrawLine(Canvas canvas) {
        if (m_status == STA_LINE && m_rects != null) {
            int len = m_rects.length;
            int cur;
            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(Global.g_line_annot_width);
            paint1.setColor(Global.g_line_annot_color);
            for (cur = 0; cur < len; cur += 4) {
                canvas.drawLine(m_rects[cur], m_rects[cur + 1], m_rects[cur + 2], m_rects[cur + 3], paint1);
            }
        }
    }

    private static int dp2px(Context context, float dpValue) {
        return (int) (dpValue * context.getResources().getDisplayMetrics().density);
    }

    private void onDrawPolygon(Canvas canvas) {
        if (m_status != STA_POLYGON || m_polygon == null) return;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Global.g_line_annot_color);
        paint.setStrokeWidth(Global.g_line_annot_width);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeJoin(Paint.Join.BEVEL);
        m_polygon.OnDraw(canvas, 0, 0, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Global.g_line_annot_fill_color);
        if (m_polygon.GetNodeCount() > 2)
            m_polygon.OnDraw(canvas, 0, 0, paint);
        m_polygon.onDrawPoint(canvas, 0, 0, dp2px(getContext(), 4), paint);
    }

    private void onDrawPolyline(Canvas canvas) {
        if (m_status != STA_POLYLINE || m_polygon == null) return;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Global.g_line_annot_color);
        paint.setStrokeWidth(Global.g_line_annot_width);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeJoin(Paint.Join.BEVEL);
        m_polygon.OnDraw(canvas, 0, 0, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Global.g_line_annot_fill_color);
        m_polygon.onDrawPoint(canvas, 0, 0, dp2px(getContext(), 4), paint);
    }

    private void onDrawStamp(Canvas canvas) {
        if (m_status == STA_STAMP && m_rects != null) {
            int len = m_rects.length;
            int cur;
            for (cur = 0; cur < len; cur += 4) {
                float[] rect = new float[4];
                if (m_rects[cur] > m_rects[cur + 2]) {
                    rect[0] = m_rects[cur + 2];
                    rect[2] = m_rects[cur];
                } else {
                    rect[0] = m_rects[cur];
                    rect[2] = m_rects[cur + 2];
                }
                if (m_rects[cur + 1] > m_rects[cur + 3]) {
                    rect[1] = m_rects[cur + 3];
                    rect[3] = m_rects[cur + 1];
                } else {
                    rect[1] = m_rects[cur + 1];
                    rect[3] = m_rects[cur + 3];
                }
                if (m_icon != null) {
                    Rect rc = new Rect();
                    rc.left = (int) rect[0];
                    rc.top = (int) rect[1];
                    rc.right = (int) rect[2];
                    rc.bottom = (int) rect[3];
                    canvas.drawBitmap(m_icon, null, rc, null);
                }
            }
        }
    }

    private void onDrawEditbox(Canvas canvas) {
        if (m_status == STA_EDITBOX && m_rects != null) {
            int len = m_rects.length;
            int cur;
            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(3);
            paint1.setColor(0x80FF0000);
            for (cur = 0; cur < len; cur += 4) {
                float[] rect = new float[4];
                if (m_rects[cur] > m_rects[cur + 2]) {
                    rect[0] = m_rects[cur + 2];
                    rect[2] = m_rects[cur];
                } else {
                    rect[0] = m_rects[cur];
                    rect[2] = m_rects[cur + 2];
                }
                if (m_rects[cur + 1] > m_rects[cur + 3]) {
                    rect[1] = m_rects[cur + 3];
                    rect[3] = m_rects[cur + 1];
                } else {
                    rect[1] = m_rects[cur + 1];
                    rect[3] = m_rects[cur + 3];
                }
                canvas.drawRect(rect[0], rect[1], rect[2], rect[3], paint1);
            }
        }
    }

    private void onDrawFieldEditbox(Canvas canvas) {
        if (m_status == STA_FIELD_EDITBOX && m_rects != null) {
            int len = m_rects.length;
            int cur;
            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(3);
            paint1.setColor(0x80FF0000);
            for (cur = 0; cur < len; cur += 4) {
                float[] rect = new float[4];
                if (m_rects[cur] > m_rects[cur + 2]) {
                    rect[0] = m_rects[cur + 2];
                    rect[2] = m_rects[cur];
                } else {
                    rect[0] = m_rects[cur];
                    rect[2] = m_rects[cur + 2];
                }
                if (m_rects[cur + 1] > m_rects[cur + 3]) {
                    rect[1] = m_rects[cur + 3];
                    rect[3] = m_rects[cur + 1];
                } else {
                    rect[1] = m_rects[cur + 1];
                    rect[3] = m_rects[cur + 3];
                }
                canvas.drawRect(rect[0], rect[1], rect[2], rect[3], paint1);
            }
        }
    }

    private void onDrawFieldSignature(Canvas canvas) {
        if (m_status == STA_FIELD_SIGNATURE && m_rects != null) {
            int len = m_rects.length;
            int cur;
            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(3);
            paint1.setColor(0x80FF0000);
            for (cur = 0; cur < len; cur += 4) {
                float[] rect = new float[4];
                if (m_rects[cur] > m_rects[cur + 2]) {
                    rect[0] = m_rects[cur + 2];
                    rect[2] = m_rects[cur];
                } else {
                    rect[0] = m_rects[cur];
                    rect[2] = m_rects[cur + 2];
                }
                if (m_rects[cur + 1] > m_rects[cur + 3]) {
                    rect[1] = m_rects[cur + 3];
                    rect[3] = m_rects[cur + 1];
                } else {
                    rect[1] = m_rects[cur + 1];
                    rect[3] = m_rects[cur + 3];
                }
                canvas.drawRect(rect[0], rect[1], rect[2], rect[3], paint1);
            }
        }
    }

    private void onDrawFieldCheck(Canvas canvas) {
        if (m_status == STA_FIELD_CHECK && m_rects != null) {
            int len = m_rects.length;
            int cur;
            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(3);
            paint1.setColor(0x80FF0000);
            for (cur = 0; cur < len; cur += 4) {
                float[] rect = new float[4];
                if (m_rects[cur] > m_rects[cur + 2]) {
                    rect[0] = m_rects[cur + 2];
                    rect[2] = m_rects[cur];
                } else {
                    rect[0] = m_rects[cur];
                    rect[2] = m_rects[cur + 2];
                }
                if (m_rects[cur + 1] > m_rects[cur + 3]) {
                    rect[1] = m_rects[cur + 3];
                    rect[3] = m_rects[cur + 1];
                } else {
                    rect[1] = m_rects[cur + 1];
                    rect[3] = m_rects[cur + 3];
                }
                canvas.drawRect(rect[0], rect[1], rect[2], rect[3], paint1);
            }
        }
    }

    private void onDrawFieldRadio(Canvas canvas) {
        if (m_status == STA_FIELD_RADIO && m_rects != null) {
            int len = m_rects.length;
            int cur;
            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(3);
            paint1.setColor(0x80FF0000);
            for (cur = 0; cur < len; cur += 4) {
                float[] rect = new float[4];
                if (m_rects[cur] > m_rects[cur + 2]) {
                    rect[0] = m_rects[cur + 2];
                    rect[2] = m_rects[cur];
                } else {
                    rect[0] = m_rects[cur];
                    rect[2] = m_rects[cur + 2];
                }
                if (m_rects[cur + 1] > m_rects[cur + 3]) {
                    rect[1] = m_rects[cur + 3];
                    rect[3] = m_rects[cur + 1];
                } else {
                    rect[1] = m_rects[cur + 1];
                    rect[3] = m_rects[cur + 3];
                }
                canvas.drawRect(rect[0], rect[1], rect[2], rect[3], paint1);
            }
        }
    }

    private void onDrawEllipse(Canvas canvas) {
        if (m_status == STA_ELLIPSE && m_rects != null) {
            int len = m_rects.length;
            int cur;
            Paint paint1 = new Paint();
            Paint paint2 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(Global.g_oval_annot_width);
            paint1.setColor(Global.g_oval_annot_color);
            paint2.setStyle(Paint.Style.FILL);
            paint2.setColor(Global.g_oval_annot_fill_color);
            float rad = Global.g_oval_annot_width * 0.5f;
            for (cur = 0; cur < len; cur += 4) {
                float[] rect = new float[4];
                if (m_rects[cur] > m_rects[cur + 2]) {
                    rect[0] = m_rects[cur + 2];
                    rect[2] = m_rects[cur];
                } else {
                    rect[0] = m_rects[cur];
                    rect[2] = m_rects[cur + 2];
                }
                if (m_rects[cur + 1] > m_rects[cur + 3]) {
                    rect[1] = m_rects[cur + 3];
                    rect[3] = m_rects[cur + 1];
                } else {
                    rect[1] = m_rects[cur + 1];
                    rect[3] = m_rects[cur + 3];
                }
                RectF rc = new RectF();
                rc.left = rect[0];
                rc.top = rect[1];
                rc.right = rect[2];
                rc.bottom = rect[3];
                canvas.drawOval(rc, paint1);
                rc.left += rad;
                rc.top += rad;
                rc.right -= rad;
                rc.bottom -= rad;
                canvas.drawOval(rc, paint2);
            }
        }
    }

    private ActivityManager m_amgr;
    private final ActivityManager.MemoryInfo m_info = new ActivityManager.MemoryInfo();
    private final Paint m_info_paint = new Paint();

    @Override
    public void drawLayer(Canvas canvas) {
        if (m_layout != null) {
            m_layout.vFindDraw(canvas);
            onDrawSelect(canvas);
            onDrawRect(canvas);
            onDrawEllipse(canvas);
            onDrawAnnot(canvas);
            onDrawLine(canvas);
            onDrawStamp(canvas);
            onDrawEditbox(canvas);
            onDrawFieldEditbox(canvas);
            onDrawFieldSignature(canvas);
            onDrawFieldCheck(canvas);
            onDrawFieldRadio(canvas);
            onDrawPolygon(canvas);
            onDrawPolyline(canvas);
            onDrawFTS(canvas);
            if (m_status == STA_INK && m_ink != null)
                m_ink.OnDraw(canvas, 0, 0);
        }
        if (Global.debug_mode) {
            try {
                if (m_amgr == null) {
                    m_amgr = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
                    m_info_paint.setARGB(255, 255, 0, 0);
                    m_info_paint.setTextSize(30);
                }
                m_amgr.getMemoryInfo(m_info);
                canvas.drawText("AvialMem:" + m_info.availMem / (1024 * 1024) + " M", 20, 150, m_info_paint);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    static class PDFGLPageSet {
        PDFGLPageSet(int max_len) {
            pages = new PDFGLPage[max_len];
            pages_cnt = 0;
        }

        void Insert(PDFGLPage vpage) {
            int cur;
            for (cur = 0; cur < pages_cnt; cur++) {
                if (pages[cur] == vpage) return;
            }
            pages[cur] = vpage;
            pages_cnt++;
        }

        PDFGLPage[] pages;
        int pages_cnt;
    }

    private Page.Annotation m_annot = null;
    private PDFGLLayout.PDFPos m_annot_pos = null;
    private PDFGLPage m_annot_page = null;
    private Page m_annot_pg = null;
    private float[] m_annot_rect;
    private float[] m_annot_rect0;
    private float m_annot_x0;
    private float m_annot_y0;
    private Ink m_ink = null;
    private Path m_polygon;
    private Bitmap m_icon = null;
    private Document.DocImage m_dicon = null;
    private float[] m_rects;
    private PDFGLPage[] m_note_pages;
    private int[] m_note_indecs;
    private PDFLayoutOPStack m_opstack = new PDFLayoutOPStack();
    private Bitmap m_sel_icon1 = null;
    private Bitmap m_sel_icon2 = null;
    private int m_edit_type = 0;
    private UIAnnotPopEdit m_pEdit = null;
    private UIAnnotPopCombo m_pCombo = null;
    private UIAnnotMenu m_aMenu = null;
    private PDFSel m_sel = null;

    public void PDFSetInk(int code) {
        if (code == 0)//start
        {
            m_status = STA_INK;
            m_ink = new Ink(Global.g_ink_width, Global.g_ink_color);
        } else if (code == 1)//end
        {
            m_status = STA_NONE;
            if (m_annot_page != null) {
                Page page = m_doc.GetPage(m_annot_page.GetPageNo());
                if (page != null) {
                    page.ObjsStart();
                    Matrix mat = m_annot_page.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                    mat.TransformInk(m_ink);
                    page.AddAnnotInk(m_ink);
                    mat.Destroy();
                    //add to redo/undo stack.
                    m_opstack.push(new OPAdd(m_annot_page.GetPageNo(), page, page.GetAnnotCount() - 1));
                    Page.Annotation annot = page.GetAnnot(page.GetAnnotCount() - 1);
                    m_layout.gl_render(m_annot_page, annot.GetRect());
                    requestRender();
                    page.Close();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                }
            }
            if (m_ink != null) m_ink.Destroy();
            m_ink = null;
            m_annot_page = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_ink.Destroy();
            m_ink = null;
            m_annot_page = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetPolygon(int code) {
        if (code == 0)//start
        {
            m_status = STA_POLYGON;
            m_polygon = new Path();
        } else if (code == 1)//end
        {
            m_status = STA_NONE;
            if (m_annot_page != null) {
                Page page = m_doc.GetPage(m_annot_page.GetPageNo());
                if (page != null && m_polygon.GetNodeCount() > 2) {
                    page.ObjsStart();
                    Matrix mat = m_annot_page.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                    mat.TransformPath(m_polygon);
                    page.AddAnnotPolygon(m_polygon, Global.g_line_annot_color, Global.g_line_annot_fill_color, m_annot_page.ToPDFSize(Global.g_line_annot_width));
                    mat.Destroy();
                    int aidx = page.GetAnnotCount() - 1;
                    //add to redo/undo stack.
                    m_opstack.push(new OPAdd(m_annot_page.GetPageNo(), page, aidx));
                    Page.Annotation annot = page.GetAnnot(page.GetAnnotCount() - 1);
                    m_layout.gl_render(m_annot_page, annot.GetRect());
                    requestRender();
                    page.Close();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                }
            }
            if (m_polygon != null) m_polygon.Destroy();
            m_polygon = null;
            m_annot_page = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_polygon.Destroy();
            m_polygon = null;
            m_annot_page = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetPolyline(int code) {
        if (code == 0)//start
        {
            m_status = STA_POLYLINE;
            m_polygon = new Path();
        } else if (code == 1)//end
        {
            m_status = STA_NONE;
            if (m_annot_page != null) {
                Page page = m_doc.GetPage(m_annot_page.GetPageNo());
                if (page != null && m_polygon.GetNodeCount() > 1) {
                    page.ObjsStart();
                    Matrix mat = m_annot_page.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                    mat.TransformPath(m_polygon);
                    page.AddAnnotPolyline(m_polygon, 0, 0, Global.g_line_annot_color, Global.g_line_annot_fill_color, m_annot_page.ToPDFSize(Global.g_line_annot_width));
                    mat.Destroy();
                    int aidx = page.GetAnnotCount() - 1;
                    //add to redo/undo stack.
                    m_opstack.push(new OPAdd(m_annot_page.GetPageNo(), page, aidx));
                    Page.Annotation annot = page.GetAnnot(aidx);
                    m_layout.gl_render(m_annot_page, annot.GetRect());
                    requestRender();
                    page.Close();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                }
            }
            if (m_polygon != null) m_polygon.Destroy();
            m_polygon = null;
            m_annot_page = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_polygon.Destroy();
            m_polygon = null;
            m_annot_page = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetRect(int code) {
        if (code == 0)//start
        {
            m_status = STA_RECT;
        } else if (code == 1)//end
        {
            if (m_rects != null) {
                int len = m_rects.length;
                int cur;
                PDFGLPageSet pset = new PDFGLPageSet(len);
                for (cur = 0; cur < len; cur += 4) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) m_rects[cur], (int) m_rects[cur + 1]);
                    PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    if (page != null) {
                        page.ObjsStart();
                        Matrix mat = vpage.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                        float[] rect = new float[4];
                        if (m_rects[cur] > m_rects[cur + 2]) {
                            rect[0] = m_rects[cur + 2];
                            rect[2] = m_rects[cur];
                        } else {
                            rect[0] = m_rects[cur];
                            rect[2] = m_rects[cur + 2];
                        }
                        if (m_rects[cur + 1] > m_rects[cur + 3]) {
                            rect[1] = m_rects[cur + 3];
                            rect[3] = m_rects[cur + 1];
                        } else {
                            rect[1] = m_rects[cur + 1];
                            rect[3] = m_rects[cur + 3];
                        }
                        mat.TransformRect(rect);
                        page.AddAnnotRect(rect, vpage.ToPDFSize(Global.g_rect_annot_width), Global.g_rect_annot_color, Global.g_rect_annot_fill_color);
                        mat.Destroy();
                        //add to redo/undo stack.
                        m_opstack.push(new OPAdd(pos.pageno, page, page.GetAnnotCount() - 1));
                        pset.Insert(vpage);
                        page.Close();
                    }
                }
                for (cur = 0; cur < pset.pages_cnt; cur++) {
                    PDFGLPage vpage = pset.pages[cur];
                    m_layout.gl_render(vpage);
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
            }
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetEllipse(int code) {
        if (code == 0)//start
        {
            m_status = STA_ELLIPSE;
        } else if (code == 1)//end
        {
            if (m_rects != null) {
                int len = m_rects.length;
                int cur;
                PDFGLPageSet pset = new PDFGLPageSet(len);
                for (cur = 0; cur < len; cur += 4) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) m_rects[cur], (int) m_rects[cur + 1]);
                    PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    if (page != null) {
                        page.ObjsStart();
                        Matrix mat = vpage.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                        float[] rect = new float[4];
                        if (m_rects[cur] > m_rects[cur + 2]) {
                            rect[0] = m_rects[cur + 2];
                            rect[2] = m_rects[cur];
                        } else {
                            rect[0] = m_rects[cur];
                            rect[2] = m_rects[cur + 2];
                        }
                        if (m_rects[cur + 1] > m_rects[cur + 3]) {
                            rect[1] = m_rects[cur + 3];
                            rect[3] = m_rects[cur + 1];
                        } else {
                            rect[1] = m_rects[cur + 1];
                            rect[3] = m_rects[cur + 3];
                        }
                        mat.TransformRect(rect);
                        page.AddAnnotEllipse(rect, vpage.ToPDFSize(Global.g_oval_annot_width), Global.g_oval_annot_color, Global.g_oval_annot_fill_color);
                        mat.Destroy();
                        //add to redo/undo stack.
                        m_opstack.push(new OPAdd(pos.pageno, page, page.GetAnnotCount() - 1));
                        page.Close();
                        pset.Insert(vpage);
                    }
                }
                for (cur = 0; cur < pset.pages_cnt; cur++) {
                    PDFGLPage vpage = pset.pages[cur];
                    m_layout.gl_render(vpage);
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
            }
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetSelect() {
        if (m_status == STA_SELECT) {
            m_sel_icon1.recycle();
            m_sel_icon2.recycle();
            m_sel_icon1 = null;
            m_sel_icon2 = null;
            m_annot_page = null;
            m_status = STA_NONE;
            if (m_canvas != null) m_canvas.invalidate();
            if (m_listener != null)
                m_listener.OnPDFSelectEnd();
        } else {
            m_sel_icon1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.pt_start);
            m_sel_icon2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.pt_end);
            m_annot_page = null;
            m_status = STA_SELECT;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetNote(int code) {
        if (code == 0) {
            m_note_pages = null;
            m_note_indecs = null;
            m_status = STA_NOTE;
        } else if (code == 1)//end
        {
            if (m_listener != null && m_note_pages != null) {
                int cur = 0;
                int cnt = m_note_pages.length;
                while (cur < cnt) {
                    m_listener.OnPDFPageModified(m_note_pages[cur].GetPageNo());
                    cur++;
                }
            }
            m_note_pages = null;
            m_note_indecs = null;
            m_status = STA_NONE;
        } else//cancel
        {
            if (m_note_pages != null)//remove added note.
            {
                int cur = 0;
                int cnt = m_note_pages.length;
                while (cur < cnt) {
                    PDFGLPage vpage = m_note_pages[cur];
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    page.ObjsStart();
                    int index = m_note_indecs[cur];
                    Page.Annotation annot;
                    while ((annot = page.GetAnnot(index)) != null) {
                        annot.RemoveFromPage();
                        m_opstack.undo();
                    }
                    page.Close();
                    m_layout.gl_render(vpage);
                    requestRender();
                    cur++;
                }
                m_note_pages = null;
                m_note_indecs = null;
                if (m_canvas != null) m_canvas.invalidate();
            }
            m_status = STA_NONE;
        }
    }

    public void PDFSetLine(int code) {
        if (code == 0)//start
        {
            m_status = STA_LINE;
        } else if (code == 1)//end
        {
            if (m_rects != null) {
                int len = m_rects.length;
                int cur;
                float[] pt1 = new float[2];
                float[] pt2 = new float[2];
                PDFGLPageSet pset = new PDFGLPageSet(len);
                for (cur = 0; cur < len; cur += 4) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) m_rects[cur], (int) m_rects[cur + 1]);
                    PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                    pt1[0] = m_rects[cur];
                    pt1[1] = m_rects[cur + 1];
                    pt2[0] = m_rects[cur + 2];
                    pt2[1] = m_rects[cur + 3];
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    if (page != null) {
                        page.ObjsStart();
                        Matrix mat = vpage.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                        mat.TransformPoint(pt1);
                        mat.TransformPoint(pt2);
                        page.AddAnnotLine(pt1, pt2, Global.g_line_annot_style1, Global.g_line_annot_style2, vpage.ToPDFSize(Global.g_line_annot_width), Global.g_line_annot_color, Global.g_line_annot_fill_color);
                        mat.Destroy();
                        //add to redo/undo stack.
                        m_opstack.push(new OPAdd(pos.pageno, page, page.GetAnnotCount() - 1));
                        page.Close();
                        pset.Insert(vpage);
                    }
                }
                for (cur = 0; cur < pset.pages_cnt; cur++) {
                    PDFGLPage vpage = pset.pages[cur];
                    m_layout.gl_render(vpage);
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
            }
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetStamp(int code) {
        if (code == 0)//start
        {
            m_status = STA_STAMP;
            if (m_icon == null) {
                m_icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.pdf_custom_stamp);
                if (m_dicon == null) {
                    if (m_icon != null) {
                        m_dicon = m_doc.NewImage(m_icon, 0, true);
                    }
                }
            }
        } else if (code == 1)//end
        {
            if (m_rects != null) {
                int len = m_rects.length;
                int cur;
                PDFGLPageSet pset = new PDFGLPageSet(len);
                for (cur = 0; cur < len; cur += 4) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) m_rects[cur], (int) m_rects[cur + 1]);
                    PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    if (page != null) {
                        Matrix mat = vpage.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                        float[] rect = new float[4];
                        if (m_rects[cur] > m_rects[cur + 2]) {
                            rect[0] = m_rects[cur + 2];
                            rect[2] = m_rects[cur];
                        } else {
                            rect[0] = m_rects[cur];
                            rect[2] = m_rects[cur + 2];
                        }
                        if (m_rects[cur + 1] > m_rects[cur + 3]) {
                            rect[1] = m_rects[cur + 3];
                            rect[3] = m_rects[cur + 1];
                        } else {
                            rect[1] = m_rects[cur + 1];
                            rect[3] = m_rects[cur + 3];
                        }
                        mat.TransformRect(rect);
                        page.ObjsStart();
                        page.AddAnnotBitmap(m_dicon, rect);
                        mat.Destroy();
                        //add to redo/undo stack.
                        m_opstack.push(new OPAdd(pos.pageno, page, page.GetAnnotCount() - 1));
                        page.Close();
                        pset.Insert(vpage);
                    }
                }
                for (cur = 0; cur < pset.pages_cnt; cur++) {
                    PDFGLPage vpage = pset.pages[cur];
                    m_layout.gl_render(vpage);
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
            }
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
            if (m_icon != null)
                m_icon.recycle();
            m_icon = null;
        } else//cancel
        {
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
            if (m_icon != null)
                m_icon.recycle();
            m_icon = null;
        }
    }

    public void PDFSetEditbox(int code) {
        if (code == 0)//start
        {
            m_status = STA_EDITBOX;
        } else if (code == 1)//end
        {
            if (m_rects != null) {
                int len = m_rects.length;
                int cur;
                PDFGLPageSet pset = new PDFGLPageSet(len);
                for (cur = 0; cur < len; cur += 4) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) m_rects[cur], (int) m_rects[cur + 1]);
                    PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    if (page != null) {
                        page.ObjsStart();
                        Matrix mat = vpage.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                        float[] rect = new float[4];
                        if (m_rects[cur] > m_rects[cur + 2]) {
                            rect[0] = m_rects[cur + 2];
                            rect[2] = m_rects[cur];
                        } else {
                            rect[0] = m_rects[cur];
                            rect[2] = m_rects[cur + 2];
                        }
                        if (m_rects[cur + 1] > m_rects[cur + 3]) {
                            rect[1] = m_rects[cur + 3];
                            rect[3] = m_rects[cur + 1];
                        } else {
                            rect[1] = m_rects[cur + 1];
                            rect[3] = m_rects[cur + 3];
                        }
                        mat.TransformRect(rect);
                        if (rect[2] - rect[0] < 80) rect[2] = rect[0] + 80;
                        if (rect[3] - rect[1] < 16) rect[1] = rect[3] - 16;
                        page.AddAnnotEditbox(rect, 0xFFFF0000, vpage.ToPDFSize(3), 0, 12, 0xFFFF0000);
                        mat.Destroy();
                        //add to redo/undo stack.
                        m_opstack.push(new OPAdd(pos.pageno, page, page.GetAnnotCount() - 1));
                        pset.Insert(vpage);
                        page.Close();
                    }
                }
                for (cur = 0; cur < pset.pages_cnt; cur++) {
                    PDFGLPage vpage = pset.pages[cur];
                    m_layout.gl_render(vpage);
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
            }
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetFieldSign(int code) {
        if (code == 0)//start
        {
            m_status = STA_FIELD_SIGNATURE;
        } else if (code == 1)//end
        {
            if (m_rects != null) {
                int len = m_rects.length;
                int cur;
                PDFGLPageSet pset = new PDFGLPageSet(len);
                for (cur = 0; cur < len; cur += 4) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) m_rects[cur], (int) m_rects[cur + 1]);
                    PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    if (page != null) {
                        page.ObjsStart();
                        Matrix mat = vpage.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                        float[] rect = new float[4];
                        if (m_rects[cur] > m_rects[cur + 2]) {
                            rect[0] = m_rects[cur + 2];
                            rect[2] = m_rects[cur];
                        } else {
                            rect[0] = m_rects[cur];
                            rect[2] = m_rects[cur + 2];
                        }
                        if (m_rects[cur + 1] > m_rects[cur + 3]) {
                            rect[1] = m_rects[cur + 3];
                            rect[3] = m_rects[cur + 1];
                        } else {
                            rect[1] = m_rects[cur + 1];
                            rect[3] = m_rects[cur + 3];
                        }
                        mat.TransformRect(rect);
                        if (rect[2] - rect[0] < 100) rect[2] = rect[0] + 100;
                        if (rect[3] - rect[1] < 50) rect[1] = rect[3] - 50;
                        int isign = 0;
                        String sname = String.format("rdsign%03d", isign++);
                        while (!page.AddFieldSign(rect, sname) && isign < 1000)
                            sname = String.format("rdsign%03d", isign++);

                        mat.Destroy();
                        //add to redo/undo stack.
                        m_opstack.push(new OPAdd(pos.pageno, page, page.GetAnnotCount() - 1));
                        pset.Insert(vpage);
                        page.Close();
                    }
                }
                for (cur = 0; cur < pset.pages_cnt; cur++) {
                    PDFGLPage vpage = pset.pages[cur];
                    m_layout.gl_render(vpage);
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
            }
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetFieldEditbox(int code) {
        if (code == 0)//start
        {
            m_status = STA_FIELD_EDITBOX;
        } else if (code == 1)//end
        {
            if (m_rects != null) {
                int len = m_rects.length;
                int cur;
                PDFGLView.PDFGLPageSet pset = new PDFGLPageSet(len);
                for (cur = 0; cur < len; cur += 4) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) m_rects[cur], (int) m_rects[cur + 1]);
                    PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    if (page != null) {
                        page.ObjsStart();
                        Matrix mat = vpage.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                        float[] rect = new float[4];
                        if (m_rects[cur] > m_rects[cur + 2]) {
                            rect[0] = m_rects[cur + 2];
                            rect[2] = m_rects[cur];
                        } else {
                            rect[0] = m_rects[cur];
                            rect[2] = m_rects[cur + 2];
                        }
                        if (m_rects[cur + 1] > m_rects[cur + 3]) {
                            rect[1] = m_rects[cur + 3];
                            rect[3] = m_rects[cur + 1];
                        } else {
                            rect[1] = m_rects[cur + 1];
                            rect[3] = m_rects[cur + 3];
                        }
                        mat.TransformRect(rect);
                        if (rect[2] - rect[0] < 80) rect[2] = rect[0] + 80;
                        if (rect[3] - rect[1] < 16) rect[1] = rect[3] - 16;
                        int isign = 0;
                        String sname = String.format("rdedit%03d", isign++);
                        while (!page.AddFieldEditbox(rect, sname, false, false) && isign < 1000)
                            sname = String.format("rdedit%03d", isign++);

                        mat.Destroy();
                        //add to redo/undo stack.
                        m_opstack.push(new OPAdd(pos.pageno, page, page.GetAnnotCount() - 1));
                        pset.Insert(vpage);
                        page.Close();
                    }
                }
                for (cur = 0; cur < pset.pages_cnt; cur++) {
                    PDFGLPage vpage = pset.pages[cur];
                    m_layout.gl_render(vpage);
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
            }
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFSetFieldCheckbox(int code) {
        if (code == 0)//start
        {
            m_status = STA_FIELD_CHECK;
        } else if (code == 1)//end
        {
            if (m_rects != null) {
                int len = m_rects.length;
                int cur;
                PDFGLView.PDFGLPageSet pset = new PDFGLPageSet(len);
                for (cur = 0; cur < len; cur += 4) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) m_rects[cur], (int) m_rects[cur + 1]);
                    PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    if (page != null) {
                        page.ObjsStart();
                        Matrix mat = vpage.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                        float[] rect = new float[4];
                        if (m_rects[cur] > m_rects[cur + 2]) {
                            rect[0] = m_rects[cur + 2];
                            rect[2] = m_rects[cur];
                        } else {
                            rect[0] = m_rects[cur];
                            rect[2] = m_rects[cur + 2];
                        }
                        if (m_rects[cur + 1] > m_rects[cur + 3]) {
                            rect[1] = m_rects[cur + 3];
                            rect[3] = m_rects[cur + 1];
                        } else {
                            rect[1] = m_rects[cur + 1];
                            rect[3] = m_rects[cur + 3];
                        }
                        mat.TransformRect(rect);
                        if (rect[2] - rect[0] < 20) rect[2] = rect[0] + 20;
                        if (rect[3] - rect[1] < 20) rect[1] = rect[3] - 20;
                        int isign = 0;
                        String sname = String.format("rdchk%03d", isign++);
                        while (!page.AddFieldCheck(rect, sname, sname, null, null) && isign < 1000)
                            sname = String.format("rdchk%03d", isign++);

                        mat.Destroy();
                        //add to redo/undo stack.
                        m_opstack.push(new OPAdd(pos.pageno, page, page.GetAnnotCount() - 1));
                        pset.Insert(vpage);
                        page.Close();
                    }
                }
                for (cur = 0; cur < pset.pages_cnt; cur++) {
                    PDFGLPage vpage = pset.pages[cur];
                    m_layout.gl_render(vpage);
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
            }
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    private int m_radio_nidx = 0;
    private int m_radio_val = 0;

    public void PDFSetFieldRadio(int code) {
        if (code == 0)//start
        {
            m_status = STA_FIELD_RADIO;
        } else if (code == 1)//end
        {
            if (m_rects != null) {
                int len = m_rects.length;
                int cur;
                PDFGLView.PDFGLPageSet pset = new PDFGLPageSet(len);
                String sname = String.format("rdrad%03d", m_radio_nidx);
                for (cur = 0; cur < len; cur += 4) {
                    PDFGLLayout.PDFPos pos = m_layout.vGetPos((int) m_rects[cur], (int) m_rects[cur + 1]);
                    PDFGLPage vpage = m_layout.vGetPage(pos.pageno);
                    Page page = m_doc.GetPage(vpage.GetPageNo());
                    if (page != null) {
                        page.ObjsStart();
                        Matrix mat = vpage.CreateInvertMatrix(m_layout.vGetX(), m_layout.vGetY());
                        float[] rect = new float[4];
                        if (m_rects[cur] > m_rects[cur + 2]) {
                            rect[0] = m_rects[cur + 2];
                            rect[2] = m_rects[cur];
                        } else {
                            rect[0] = m_rects[cur];
                            rect[2] = m_rects[cur + 2];
                        }
                        if (m_rects[cur + 1] > m_rects[cur + 3]) {
                            rect[1] = m_rects[cur + 3];
                            rect[3] = m_rects[cur + 1];
                        } else {
                            rect[1] = m_rects[cur + 1];
                            rect[3] = m_rects[cur + 3];
                        }
                        mat.TransformRect(rect);
                        if (rect[2] - rect[0] < 20) rect[2] = rect[0] + 20;
                        if (rect[3] - rect[1] < 20) rect[1] = rect[3] - 20;
                        String sval = String.format("rad%03d", m_radio_val++);
                        page.AddFieldRadio(rect, sname, sval, null, null);

                        mat.Destroy();
                        //add to redo/undo stack.
                        m_opstack.push(new OPAdd(pos.pageno, page, page.GetAnnotCount() - 1));
                        pset.Insert(vpage);
                        page.Close();
                    }
                }
                for (cur = 0; cur < pset.pages_cnt; cur++) {
                    PDFGLPage vpage = pset.pages[cur];
                    m_layout.gl_render(vpage);
                    requestRender();
                    if (m_listener != null)
                        m_listener.OnPDFPageModified(vpage.GetPageNo());
                }
            }
            m_radio_nidx++;///increase group number.
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        } else//cancel
        {
            m_status = STA_NONE;
            m_rects = null;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public void PDFCancelAnnot() {
        if (m_status == STA_NOTE) PDFSetNote(2);
        if (m_status == STA_RECT) PDFSetRect(2);
        if (m_status == STA_INK) PDFSetInk(2);
        if (m_status == STA_LINE) PDFSetLine(2);
        if (m_status == STA_STAMP) PDFSetStamp(2);
        if (m_status == STA_ELLIPSE) PDFSetEllipse(2);
        if (m_status == STA_EDITBOX) PDFSetEditbox(2);
        if (m_status == STA_POLYGON) PDFSetPolygon(2);
        if (m_status == STA_POLYLINE) PDFSetPolyline(2);
        if (m_status == STA_FIELD_EDITBOX) PDFSetFieldEditbox(2);
        if (m_status == STA_FIELD_SIGNATURE) PDFSetFieldSign(2);
        if (m_status == STA_FIELD_CHECK) PDFSetFieldCheckbox(2);
        if (m_status == STA_FIELD_RADIO) PDFSetFieldRadio(2);
        if (m_status == STA_ANNOT) PDFEndAnnot();
        if (m_canvas != null) m_canvas.invalidate();
    }

    public void PDFRemoveAnnot() {
        if (m_status != STA_ANNOT || !m_doc.CanSave()) return;
        if ((Global.g_annot_readonly && m_annot.IsReadOnly()) || (Global.g_annot_lock && m_annot.IsLocked()))
        {
            Toast.makeText(getContext(), R.string.cannot_write_or_encrypted, Toast.LENGTH_SHORT).show();
            PDFEndAnnot();
            return;
        }
        //add to redo/undo stack.
        Page page = m_doc.GetPage(m_annot_page.GetPageNo());
        page.ObjsStart();
        m_opstack.push(new OPDel(m_annot_page.GetPageNo(), page, m_annot.GetIndexInPage()));
        page.Close();

        float[] rect = m_annot.GetRect();
        m_annot.RemoveFromPage();
        m_annot = null;
        m_layout.gl_render(m_annot_page, rect);
        requestRender();
        if (m_listener != null)
            m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
        PDFEndAnnot();
    }

    public void PDFEndAnnot() {
        if (m_status != STA_ANNOT) return;
        if (m_aMenu != null) m_aMenu.hide();

        if (m_annot_pg != null) {
            m_annot_pg.Close();
            m_annot_pg = null;
        }
        m_annot_page = null;
        m_annot_pos = null;
        m_annot = null;
        if (m_canvas != null) m_canvas.invalidate();
        m_status = STA_NONE;
        try {
            if (m_pEdit != null && m_pEdit.isShowing()) m_pEdit.dismiss();
            if (m_pCombo != null && m_pCombo.isShowing()) m_pCombo.dismiss();
        } catch (Exception ignored) {

        }
        if (m_listener != null)
            m_listener.OnPDFAnnotTapped(-1, null);
    }

    public void PDFEditAnnot() {
        if (m_status != STA_ANNOT) return;
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.dlg_note, null);
        final EditText subj = (EditText) layout.findViewById(R.id.txt_subj);
        final EditText content = (EditText) layout.findViewById(R.id.txt_content);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String str_subj = subj.getText().toString();
                String str_content = content.getText().toString();
                m_annot.SetPopupSubject(str_subj);
                m_annot.SetPopupText(str_content);
                dialog.dismiss();
                if (m_listener != null)
                    m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
                PDFEndAnnot();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                PDFEndAnnot();
            }
        });
        builder.setTitle("Note Content");
        builder.setCancelable(false);
        builder.setView(layout);

        subj.setText(m_annot.GetPopupSubject());
        content.setText(m_annot.GetPopupText());
        AlertDialog dlg = builder.create();
        dlg.show();
    }

    public void PDFPerformAnnot() {
        if (m_status != STA_ANNOT) return;
        Page page = m_doc.GetPage(m_annot_page.GetPageNo());
        if (page == null || m_annot == null) return;
        page.ObjsStart();
        int dest = m_annot.GetDest();
        if (dest >= 0) {
            m_layout.vGotoPage(dest);
            requestRender();
            if (m_canvas != null) m_canvas.invalidate();
        }
        String js = m_annot.GetJS();
        if (m_listener != null && js != null)
            m_listener.OnPDFOpenJS(js);
        String uri = m_annot.GetURI();
        if (m_listener != null && uri != null)
            m_listener.OnPDFOpenURI(uri);
        int index;
        String mov = m_annot.GetMovie();
        if (mov != null) {
            index = mov.lastIndexOf('\\');
            if (index < 0) index = mov.lastIndexOf('/');
            if (index < 0) index = mov.lastIndexOf(':');
            String save_file = Global.tmp_path + "/" + mov.substring(index + 1);
            m_annot.GetMovieData(save_file);
            if (m_listener != null)
                m_listener.OnPDFOpenMovie(save_file);
        }
        String snd = m_annot.GetSound();
        if (snd != null) {
            int[] paras = new int[4];
            index = snd.lastIndexOf('\\');
            if (index < 0) index = snd.lastIndexOf('/');
            if (index < 0) index = snd.lastIndexOf(':');
            String save_file = Global.tmp_path + "/" + snd.substring(index + 1);
            m_annot.GetSoundData(paras, save_file);
            if (m_listener != null)
                m_listener.OnPDFOpenSound(paras, save_file);
        }
        String att = m_annot.GetAttachment();
        if (att != null) {
            index = att.lastIndexOf('\\');
            if (index < 0) index = att.lastIndexOf('/');
            if (index < 0) index = att.lastIndexOf(':');
            String save_file = Global.tmp_path + "/" + att.substring(index + 1);
            m_annot.GetAttachmentData(save_file);
            if (m_listener != null)
                m_listener.OnPDFOpenAttachment(save_file);
        }
        String rend = m_annot.GetRendition();
        if (rend != null) {
            index = rend.lastIndexOf('\\');
            if (index < 0) index = rend.lastIndexOf('/');
            if (index < 0) index = rend.lastIndexOf(':');
            String save_file = Global.tmp_path + "/" + rend.substring(index + 1);
            m_annot.GetRenditionData(save_file);
            if (m_listener != null)
                m_listener.OnPDFOpenRendition(save_file);
        }

        String f3d = m_annot.Get3D();
        if (f3d != null) {
            index = f3d.lastIndexOf('\\');
            if (index < 0) index = f3d.lastIndexOf('/');
            if (index < 0) index = f3d.lastIndexOf(':');
            String save_file = Global.tmp_path + "/" + f3d.substring(index + 1);
            m_annot.Get3DData(save_file);
            if (m_listener != null)
                m_listener.OnPDFOpen3D(save_file);
        }

        boolean reset = m_annot.GetReset();
        if (reset && m_doc.CanSave()) {
            m_annot.SetReset();
            m_layout.gl_render(m_annot_page);
            requestRender();
            if (m_listener != null)
                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
        }
        String tar = m_annot.GetSubmitTarget();
        if (tar != null) {
            if (m_listener != null)
                m_listener.OnPDFOpenURI(tar + "?" + m_annot.GetSubmitTarget());
        }
        page.Close();
        PDFEndAnnot();
    }

    public final void PDFFindStart(String key, boolean match_case, boolean whole_word) {
        m_layout.vFindStart(key, match_case, whole_word);
    }

    public final void PDFFind(int dir) {
        m_layout.vFind(dir);
        requestRender();
    }

    public final void PDFFindEnd() {
        m_layout.vFindEnd();
    }

    public boolean PDFSetSelMarkup(int type) {
        if (m_status == STA_SELECT && m_sel != null && m_sel.SetSelMarkup(type)) {
            //add to redo/undo stack.
            Page page = m_sel.GetPage();
            int aidx = page.GetAnnotCount() - 1;
            Page.Annotation annot = page.GetAnnot(aidx);
            m_opstack.push(new OPAdd(m_annot_page.GetPageNo(), page, aidx));
            m_layout.gl_render(m_annot_page, annot.GetRect());
            requestRender();
            if (m_canvas != null) m_canvas.invalidate();
            if (m_listener != null)
                m_listener.OnPDFPageModified(m_annot_page.GetPageNo());
            return true;
        } else {
            return false;
        }
    }

    public Document PDFGetDoc() {
        return m_doc;
    }

    private PDFGLLayout.PDFPos m_goto_pos = null;

    public void BundleSavePos(Bundle bundle) {
        if (m_layout != null) {
            PDFGLLayout.PDFPos pos = m_layout.vGetPos(0, 0);
            bundle.putInt("view_page", pos.pageno);
            bundle.putFloat("view_x", pos.x);
            bundle.putFloat("view_y", pos.y);
        }
    }

    public void BundleRestorePos(Bundle bundle) {
        if (m_layout != null) {
            final PDFGLLayout.PDFPos pos = new PDFGLLayout.PDFPos();
            pos.pageno = bundle.getInt("view_page");
            pos.x = bundle.getFloat("view_x");
            pos.y = bundle.getFloat("view_y");
            if (m_w <= 0 || m_h <= 0) {
                m_goto_pos = pos;
            } else {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        m_layout.vSetPos(0, 0, pos);
                    }
                });
            }
        }
    }

    public void PDFGotoPage(int pageno) {
        if (m_layout == null) return;
        if (m_w <= 0 || m_h <= 0) {
            PDFGLLayout.PDFPos pos = new PDFGLLayout.PDFPos();
            pos.pageno = pageno;
            pos.x = 0;
            pos.y = m_doc.GetPageHeight(pageno) + 1;
            m_goto_pos = pos;
        } else {
            final int pgno = pageno;
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    m_layout.vGotoPage(pgno);
                    //thinking that gl_sync may cause UI block, so, synchronized jumping are not recommended.
                    //m_layout.gl_sync();//render all pages in screen area in synchronized way.
                    requestRender();
                }
            });
        }
        m_canvas.postInvalidate();
    }

    public void PDFScrolltoPage(int pageno) {
        if (m_layout == null) return;
        if (m_w <= 0 || m_h <= 0) {
            PDFGLLayout.PDFPos pos = new PDFGLLayout.PDFPos();
            pos.pageno = pageno;
            pos.x = 0;
            pos.y = m_doc.GetPageHeight(pageno) + 1;
            m_goto_pos = pos;
        } else
            m_layout.vScrolltoPage(pageno);
        requestRender();
        m_canvas.postInvalidate();
    }

    public boolean PDFCanUndo() {
        return m_opstack.can_undo();
    }

    public boolean PDFCanRedo() {
        return m_opstack.can_redo();
    }

    public void PDFUndo() {
        //if(m_opstack.can_undo()) return;
        OPItem item = m_opstack.undo();
        if (item != null) {
            item.op_undo(m_doc);
            PDFGotoPage(item.m_pageno);
            m_layout.gl_render(m_layout.vGetPage(item.m_pageno));
            requestRender();
        } else
            Toast.makeText(getContext(), "No more undo.", Toast.LENGTH_SHORT).show();
    }

    public void PDFRedo() {
        //if(m_opstack.can_redo()) return;
        OPItem item = m_opstack.redo();
        if (item != null) {
            item.op_redo(m_doc);
            PDFGotoPage(item.m_pageno);
            m_layout.gl_render(m_layout.vGetPage(item.m_pageno));
            requestRender();
        } else
            Toast.makeText(getContext(), "No more redo.", Toast.LENGTH_SHORT).show();
    }

    public boolean PDFCanSave() {
        if (m_layout != null) return m_layout.vCanSave();
        return false;
    }

    public boolean PDFSave() {
        return m_doc.Save();
    }

    public void PDFSetBGColor(int color) {
        m_back_color = color;
        if (m_gl10 != null)
            requestRender();
    }

    public void PDFUpdatePage(int pageno) {
        if (m_layout != null) {
            PDFGLPage page = m_layout.vGetPage(pageno);
            if (page != null) m_layout.gl_render(page);
            requestRender();
        }
    }

    private int mFTSPage;
    private Paint mFTSPaint;
    private float[] mFTSRect;

    private void onDrawFTS(Canvas canvas) {
        if (mFTSRect == null)
            return;
        PDFGLPage vpage;
        if (m_layout.getClass().equals(PDFGLLayoutDual.class)) {
            //Dual mode
            if ((mFTSPage & 1) == 1 && mFTSPage != m_cur_pageno) {
                //Right page in dual mode
                vpage = m_layout.vGetPage(m_cur_pageno + 1);
            } else
                vpage = m_layout.vGetPage(m_cur_pageno);
        } else {
            vpage = m_layout.vGetPage(m_cur_pageno);
        }
        if (vpage == null)
            return;
        if (vpage.GetPageNo() == mFTSPage) {
            float[] mSearchDrawRect = new float[4];
            mSearchDrawRect[0] = vpage.GetVX(mFTSRect[0]) - m_layout.vGetX();
            mSearchDrawRect[1] = vpage.GetVY(mFTSRect[3]) - m_layout.vGetY();
            mSearchDrawRect[2] = vpage.GetVX(mFTSRect[2]) - m_layout.vGetX();
            mSearchDrawRect[3] = vpage.GetVY(mFTSRect[1]) - m_layout.vGetY();
            canvas.drawRect(mSearchDrawRect[0], mSearchDrawRect[1], mSearchDrawRect[2], mSearchDrawRect[3], mFTSPaint);
        }
    }

    public void PDFSetFTSRect(float[] rect, int pageNo) {
        mFTSRect = rect;
        mFTSPage = pageNo;
        if (mFTSRect != null) {
            if (mFTSPaint == null) {
                mFTSPaint = new Paint();
                mFTSPaint.setColor(Global.g_find_primary_color);
                mFTSPaint.setStyle(Paint.Style.FILL);
            }
        }
        if (m_canvas != null) m_canvas.invalidate();
    }

    public void PDFSetScale(float scale) {
        is_waitting = false;
        is_notified = false;
        final int x = getWidth() >> 1;
        final int y = getHeight() >> 1;
        final PDFGLLayout.PDFPos pos = m_layout.vGetPos(x, y);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                m_layout.gl_zoom_start(m_gl10);
                m_layout.gl_zoom_set(scale);
                m_layout.vSetPos(x, y, pos);
                m_layout.gl_zoom_confirm(m_gl10);
                notify_init();
                requestRender();
            }
        });
        wait_init();
    }
}
