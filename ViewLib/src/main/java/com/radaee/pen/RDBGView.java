package com.radaee.pen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class RDBGView extends View
{
    abstract public static class BGRender implements Parcelable
    {
        protected int m_type;
        protected float m_dpunit;
        protected int m_fore_color;
        protected int m_bg_color;
        protected static float dp2px(Context context, float dpValue) {
            return dpValue * context.getResources().getDisplayMetrics().density;
        }
        public void onCreate(Context context)
        {
            m_type = 0;
            m_bg_color = -1;
            m_fore_color = 0xFF000000;
            m_dpunit = dp2px(context, 1);
        }
        public void SetForeColor(int color)
        {
            m_fore_color = color;
        }
        public void SetBGColor(int color)
        {
            m_bg_color = color;
        }
        public abstract void onDraw(Canvas canvas, int w, int h);
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(m_type);
            dest.writeFloat(m_dpunit);
            dest.writeInt(m_fore_color);
            dest.writeInt(m_bg_color);
        }
        @Override
        public int describeContents() {
            return 0;
        }
        public static final Creator<BGRender> CREATOR = new Creator<BGRender>() {

            @Override
            public BGRender[] newArray(int size) {
                return new BGRender[size];
            }

            @Override
            public BGRender createFromParcel(Parcel source) {
                int type = source.readInt();
                switch(type)
                {
                    case 1: {
                        BGRenderLines1 ret = new BGRenderLines1();
                        ret.m_type = type;
                        ret.m_dpunit = source.readFloat();
                        ret.m_fore_color = source.readInt();
                        ret.m_bg_color = source.readInt();
                        ret.gap_times = source.readFloat();
                        return ret;
                    }
                    case 2: {
                        BGRenderLines2 ret = new BGRenderLines2();
                        ret.m_type = type;
                        ret.m_dpunit = source.readFloat();
                        ret.m_fore_color = source.readInt();
                        ret.m_bg_color = source.readInt();
                        ret.gap_times = source.readFloat();
                        return ret;
                    }
                    case 3: {
                        BGRenderLines3 ret = new BGRenderLines3();
                        ret.m_type = type;
                        ret.m_dpunit = source.readFloat();
                        ret.m_fore_color = source.readInt();
                        ret.m_bg_color = source.readInt();
                        ret.gap_times = source.readFloat();
                        return ret;
                    }
                    case 4: {
                        BGRenderLines4 ret = new BGRenderLines4();
                        ret.m_type = type;
                        ret.m_dpunit = source.readFloat();
                        ret.m_fore_color = source.readInt();
                        ret.m_bg_color = source.readInt();
                        ret.gap_times = source.readFloat();
                        return ret;
                    }
                    case 5: {
                        BGRenderGrid1 ret = new BGRenderGrid1();
                        ret.m_type = type;
                        ret.m_dpunit = source.readFloat();
                        ret.m_fore_color = source.readInt();
                        ret.m_bg_color = source.readInt();
                        ret.gap_times = source.readFloat();
                        return ret;
                    }
                    case 6: {
                        BGRenderGrid2 ret = new BGRenderGrid2();
                        ret.m_type = type;
                        ret.m_dpunit = source.readFloat();
                        ret.m_fore_color = source.readInt();
                        ret.m_bg_color = source.readInt();
                        ret.gap_times = source.readFloat();
                        return ret;
                    }
                    case 7: {
                        BGRenderGrid3 ret = new BGRenderGrid3();
                        ret.m_type = type;
                        ret.m_dpunit = source.readFloat();
                        ret.m_fore_color = source.readInt();
                        ret.m_bg_color = source.readInt();
                        ret.gap_times = source.readFloat();
                        return ret;
                    }
                    case 8: {
                        BGRenderGrid4 ret = new BGRenderGrid4();
                        ret.m_type = type;
                        ret.m_dpunit = source.readFloat();
                        ret.m_fore_color = source.readInt();
                        ret.m_bg_color = source.readInt();
                        ret.gap_times = source.readFloat();
                        return ret;
                    }
                    default: {
                        BGRenderEmpty ret = new BGRenderEmpty();
                        ret.m_type = 0;
                        ret.m_dpunit = source.readFloat();
                        ret.m_fore_color = source.readInt();
                        ret.m_bg_color = source.readInt();
                        return ret;
                    }
                }
            }
        };
    }
    public static class BGRenderEmpty extends BGRender
    {
        @Override
        public void onDraw(Canvas canvas, int w, int h) {
            canvas.drawColor(m_bg_color);
        }
    }
    public static class BGRenderLines1 extends BGRender
    {
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(m_type);
            dest.writeFloat(m_dpunit);
            dest.writeInt(m_fore_color);
            dest.writeInt(m_bg_color);
            dest.writeFloat(gap_times);
        }
        protected float gap_times;
        public void onCreate(Context context)
        {
            super.onCreate(context);
            m_type = 1;
            gap_times = 40;
        }
        @Override
        public void onDraw(Canvas canvas, int w, int h) {
            canvas.drawColor(m_bg_color);
            int gap = (int)(m_dpunit * gap_times);
            int left = (int)(m_dpunit * 10);
            int right = w - left;
            Paint paint = new Paint();
            paint.setColor(m_fore_color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(m_dpunit);
            paint.setStrokeCap(Paint.Cap.BUTT);
            for(int y = (gap >> 1); y < h; y += gap)
            {
                canvas.drawLine(left, y, right, y, paint);
            }
        }
    }
    public static class BGRenderLines2 extends BGRenderLines1
    {
        @Override
        public void onCreate(Context context)
        {
            super.onCreate(context);
            m_type = 2;
            gap_times = 30;
        }
    }
    public static class BGRenderLines3 extends BGRenderLines1
    {
        @Override
        public void onCreate(Context context)
        {
            super.onCreate(context);
            m_type = 3;
            gap_times = 20;
        }
    }
    public static class BGRenderLines4 extends BGRenderLines1
    {
        @Override
        public void onCreate(Context context)
        {
            super.onCreate(context);
            m_type = 4;
            gap_times = 10;
        }
    }
    public static class BGRenderGrid1 extends BGRender
    {
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(m_type);
            dest.writeFloat(m_dpunit);
            dest.writeInt(m_fore_color);
            dest.writeInt(m_bg_color);
            dest.writeFloat(gap_times);
        }
        protected float gap_times;
        public void onCreate(Context context)
        {
            super.onCreate(context);
            m_type = 5;
            gap_times = 40;
        }
        @Override
        public void onDraw(Canvas canvas, int w, int h) {
            canvas.drawColor(m_bg_color);
            int gap = (int)(m_dpunit * gap_times);
            Paint paint = new Paint();
            paint.setColor(m_fore_color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(m_dpunit);
            paint.setStrokeCap(Paint.Cap.BUTT);
            for(int y = (gap >> 1); y < h; y += gap)
                canvas.drawLine(0, y, w, y, paint);
            for(int x = (gap >> 1); x < w; x += gap)
                canvas.drawLine(x, 0, x, h, paint);
        }
    }
    public static class BGRenderGrid2 extends BGRenderGrid1
    {
        public void onCreate(Context context)
        {
            super.onCreate(context);
            m_type = 6;
            gap_times = 30;
        }
    }
    public static class BGRenderGrid3 extends BGRenderGrid1
    {
        public void onCreate(Context context)
        {
            super.onCreate(context);
            m_type = 7;
            gap_times = 20;
        }
    }
    public static class BGRenderGrid4 extends BGRenderGrid1
    {
        public void onCreate(Context context)
        {
            super.onCreate(context);
            m_type = 8;
            gap_times = 10;
        }
    }

    private BGRender m_render;
    public RDBGView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        m_render = new BGRenderEmpty();
        m_render.onCreate(context);
    }
    public void SetType(int type)
    {
        switch(type)
        {
            case 1:
                m_render = new BGRenderLines1();
                break;
            case 2:
                m_render = new BGRenderLines2();
                break;
            case 3:
                m_render = new BGRenderLines3();
                break;
            case 4:
                m_render = new BGRenderLines4();
                break;
            case 5:
                m_render = new BGRenderGrid1();
                break;
            case 6:
                m_render = new BGRenderGrid2();
                break;
            case 7:
                m_render = new BGRenderGrid3();
                break;
            case 8:
                m_render = new BGRenderGrid4();
                break;
            default:
                m_render = new BGRenderEmpty();
                break;
        }
        m_render.onCreate(getContext());
        invalidate();
    }
    public void SetForeColor(int color)
    {
        m_render.SetForeColor(color);
        invalidate();
    }
    public void SetBGColor(int color)
    {
        m_render.SetBGColor(color);
        invalidate();
    }
    public BGRender GetBackground()
    {
        return m_render;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        m_render.onDraw(canvas, getWidth(), getHeight());
    }
}
