package com.radaee.pen;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.Vector;

public class PenPlatform {
    public static PenPlatform inst;
    public static void Init(Context context)
    {
        if(inst != null) return;
        inst = new PenPlatform(context.getExternalFilesDir(null));
    }

    private File m_root;
    public PenPlatform(File root)
    {
        m_root = root;
        m_notes = null;
        load_notes();
        m_thread = new PenThread();
        m_thread.start();
    }

    /**
     * 枚举Note列表，并按时间排序。
     * 此函数速度很快，只是简单读取一些基本信息
     * @return
     */
    Vector<PenDoc> m_notes;
    private void sort_notes()
    {
        if (m_notes == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            m_notes.sort(new Comparator<PenDoc>()
            {
                @Override
                public int compare(PenDoc doc1, PenDoc doc2) {
                    return (int)(doc2.get_time_modify() - doc1.get_time_modify());
                }
            });
        }
    }
    private void load_notes()
    {
        if(m_notes != null) return;
        if(m_root == null) return;
        m_notes = new Vector<PenDoc>();
        File fnote_root = new File(m_root, "Note");
        if(!fnote_root.exists())
        {
            fnote_root.mkdir();
            return;
        }
        File[] fnotes = fnote_root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return PenDoc.is_dir_doc(file);
            }
        });
        if(fnotes == null || fnotes.length <= 0) return;
        for(int idx = 0; idx < fnotes.length; idx++)
            m_notes.add(new PenDoc(fnotes[idx]));
        sort_notes();
    }
    public int getNotesCount()
    {
        synchronized (m_notes) {
            if (m_notes == null) return 0;
            return m_notes.size();
        }
    }
    public void sortNotes()
    {
        synchronized (m_notes) {
            sort_notes();
        }
    }
    public PenDoc getNote(int idx)
    {
        synchronized (m_notes) {
            if(idx >= m_notes.size()) return null;
            return m_notes.get(idx);
        }
    }
    public PenDoc getNote(long id)
    {
        synchronized (m_notes) {
            int cnt  = m_notes.size();
            for(int idx = 0; idx < cnt; idx++)
            {
                PenDoc note = m_notes.get(idx);
                if(note.get_id() == id) return note;
            }
        }
        return null;
    }
    public boolean delNote(int idx)
    {
        synchronized (m_notes) {
            if (idx >= m_notes.size()) return false;
            PenDoc note = m_notes.remove(idx);
            return note.delete();
        }
    }
    public boolean delNote(PenDoc note)
    {
        synchronized (m_notes) {
            m_notes.remove(note);
            return note.delete();
        }
    }
    public PenDoc newNote()
    {
        synchronized (m_notes) {
            long cms = System.currentTimeMillis();
            File fnote_root = new File(m_root, "Note");
            if (m_root == null) return null;
            File file = new File(fnote_root, "" + cms);
            file.mkdir();
            PenDoc note = new PenDoc(file);
            m_notes.insertElementAt(note, 0);
            return note;
        }
    }
    private class PenThread extends Thread
    {
        private Handler m_handler;
        private boolean is_notified = false;
        private boolean is_waitting = false;
        private synchronized void wait_init()
        {
            try
            {
                if( is_notified )
                    is_notified = false;
                else
                {
                    is_waitting = true;
                    wait();
                    is_waitting = false;
                }
            }
            catch(Exception e)
            {
            }
        }
        private synchronized void notify_init()
        {
            if( is_waitting )
                notify();
            else
                is_notified = true;
        }
        @Override
        public void start()
        {
            super.start();
            wait_init();
        }
        @Override
        public void run()
        {
            Looper.prepare();
            m_handler = new Handler(Looper.myLooper())
            {
                @Override
                public void handleMessage(Message msg)
                {
                    switch(msg.what)
                    {
                    case 0:
                        ((Runnable)msg.obj).run();
                        msg.obj = null;
                        break;
                    case 1:
                        ((Runnable)msg.obj).run();
                        msg.obj = null;
                        break;
                    case 1000:
                        getLooper().quit();
                        break;
                    }
                    super.handleMessage(msg);
                }
            };
            notify_init();
            Looper.loop();
        }
        public synchronized  boolean post(Runnable runnable, boolean check_pending)
        {
            if(check_pending && m_handler.hasMessages(0)) return false;
            m_handler.sendMessageAtFrontOfQueue(m_handler.obtainMessage(0, runnable));
            return true;
        }
        public synchronized void destroy()
        {
            try
            {
                m_handler.sendEmptyMessage(1000);
                join();
            }
            catch(InterruptedException e)
            {
            }
        }
    }
    PenThread m_thread;
    public boolean postRunnable(Runnable runnable, boolean check_pending)
    {
        if(runnable == null) return false;
        return m_thread.post(runnable, check_pending);
    }
}
