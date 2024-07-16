package com.radaee.view;

import android.content.Context;

public class PDFLayoutVert extends PDFLayout {
    @Override
    public void vLayout() {
        if (m_w <= 0 || m_h <= 0 || m_doc == null || m_pages == null) return;
        int cnt = m_doc.GetPageCount();
        int cur;
        m_scale_min = (m_w - m_page_gap) / m_page_maxw;
        m_scale_max = m_scale_min * m_zoom_level;
        if (m_scale < m_scale_min) m_scale = m_scale_min;
        if (m_scale > m_scale_max) m_scale = m_scale_max;
        boolean clip = m_scale / m_scale_min > m_zoom_level_clip;
        m_tw = (int) (m_page_maxw * m_scale);
        m_th = 0;
        int y = m_page_gap >> 1;
        for (cur = 0; cur < cnt; cur++) {
            int w = (int) (m_doc.GetPageWidth(cur) * m_scale);
            int h = (int) (m_doc.GetPageHeight(cur) * m_scale);
            int x = ((int) (m_page_maxw * m_scale) + m_page_gap - w) >> 1;
            m_pages[cur].vLayout(x, y, m_scale, clip);
            y += h + m_page_gap;
        }
        m_th = y - (m_page_gap >> 1);
    }

    public PDFLayoutVert(Context context) {
        super(context);
    }

    @Override
    public int vGetPage(int vx, int vy) {
        if (m_pages == null) return -1;
        vx += vGetX();
        vy += vGetY();
        int left = 0;
        int right = m_pages.length - 1;
        PDFPage vpage;
        if (vy < m_pages[0].GetY()) {
            return 0;
        } else if (vy > m_pages[right].GetY()) {
            return right;
        }
        while (left <= right) {
            int mid = (left + right) >> 1;
            vpage = m_pages[mid];
            switch (vpage.LocVert(vy, m_page_gap >> 1)) {
                case -1:
                    right = mid - 1;
                    break;
                case 1:
                    left = mid + 1;
                    break;
                default:
                    if (vpage.GetWidth() <= 0 || vpage.GetHeight() <= 0) return -1;
                    return mid;
            }
        }
        return -1;
    }
}
