package com.radaee.annotui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.radaee.pdf.Document;
import com.radaee.pdf.Page;
import com.radaee.util.FileBrowserAdt;
import com.radaee.util.FileBrowserView;
import com.radaee.viewlib.R;

public class UIAnnotDlgSign extends UIAnnotDlg {
    private Document m_doc;

    public static float dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }

    public UIAnnotDlgSign(Context ctx) {
        super((RelativeLayout) LayoutInflater.from(ctx).inflate(R.layout.dlg_annot_signature, null));
        setCancelable(false);
        ImageView btn_browser = m_layout.findViewById(R.id.btn_browser);
        EditText edit_path = m_layout.findViewById(R.id.edit_path);
        btn_browser.setOnClickListener(v -> {
            //open browser dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(m_layout.getContext());
            builder.setTitle(R.string.text_select_cert_file_label);
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dlg_browser, null);
            builder.setView(view);
            builder.setNegativeButton(R.string.text_close_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dlg = builder.create();
            dlg.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    FileBrowserView fb_view = dlg.findViewById(R.id.fb_view);
                    TextView txt_filter = dlg.findViewById(R.id.txt_filter);
                    txt_filter.setText("*.p12 *.pfx");
                    fb_view.FileInit(Environment.getExternalStorageDirectory().getPath(), new String[]{".p12", ".pfx"});
                    fb_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            FileBrowserAdt.SnatchItem item = (FileBrowserAdt.SnatchItem) fb_view.getItemAtPosition(position);
                            if (item.m_item.is_dir())
                                fb_view.FileGotoSubdir(item.m_item.get_name());
                            else {
                                edit_path.setText(item.m_item.get_path());
                                dlg.dismiss();
                            }
                        }
                    });
                }
            });
            dlg.show();
        });
        setPositiveButton(R.string.text_confirm_label, (dialog, which) -> {
            UISignView sign_pad = m_layout.findViewById(R.id.sign_pad);
            EditText edit_pswd = m_layout.findViewById(R.id.edit_pswd);
            Document.DocForm form = sign_pad.SignMakeForm(m_doc, m_annot);
            String spath = edit_path.getText().toString();
            String spswd = edit_pswd.getText().toString();
            int iret = m_annot.SignField(form, spath, spswd, "radaee", "", "", "");
            if (iret == 0) {
                dialog.dismiss();
                if (m_callback != null)
                    m_callback.onUpdate();
            } else if (iret == -5)
                Toast.makeText(getContext(), R.string.text_cert_faile_label, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getContext(), R.string.text_sign_error_label, Toast.LENGTH_LONG).show();
        });
        setNegativeButton(R.string.text_cancel_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (m_callback != null)
                    m_callback.onCancel();
            }
        });
        /*
        m_layout.addOnLayoutChangeListener(
                new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        //keep signature area aspect.
                        //104 dp is exclude height value that assigned by layout xml file.
                        //if layout file changed, you shall update exclude dp value in following codes.
                        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
                        float layw = dm.widthPixels * 4 / 5;
                        float layh = dm.heightPixels * 4 / 5;
                        float exclude = dp2px(getContext(), 104);
                        float arect[] = m_annot.GetRect();
                        float scale1 = layw / (arect[2] - arect[0]);
                        float scale2 = (layh - exclude) / (arect[3] - arect[1]);
                        if (scale1 > scale2) scale1 = scale2;

                        layw = (arect[2] - arect[0]) * scale1;
                        layh = (arect[3] - arect[1]) * scale1;

                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) layw, (int) (layh + exclude));
                        //RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) layw, (int) (layh + exclude));
                        v.setLayoutParams(lp);
                    }
                });
                */
    }

    public void show(Page.Annotation annot, Document doc, UIAnnotMenu.IMemnuCallback calllback) {
        setTitle(R.string.text_sign_the_field_label);
        m_annot = annot;
        m_doc = doc;
        m_callback = calllback;
        AlertDialog dlg = create();
        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
                float layw = dm.widthPixels * 0.8f;
                float layh = dm.heightPixels * 0.8f;
                float exclude = dp2px(getContext(), 104);
                float[] arect = m_annot.GetRect();
                float scale1 = layw / (arect[2] - arect[0]);
                float scale2 = (layh - exclude) / (arect[3] - arect[1]);
                if (scale1 > scale2) scale1 = scale2;

                layw = (arect[2] - arect[0]) * scale1;
                layh = (arect[3] - arect[1]) * scale1;

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) layw, (int) (layh + exclude));
                //RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) layw, (int) (layh + exclude));
                m_layout.setLayoutParams(lp);
            }
        });
        dlg.show();
    }
}
