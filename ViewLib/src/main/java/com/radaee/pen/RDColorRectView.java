package com.radaee.pen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class RDColorRectView extends View {
    private int m_color = 0xFF000000;
    private Paint m_paint = new Paint();
    private final int m_mdual;
    private final int m_margin;
    private Rect m_rect = new Rect();
    public RDColorRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        m_mdual = dp2px(context, 10);
        m_margin = dp2px(context, 5);
        m_paint = new Paint();
        m_paint.setStyle(Paint.Style.FILL);
        m_paint.setColor(m_color);
    }
    private static int dp2px(Context context, float dpValue) {
        return (int)(dpValue * context.getResources().getDisplayMetrics().density);
    }
    public void SetColor(int color)
    {
        m_color = color;
        m_paint.setColor(color);
        invalidate();
    }
    public int GetColor()
    {
        return m_color;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        m_rect.set(m_margin, m_margin, w - m_margin, h - m_margin);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(m_rect, m_paint);
        //super.onDraw(canvas);
    }
}
