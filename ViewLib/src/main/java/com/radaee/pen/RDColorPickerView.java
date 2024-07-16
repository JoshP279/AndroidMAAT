package com.radaee.pen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class RDColorPickerView extends View
{
    public interface OnColorChangeListener
    {
        void OnColorChange(int color);
    }
    private Bitmap m_bmp;
    private final Paint m_paint = new Paint();
    private final Paint m_paint1 = new Paint();
    private final int[] m_colors;
    private final int m_mdual;
    private final int m_margin;
    private int m_pos;
    private OnColorChangeListener m_listener;
    public RDColorPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        m_mdual = dp2px(context, 10);
        m_margin = dp2px(context, 5);
        m_colors = new int[] {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
        m_paint1.setStyle(Paint.Style.FILL);
        m_paint1.setColor(0xC0000000);
        m_paint1.setAntiAlias(true);
    }
    public void SetListener(OnColorChangeListener listener)
    {
        m_listener = listener;
    }
    private static int dp2px(Context context, float dpValue) {
        return (int)(dpValue * context.getResources().getDisplayMetrics().density);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        w -= m_mdual;
        h -= m_mdual;
        if (w <= 0 || h <= 0) return;
        LocColor(m_cr, m_cg, m_cb);
        LinearGradient gradient = new LinearGradient(0, 0, 0, h, m_colors, new float[] {0, 1.0f/6, 1.0f/3, 0.5f, 2.0f/3, 5.0f/6, 1}, Shader.TileMode.CLAMP);
        m_bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(m_bmp);
        m_paint.setShader(gradient);
        canvas.drawRect(0, 0, w, h, m_paint);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (m_bmp != null)
            canvas.drawBitmap(m_bmp, m_margin, m_margin, null);
        Path path = new Path();
        int pos = m_pos + m_margin;
        path.moveTo(0, pos - m_margin * 0.5f);
        path.lineTo(0, pos + m_margin * 0.5f);
        path.lineTo(m_margin, pos);
        path.close();
        canvas.drawPath(path, m_paint1);
        canvas.drawRect(0, pos - 2, getRight(), pos + 2, m_paint1);
        //super.onDraw(canvas);
    }
    private int m_cr;
    private int m_cg;
    private int m_cb;
    public void LocColor(int cr, int cg, int cb)
    {
        m_cr = cr;
        m_cg = cg;
        m_cb = cb;
        if(m_bmp == null) return;
        int bh = m_bmp.getHeight();
        //m_colors = new int[] {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
        //m_positi = new float[] {0, 1.0f/6, 1.0f/3, 0.5f, 2.0f/3, 5.0f/6, 1}
        if (cr == 255)
        {
            if (cg > 0)//rg
            {
                m_pos = (bh * cg) / (255 * 6);
            }
            else if (cb > 0)//rb
            {
                m_pos = (bh * cb) / (255 * 6) + bh * 5 / 6;
            }
            else//pure red
            {
                m_pos = 0;
            }
        }
        else if (cg == 255)
        {
            if (cr > 0)//rg
            {
                m_pos = (bh * cg) / (255 * 6) + bh / 6;
            }
            else if (cb > 0)
            {
                m_pos = (bh * cg) / (255 * 6) + bh / 3;
            }
            else
            {
                m_pos = bh / 3;
            }
        }
        else if (cb == 255)
        {
            if (cr > 0)
            {
                m_pos = (bh * cg) / (255 * 6) + bh * 2 / 3;
            }
            else if (cg > 0)
            {
                m_pos = (bh * cg) / (255 * 6) + bh / 2;
            }
            else
            {
                m_pos = bh * 2 / 3;
            }
        }
        if (m_pos > m_bmp.getHeight()) m_pos = m_bmp.getHeight() - 1;
        m_listener.OnColorChange(m_bmp.getPixel(m_mdual, m_pos));
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int y = (int)event.getY();
        int pos = y - m_margin;
        if (pos < 0) pos = 0;
        if (pos > m_bmp.getHeight()) pos = m_bmp.getHeight() - 1;
        m_pos = pos;
        m_listener.OnColorChange(m_bmp.getPixel(m_mdual, m_pos));
        invalidate();
        return true;
    }
}
