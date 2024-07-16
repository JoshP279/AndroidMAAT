package com.radaee.pen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Page对象是指管理一个笔迹页面的对象。<br/>
 * 主要是管理笔迹数据。
 */
public class PenPage {
    protected PenStrokeDoc m_data;
    protected boolean m_loaded_info;
    protected String m_path_info;
    protected String m_path_bgbmp;
    protected int m_bg_color;
    protected Bitmap m_bg_bmp;
    protected String m_path_panel;
    /**
     * 构造函数，如果传入的两个文件已存在，则打开。如果不存在，则创建
     * @param path_dat 保存笔迹数据的文件名
     * @param path_info 保存页面背景、文本等
     * @param path_bgbmp 页面背景图片
     */
    public PenPage(String path_dat, String path_info, String path_bgbmp, String path_panel)
    {
        m_data = new PenStrokeDoc(path_dat);
        m_path_info = path_info;
        m_path_bgbmp = path_bgbmp;
        m_path_panel = path_panel;
        m_loaded_info = false;
    }
    private void load_info()
    {
        if(m_loaded_info) return;
        PenBinFile file = new PenBinFile(m_path_info);
        if(file.ReadInt() == -1023)
        {
            m_bg_color = file.ReadInt();
            File fbg = new File(m_path_bgbmp);
            if (fbg.exists())
                m_bg_bmp = BitmapFactory.decodeFile(m_path_bgbmp);
            file.Close();
        }
        else
        {
            file.Close();
            m_bg_color = -1;
            save_info();
        }
        m_loaded_info = true;
    }
    public void Load()
    {
        load_info();
    }
    public synchronized int GetBGColor()
    {
        load_info();
        return m_bg_color;
    }
    /**
     * set background as color value, this will make background bitmap to null.
     * @param color background color
     */
    public synchronized void SetBGColor(int color)
    {
        m_loaded_info = true;
        m_bg_color = color;
        m_bg_bmp = null;
    }
    /**
     * 获取背景图片
     * @return background bitmap object
     */
    public synchronized Bitmap GetBGBmp()
    {
        load_info();
        return m_bg_bmp;
    }
    public synchronized Bitmap LoadPanel()
    {
        try {
            File file = new File(m_path_panel);
            if (!file.exists()) return null;
            return BitmapFactory.decodeFile(m_path_panel);
        }catch (Exception ex)
        {
            return null;
        }
    }
    /**
     * 设置背景图片
     * @param bmp bitmap object
     */
    public synchronized void SetBGBmp(Bitmap bmp)
    {
        m_loaded_info = true;
        m_bg_bmp = bmp;
    }
    public static void save_png(Bitmap bmp, String path)
    {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }catch (Exception ignored)
        {
        }
    }
    public static void save_jpeg(Bitmap bmp, String path, int quality)
    {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
        }catch (Exception ignored)
        {
        }
    }
    private void save_info()
    {
        PenBinFile file = new PenBinFile(m_path_info);
        file.WriteInt(-1023);
        file.WriteInt(m_bg_color);
        file.WriteInt(0);
        file.Close();
    }
    public void SaveStroke(Bitmap bmp)
    {
        m_data.Save();
        if (bmp == null) return;
        save_png(bmp, m_path_panel);
        if (m_bg_bmp == null)
        {
            File file = new File(m_path_bgbmp);
            file.deleteOnExit();
        }
        else
            save_png(m_bg_bmp, m_path_bgbmp);
    }
    public void Close()
    {
        m_data.Close();
    }
}
