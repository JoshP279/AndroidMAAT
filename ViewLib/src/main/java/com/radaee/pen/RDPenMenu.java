package com.radaee.pen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.radaee.viewlib.R;

public class RDPenMenu extends RelativeLayout
{
    public interface OnItemClickListener
    {
        void onItemClick(int index, int type, float width);
    }
    private LinearLayout m_item_pen;
    private LinearLayout m_item_color;
    private LinearLayout m_item_mark;
    private LinearLayout m_item_rubber;
    private SeekBar m_seek_pen;
    private SeekBar m_seek_color;
    private SeekBar m_seek_mark;
    private SeekBar m_seek_rubber;
    private RDPenDemoView m_demo_pen;
    private RDPenDemoView m_demo_color;
    private RDPenDemoView m_demo_mark;
    private RDPenDemoView m_demo_rubber;
    private float m_fw_pen;
    private float m_fw_color;
    private float m_fw_mark;
    private float m_fw_rubber;
    private TextView m_txt_pen;
    private TextView m_txt_color;
    private TextView m_txt_mark;
    private TextView m_txt_rubber;
    public RDPenMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void SetSelectPen(int type)
    {
        m_item_pen.setBackgroundColor(0);
        m_item_color.setBackgroundColor(0);
        m_item_mark.setBackgroundColor(0);
        m_item_rubber.setBackgroundColor(0);
        switch(type)
        {
            case 1:
                m_item_pen.setBackgroundColor(0xFFC0C0FF);
                break;
            case 2:
                m_item_color.setBackgroundColor(0xFFC0C0FF);
                break;
            case 3:
                m_item_mark.setBackgroundColor(0xFFC0C0FF);
                break;
            case 100:
                m_item_rubber.setBackgroundColor(0xFFC0C0FF);
                break;
        }
    }
    public void SetPen(int type, float width)
    {
        switch (type)
        {
            case 1:
                m_demo_pen.SetPen(type, width, 0xFF000000);
                m_seek_pen.setProgress((int)(width * 10) - 10);
                break;
            case 2:
                m_demo_color.SetPen(type, width, 0x80000000);
                m_seek_color.setProgress((int)(width * 10) - 100);
                break;
            case 3:
                m_demo_mark.SetPen(type, width, 0x80000000);
                m_seek_mark.setProgress((int)(width * 10) - 100);
                break;
            case 100:
                m_demo_rubber.SetPen(type, width, 0x80000000);
                m_seek_rubber.setProgress((int)(width * 10) - 100);
                break;
        }
    }
    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        m_txt_pen = findViewById(R.id.txt_pen);
        m_txt_color = findViewById(R.id.txt_color);
        m_txt_mark = findViewById(R.id.txt_mark);
        m_txt_rubber = findViewById(R.id.txt_rubber);
        m_demo_pen = findViewById(R.id.vw_pen);
        m_demo_color = findViewById(R.id.vw_color);
        m_demo_mark = findViewById(R.id.vw_mark);
        m_demo_rubber = findViewById(R.id.vw_rubber);
        m_seek_pen = findViewById(R.id.seek_pen);
        m_seek_pen.setMax(90);
        m_seek_pen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_fw_pen = (i + 10) * 0.1f;
                m_txt_pen.setText(String.format("%.1f", m_fw_pen));
                m_demo_pen.SetPen(1, m_fw_pen, 0xFF000000);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        m_seek_color = findViewById(R.id.seek_color);
        m_seek_color.setMax(400);
        m_seek_color.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_fw_color = (i + 100) * 0.1f;
                m_txt_color.setText(String.format("%.1f", m_fw_color));
                m_demo_color.SetPen(2, m_fw_color, 0xFF000000);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        m_seek_mark = findViewById(R.id.seek_mark);
        m_seek_mark.setMax(400);
        m_seek_mark.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_fw_mark = (i + 100) * 0.1f;
                m_txt_mark.setText(String.format("%.1f", m_fw_mark));
                m_demo_mark.SetPen(3, m_fw_mark, 0xFF000000);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        m_seek_rubber = findViewById(R.id.seek_rubber);
        m_seek_rubber.setMax(400);
        m_seek_rubber.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_fw_rubber = (i + 100) * 0.1f;
                m_txt_rubber.setText(String.format("%.1f", m_fw_rubber));
                m_demo_rubber.SetPen(100, m_fw_rubber, 0xFF000000);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        m_item_pen = findViewById(R.id.item_pen);
        m_item_color = findViewById(R.id.item_color);
        m_item_mark = findViewById(R.id.item_mark);
        m_item_rubber = findViewById(R.id.item_rubber);
        m_item_pen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_listener != null)
                    m_listener.onItemClick(0, 1, m_fw_pen);
                setVisibility(View.GONE);
            }
        });
        m_item_color.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_listener != null)
                    m_listener.onItemClick(1, 2, m_fw_color);
                setVisibility(View.GONE);
            }
        });
        m_item_mark.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_listener != null)
                    m_listener.onItemClick(2, 3, m_fw_mark);
                setVisibility(View.GONE);
            }
        });
        m_item_rubber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_listener != null)
                    m_listener.onItemClick(3, 100, m_fw_rubber);
                setVisibility(View.GONE);
            }
        });
    }
    private OnItemClickListener m_listener;
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        m_listener = listener;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        setVisibility(View.GONE);
        return true;
    }
}
