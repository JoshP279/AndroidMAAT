package com.radaee.pen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class RDColorOvalView extends View {
    private Paint m_pfill;
    private Paint m_pborder0;
    private Paint m_pborder1;
    private boolean m_selected;
    private int m_pos;
    private float m_rad;
    private void init(Context context)
    {
        m_pfill = new Paint();
        m_pfill.setStyle(Paint.Style.FILL);
        m_pfill.setColor(0xFF000000);
        m_pborder0 = new Paint();
        m_pborder0.setStyle(Paint.Style.STROKE);
        m_pborder0.setStrokeWidth(dp2px(context, 1));
        m_pborder0.setColor(0xFF000000);
        m_pborder0.setAntiAlias(true);

        m_pborder1 = new Paint();
        m_pborder1.setStyle(Paint.Style.STROKE);
        m_pborder1.setStrokeWidth(dp2px(context, 2));
        m_pborder1.setColor(0xFF0000FF);
        m_pborder1.setAntiAlias(true);

        m_pos = dp2px(context, 20);
        m_rad = dp2px(context, 12);
    }
    private static int dp2px(Context context, float dpValue) {
        return (int)(dpValue * context.getResources().getDisplayMetrics().density);
    }
    public RDColorOvalView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public void SetColor(int color)
    {
        m_pfill.setColor(color | 0xFF000000);
        invalidate();
    }
    public void SetSelected(boolean selected)
    {
        m_selected = selected;
        invalidate();
    }
    public int GetColor()
    {
        return m_pfill.getColor() | 0xFF000000;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(m_pos, m_pos, m_rad, m_pfill);
        if (m_selected)
            canvas.drawCircle(m_pos, m_pos, m_rad, m_pborder1);
        else
            canvas.drawCircle(m_pos, m_pos, m_rad, m_pborder0);
    }
}
