package com.radaee.view;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.radaee.pdf.Page;

import javax.microedition.khronos.opengles.GL10;

public class PDFGLThread extends Thread {
    private Handler m_hand = null;
    private Handler m_hand_gl = null;
    private boolean is_notified = false;
    private boolean is_waitting = false;

    private synchronized void wait_init() {
        try {
            if (is_notified)
                is_notified = false;
            else {
                is_waitting = true;
                wait();
                is_waitting = false;
            }
        } catch (Exception ignored) {
        }
    }

    private synchronized void notify_init() {
        if (is_waitting)
            notify();
        else
            is_notified = true;
    }

    @Override
    public void run() {
        Looper.prepare();
        m_hand = new Handler(Looper.myLooper()) {
            public void handleMessage(Message msg) {
                if (msg == null) return;
                if (msg.what == 0)//render function
                {
                    PDFGLBlock blk = (PDFGLBlock) msg.obj;
                    blk.bk_render();
                    if (m_hand_gl != null)
                        m_hand_gl.sendMessage(m_hand_gl.obtainMessage(0, 0, 0, blk));
                    msg.obj = null;
                    super.handleMessage(msg);
                } else if (msg.what == 1) {
                    PDFGLBlock blk = (PDFGLBlock) msg.obj;
                    blk.bk_destroy();
                    msg.obj = null;
                    super.handleMessage(msg);
                } else if (msg.what == 2) {
                    int ret = ((PDFFinder) msg.obj).find();
                    if (m_hand_gl != null)
                        m_hand_gl.sendMessage(m_hand_gl.obtainMessage(2, ret, 0, msg.obj));
                    msg.obj = null;
                    super.handleMessage(msg);
                }
                else if(msg.what == 3)
                {
                    ((PDFGLReflowBlock)msg.obj).render();
                    if(m_hand_gl != null)
                        m_hand_gl.sendMessage( m_hand_gl.obtainMessage(1, 0, 0, msg.obj) );
                }
                else if(msg.what == 4)
                {
                    ((PDFGLReflowBlock)msg.obj).destroy();
                }
                else if(msg.what == 5)
                {
                    ((Page)msg.obj).Close();
                }
                else if (msg.what == 100)//quit
                {
                    super.handleMessage(msg);
                    getLooper().quit();
                }
            }
        };
        notify_init();
        Looper.loop();
    }

    public void render_start(PDFGLBlock blk) {
        if (blk != null && blk.gl_start()) {
            m_hand.sendMessage(m_hand.obtainMessage(0, blk));
        }
    }

    public boolean render_end(GL10 gl10, PDFGLBlock blk) {
        if (blk == null) return false;
        if (blk.gl_end(gl10)) {
            m_hand.sendMessage(m_hand.obtainMessage(1, blk));
            return true;
        } else return false;
    }

    protected void find_start(PDFFinder finder) {
        m_hand.sendMessage(m_hand.obtainMessage(2, finder));
    }

    @Override
    public void start() {
        super.start();
        wait_init();
    }

    public void set_handler(Handler hand_gl) {
        m_hand_gl = hand_gl;
    }

    public synchronized void destroy() {
        if (m_hand == null) return;
        try {
            m_hand.sendEmptyMessage(100);
            join();
            m_hand = null;
        } catch (InterruptedException ignored) {
        }
    }
    public void reflow_start(PDFGLReflowBlock blk)
    {
        if(blk.render_start())
            m_hand.sendMessage(m_hand.obtainMessage(3, blk));
    }
    public void reflow_end(PDFGLReflowBlock blk)
    {
        if(blk.render_cancel())
            m_hand.sendMessage(m_hand.obtainMessage(4, blk));
    }
    public void reflow_destroy_page(Page page)
    {
        if(page == null) return;
        m_hand.sendMessage(m_hand.obtainMessage(5, page));
    }
}
