package com.radaee.pen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class RDColorPanelView extends View
{
    public interface OnColorChangeListener
    {
        void OnColorChange(int color);
    }
    private Bitmap m_bmp;
    private int m_color;
    private final Paint m_paint = new Paint();
    private final Paint m_paint2 = new Paint();
    private final int m_mdual;
    private final int m_margin;
    private final int m_rad;
    private int m_sel_x;
    private int m_sel_y;
    private OnColorChangeListener m_listener;
    public RDColorPanelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        m_paint2.setStyle(Paint.Style.STROKE);
        m_paint2.setColor(0xFF000000);
        m_paint2.setAntiAlias(true);
        m_paint2.setStrokeWidth(dp2px(context, 1));
        m_color = 0xFFFF0000;
        m_mdual = dp2px(context, 10);
        m_margin = dp2px(context, 5);
        m_rad = dp2px(context, 3);
    }
    private static int dp2px(Context context, float dpValue) {
        return (int)(dpValue * context.getResources().getDisplayMetrics().density);
    }
    public void SetListener(OnColorChangeListener listener)
    {
        m_listener = listener;
    }
    public void SetColor(int color)
    {
        m_color = color;
        int w = getWidth() - m_mdual;
        int h = getHeight() - m_mdual;
        if(w <= 0 || h <= 0) return;
        LinearGradient gradient0 = new LinearGradient(0, 0, w, 0, -1, m_color, Shader.TileMode.CLAMP);
        LinearGradient gradient1 = new LinearGradient(0, 0, 0, h, -1, 0xFF000000, Shader.TileMode.CLAMP);
        ComposeShader shader = new ComposeShader(gradient0, gradient1, PorterDuff.Mode.MULTIPLY);
        m_bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(m_bmp);
        m_paint.setShader(shader);
        canvas.drawRect(0, 0, w, h, m_paint);
        LocColor(m_cr, m_cg, m_cb);
        if (m_listener != null)
            m_listener.OnColorChange(m_bmp.getPixel(m_sel_x, m_sel_y));
        invalidate();
    }
    private int m_cr;
    private int m_cg;
    private int m_cb;
    public void LocColor(int cr, int cg, int cb)
    {
        m_cr = cr;
        m_cg = cg;
        m_cb = cb;
        if (m_bmp == null) return;
        int dr = 255 - cr;
        int dg = 255 - cg;
        int db = 255 - cb;
        if(dr > dg) dr = dg;
        if(dr > db) dr = db;
        int wr = cr;
        if (wr > cg) wr = cg;
        if (wr > cb) wr = cb;
        m_sel_y = dr * m_bmp.getHeight() / 255;
        m_sel_x = (255 - wr) * m_bmp.getWidth() / 255;
        if (m_sel_y >= m_bmp.getHeight()) m_sel_y = m_bmp.getHeight() - 1;
        if (m_sel_x >= m_bmp.getWidth()) m_sel_x = m_bmp.getWidth() - 1;
        if (m_listener != null)
            m_listener.OnColorChange(m_bmp.getPixel(m_sel_x, m_sel_y));
        invalidate();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        SetColor(m_color);
        LocColor(m_cr, m_cg, m_cb);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (m_bmp != null) canvas.drawBitmap(m_bmp, m_margin, m_margin, null);
        canvas.drawOval(new RectF(m_sel_x - m_rad + m_margin, m_sel_y - m_rad + m_margin, m_sel_x + m_rad + m_margin, m_sel_y + m_rad + m_margin), m_paint2);
        //super.onDraw(canvas);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = (int)event.getX();
        m_sel_x = x - m_margin;
        if (m_sel_x < 0) m_sel_x = 0;
        if (m_sel_x > m_bmp.getWidth()) m_sel_x = m_bmp.getWidth() - 1;

        int y = (int)event.getY();
        m_sel_y = y - m_margin;
        if (m_sel_y < 0) m_sel_y = 0;
        if (m_sel_y > m_bmp.getHeight()) m_sel_y = m_bmp.getHeight() - 1;

        if (m_listener != null)
            m_listener.OnColorChange(m_bmp.getPixel(m_sel_x, m_sel_y));
        invalidate();
        return true;
    }
}
