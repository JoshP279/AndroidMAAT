package com.radaee.reader;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.radaee.comm.Global;
import com.radaee.ofd.Document;
import com.radaee.ofd.Page;
import com.radaee.view.OFDGLLayout;
import com.radaee.view.OFDGLLayoutCurl;
import com.radaee.view.OFDGLLayoutDual;
import com.radaee.view.OFDGLLayoutHorz;
import com.radaee.view.OFDGLLayoutVert;
import com.radaee.view.OFDGLPage;
import com.radaee.view.OFDSel;
import com.radaee.view.IOFDLayoutView;
import com.radaee.viewlib.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OFDGLView extends GLSurfaceView implements OFDGLCanvas.CanvasListener {
    static final public int STA_NONE = 0;
    static final public int STA_ZOOM = 1;
    static final public int STA_SELECT = 2;
    static final public int STA_INK = 3;
    static final public int STA_RECT = 4;
    static final public int STA_ELLIPSE = 5;
    static final public int STA_NOTE = 6;
    static final public int STA_LINE = 7;
    static final public int STA_STAMP = 8;
    static final public int STA_ANNOT = 100;
    private int m_status = STA_NONE;
    private OFDGLLayout m_layout;
    private GestureDetector m_gesture;
    private IOFDLayoutView.OFDLayoutListener m_listener;
    private OFDGLCanvas m_canvas;
    private int m_w;
    private int m_h;
    private int m_back_color = 0xFFC0C0C0;
    private Bitmap m_sel_icon1 = null;
    private Bitmap m_sel_icon2 = null;
    private OFDGLPage m_annot_page = null;
    private OFDGLLayout.OFDPos m_annot_pos = null;
    private OFDSel m_sel = null;

    class OFDGestureListener extends GestureDetector.SimpleOnGestureListener {
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
                final GLLayout.OFDPos pos = m_layout.vGetPos(x, y);
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
                    m_listener.OnOFDDoubleTapped(x, y);
                    return true;
                }
                */
                //if double tap is enabled for zooming, these 2 lines need to comment.
                if (m_listener == null || !m_listener.OnOFDDoubleTapped(e.getX(), e.getY()))
                    return false;
                return true;
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
            if (m_status != STA_NONE) return;
            m_sel_icon1 = BitmapFactory.decodeResource(OFDGLView.this.getResources(), R.drawable.pt_start);
            m_sel_icon2 = BitmapFactory.decodeResource(OFDGLView.this.getResources(), R.drawable.pt_end);
            m_status = STA_SELECT;
            m_hold_x = e.getX();
            m_hold_y = e.getY();
            if (m_sel != null) {
                m_sel.Clear();
                m_sel = null;
            }
            m_annot_pos = m_layout.vGetPos((int) m_hold_x, (int) m_hold_y);
            m_annot_page = m_layout.vGetPage(m_annot_pos.pageno);
            m_sel = new OFDSel(m_doc.GetPage(m_annot_pos.pageno));
            if (m_canvas != null) m_canvas.invalidate();
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
            if (m_status != STA_NONE && m_status != STA_ANNOT) return false;
            if (m_listener != null) {
                boolean has_link = false;
                OFDGLLayout.OFDPos pos = m_layout.vGetPos((int) e.getX(), (int) e.getY());
                if (pos.pageno >= 0) {
                    Page page = m_doc.GetPage(pos.pageno);
                    String slink = page.GetHyperLink(pos.x, pos.y);
                    if (slink != null) {
                        m_listener.OnOFDOpenURI(slink);
                        has_link = true;
                    }
                }
                if (!has_link) m_listener.OnOFDBlankTapped();
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
    }

    public OFDGLView(Context context) {
        super(context);
        init(context);
    }

    public OFDGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private Document m_doc;
    private int m_page_gap = 4;
    private GL10 m_gl10;
    private int m_cur_pageno = 0;

    private void init(Context context) {
        m_gesture = new GestureDetector(context, new OFDGestureListener());
        //getHolder().setFormat(PixelFormat.RGBA_8888);
        //setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        m_doc = null;
        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
                setRenderMode(RENDERMODE_WHEN_DIRTY);//faster way.
                Looper.prepare();
                gl10.glEnable(GL10.GL_BLEND);
                gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                if (m_layout != null) m_layout.gl_surface_create(gl10);
            }

            @Override
            public void onSurfaceChanged(GL10 gl10, int width, int height) {
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
                m_gl10 = gl10;
                if (m_layout == null) return;

                m_layout.gl_reset(gl10);
                m_layout.gl_resize(m_w, m_h);
                requestRender();
            }

            private boolean darkmode = Global.g_dark_mode;
            @Override
            public void onDrawFrame(GL10 gl10) {
                m_gl10 = gl10;
                if (m_layout == null) return;
                if (darkmode != Global.g_dark_mode)
                {
                    darkmode = Global.g_dark_mode;
                    m_layout.gl_reset(gl10);
                    if (darkmode)
                        gl10.glClearColor(1.0f - ((m_back_color >> 16) & 0xff) / 255.0f, 1.0f - ((m_back_color >> 8) & 0xff) / 255.0f, 1.0f - (m_back_color & 0xff) / 255.0f, ((m_back_color >> 24) & 0xff) / 255.0f);
                    else
                        gl10.glClearColor(((m_back_color >> 16) & 0xff) / 255.0f, ((m_back_color >> 8) & 0xff) / 255.0f, (m_back_color & 0xff) / 255.0f, ((m_back_color >> 24) & 0xff) / 255.0f);
                }
                if (m_goto_pos != null) {
                    m_layout.vSetPos(0, 0, m_goto_pos);
                    m_goto_pos = null;
                }
                else if(m_goto_page >= 0) {
                    m_layout.vGotoPage(m_goto_page);
                    m_goto_page = -1;
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
                    OFDGLView.this.post(new Runnable() {
                        @Override
                        public void run() {
                            m_listener.OnOFDPageChanged(pgno);
                        }
                    });
                }
                if ((Global.debug_mode || m_layout.vHasFind()) && m_canvas != null)
                    m_canvas.postInvalidate();
                if (!m_layout.gl_is_scroll_finished())
                    requestRender();
            }
        });
    }

    /**
     * fired when resume.
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Log.e("GLView", "surfaceCreated");
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
        //Log.e("GLView", "surfaceDestroyed");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (m_gl10 != null && m_layout != null)
                    m_layout.gl_surface_destroy(m_gl10);
            }
        });
        super.surfaceDestroyed(holder);
    }

    public void DOCXClose() {
        is_waitting = false;
        is_notified = false;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (m_layout != null) {
                    OFDGLLayout layout = m_layout;
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

    public void OFDOpen(Document doc, IOFDLayoutView.OFDLayoutListener listener, OFDGLCanvas canvas, int page_gap) {
        m_doc = doc;
        m_listener = listener;
        m_canvas = canvas;
        m_page_gap = (page_gap + 1) & -2;
        OFDSetView(Global.g_view_mode);
    }
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
    public void OFDSetView(final int view_mode) {
        is_notified = false;
        is_waitting = false;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                OFDGLLayout layout;
                OFDGLLayout.OFDPos pos = null;
                if (m_layout != null) pos = m_layout.vGetPos(m_w >> 1, m_h >> 1);
                switch (view_mode) {
                    case 1://horz
                        layout = new OFDGLLayoutHorz(getContext(), Global.g_layout_rtol);
                        break;
                    case 2://curl
                        layout = new OFDGLLayoutCurl(getContext());
                        break;
                    case 3://single
                        layout = new OFDGLLayoutDual(getContext(), OFDGLLayoutDual.ALIGN_CENTER, OFDGLLayoutDual.SCALE_SAME_HEIGHT, Global.g_layout_rtol, null, null);
                        break;
                    case 4:
                    case 5:
                    case 6://dual when landscape
                    {
                        int pcnt = m_doc.GetPageCount();
                        boolean[] bval = new boolean[pcnt];
                        /*
                        //this commented code make first page as single, and others are dual.
                        bval[0] = false;
                        int position = 1;
                        for (int pcur = 1; pcur < pcnt; pcur++) {
                            float pw = m_doc.GetPageWidth(pcur);
                            float ph = m_doc.GetPageHeight(pcur);
                            if (pw > ph) bval[position] = false;
                            else if(pcur < pcnt - 1)
                            {
                                float pw_next = m_doc.GetPageWidth(pcur + 1);
                                float ph_next = m_doc.GetPageHeight(pcur + 1);
                                if (pw_next > ph_next)
                                    bval[position] = false;
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
                            if (pw > ph) bval[position] = false;
                            else if (pcur < pcnt - 1) {
                                float pw_next = m_doc.GetPageWidth(pcur + 1);
                                float ph_next = m_doc.GetPageHeight(pcur + 1);
                                if (pw_next > ph_next)
                                    bval[position] = false;
                                else {
                                    bval[position] = true;
                                    pcur++;
                                }
                            }
                            position++;
                        }
                        layout = new OFDGLLayoutDual(getContext(), OFDGLLayoutDual.ALIGN_CENTER, OFDGLLayoutDual.SCALE_FIT, Global.g_layout_rtol, bval, null);
                    }
                    break;
                    default://vertical.
                        layout = new OFDGLLayoutVert(getContext(), OFDGLLayoutVert.ALIGN_CENTER, false);
                        break;
                }
                layout.vOpen(m_doc, new OFDGLLayout.GLListener() {
                    @Override
                    public void OnBlockRendered(int pageno) {
                        requestRender();
                        if (m_listener != null)
                            m_listener.OnOFDPageRendered(m_layout.vGetPage(pageno));
                    }
                    @Override
                    public void OnFound(boolean found) {
                        requestRender();
                        if (m_listener != null) m_listener.OnOFDSearchFinished(found);
                        if (found) {
                            if (m_canvas != null) invalidate();
                        }
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
    private OFDGLLayout.OFDPos m_zoom_pos;
    private float m_zoom_dis0;
    private float m_zoom_scale;

    private boolean onTouchNone(MotionEvent event) {
        if (m_status != STA_NONE) return false;
        if (m_gesture.onTouchEvent(event)) return true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                m_hold_x = event.getX();
                m_hold_y = event.getY();
                m_hold = true;
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        m_layout.gl_abort_scroll();
                        m_layout.gl_down((int) m_hold_x, (int) m_hold_y);
                        m_hold_docx = m_layout.vGetX();
                        m_hold_docy = m_layout.vGetY();
                    }
                });
                break;
            case MotionEvent.ACTION_MOVE:
                if (m_hold) {
                    final float nx = event.getX();
                    final float ny = event.getY();
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            m_layout.gl_move((int) nx, (int) ny);
                            m_layout.vSetX((int) (m_hold_docx + m_hold_x - nx));
                            m_layout.vSetY((int) (m_hold_docy + m_hold_y - ny));
                            requestRender();
                        }
                    });
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (m_hold) {
                    m_hold = false;
                    final float nx = event.getX();
                    final float ny = event.getY();
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            m_layout.vSetX((int) (m_hold_docx + m_hold_x - nx));
                            m_layout.vSetY((int) (m_hold_docy + m_hold_y - ny));
                            m_layout.gl_move_end();
                            requestRender();
                        }
                    });
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
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            m_zoom_pos = m_layout.vGetPos((int) m_hold_x, (int) m_hold_y);
                            m_layout.gl_zoom_start(m_gl10);
                        }
                    });
                    if (m_listener != null)
                        m_listener.OnOFDZoomStart();
                }
                break;
        }
        return true;
    }

    private boolean onTouchZoom(MotionEvent event) {
        if (m_status != STA_ZOOM) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (m_status == STA_ZOOM && event.getPointerCount() >= 2) {
                    float dx = event.getX(0) - event.getX(1);
                    float dy = event.getY(0) - event.getY(1);
                    final float dis1 = Global.sqrtf(dx * dx + dy * dy);
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            m_layout.gl_zoom_set(m_zoom_scale * dis1 / m_zoom_dis0);
                            m_layout.vSetPos((int) m_hold_x, (int) m_hold_y, m_zoom_pos);
                            requestRender();
                        }
                    });
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (m_status == STA_ZOOM && event.getPointerCount() == 2) {
                    m_status = STA_NONE;
                    m_hold = false;
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            m_layout.gl_zoom_confirm(m_gl10);
                            m_hold_x = -10000;
                            m_hold_y = -10000;
                            requestRender();
                        }
                    });
                    if (m_listener != null)
                        m_listener.OnOFDZoomEnd();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (m_status == STA_ZOOM) {
                    m_status = STA_NONE;
                    m_hold = false;
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            m_layout.gl_zoom_confirm(m_gl10);
                            m_hold_x = -10000;
                            m_hold_y = -10000;
                            requestRender();
                        }
                    });
                    if (m_listener != null)
                        m_listener.OnOFDZoomEnd();
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
                float dist_threshold = 25000f;
                if (dist_start < dist_threshold || dist_end < dist_threshold) {
                    SelectFromStart = dist_start < dist_end;
                } else {
                    m_hold_x = event.getX();
                    m_hold_y = event.getY();
                    SelectFromStart = false;
                    OFDSetSelect();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (m_sel != null && m_annot_pos != null && m_annot_page != null && m_layout != null) {
                    if (!SelectFromStart)
                        m_sel.SetSel(m_annot_pos.x, m_annot_pos.y,
                                m_annot_page.ToOFDX(event.getX(), m_layout.vGetX()),
                                m_annot_page.ToOFDY(event.getY(), m_layout.vGetY()));
                    else {
                        m_annot_pos = m_layout.vGetPos((int) event.getX(), (int) event.getY());
                        m_sel.SetSel(m_annot_pos.x, m_annot_pos.y,
                                m_annot_page.ToOFDX(m_end_x, m_layout.vGetX()),
                                m_annot_page.ToOFDY(m_end_y, m_layout.vGetY())
                        );
                    }
                    /*m_sel.SetSel(m_annot_pos.x, m_annot_pos.y,
                            m_annot_page.ToDOCXX(event.getX(), m_layout.vGetX()),
                            m_annot_page.ToDOCXY(event.getY(), m_layout.vGetY()));*/
                    if (m_canvas != null) m_canvas.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (m_sel != null && m_annot_pos != null && m_annot_page != null && m_layout != null) {
                    if (!SelectFromStart) {
                        m_sel.SetSel(m_annot_pos.x, m_annot_pos.y,
                                m_annot_page.ToOFDX(event.getX(), m_layout.vGetX()),
                                m_annot_page.ToOFDY(event.getY(), m_layout.vGetY()));
                        m_end_x = event.getX();
                        m_end_y = event.getY();
                    } else {
                        m_annot_pos = m_layout.vGetPos((int) event.getX(), (int) event.getY());
                        m_sel.SetSel(m_annot_pos.x, m_annot_pos.y,
                                m_annot_page.ToOFDX(m_end_x, m_layout.vGetX()),
                                m_annot_page.ToOFDY(m_end_y, m_layout.vGetY())
                        );
                        m_hold_x = event.getX();
                        m_hold_y = event.getY();
                    }
                    if (m_canvas != null) m_canvas.invalidate();
                    if (m_listener != null) m_listener.OnOFDTextSelected(m_sel.GetSelString(), event.getX(), event.getY());
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
        return true;
    }

    private void onDrawSelect(Canvas canvas) {
        if (m_status == STA_SELECT && m_sel != null && m_annot_page != null) {
            int orgx = m_annot_page.GetVX(0) - m_layout.vGetX();
            int orgy = m_annot_page.GetVY(0) - m_layout.vGetY();
            float scale = m_annot_page.GetScale();
            float ph = m_doc.GetPageHeight(m_annot_page.GetPageNo());
            m_sel.DrawSel(canvas, scale, ph, orgx, orgy);
            int[] rect1 = m_sel.GetRect1(scale, ph, orgx, orgy);
            int[] rect2 = m_sel.GetRect2(scale, ph, orgx, orgy);
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

    private ActivityManager m_amgr;
    private final ActivityManager.MemoryInfo m_info = new ActivityManager.MemoryInfo();
    private final Paint m_info_paint = new Paint();

    @Override
    public void drawLayer(Canvas canvas) {
        if (m_layout != null) {
            m_layout.vFindDraw(canvas);
            onDrawSelect(canvas);
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


    public void OFDSetSelect() {
        if (m_status == STA_SELECT) {
            m_sel_icon1.recycle();
            m_sel_icon2.recycle();
            m_sel_icon1 = null;
            m_sel_icon2 = null;
            m_annot_page = null;
            m_status = STA_NONE;
            if (m_canvas != null) m_canvas.invalidate();
            if (m_listener != null)
                m_listener.OnOFDSelectEnd();
        } else {
            m_sel_icon1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.pt_start);
            m_sel_icon2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.pt_end);
            m_annot_page = null;
            m_status = STA_SELECT;
            if (m_canvas != null) m_canvas.invalidate();
        }
    }

    public final void OFDFindStart(String key, boolean match_case, boolean whole_word) {
        m_layout.vFindStart(key, match_case, whole_word);
    }

    public final void OFDFind(int dir) {
        m_layout.vFind(dir);
        requestRender();
    }

    public final void OFDFindEnd() {
        m_layout.vFindEnd();
    }

    private OFDGLLayout.OFDPos m_goto_pos = null;
    private int m_goto_page = -1;

    public void BundleSavePos(Bundle bundle) {
        if (m_layout != null) {
            OFDGLLayout.OFDPos pos = m_layout.vGetPos(0, 0);
            bundle.putInt("view_page", pos.pageno);
            bundle.putFloat("view_x", pos.x);
            bundle.putFloat("view_y", pos.y);
        }
    }

    public void BundleRestorePos(Bundle bundle) {
        if (m_layout != null) {
            final OFDGLLayout.OFDPos pos = new OFDGLLayout.OFDPos();
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
                        requestRender();
                    }
                });
            }
        }
    }

    public void OFDGotoPage(int pageno) {
        if (m_layout == null) return;
        m_goto_page = pageno;
        /*
        OFDGLLayout.OFDPos pos = m_layout.new OFDPos();
        pos.pageno = pageno;
        pos.x = 0;
        pos.y = m_doc.GetPageHeight(pageno) + 1;
        m_goto_pos = pos;
         */
    }

    public void OFDScrolltoPage(int pageno) {
        if (m_layout == null) return;
        if (m_w <= 0 || m_h <= 0) {
            OFDGLLayout.OFDPos pos = new OFDGLLayout.OFDPos();
            pos.pageno = pageno;
            pos.x = 0;
            pos.y = 0;
            m_goto_pos = pos;
        } else
            m_layout.vScrolltoPage(pageno);
        requestRender();
        m_canvas.postInvalidate();
    }

    public void OFDSetBGColor(int color) {
        m_back_color = color;
        if (m_gl10 != null)
            requestRender();
    }

    public int OFDGetCurrPage() {
        return m_cur_pageno;
    }

    public void OFDUpdateCurrPage() {
        if (m_layout != null) {
            OFDGLPage page = m_layout.vGetPage(m_cur_pageno);
            if (page != null) m_layout.gl_render(page);
            requestRender();
        }
    }

    public Document OFDGetDoc() {
        return m_doc;
    }
    public void OFDSetScale(float scale)
    {
        is_waitting = false;
        is_notified = false;
        final int x = getWidth() >> 1;
        final int y = getHeight() >> 1;
        //final float z = m_layout.vGetZoom();
        final OFDGLLayout.OFDPos pos = m_layout.vGetPos(x, y);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                m_layout.gl_zoom_start(m_gl10);
                m_layout.gl_zoom_set(scale);
                m_layout.vSetPos(x, y, pos);
                m_layout.gl_zoom_confirm(m_gl10);
                requestRender();
                notify_init();
            }
        });
        wait_init();
    }
}
