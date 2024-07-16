package com.radaee.reader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.radaee.comm.Global;
import com.radaee.pdf.Document;
import com.radaee.pdf.Page.Annotation;
import com.radaee.util.RDAssetStream;
import com.radaee.util.RDHttpStream;
import com.radaee.util.RadaeePluginCallback;
import com.radaee.view.IPDFLayoutView;
import com.radaee.viewlib.R;

public class PDFViewAct extends Activity implements IPDFLayoutView.PDFLayoutListener {
    private String mFindQuery = "";
    private boolean mDidShowReader = false;

    static public Document ms_tran_doc;
    private RDAssetStream m_asset_stream = null;
    private RDHttpStream m_http_stream = null;
    private Document m_doc = null;
    private RelativeLayout m_layout = null;
    private PDFLayoutView m_view = null;
    private PDFViewController m_controller = null;
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
                onFail(getString(R.string.failed_invalid_password));
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
                m_need_save_doc = false;
                OpenTask task = new OpenTask();
                task.execute();
                break;
            default://unknown error
                onFail(getString(R.string.failed_unknown));
                break;
        }
    }

    private class OpenTask extends AsyncTask<Void, Integer, Integer> {
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
                    dlg = ProgressDialog.show(PDFViewAct.this, getString(R.string.please_wait), getString(R.string.loading_pdf), true);
                }
            };
            handler.postDelayed(runable, 1000);//delay 1 second to display progress dialog.
        }

        @Override
        protected void onPostExecute(Integer integer) {
            m_view.PDFOpen(m_doc, PDFViewAct.this);
            m_view.setReadOnly(getIntent().getBooleanExtra("READ_ONLY", false));
            m_controller = new PDFViewController(m_layout, m_view);
            if (dlg != null)
                dlg.dismiss();
            else
                handler.removeCallbacks(runable);

            int gotoPage = getIntent().getIntExtra("GOTO_PAGE", -1);
            if (gotoPage > 0)
                m_view.PDFGotoPage(gotoPage);
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
        m_layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.pdf_layout, null);
        m_view = (PDFLayoutView) m_layout.findViewById(R.id.pdf_view);
        show_progress = true;
        RadaeePluginCallback.getInstance().willShowReader();
        if (!Global.g_cache_enable)
            m_layout.findViewById(R.id.progress).setVisibility(View.GONE);

        Intent intent = getIntent();
        String bmp_format = intent.getStringExtra("BMPFormat");
        if (bmp_format != null) {
            if (bmp_format.compareTo("RGB_565") == 0)
                m_view.PDFSetBmpFormat(Bitmap.Config.RGB_565);
            else if (bmp_format.compareTo("ARGB_4444") == 0)
                m_view.PDFSetBmpFormat(Bitmap.Config.ARGB_4444);
        }
        if (ms_tran_doc != null) {
            m_doc = ms_tran_doc;
            ms_tran_doc = null;
            //m_doc.SetCache(String.format("%s/temp%08x.dat", Global.tmp_path, m_tmp_index));//set temporary cache for editing.
            //m_tmp_index++;
            m_need_save_doc = true;
            OpenTask task = new OpenTask();
            task.execute();
            /*
            m_view.PDFOpen(m_doc, this);
    		m_controller = new PDFViewController(m_layout, m_view);
    		need_save_doc = true;
    		*/
        } else {
            String pdf_asset = intent.getStringExtra("PDFAsset");
            String pdf_path = intent.getStringExtra("PDFPath");
            String pdf_pswd = intent.getStringExtra("PDFPswd");
            String pdf_http = intent.getStringExtra("PDFHttp");
            if (!TextUtils.isEmpty(pdf_http)) {
                //PDFHttpStream.open sometimes spend too long time, so, we open url in backing thread.
                new Thread(){
                    @Override
                    public void run() {
                        m_http_stream = new RDHttpStream();
                        m_http_stream.open(pdf_http);
                        //after http link opened, open PDF in UI thread.
                        m_layout.post(new Runnable() {
                            @Override
                            public void run() {
                                m_doc = new Document();
                                int ret = m_doc.OpenStream(m_http_stream, pdf_pswd);
                                ProcessOpenResult(ret);
                            }
                        });
                    }
                }.start();
            } else if (!TextUtils.isEmpty(pdf_asset)) {
                m_asset_stream = new RDAssetStream();
                m_asset_stream.open(getAssets(), pdf_asset);
                m_doc = new Document();
                int ret = m_doc.OpenStream(m_asset_stream, pdf_pswd);

                ProcessOpenResult(ret);
            } else if (!TextUtils.isEmpty(pdf_path)) {
                m_doc = new Document();
                int ret = m_doc.Open(pdf_path, pdf_pswd);
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
        m_controller.onConfigChanged();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (m_doc == null)
            m_doc = m_view.PDFGetDoc();
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
            m_view.PDFOpen(m_doc, this);
            m_controller = new PDFViewController(m_layout, m_view);
            m_need_save_doc = true;
        }
        m_view.BundleRestorePos(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (m_controller == null || m_controller.OnBackPressed()) {
            if (m_controller.getFileState() == PDFViewController.MODIFIED_NOT_SAVED) {
                if (getIntent().getBooleanExtra("AUTOMATIC_SAVE", false)) {
                    m_controller.savePDF();
                    super.onBackPressed();
                } else {
                    TextView txtView = new TextView(this);
                    txtView.setText(R.string.save_msg);
                    new AlertDialog.Builder(this).setTitle(R.string.exiting).setView(
                            txtView).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_controller.savePDF();
                            PDFViewAct.super.onBackPressed();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PDFViewAct.super.onBackPressed();
                        }
                    }).show();
                }
            } else super.onBackPressed();
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onDestroy() {
        RadaeePluginCallback.getInstance().willCloseReader();

        final PDFViewController vctrl = m_controller;
        final Document doc = m_doc;
        final PDFLayoutView view = m_view;
        final RDAssetStream astr = m_asset_stream;
        final RDHttpStream hstr = m_http_stream;
        m_controller = null;
        m_doc = null;
        m_view = null;
        m_asset_stream = null;
        m_http_stream = null;
        if (view != null) view.PDFCloseOnUI();
        new Thread(){
            @Override
            public void run() {
                if (vctrl != null) vctrl.onDestroy();
                if (view != null) view.PDFClose();
                if (doc != null) doc.Close();
                if (astr != null) astr.close();
                if (hstr != null) hstr.close();
                Global.RemoveTmp();
                synchronized (PDFViewAct.this) { PDFViewAct.this.notify(); }
            }
        }.start();
        synchronized (this)
        {
            try { wait(1500); }
            catch(Exception ignored) { }
        }
        super.onDestroy();

        RadaeePluginCallback.getInstance().didCloseReader();
    }

    @Override
    public void OnPDFPageModified(int pageno) {
        if (m_controller != null) m_controller.onPageModified(pageno);
    }

    private int m_cur_page = 0;
    @Override
    public void OnPDFPageChanged(int pageno) {
        m_cur_page = pageno;
        if (m_controller != null)
            m_controller.OnPageChanged(pageno);
        RadaeePluginCallback.getInstance().didChangePage(pageno);
    }

    @Override
    public void OnPDFAnnotTapped(int pageno, Annotation annot) {
        if (m_controller != null)
            m_controller.OnAnnotTapped(pageno, annot);
        if (annot != null)
            RadaeePluginCallback.getInstance().onAnnotTapped(annot);
    }

    @Override
    public void OnPDFBlankTapped(int pageno) {
        if (m_controller != null)
            m_controller.OnBlankTapped();
        RadaeePluginCallback.getInstance().onBlankTapped(pageno);
    }

    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }

    @Override
    public void OnPDFSelectEnd(){
        LinearLayout layout = findViewById(R.id.text_select_menu);
        if(layout.getVisibility() == View.VISIBLE)
            layout.setVisibility(View.GONE);
    }

    @Override
    public void OnPDFTextSelected(String text, float x, float y) {
        LinearLayout layout = m_layout.findViewById(R.id.text_select_menu);
        //final RadioGroup rad_group = layout.findViewById(com.radaee.viewlib.R.id.rad_group);

        View.OnClickListener TextSelectionListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ret = true;
                int id = v.getId();
                if (id == R.id.icon_copy) {
                    Toast.makeText(PDFViewAct.this, getString(R.string.text_copy_hint) + text, Toast.LENGTH_SHORT).show();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Radaee", text);
                    clipboard.setPrimaryClip(clip);
                } else if (id == R.id.icon_highlight) {
                    ret = m_view.PDFSetSelMarkup(0);
                } else if (id == R.id.icon_underline) {
                    ret = m_view.PDFSetSelMarkup(1);
                } else if (id == R.id.icon_strikeout) {
                    ret = m_view.PDFSetSelMarkup(2);
                } else if (id == R.id.icon_squiggly) {
                    ret = m_view.PDFSetSelMarkup(4);
                } else if (id == R.id.icon_eraser) {
                    ret = m_view.PDFEraseSel();
                }
                if (!ret) {
                    Toast.makeText(PDFViewAct.this, getString(R.string.text_add_annot_failed_hint), Toast.LENGTH_SHORT).show();
                }
            }
        };

        layout.findViewById(R.id.icon_copy).setOnClickListener(TextSelectionListener);
        if (!m_doc.CanSave()) {
            layout.findViewById(R.id.icon_highlight).setVisibility(View.GONE);
            layout.findViewById(R.id.icon_underline).setVisibility(View.GONE);
            layout.findViewById(R.id.icon_strikeout).setVisibility(View.GONE);
            layout.findViewById(R.id.icon_squiggly).setVisibility(View.GONE);
            layout.findViewById(R.id.icon_eraser).setVisibility(View.GONE);
        } else {
            layout.findViewById(R.id.icon_highlight).setOnClickListener(TextSelectionListener);
            layout.findViewById(R.id.icon_underline).setOnClickListener(TextSelectionListener);
            layout.findViewById(R.id.icon_strikeout).setOnClickListener(TextSelectionListener);
            layout.findViewById(R.id.icon_squiggly).setOnClickListener(TextSelectionListener);
            layout.findViewById(R.id.icon_eraser).setOnClickListener(TextSelectionListener);
        }

        layout.setVisibility(View.VISIBLE);
        DisplayMetrics displayMetrics =  getResources().getDisplayMetrics();
        int height = layout.getHeight();
        int width = layout.getWidth();
        if(x + width > displayMetrics.widthPixels)
            x = displayMetrics.widthPixels - width - dp2px(10);
        layout.setX(x);
        float yPosition = y + dp2px(30);
        if (yPosition + height > displayMetrics.heightPixels)
            yPosition = displayMetrics.heightPixels - height - dp2px(30);
        layout.setY(yPosition);
    }

    @Override
    public void OnPDFOpenURI(String uri) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(uri);
            intent.setData(content_url);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(PDFViewAct.this, getString(R.string.todo_open_url) + uri, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void OnPDFOpenJS(String js) {
        Toast.makeText(PDFViewAct.this, R.string.todo_java_script, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnPDFOpenMovie(String path) {
        Toast.makeText(PDFViewAct.this, R.string.todo_play_movie, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnPDFOpenSound(int[] paras, String path) {
        Toast.makeText(PDFViewAct.this, R.string.todo_play_sound, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnPDFOpenAttachment(String path) {
        Toast.makeText(PDFViewAct.this, R.string.todo_attachment, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnPDFOpenRendition(String path) {
        Toast.makeText(PDFViewAct.this, "todo: play rendition", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnPDFOpen3D(String path) {
        Toast.makeText(PDFViewAct.this, R.string.todo_3d, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnPDFZoomStart() {
    }

    @Override
    public void OnPDFZoomEnd() {
    }

    @Override
    public boolean OnPDFDoubleTapped(float x, float y) {
        float mCurZoomLevel = m_view.PDFGetZoom();
        if (m_view.PDFGetScale() <= m_view.PDFGetMinScale())
            Global.g_zoom_step = 1;
        if ((mCurZoomLevel > Global.g_view_zoom_level && Global.g_zoom_step > 0) ||
                (mCurZoomLevel == 1 && Global.g_zoom_step < 0)) //reverse zoom step
            Global.g_zoom_step *= -1;

        m_view.PDFSetZoom((int) x, (int) y, m_view.PDFGetPos((int) x, (int) y), mCurZoomLevel + Global.g_zoom_step);
        RadaeePluginCallback.getInstance().onDoubleTapped(m_cur_page, x, y);
        return true;
    }

    @Override
    public void OnPDFLongPressed(float x, float y) {
        RadaeePluginCallback.getInstance().onLongPressed(m_cur_page, x, y);
    }

    private boolean show_progress = true;
    @Override
    public void OnPDFPageRendered(IPDFLayoutView.IVPage vpage) {
        if (!mDidShowReader) {
            RadaeePluginCallback.getInstance().didShowReader();
            mDidShowReader = true;
        }
        if (show_progress) {
            findViewById(R.id.progress).setVisibility(View.GONE);
            show_progress = false;
        }
    }

    @Override
    public void OnPDFSearchFinished(boolean found) {
        if (!mFindQuery.equals(m_controller.getFindQuery())) {
            mFindQuery = m_controller.getFindQuery();
            RadaeePluginCallback.getInstance().didSearchTerm(mFindQuery, found);
        }
    }

    @Override
    public void OnPDFPageDisplayed(Canvas canvas, IPDFLayoutView.IVPage vpage) {
    }

    /**
     * returns current rendered page.
     *
     * @return current rendered page, -1 otherwise
     */
    public int getCurrentPage() {
        if (m_view != null)
            return m_cur_page;
        return -1;
    }
}