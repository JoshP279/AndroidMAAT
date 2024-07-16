package com.radaee.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.radaee.pdf.Document;
import com.radaee.viewlib.R;

import java.io.File;

public class PDFAttListView extends ListView {
    private static final String[][] MIME_MapTable = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"}, {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"}, {".rtf", "application/rtf"},
            {".sh", "text/plain"}, {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"}, {".txt", "text/plain"},
            {".wav", "audio/x-wav"}, {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"}, {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"}, {"", "*/*"}
    };

    private Context mContext;
    private BaseAdapter m_adt;

    public interface PDFAttListListener {
        void onDeleteItem(int item);
    }

    public PDFAttListView(Context context) {
        super(context);

        mContext = context;
    }

    public PDFAttListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
    }

    public void updateList() {
        m_adt.notifyDataSetChanged();
    }

    public void init(Document doc, PDFAttListListener listener) {
        m_adt = new BaseAdapter() {
            Document m_doc = doc;
            PDFAttListListener m_listener = listener;

            @Override
            public int getCount() {
                return doc.GetEmbedFilesCount();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                RelativeLayout view = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_attachment, null);
                ImageView btn_del = view.findViewById(R.id.btn_del);
                btn_del.setOnClickListener(v -> {
                    m_doc.DelEmbedFile(position);
                    m_listener.onDeleteItem(position);
                    notifyDataSetChanged();
                });
                TextView txt_lab = view.findViewById(R.id.txt_lab);
                txt_lab.setText(m_doc.GetEmbedFileName(position));
                TextView txt_desc = view.findViewById(R.id.txt_desc);
                txt_desc.setText(m_doc.GetEmbedFileDesc(position));
                return view;
            }
        };
        setAdapter(m_adt);

        this.setOnItemClickListener((parent, view, position, id) -> {
            String path = Environment.getExternalStorageDirectory().getPath();
            path += File.separator + "attachments/";
            File folder = new File(path);
            if (!folder.exists() || folder.isFile())
                folder.mkdir();
            path = path + File.separator + doc.GetEmbedFileName(position);
            if (doc.GetEmbedFileData(position, path)) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(path);
                Uri uri = FileProvider.getUriForFile(mContext, /*mContext.getApplicationContext().getPackageName() + */"ViewLib.provider", file);
                //Uri uri = Uri.fromFile(file);
                String type = getMIMEType(file);
                intent.setDataAndType(uri, type);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(mContext, R.string.hint_cannot_open_attachment_label, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private static String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();

        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }

        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (TextUtils.isEmpty(end)) {
            return type;
        }

        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0])) {
                type = MIME_MapTable[i][1];
                break;
            }
        }
        return type;
    }
}