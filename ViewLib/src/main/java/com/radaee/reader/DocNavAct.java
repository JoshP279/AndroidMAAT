package com.radaee.reader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.radaee.comm.Global;
import com.radaee.util.PDFGridItem;
import com.radaee.util.PDFGridView;
import com.radaee.viewlib.R;

public class DocNavAct extends Activity implements OnItemClickListener {
    private LinearLayout m_layout;
    private PDFGridView m_grid;
    private EditText m_path;
    private String m_engine;
    private boolean m_pending = false;

    class OpenTask extends AsyncTask<Void, Integer, Integer> {
        private ProgressDialog dlg;
        private Handler handler;
        private Runnable runable;
        private final PDFGridItem item;
        private final String pswd;
        private int ret;
        com.radaee.pdf.Document m_pdf;
        com.radaee.docx.Document m_docx;

        OpenTask(PDFGridItem item, String pswd) {
            this.item = item;
            this.pswd = pswd;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            m_pdf = new com.radaee.pdf.Document();
            int err = item.open_pdf(m_pdf, pswd);
            if (err != 0) {
                m_pdf.Close();
                m_pdf = null;
            }
            if (err == -3 || err == -10) {
                m_docx = new com.radaee.docx.Document();
                err = item.open_docx(m_docx, pswd);
                if (err != 0) {
                    m_docx.Close();
                    m_docx = null;
                }
            }
            ret = err;
            return null;
        }

        @Override
        protected void onPreExecute() {
            m_pending = true;
            handler = new Handler();
            runable = new Runnable() {
                public void run() {
                    dlg = ProgressDialog.show(DocNavAct.this, getString(R.string.please_wait), getString(R.string.thumbnail_creation_running), true);
                }
            };
            handler.postDelayed(runable, 1000);//delay 1 second to display progress dialog.

        }

        @Override
        protected void onPostExecute(Integer integer) {
            m_pending = false;
            switch (ret) {
                case -1://need input password
                    InputPswd(item);
                    break;
                case -2://unknown encryption
                    onFail(getString(R.string.failed_encryption));
                    break;
                case -3://damaged or invalid format
                    onFail(getString(R.string.failed_invalid_format));
                    break;
                case -10://access denied or invalid file path
                    onFail(getString(R.string.failed_invalid_path));
                    break;
                case 0://succeeded, and continue
                    InitView(m_pdf, m_docx);
                    break;
                default://unknown error
                    onFail(getString(R.string.failed_unknown));
                    break;
            }
            if (dlg != null)
                dlg.dismiss();
            else
                handler.removeCallbacks(runable);
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //plz set this line to Activity in AndroidManifes.xml:
        //    android:configChanges="orientation|keyboardHidden|screenSize"
        //otherwise, APP shall destroy this Activity and re-create a new Activity when rotate. 
        Global.Init(this);
        m_engine = getIntent().getStringExtra("ENGINE");
        m_layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.pdf_nav, null);
        m_grid = (PDFGridView) m_layout.findViewById(R.id.pdf_nav);
        m_path = (EditText) m_layout.findViewById(R.id.txt_path);
        m_grid.PDFSetRootPath(Environment.getExternalStorageDirectory().getPath());
        m_path.setText(m_grid.getPath());
        m_path.setEnabled(false);
        m_grid.setOnItemClickListener(this);
        setContentView(m_layout);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onDestroy() {
        Global.RemoveTmp();
        super.onDestroy();
    }

    private void onFail(String msg)//treat open failed.
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void InputPswd(PDFGridItem item)//treat password
    {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dlg_pswd, null);
        final EditText tpassword = (EditText) layout.findViewById(R.id.txt_password);
        final PDFGridItem gitem = item;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String password = tpassword.getText().toString();
                OpenTask task = new OpenTask(gitem, password);
                task.execute();
            }
        });
        builder.setNegativeButton(R.string.text_cancel_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle(R.string.input_password);
        builder.setCancelable(false);
        builder.setView(layout);

        AlertDialog dlg = builder.create();
        dlg.show();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)//listener for icon clicked.
    {
        if (m_pending) return;
        PDFGridItem item = (PDFGridItem) arg1;
        if (item.is_dir()) {
            m_grid.PDFGotoSubdir(item.get_name());
            m_path.setText(m_grid.getPath());
        } else {
            OpenTask task = new OpenTask(item, null);
            task.execute();
        }
    }

    private void InitView(com.radaee.pdf.Document pdf, com.radaee.docx.Document docx)//process to view PDF file
    {
        if (pdf != null) {
            PDFEditViewAct.ms_tran_doc = pdf;
            Intent intent = new Intent(this, PDFEditViewAct.class);
            startActivity(intent);
            //PDFPagesAct.ms_tran_doc = pdf;
            //Intent intent = new Intent(this, PDFPagesAct.class);
            //startActivity(intent);
        } else {
            DOCXGLViewAct.ms_tran_doc = docx;
            Intent intent = new Intent(this, DOCXGLViewAct.class);
            startActivity(intent);
        }
    }
}
