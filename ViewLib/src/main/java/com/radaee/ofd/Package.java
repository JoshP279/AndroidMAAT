package com.radaee.ofd;

import com.radaee.comm.RDStream;

public class Package {
    private static native long open(String path, String password);
    private static native long openStream(RDStream str_obj, String password);
    private static native void close(long pkg);
    private static native int getDocCount(long pkg);
    private static native long loadDoc(long pkg, int idx);
    private long m_hand;
    public Package()
    {
        m_hand = 0;
    }
    @Override
    protected void finalize() throws Throwable {
        Close();
        super.finalize();
    }
    /**
     * open document.<br/>
     * first time, SDK try password as user password, and then try password as owner password.
     *
     * @param path     PDF file to be open.
     * @param password password or null.
     * @return error code:<br/>
     * 0:succeeded, and continue<br/>
     * -1:need input password<br/>
     * -2:unknown encryption<br/>
     * -3:damaged or invalid format<br/>
     * -10:access denied or invalid file path<br/>
     * others:unknown error
     */
    public int Open(String path, String password) {
        if (m_hand == 0) {
            int ret = 0;
            try {
                m_hand = open(path, password);
            } catch (Exception e) {
                e.printStackTrace();
                m_hand = -10;
            }
            if (m_hand <= 0 && m_hand >= -10)//error
            {
                ret = (int) m_hand;
                m_hand = 0;
            }
            return ret;
        }
        return 0;
    }

    public int OpenStream(RDStream stream, String password) {
        if (m_hand == 0) {
            int ret = 0;
            try {
                m_hand = openStream(stream, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (m_hand <= 0 && m_hand >= -10)//error
            {
                ret = (int) m_hand;
                m_hand = 0;
            }
            return ret;
        }
        return 0;
    }
    public void Close()
    {
        close(m_hand);
    }
    public int GetDocCount()
    {
        return getDocCount(m_hand);
    }
    public Document LoadDoc(int idx)
    {
        long hand = loadDoc(m_hand, idx);
        if (hand == 0) return null;
        return new Document(hand);
    }
}
