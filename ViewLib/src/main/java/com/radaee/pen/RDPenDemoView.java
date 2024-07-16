package com.radaee.pen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class RDPenDemoView extends View {
    public RDPenDemoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    private Bitmap m_bmp;
    @Override
    public void onDraw(Canvas canvas)
    {
        if (m_bmp != null)  canvas.drawBitmap(m_bmp, 0, 0, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        m_bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        if(m_type > 0) SetPen(m_type, m_width, m_color);
    }
    private static int dp2px(Context context, float dpValue) {
        return (int)(dpValue * context.getResources().getDisplayMetrics().density);
    }
    private int m_type;
    private float m_width;
    private int m_color;
    public void SetPen(int type, float width, int color)
    {
        m_type = type;
        m_width = width;
        m_color = color;
        int vw = getWidth();
        int vh = getHeight();
        if(vw <= 0 || vh <= 0) return;
        Panel panel = new Panel(vw, vh);
        Pen pen = new Pen(m_type);
        panel.SetPen(pen);
        pen.SetWidth(dp2px(getContext(), m_width) * 0.4f);
        pen.SetColor(m_color);

        if(m_type == 100)
            m_bmp.eraseColor(0xFF000000);
        else
            m_bmp.eraseColor(0);
        panel.UpdatePanel(m_bmp);

        float margin = dp2px(getContext(), 50) * 0.2f;
        panel.OnTouchDown(margin, vh * 0.5f, 0);
        panel.Update(m_bmp);
        panel.OnTouchMove(vw * 0.35f, vh * 0.25f, 1);
        panel.Update(m_bmp);
        panel.OnTouchMove(vw * 0.65f, vh * 0.75f, 1);
        panel.Update(m_bmp);
        panel.OnTouchMove((float) vw - margin, vh * 0.5f, 0);
        panel.Update(m_bmp);
        panel.OnTouchEnd();
        panel.Update(m_bmp);
        panel.Destroy();
        pen.Destroy();

        invalidate();
    }
}
