package com.radaee.pdf;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.util.Log;

import com.radaee.comm.Global;

/**
 * class for ink.
 * @author radaee
 *
 */
public class Ink 
{
	protected long hand = 0;
	protected int color = 0;
	protected float width = 0;
	private static native long create( float line_w, int color, int style );
	private static native void onDown( long hand, float x, float y );
	private static native void onMove( long hand, float x, float y );
	private static native void onUp( long hand, float x, float y );
	private static native int getNodeCount( long hand );
	private static native int getNode( long hand, int index, float[] pt );
	private static native void destroy( long hand );
    public int ID;
	/**
	 * constructor for ink.
	 * @param line_w width of line.
	 */
	public Ink( float line_w, int ink_color )
	{
		width = line_w;
		color = ink_color;
		hand = create( line_w, color, 1 );
        m_paint.setStrokeCap(Cap.ROUND);
        m_paint.setStrokeJoin(Join.ROUND);
        m_paint.setStrokeWidth(width);
        m_paint.setColor(color);
        m_paint.setStyle(Style.STROKE);
        m_paint.setAntiAlias(true);
	}
	/**
	 * destroy and free memory.
	 */
	public final void Destroy()
	{
        if(hand != 0)
        {
            destroy(hand);
            hand = 0;
            m_path.reset();
            m_path_append.reset();
            path_idx = 0;
        }
	}
	/**
	 * call when click down
	 * @param x x value of point in this object.
	 * @param y y value of point in this object.
	 */
	public final void OnDown( float x, float y )
	{
		onDown( hand, x, y );
	}
	/**
	 * call when moving
	 * @param x x value of point in this object.
	 * @param y y value of point in this object.
	 */
	public final void OnMove( float x, float y )
	{
		onMove( hand, x, y );
	}
	/**
	 * call when click up
	 * @param x x value of point in this object.
	 * @param y y value of point in this object.
	 */
	public final void OnUp( float x, float y )
	{
		onUp( hand, x, y );
	}
    private int m_method = 0;
    private int path_idx = 0;
    private Path m_path = new Path();
    private Path m_path_cur = new Path();
    private Path m_path_append = new Path();
    private float pt1[] = new float[2];
    private float pt2[] = new float[2];
    private Paint m_paint = new Paint();
	/**
	 * draw to canvas
	 * @param canvas Canvas to draw
	 */
	public void OnDraw(Canvas canvas)
	{
        if(canvas == null) return;
        if(m_method != 1)
        {
            m_path.reset();
            m_path_append.reset();
            path_idx = 0;
            m_method = 1;
        }
        int index = path_idx;
        int cnt = getNodeCount(hand);
        int new_idx = 0;
        while( index < cnt )
        {
            int op = getNode( hand, index, pt1 );
            switch( op )
            {
                case 1:
                    m_path_cur.lineTo(pt1[0], pt1[1]);
                    index++;
                    break;
                case 2:
                    getNode( hand, index + 1, pt2 );
                    m_path_cur.quadTo(pt1[0], pt1[1], pt2[0], pt2[1]);
                    index += 2;
                    break;
                default:
                    m_path_append.reset();
                    m_path_append.addPath(m_path_cur);
                    new_idx = index;//last MoveTo
                    m_path_cur.moveTo(pt1[0], pt1[1]);
                    index++;
                    break;
            }
        }
        canvas.drawPath(m_path, m_paint);
        canvas.drawPath(m_path_cur, m_paint);
        if(new_idx > path_idx)
        {
            path_idx = new_idx;
            m_path.addPath(m_path_append);
        }
        m_path_cur.reset();
	}
	public void OnDraw(Canvas canvas, float scrollx, float scrolly)
	{
        if(canvas == null) return;
        if(m_method != 2)
        {
            m_path.reset();
            m_path_append.reset();
            path_idx = 0;
            m_method = 2;
        }
		int index = path_idx;
		int cnt = getNodeCount(hand);
        int new_idx = 0;
		while( index < cnt )
		{
			int op = getNode( hand, index, pt1 );
			switch( op )
			{
			case 1:
                m_path_cur.lineTo(pt1[0] + scrollx, pt1[1] + scrolly);
				index++;
				break;
			case 2:
				getNode( hand, index + 1, pt2 );
                m_path_cur.quadTo(pt1[0] + scrollx, pt1[1] + scrolly, pt2[0] + scrollx, pt2[1] + scrolly);
				index += 2;
				break;
			default:
                m_path_append.reset();
                m_path_append.addPath(m_path_cur);
                new_idx = index;//last MoveTo
                m_path_cur.moveTo(pt1[0] + scrollx, pt1[1] + scrolly);
				index++;
				break;
			}
		}
		canvas.drawPath(m_path, m_paint);
        canvas.drawPath(m_path_cur, m_paint);
        if(new_idx > path_idx)
        {
            path_idx = new_idx;
            m_path.addPath(m_path_append);
        }
        m_path_cur.reset();
	}
	public long get_hand()
    {
        return hand;
    }
    @Override
    protected void finalize() throws Throwable
    {
        Destroy();
        super.finalize();
    }
    public Ink clone() {
        Ink clonedInk = new Ink(this.width, this.color);

        // Clone fields
        clonedInk.m_method = this.m_method;
        clonedInk.path_idx = this.path_idx;

        // Deep copy of Path objects
        clonedInk.m_path = new Path(this.m_path);
        clonedInk.m_path_cur = new Path(this.m_path_cur);
        clonedInk.m_path_append = new Path(this.m_path_append);

        clonedInk.m_paint = new Paint(this.m_paint);

        clonedInk.pt1 = this.pt1.clone();
        clonedInk.pt2 = this.pt2.clone();
        clonedInk.hand = create(this.width, this.color, 1);
        return clonedInk;
    }
}
