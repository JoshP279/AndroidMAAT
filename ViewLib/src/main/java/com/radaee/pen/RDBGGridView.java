package com.radaee.pen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.radaee.viewlib.R;

public class RDBGGridView extends GridView {
    protected float m_dpunit;
    protected static float dp2px(Context context, float dpValue) {
        return dpValue * context.getResources().getDisplayMetrics().density;
    }
    private final RelativeLayout[] m_list = new RelativeLayout[9];
    public RDBGGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);

        m_dpunit = dp2px(context, 1);
        for(int iv = 0; iv < 9; iv++) {
            m_list[iv] = (RelativeLayout) inflater.inflate(R.layout.lay_bg_item, null);
            ((RDBGView)m_list[iv].findViewById(R.id.vw_bgitem)).SetType(iv);
        }
        setAdapter(new BaseAdapter()
        {
            @Override
            public int getCount() {
                return 9;
            }

            @Override
            public Object getItem(int i) {
                return m_list[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                return m_list[i];
            }
        });
        setNumColumns(1);
    }
}
