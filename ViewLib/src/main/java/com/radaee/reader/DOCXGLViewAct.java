package com.radaee.reader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.radaee.comm.Global;
import com.radaee.docx.Document;
import com.radaee.util.RDAssetStream;
import com.radaee.util.RDHttpStream;
import com.radaee.view.IDOCXLayoutView;
import com.radaee.viewlib.R;

public class DOCXGLViewAct extends Activity implements IDOCXLayoutView.DOCXLayoutListener {
    static public Document ms_tran_doc;
    private RDAssetStream m_asset_stream = null;
    private RDHttpStream m_http_stream = null;
    private Document m_doc = null;
    private RelativeLayout m_layout = null;
    private DOCXGLLayoutView m_view = null;
    private DOCXViewController m_controller = null;
    private boolean m_need_save_doc = false;

    private void onFail(String msg)//treat open failed.
    {
        m_doc.Close();
        m_doc = null;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void ProcessOpenResult(int ret) {
        switch (ret) {
            case -1://need input password
                onFail("Open Failed: Invalid Password");
                break;
            case -2://unknown encryption
                onFail("Open Failed: Unknown Encryption");
                break;
            case -3://damaged or invalid format
                onFail("Open Failed: Damaged or Invalid DOCX file");
                break;
            case -10://access denied or invalid file path
                onFail("Open Failed: Access denied or Invalid path");
                break;
            case 0://succeeded, and continue
                m_need_save_doc = false;
                OpenTask task = new OpenTask();
                task.execute();
                //m_view.DOCXOpen(m_doc, this);
                //m_controller = new DOCXViewController(m_layout, m_view);
                break;
            default://unknown error
                onFail("Open Failed: Unknown Error");
                break;
        }
    }

    class OpenTask extends AsyncTask<Void, Integer, Integer> {
        private ProgressDialog dlg;
        private Handler handler;
        private Runnable runable;

        @Override
        protected Integer doInBackground(Void... voids) {
            m_doc.GetPagesMaxSize();//it may spend much time for first invoking this method.
            return null;
        }

        @Override
        protected void onPreExecute() {
            handler = new Handler();
            runable = new Runnable() {
                public void run() {
                    dlg = ProgressDialog.show(DOCXGLViewAct.this, "Please wait", "Loading DOCX file...", true);
                }
            };
            handler.postDelayed(runable, 1000);//delay 1 second to display progress dialog.
        }

        @Override
        protected void onPostExecute(Integer integer) {
            m_view.DOCXOpen(m_doc, DOCXGLViewAct.this);
            m_controller = new DOCXViewController(m_layout, m_view);
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
        m_layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.docx_gllayout, null);
        m_view = (DOCXGLLayoutView) m_layout.findViewById(R.id.docx_glview);
        Intent intent = getIntent();
        if (ms_tran_doc != null) {
            m_doc = ms_tran_doc;
            ms_tran_doc = null;
            m_need_save_doc = true;
            OpenTask task = new OpenTask();
            task.execute();
            /*
        	m_view.DOCXOpen(m_doc, this);
    		m_controller = new DOCXViewController(m_layout, m_view);
    		need_save_doc = true;
    		*/
        } else {
            String docx_asset = intent.getStringExtra("DOCXAsset");
            String docx_path = intent.getStringExtra("DOCXPath");
            String docx_pswd = intent.getStringExtra("DOCXPswd");
            String docx_http = intent.getStringExtra("DOCXHttp");
            if (docx_http != null && docx_http != "") {
                //PDFHttpStream.open sometimes spend too long time, so, we open url in backing thread.
                new Thread(){
                    @Override
                    public void run() {
                        m_http_stream = new RDHttpStream();
                        m_http_stream.open(docx_http);
                        //after http link opened, open PDF in UI thread.
                        m_layout.post(new Runnable() {
                            @Override
                            public void run() {
                                m_doc = new Document();
                                int ret = m_doc.OpenStream(m_http_stream, docx_pswd);
                                ProcessOpenResult(ret);
                            }
                        });
                    }
                }.start();
            } else if (docx_asset != null && docx_asset != "") {
                m_asset_stream = new RDAssetStream();
                m_asset_stream.open(getAssets(), docx_asset);
                m_doc = new Document();
                int ret = m_doc.OpenStream(m_asset_stream, docx_pswd);
                ProcessOpenResult(ret);
            } else if (docx_path != null && docx_path != "") {
                m_doc = new Document();
                int ret = m_doc.Open(docx_path, docx_pswd);
                //m_doc.SetCache(String.format("%s/temp%08x.dat", Global.tmp_path, m_tmp_index));//set temporary cache for editing.
                //m_tmp_index++;
                //m_doc.SetFontDel(m_font_del);
                ProcessOpenResult(ret);
            }
        }
        setContentView(m_layout);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (m_doc == null)
            m_doc = m_view.DOCXGetDoc();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        m_view.BundleSavePos(savedInstanceState);
        if (m_need_save_doc && m_doc != null) {
            Document.BundleSave(savedInstanceState, m_doc);//save Document object
            m_doc = null;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (m_doc == null) {
            m_doc = Document.BundleRestore(savedInstanceState);//restore Document object
            if (m_doc == null) return;
            m_view.DOCXOpen(m_doc, this);
            m_controller = new DOCXViewController(m_layout, m_view);
            m_need_save_doc = true;
        }
        m_view.BundleRestorePos(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (m_controller == null || m_controller.OnBackPressed()) {
            super.onBackPressed();
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onDestroy() {
        final DOCXViewController vctrl = m_controller;
        final Document doc = m_doc;
        final DOCXGLLayoutView view = m_view;
        final RDAssetStream astr = m_asset_stream;
        final RDHttpStream hstr = m_http_stream;
        m_controller = null;
        m_doc = null;
        m_view = null;
        m_asset_stream = null;
        m_http_stream = null;
        new Thread(){
            @Override
            public void run() {
                if (vctrl != null) vctrl.onDestroy();
                if (view != null) view.DOCXClose();
                if (doc != null) doc.Close();
                if (astr != null) astr.close();
                if (hstr != null) hstr.close();
                Global.RemoveTmp();
                synchronized (DOCXGLViewAct.this) { DOCXGLViewAct.this.notify(); }
            }
        }.start();
        synchronized (this)
        {
            try { wait(1500); }
            catch(Exception ignored) { }
        }
        super.onDestroy();
    }

    @Override
    public void OnDOCXPageChanged(int pageno) {
        if (m_controller != null)
            m_controller.OnPageChanged(pageno);
    }

    @Override
    public void OnDOCXBlankTapped() {
        if (m_controller != null)
            m_controller.OnBlankTapped();
    }

    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }
    @Override
    public void OnDOCXSelectEnd() {
        LinearLayout layout = findViewById(R.id.text_select_menu);
        if (layout.getVisibility() == View.VISIBLE)
            layout.setVisibility(View.GONE);
    }

    @Override
    public void OnDOCXTextSelected(String text, float x, float y) {
        LinearLayout layout = findViewById(R.id.text_select_menu);

        View.OnClickListener TextSelectionListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.icon_copy) {
                    Toast.makeText(DOCXGLViewAct.this, getString(R.string.text_copy_hint) + text, Toast.LENGTH_SHORT).show();
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Radaee", text);
                    clipboard.setPrimaryClip(clip);
                }
            }
        };

        layout.findViewById(R.id.icon_copy).setOnClickListener(TextSelectionListener);

        layout.setVisibility(View.VISIBLE);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int height = layout.getHeight();
        int width = layout.getWidth();
        if (x + width > displayMetrics.widthPixels)
            x = displayMetrics.widthPixels - width - dp2px(10);
        layout.setX(x);
        float yPosition = y + dp2px(30);
        if (yPosition + height > displayMetrics.heightPixels)
            yPosition = displayMetrics.heightPixels - height - dp2px(30);
        layout.setY(yPosition);
    }

    @Override
    public void OnDOCXOpenURI(String uri) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(uri);
            intent.setData(content_url);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(DOCXGLViewAct.this, "todo: open url:" + uri, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void OnDOCXZoomStart() {
    }

    @Override
    public void OnDOCXZoomEnd() {
    }

    @Override
    public boolean OnDOCXDoubleTapped(float x, float y) {
        return false;
    }

    @Override
    public void OnDOCXLongPressed(float x, float y) {
    }

    @Override
    public void OnDOCXSearchFinished(boolean found) {
        if (!found) Toast.makeText(this, "No more found.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void OnDOCXPageDisplayed(Canvas canvas, IDOCXLayoutView.IVPage vpage) {
    }

    @Override
    public void OnDOCXPageRendered(IDOCXLayoutView.IVPage vpage) {
    }
}
