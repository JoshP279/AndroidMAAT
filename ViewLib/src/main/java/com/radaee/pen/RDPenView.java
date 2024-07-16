package com.radaee.pen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class RDPenView extends SurfaceView
{
    public interface PenListener
    {
        void OnSurfaceOK();
    }
    private SurfaceHolder m_holder;
    private Panel m_panel;
    private Bitmap m_bmp;
    private Pen m_pen;
    private Pen m_color_pen;
    private Pen m_mark_pen;
    private Pen m_rubber;
    private int m_type;
    private PenDoc m_doc;
    private PenPage m_page;
    private Paint m_paint_rubber;
    private float m_width_rubber;
    private RDBGView.BGRender m_backgraound;
    public RDPenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rd_init();
    }
    private void rd_init()
    {
        SurfaceHolder holder = getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder)
            {
                m_holder = surfaceHolder;
                m_pen = new Pen(1);
                m_color_pen = new Pen(2);
                m_mark_pen = new Pen(3);
                m_rubber = new Pen(100);
                m_pen.SetWidth(dp2px(getContext(), 3));
                m_color_pen.SetWidth(dp2px(getContext(), 40));
                m_mark_pen.SetWidth(dp2px(getContext(), 40));
                m_rubber.SetWidth(dp2px(getContext(), 40));
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height)
            {
                m_holder = surfaceHolder;
                if (m_panel != null)
                {
                    m_panel.Destroy();
                    m_panel = null;
                }
                m_panel = new Panel(width, height);
                m_panel.SetPen(m_pen);
                if (m_bmp != null)
                {
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    bmp.eraseColor(0);
                    Canvas canvas = new Canvas(bmp);
                    canvas.drawBitmap(m_bmp, 0, 0, null);
                    m_bmp.recycle();
                    m_bmp = bmp;
                    m_panel.UpdatePanel(m_bmp);
                }
                else
                {
                    m_bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    m_bmp.eraseColor(0);
                    if (m_page != null)
                    {
                        Bitmap bmp = m_page.LoadPanel();
                        if (bmp != null) {
                            Canvas canvas = new Canvas(m_bmp);
                            canvas.drawBitmap(bmp, 0, 0, null);
                            bmp.recycle();
                            m_panel.UpdatePanel(m_bmp);
                        }
                    }
                }
                m_type = 1;
                if (m_backgraound != null)
                {
                    m_page.m_bg_bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    m_backgraound.onDraw(new Canvas(m_page.m_bg_bmp), width, height);
                }
                if (m_listener != null) m_listener.OnSurfaceOK();
                onRedraw();
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder)
            {
                m_holder = null;
            }
        });
        //setFocusable(true);
        //setKeepScreenOn(true);
        //setFocusableInTouchMode(true);
        //setZOrderOnTop(true);
        m_paint_rubber = new Paint();
        m_paint_rubber.setColor(0xFF000080);
        m_paint_rubber.setStyle(Paint.Style.STROKE);
        m_width_rubber = 2;
        m_paint_rubber.setStrokeWidth(m_width_rubber);
        m_paint_rubber.setAntiAlias(true);
        m_backgraound = null;
    }
    private PenListener m_listener;
    public void SetPenListener(PenListener listener)
    {
        m_listener = listener;
    }
    private static int dp2px(Context context, float dpValue) {
        return (int)(dpValue * context.getResources().getDisplayMetrics().density);
    }

    private void drawPanel(Canvas canvas)
    {
        if (m_page.m_bg_bmp != null)
            canvas.drawBitmap(m_page.m_bg_bmp, 0, 0, null);
        else
            canvas.drawColor(m_page.m_bg_color);
        if (m_bmp != null) canvas.drawBitmap(m_bmp, 0, 0, null);
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        drawPanel(canvas);
    }
    private final int[] m_dirty = new int[4];
    private final Rect m_rect = new Rect();
    private void onRedraw()
    {
        Canvas canvas = m_holder.lockCanvas();
        if (canvas != null)
        {
            drawPanel(canvas);
            m_holder.unlockCanvasAndPost(canvas);
        }
    }
    private void updateDirty(float x, float y)
    {
        if (m_bmp == null || m_panel == null) return;
        m_panel.GetStrokeDirty(m_dirty);
        m_panel.Update(m_bmp);

        //long ltime = SystemClock.elapsedRealtime();
        if(m_type == 100)//rubber
        {
            m_rect.union(m_dirty[0], m_dirty[1], m_dirty[2], m_dirty[3]);
            if(m_pressed)
            {
                float rw = m_rubber.GetWidth();
                float rad = (rw + m_width_rubber) * 0.5f + 1;
                int il = (int)(x - rad);
                int it = (int)(y - rad);
                int ir = (int)(x + rad + 1);
                int ib = (int)(y + rad + 1);
                m_rect.union(il, it, ir, ib);
                Canvas canvas = m_holder.lockCanvas(m_rect);
                if (canvas != null)
                {
                    drawPanel(canvas);
                    canvas.drawCircle(x, y, rw * 0.5f, m_paint_rubber);
                    m_holder.unlockCanvasAndPost(canvas);
                }
                m_rect.set(il, it, ir, ib);
            }
            else
            {
                Canvas canvas = m_holder.lockCanvas(m_rect);
                if (canvas != null)
                {
                    drawPanel(canvas);
                    m_holder.unlockCanvasAndPost(canvas);
                }
                m_rect.set(0, 0, 0, 0);
            }
        }
        else
        {
            m_rect.left = m_dirty[0];
            m_rect.top = m_dirty[1];
            m_rect.right = m_dirty[2];
            m_rect.bottom = m_dirty[3];
            Canvas canvas = m_holder.lockCanvas(m_rect);
            if (canvas != null)
            {
                drawPanel(canvas);
                m_holder.unlockCanvasAndPost(canvas);
            }
            m_rect.set(0, 0, 0, 0);
        }
        //Log.e("UPDATE", "" + (SystemClock.elapsedRealtime() - ltime));
    }

    private boolean m_pressed = false;
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (m_panel == null)
        {
            Log.e("PANEL", "IS NULL.");
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        float p = event.getPressure();
        if(p > 1) p = 1;
        switch(event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                m_pressed = true;
                m_panel.OnTouchDown(x, y, p);
                updateDirty(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (m_pressed) {
                    m_panel.OnTouchMove(x, y, p);
                    updateDirty(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (m_pressed) {
                    m_pressed = false;
                    m_panel.OnTouchEnd();
                    updateDirty(x, y);
                    m_panel.SaveStroke(m_page.m_data);
                    m_panel.EndStroke(m_bmp);
                }
                break;
        }
        return true;
        //return super.onTouchEvent(event);
    }
    public void PENSetPen(int type)
    {
        if(m_type == type) return;
        switch(type)
        {
            case 1:
                m_panel.SetPen(m_pen);
                break;
            case 2:
                m_panel.SetPen(m_color_pen);
                break;
            case 3:
                m_panel.SetPen(m_mark_pen);
                break;
            case 100:
                m_panel.SetPen(m_rubber);
                break;
        }
        m_type = type;
    }
    public int PENGetPenType()
    {
        if(m_panel == null) return 0;
        Pen pen = m_panel.GetPen();
        if (pen == null) return 0;
        return pen.GetType();
    }
    public float PENGetPenWidth(int type)
    {
        switch(type)
        {
            case 1:
                return m_pen.GetWidth();
            case 2:
                return m_color_pen.GetWidth();
            case 3:
                return m_mark_pen.GetWidth();
            case 100:
                return m_rubber.GetWidth();
        }
        return 0;
    }
    public void PENSetPenWidth(int type, float width)
    {
        switch(type)
        {
            case 1:
                m_pen.SetWidth(width);
                break;
            case 2:
                m_color_pen.SetWidth(width);
                break;
            case 3:
                m_mark_pen.SetWidth(width);
                break;
            case 100:
                m_rubber.SetWidth(width);
                break;
        }
    }
    public int PENGetPenColor(int type)
    {
        switch(type)
        {
            case 1:
                return m_pen.GetColor();
            case 2:
                return m_color_pen.GetColor();
            case 3:
                return m_mark_pen.GetColor();
            case 100:
                return m_rubber.GetColor();
        }
        return 0;
    }
    public void PENSetPenColor(int type, int color)
    {
        switch(type)
        {
            case 1:
                m_pen.SetColor(color);
                break;
            case 2:
                m_color_pen.SetColor(color);
                break;
            case 3:
                m_mark_pen.SetColor(color);
                break;
            case 100:
                m_rubber.SetColor(color);
                break;
        }
    }
    public void PENLoad(PenDoc doc)
    {
        m_doc = doc;
        m_page = m_doc.load_page(0);
        m_page.Load();
        if (m_bmp != null && m_page != null)
        {
            Bitmap bmp = m_page.LoadPanel();
            if (bmp != null) {
                Canvas canvas = new Canvas(m_bmp);
                canvas.drawBitmap(bmp, 0, 0, null);
                bmp.recycle();
                m_panel.UpdatePanel(m_bmp);
            }
        }
    }
    public void PENSave()
    {
        if (m_page != null)
            m_page.SaveStroke(m_bmp);//save stroke data.
        if (m_doc != null)
        {
            Bitmap bmp = Bitmap.createBitmap(m_bmp.getWidth() >> 2, m_bmp.getHeight() >> 2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Matrix mat = new Matrix();
            mat.setScale(0.25f, 0.25f);
            canvas.setMatrix(mat);
            drawPanel(canvas);
            m_doc.save_thumb(0, bmp);
            bmp.recycle();
            m_doc.save_info();
        }
    }
    public void PENClose()
    {
        if (m_page != null)
        {
            m_page.Close();
            m_page = null;
        }
        if (m_doc != null)
        {
            m_doc = null;
        }
        if (m_bmp != null)
        {
            m_bmp.recycle();
            m_bmp = null;
        }
        if (m_panel != null)
        {
            m_panel.Destroy();
            m_panel = null;
        }
        if (m_pen != null)
        {
            m_pen.Destroy();
            m_pen = null;
        }
        if (m_color_pen != null)
        {
            m_color_pen.Destroy();
            m_color_pen = null;
        }
        if (m_mark_pen != null)
        {
            m_mark_pen.Destroy();
            m_mark_pen = null;
        }
        if (m_rubber != null)
        {
            m_rubber.Destroy();
            m_rubber = null;
        }
        m_holder = null;
    }
    public boolean PENCanUndo()
    {
        if (m_panel == null) return false;
        return m_panel.CanUndo(m_page.m_data);
    }
    public boolean PENUndo()
    {
        if (m_panel == null) return false;
        boolean ret = m_panel.Undo(m_page.m_data, m_bmp);
        if (ret) onRedraw();
        return ret;
    }
    public boolean PENCanRedo()
    {
        if (m_panel == null) return false;
        return m_panel.CanRedo(m_page.m_data);
    }
    public boolean PENRedo()
    {
        if (m_panel == null) return false;
        boolean ret = m_panel.Redo(m_page.m_data, m_bmp);
        if (ret) onRedraw();
        return ret;
    }
    public void PENSetBGBmp(Bitmap bmp)
    {
        m_page.SetBGBmp(bmp);
        onRedraw();
    }
    public void PENSetBGColor(int color)
    {
        m_page.SetBGColor(color);
        onRedraw();
    }
    public void PENSetBGRender(RDBGView.BGRender render)
    {
        m_backgraound = render;
        m_page.m_bg_bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        m_backgraound.onDraw(new Canvas(m_page.m_bg_bmp), getWidth(), getHeight());
        if (m_holder != null) onRedraw();
    }
}
