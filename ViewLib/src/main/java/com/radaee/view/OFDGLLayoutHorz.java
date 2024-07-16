package com.radaee.view;

import android.content.Context;

public class OFDGLLayoutHorz extends OFDGLLayout {
    private boolean m_rtol = false;

    public OFDGLLayoutHorz(Context context, boolean rtol) {
        super(context);
        m_rtol = rtol;
    }

    @Override
    public int vGetPage(int vx, int vy) {
        if (m_vw <= 0 || m_vh <= 0) return -1;
        vx += vGetX();
        int pl = 0;
        int pr = m_page_cnt - 1;
        int hg = (m_page_gap >> 1);
        if (!m_rtol) {
            while (pr >= pl) {
                int mid = (pl + pr) >> 1;
                OFDGLPage pmid = m_pages[mid];
                if (vx < pmid.GetLeft() - hg)
                    pr = mid - 1;
                else if (vx >= pmid.GetRight() + hg)
                    pl = mid + 1;
                else return mid;
            }
            return (pr < 0) ? 0 : pr;
        } else {
            while (pr >= pl) {
                int mid = (pl + pr) >> 1;
                OFDGLPage pmid = m_pages[mid];
                if (vx < pmid.GetLeft() - hg)
                    pl = mid + 1;
                else if (vx >= pmid.GetRight() + hg)
                    pr = mid - 1;
                else return mid;
            }
            return (pr < 0) ? 0 : pr;
        }
    }

    private void layout_ltor(float scale, boolean zoom) {
        float[] size = m_doc.GetPagesMaxSize();
        m_scale_min = (m_vh - m_page_gap) / size[1];
        float max_scale = m_scale_min * m_max_zoom;
        if (scale < m_scale_min) scale = m_scale_min;
        if (scale > max_scale) scale = max_scale;
        if (m_scale == scale) return;
        m_scale = scale;
        m_layh = (int) (size[1] * m_scale) + m_page_gap;
        int x = m_page_gap >> 1;
        for (int pcur = 0; pcur < m_page_cnt; pcur++) {
            int ph = (int) (m_doc.GetPageHeight(pcur) * m_scale);
            m_pages[pcur].gl_layout(x, (m_layh - ph) >> 1, m_scale);
            if (!zoom) m_pages[pcur].gl_alloc();
            x += (int) (m_doc.GetPageWidth(pcur) * m_scale) + m_page_gap;
        }
        m_layw = x;
    }

    private void layout_rtol(float scale, boolean zoom) {
        float[] size = m_doc.GetPagesMaxSize();
        m_scale_min = (m_vh - m_page_gap) / size[1];
        float max_scale = m_scale_min * m_max_zoom;
        if (scale < m_scale_min) scale = m_scale_min;
        if (scale > max_scale) scale = max_scale;
        if (m_scale == scale) return;
        m_scale = scale;
        m_layh = (int) (size[1] * m_scale) + m_page_gap;
        int x = m_page_gap >> 1;
        for (int pcur = 0; pcur < m_page_cnt; pcur++)
            x += (int) (m_doc.GetPageWidth(pcur) * m_scale) + m_page_gap;
        m_layw = x;

        x = m_layw - (m_page_gap >> 1);
        for (int pcur = 0; pcur < m_page_cnt; pcur++) {
            int pw = (int) (m_doc.GetPageWidth(pcur) * m_scale);
            int ph = (int) (m_doc.GetPageHeight(pcur) * m_scale);
            m_pages[pcur].gl_layout(x - pw, (m_layh - ph) >> 1, m_scale);
            if (!zoom) m_pages[pcur].gl_alloc();
            x -= pw + m_page_gap;
        }
        vSetX(m_layw - m_vw);
    }

    @Override
    public void gl_layout(float scale, boolean zoom) {
        if (m_vw <= 0 || m_vh <= 0) return;
        if (!m_rtol) layout_ltor(scale, zoom);
        else layout_rtol(scale, zoom);
    }
}
