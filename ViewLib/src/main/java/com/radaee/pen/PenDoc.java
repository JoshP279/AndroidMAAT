package com.radaee.pen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.util.Locale;

public class PenDoc
{
    protected static final String TYPE_THUMB = ".pic";
    protected static final String TYPE_STROKES = ".sts";
    protected static final String TYPE_INFO = ".inf";
    protected static final String TYPE_BGBMP = ".bgm";
    protected static final String TYPE_PANEL = ".pan";
    protected File m_dir;
    protected String m_name;
    protected long m_time_modify;
    protected long m_time_create;
    private int m_page_count;
    protected int m_ival1;
    protected int m_ival2;
    protected int m_ival3;
    protected static boolean is_dir_doc(File dir)
    {
        if(dir == null || !dir.isDirectory()) return false;
        File finfo = new File(dir, "note.info");
        if(!finfo.exists()) return false;
        PenBinFile bfile = new PenBinFile(finfo.getAbsolutePath());
        int tag = bfile.ReadInt();
        bfile.Close();
        return (tag == -1024);
    }

    public PenDoc(File dir)
    {
        m_dir = dir;
        File finfo = new File(m_dir, "note.info");
        if(!m_dir.exists() || !finfo.exists())
        {
            m_dir.mkdir();
            m_name = "untitled";
            m_time_create = System.currentTimeMillis();
            m_time_modify = m_time_create;
            m_page_count = 1;
            save_info();
        }
        else
        {
            PenBinFile bfile = new PenBinFile(finfo.getAbsolutePath());
            int tag = bfile.ReadInt();
            if (tag == -1024) {
                m_time_create = bfile.ReadLong();
                m_time_modify = bfile.ReadLong();
                m_page_count = bfile.ReadInt();
                m_ival1 = bfile.ReadInt();
                m_ival2 = bfile.ReadInt();
                m_ival3 = bfile.ReadInt();
                m_name = bfile.ReadString();
            }
            bfile.Close();
        }
    }
    public Bitmap load_thumb(int pageno)
    {
        String sname = String.format(Locale.ENGLISH, "P%03d", pageno);
        File fthumb = new File(m_dir, sname + TYPE_THUMB);
        if(fthumb.exists()) return BitmapFactory.decodeFile(fthumb.getAbsolutePath());
        else return null;
    }
    public void save_thumb(int pageno, Bitmap thumb)
    {
        String sname = String.format(Locale.ENGLISH, "P%03d", pageno);
        File fthumb = new File(m_dir, sname + TYPE_THUMB);
        PenPage.save_jpeg(thumb, fthumb.getAbsolutePath(), 100);
        //PenPage.save_png(thumb, fthumb.getAbsolutePath());
    }
    /**
     * 加载页面，返回Page对象，如果第一页不存在，自动创建<br/>
     * 注意：返回Page对象后，如果Page对象没有关闭，此页面将被锁定，删除文档、页面将会失败
     * @param pageno 页码
     * @return 锁定了文件的HVPage对象
     */
    public synchronized PenPage load_page(int pageno)
    {
        if(pageno < 0 || pageno >= m_page_count) return null;
        String sname = String.format(Locale.ENGLISH, "P%03d", pageno);
        File fstrokes = new File(m_dir, sname + TYPE_STROKES);
        File finfo = new File(m_dir, sname + TYPE_INFO);
        File fbgbmp = new File(m_dir, sname + TYPE_BGBMP);
        File fpanel = new File(m_dir, sname + TYPE_PANEL);
        return new PenPage(fstrokes.getAbsolutePath(), finfo.getAbsolutePath(), fbgbmp.getAbsolutePath(), fpanel.getAbsolutePath());
    }
    public synchronized PenPage new_page()
    {
        m_page_count++;
        return load_page(m_page_count - 1);
    }
    public synchronized boolean delete_page(int pageno)
    {
        String sname = String.format(Locale.ENGLISH, "P%03d", pageno);
        File fstrokes = new File(m_dir, sname + TYPE_STROKES);
        File finfo = new File(m_dir, sname + TYPE_INFO);
        File fbgbmp = new File(m_dir, sname + TYPE_BGBMP);
        File fthumb = new File(m_dir, sname + TYPE_THUMB);
        if(fstrokes.exists() && !fstrokes.delete()) return error_file(fstrokes);
        if(finfo.exists() && !finfo.delete()) return error_file(finfo);
        if(fbgbmp.exists() && !fbgbmp.delete()) return error_file(fbgbmp);
        if(fthumb.exists() && !fthumb.delete()) return error_file(fthumb);
        m_page_count--;
        for(; pageno < m_page_count; pageno++)
        {
            sname = String.format(Locale.ENGLISH, "P%03d", pageno + 1);
            fstrokes = new File(m_dir, sname + TYPE_STROKES);
            finfo = new File(m_dir, sname + TYPE_INFO);
            fbgbmp = new File(m_dir, sname + TYPE_BGBMP);
            fthumb = new File(m_dir, sname + TYPE_THUMB);
            String sname_new = String.format(Locale.ENGLISH, "P%03d", pageno);
            File fstrokes_new = new File(m_dir, sname_new + TYPE_STROKES);
            File finfo_new = new File(m_dir, sname_new + TYPE_INFO);
            File fbgbmp_new = new File(m_dir, sname_new + TYPE_BGBMP);
            File fthumb_new = new File(m_dir, sname_new + TYPE_THUMB);
            if(fstrokes.exists() && !fstrokes.renameTo(fstrokes_new)) return error_file(fstrokes);
            if(finfo.exists() && !finfo.renameTo(finfo_new)) return error_file(finfo);
            if(fbgbmp.exists() && !fbgbmp.renameTo(fbgbmp_new)) return error_file(fbgbmp);
            if(fthumb.exists() && !fthumb.renameTo(fthumb_new)) return error_file(fthumb);
        }
        return true;
    }
    public void save_info()
    {
        File finfo = new File(m_dir, "note.info");
        PenBinFile bfile = new PenBinFile(finfo.getAbsolutePath());
        m_time_modify = System.currentTimeMillis();
        bfile.WriteInt(-1024);
        bfile.WriteLong(m_time_create);
        bfile.WriteLong(m_time_modify);
        bfile.WriteInt(m_page_count);
        bfile.WriteInt(m_ival1);
        bfile.WriteInt(m_ival2);
        bfile.WriteInt(m_ival3);
        bfile.WriteString(m_name);
        bfile.Close();
    }
    public synchronized long get_time_modify()
    {
        return m_time_modify;
    }
    public long get_id() {return m_time_create;}
    public synchronized String get_name()
    {
        return m_name;
    }
    //设置名称
    public synchronized void set_name(String name)
    {
        m_name = name;
    }

    protected static boolean error_file(File err_file)
    {
        Log.e("RDPenDoc", "error, can't delete or rename file:" + err_file.getAbsolutePath());
        return false;
    }
    public static boolean delete_dir(File dir)
    {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) delete_dir(file);
                else if (!file.delete()) return error_file(file);
            }
        }
        if(!dir.delete()) return error_file(dir);
        return true;
    }
    /**
     * 从存储删除整份文档。
     * 注意：如果失败文件不可恢复，失败的情况：某个页面对象或者文件没有关闭
     */
    public synchronized boolean delete()
    {
        return delete_dir(m_dir);
    }
}
