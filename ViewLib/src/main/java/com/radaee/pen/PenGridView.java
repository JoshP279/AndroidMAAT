package com.radaee.pen;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.radaee.viewlib.R;

public class PenGridView extends GridView
{
    public PenGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_adt = new PenGridAdapter();
        setAdapter(m_adt);
        setNumColumns(2);//2列
    }
    public void refresh()
    {
        m_adt.refresh();
    }
    private class NoteItem
    {
        private RelativeLayout m_layout;
        private PenDoc m_note;
        private Bitmap m_thumb;
        private String m_sname;
        private ImageView m_image;
        private TextView m_text;
        private NoteItem(PenDoc note)
        {
            m_note = note;
            m_layout = (RelativeLayout)LayoutInflater.from(getContext()).inflate(R.layout.lay_note_item, null);
            m_image = m_layout.findViewById(R.id.img_thumb);
            m_text = m_layout.findViewById(R.id.txt_name);
            PenPlatform.inst.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if(m_note == null) return;
                    m_thumb = m_note.load_thumb(0);
                    m_sname = m_note.get_name();
                    m_layout.post(new Runnable() {
                        @Override
                        public void run() {
                            m_image.setImageBitmap(m_thumb);
                            m_text.setText(m_sname);
                        }
                    });
                }
            }, false);
        }
    };
    private class PenGridAdapter implements ListAdapter
    {
        private DataSetObserver m_obs;
        private NoteItem m_items[];
        public void refresh()
        {
            m_items = null;
            if(m_obs != null) m_obs.onChanged();
        }
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            m_obs = dataSetObserver;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            m_obs = null;
        }

        @Override
        public int getCount() {
            if(m_items == null) {
                int count = PenPlatform.inst.getNotesCount();
                m_items = new NoteItem[count];
                return count;
            }
            else return m_items.length;
        }

        @Override
        public Object getItem(int i) {
            if(m_items == null)
            {
                int count = PenPlatform.inst.getNotesCount();
                m_items = new NoteItem[count];
            }
            if(m_items[i] == null)
                m_items[i] = new NoteItem(PenPlatform.inst.getNote(i));
            return m_items[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            if(m_items == null)
            {
                int count = PenPlatform.inst.getNotesCount();
                m_items = new NoteItem[count];
            }
            if(m_items[i] == null)
                m_items[i] = new NoteItem(PenPlatform.inst.getNote(i));
            return m_items[i].m_layout;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return PenPlatform.inst.getNotesCount() <= 0;
        }
    };
    private PenGridAdapter m_adt;
}
