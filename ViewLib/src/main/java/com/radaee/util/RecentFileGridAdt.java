package com.radaee.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class RecentFileGridAdt extends BaseAdapter {
    private ArrayList<PDFGridAdt.SnatchItem> mItems;
    private Context mContext;
    private PDFGridThread m_thread;

    Handler m_hand_ui = new Handler() {
        public void handleMessage(Message msg) {
            PDFGridItem item = (PDFGridItem) msg.obj;
            item.page_set();
            notifyDataSetChanged();
            super.handleMessage(msg);
        }
    };

    public RecentFileGridAdt(Context context) {
        mItems = new ArrayList<>();
        mContext = context;

        m_thread = new PDFGridThread(m_hand_ui);
        m_thread.start();
        updateData();
    }

    private void updateData() {
        for (PDFGridAdt.SnatchItem item : mItems) {
            item.m_item.page_destroy();
        }
        mItems.clear();

        RDRecentFiles.loadAndUpdate(mContext, m_thread, mItems);
    }

    public void destory() {
        for (PDFGridAdt.SnatchItem item : mItems) {
            item.m_item.page_destroy();
        }
        mItems.clear();
        m_thread.destroy();
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mItems.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        if (position < mItems.size() && position > 0)
            return mItems.get(position);
        return null;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mItems.get(position).m_item;
    }
}
