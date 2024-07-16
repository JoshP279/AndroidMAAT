package com.radaee.pen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.radaee.viewlib.R;

public class RDColorMenu extends RelativeLayout
{
    public interface OnItemClickListener
    {
        void onItemClick(int index, int color);
        void onCustomerClick(int color);
    }
    public RDColorMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private RDColorOvalView btn_clr1;
    private RDColorOvalView btn_clr2;
    private RDColorOvalView btn_clr3;
    private RDColorOvalView btn_clr4;
    private RDColorOvalView btn_clr5;
    private RDColorOvalView btn_clr6;
    private RDColorOvalView btn_clr7;
    private RDColorOvalView btn_clr8;
    private RDColorOvalView btn_clr9;
    private RDColorOvalView btn_clr10;
    private TextView txt_customer;
    private int m_color;
    public void SetColor(int color)
    {
        m_color = color;
        btn_clr1.SetSelected(false);
        btn_clr2.SetSelected(false);
        btn_clr3.SetSelected(false);
        btn_clr4.SetSelected(false);
        btn_clr5.SetSelected(false);
        btn_clr6.SetSelected(false);
        btn_clr7.SetSelected(false);
        btn_clr8.SetSelected(false);
        switch(color)
        {
            case 0xFF000000:
                btn_clr1.SetSelected(true);
                break;
            case 0xFFFFFFFF:
                btn_clr2.SetSelected(true);
                break;
            case 0xFFFF0000:
                btn_clr3.SetSelected(true);
                break;
            case 0xFF00FF00:
                btn_clr4.SetSelected(true);
                break;
            case 0xFF0000FF:
                btn_clr5.SetSelected(true);
                break;
            case 0xFF808080:
                btn_clr6.SetSelected(true);
                break;
            case 0xFFFFFF00:
                btn_clr7.SetSelected(true);
                break;
            case 0xFFFF00FF:
                btn_clr8.SetSelected(true);
                break;
            case 0xFF00FFFF:
                btn_clr9.SetSelected(true);
                break;
            case 0xFFCCCCCC:
                btn_clr10.SetSelected(true);
                break;
            default:
                txt_customer.setBackgroundColor(color);
                break;
        }
    }
    private OnClickListener m_callback = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (m_listener != null) m_listener.onItemClick(0, ((RDColorOvalView)view).GetColor());
            setVisibility(View.GONE);
        }
    };
    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        btn_clr1 = findViewById(R.id.btn_clr1);
        btn_clr2 = findViewById(R.id.btn_clr2);
        btn_clr3 = findViewById(R.id.btn_clr3);
        btn_clr4 = findViewById(R.id.btn_clr4);
        btn_clr5 = findViewById(R.id.btn_clr5);
        btn_clr6 = findViewById(R.id.btn_clr6);
        btn_clr7 = findViewById(R.id.btn_clr7);
        btn_clr8 = findViewById(R.id.btn_clr8);
        btn_clr9 = findViewById(R.id.btn_clr9);
        btn_clr10 = findViewById(R.id.btn_clr10);
        txt_customer = findViewById(R.id.btn_custom);
        btn_clr1.setOnClickListener(m_callback);
        btn_clr2.SetColor(0xFFFFFFFF);
        btn_clr2.setOnClickListener(m_callback);
        btn_clr3.SetColor(0xFFFF0000);
        btn_clr3.setOnClickListener(m_callback);
        btn_clr4.SetColor(0xFF00FF00);
        btn_clr4.setOnClickListener(m_callback);
        btn_clr5.SetColor(0xFF0000FF);
        btn_clr5.setOnClickListener(m_callback);
        btn_clr6.SetColor(0xFF808080);
        btn_clr6.setOnClickListener(m_callback);
        btn_clr7.SetColor(0xFFFFFF00);
        btn_clr7.setOnClickListener(m_callback);
        btn_clr8.SetColor(0xFFFF00FF);
        btn_clr8.setOnClickListener(m_callback);
        btn_clr9.SetColor(0xFF00FFFF);
        btn_clr9.setOnClickListener(m_callback);
        btn_clr10.SetColor(0xFFCCCCCC);
        btn_clr10.setOnClickListener(m_callback);

        txt_customer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(View.GONE);
                if (m_listener != null)
                    m_listener.onCustomerClick(m_color);
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
