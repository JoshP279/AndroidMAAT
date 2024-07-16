package com.radaee.util;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.GridView;

import java.io.File;

public class PDFGridView extends GridView {

    public static final int MODE_BROWSE = 0;
    public static final int MODE_RECENT = 1;
    private PDFGridAdt m_browse_adt;
    private RecentFileGridAdt m_recent_adt;
    private String m_root;
    private String m_cur;

    private final Context mContext;

    public PDFGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        if (dm.widthPixels > dm.heightPixels)
            setNumColumns(5);
        else
            setNumColumns(3);
        mContext = context;
    }

    public void setViewMode(int mode) {
        if (mode == MODE_BROWSE) {
            m_browse_adt = new PDFGridAdt(mContext);
            this.setAdapter(m_browse_adt);
        } else if (mode == MODE_RECENT) {
            m_recent_adt = new RecentFileGridAdt(mContext);
            this.setAdapter(m_recent_adt);
        }
    }

    public void PDFSetRootPath(String path) {
        m_root = path;
        m_cur = path;
        File root = new File(m_cur);
        int w = getWidth();
        int h = getHeight();
        if (w > 0 || h > 0) {
            if (w > h)
                setNumColumns(5);
            else
                setNumColumns(3);
        } else {
            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            if (dm.widthPixels > dm.heightPixels)
                setNumColumns(5);
            else
                setNumColumns(3);
        }
        if (root.exists() && root.isDirectory()) {
            m_browse_adt.set_dir(root, false);
        }
    }

    public void PDFGotoSubdir(String name) {
        String new_path = m_cur;
        if (name.equals(".")) {
        } else if (name.equals("..")) {
            int index = m_cur.lastIndexOf('/');
            if (index < 0) return;
            new_path = new_path.substring(0, index);
        } else {
            new_path += "/";
            new_path += name;
        }
        File dir = new File(new_path);
        if (dir.exists() && dir.isDirectory()) {
            m_browse_adt.notifyDataSetInvalidated();
            m_cur = new_path;
            m_browse_adt.set_dir(dir, m_cur.compareTo(m_root) != 0);
        }
    }

    public void close() {
        if (m_browse_adt != null) {
            m_browse_adt.destroy();
            m_browse_adt = null;
        }
        if (m_recent_adt != null) {
            m_recent_adt.destory();
            m_recent_adt = null;
        }
    }

    public String getPath() {
        return m_cur;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setNumColumns(3);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setNumColumns(5);
        }
    }

    /*protected void onSizeChanged (int w, int h, int oldw, int oldh)
    {
        if( w * 3 > h * 5 )
            this.setNumColumns(5);
        else if( w * 3 < h * 4 )
            this.setNumColumns(4);
        else
            this.setNumColumns(3);
        this.invalidate();
    }*/
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
