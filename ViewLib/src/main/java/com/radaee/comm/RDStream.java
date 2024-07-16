package com.radaee.comm;

public interface RDStream {
    /**
     * check whether the stream is writable
     *
     * @return true or false
     */
    public boolean writeable();

    /**
     * get stream length.
     *
     * @return
     */
    public int get_size();

    /**
     * read data from stream
     *
     * @param data output values.
     * @return bytes read
     */
    public int read(byte[] data);

    /**
     * write data to stream
     *
     * @param data data to write
     * @return bytes written
     */
    public int write(byte[] data);

    /**
     * seek to position
     *
     * @param pos position from begin of the stream
     */
    public void seek(int pos);

    /**
     * tell current position
     *
     * @return position from begin of the stream
     */
    public int tell();
}
