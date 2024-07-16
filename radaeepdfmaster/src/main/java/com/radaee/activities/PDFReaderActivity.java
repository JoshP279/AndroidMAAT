package com.radaee.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.radaee.comm.Global;
import com.radaee.objects.RetrofitClient;
import com.radaee.pdf.Document;
import com.radaee.pdf.Page;
import com.radaee.pdfmaster.R;
import com.radaee.reader.PDFEditLayoutView;
import com.radaee.util.BookmarkHandler;
import com.radaee.util.CommonUtil;
import com.radaee.view.IPDFLayoutView;

public class PDFReaderActivity extends AppCompatActivity implements IPDFLayoutView.PDFLayoutListener {
    public enum Status {
        none,
        ink,
        note,
        text_box,
    }
    private Status mStatus = Status.none;
    static public Document submission;
    static public Document memo;
    private boolean m_modified = false;
    private boolean need_save_doc = false;
    private Document sPDFDoc = null;
    private Document mPDFDoc = null;
    private String sFilePath;
    private PDFEditLayoutView sPDFView;
    private PDFEditLayoutView mPDFView;
    private ConstraintLayout rootLayout;
    private View divider;
    private int m_cur_page = 0;
    private ImageButton inkButton;
    private boolean inked = false;
    private ImageButton undoButton;
    private ImageButton redoButton;
    private ImageButton saveButton;
    private ImageButton bookmarkButton;
    private ImageButton commentButton;
    private TextView studentNum;
    private String studentNumber;
    private int submissionID;
    private int assessmentID;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.Init(this);
        init();
        setupDivider();
        Intent intent = getIntent();
        if (intent != null){
            studentNumber = intent.getStringExtra("studentNum");
            studentNum.setText(studentNumber);
            submissionID = intent.getIntExtra("submissionID", 1);
            assessmentID = intent.getIntExtra("assessmentID", 1);
        }
        if (submission != null) {
            mPDFDoc = memo;
            sPDFDoc = submission;
            memo = null;
            submission = null;
            sPDFView.PDFOpen(sPDFDoc, PDFReaderActivity.this);
            sFilePath = sPDFDoc.getDocPath();
            mPDFView.PDFOpen(mPDFDoc, PDFReaderActivity.this);
        }
    }
    private void init(){
        setContentView(R.layout.activity_pdf_reader);
        rootLayout = findViewById(R.id.root);
        divider = findViewById(R.id.divider);
        sPDFView = findViewById(R.id.pdf_view);
        mPDFView = findViewById(R.id.memo_view);
        studentNum = findViewById(R.id.assessStudentNumTextView);
        inkButton = findViewById(R.id.inkButton);
        undoButton = findViewById(R.id.undoButton);
        redoButton = findViewById(R.id.redoButton);
        saveButton = findViewById(R.id.saveButton);
        commentButton = findViewById(R.id.commentButton);
        bookmarkButton = findViewById(R.id.bookMarkButton);
        inkButton.setOnClickListener(inkClickListener);
        undoButton.setOnClickListener(undoClickListener);
        redoButton.setOnClickListener(redoClickListener);
        saveButton.setOnClickListener(saveClickListener);
        bookmarkButton.setOnClickListener(bookmarkClickListener);
        commentButton.setOnClickListener(commentClickListener);
    }
    private final View.OnClickListener inkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!inked){
                sPDFView.PDFSetInk(0);
            }else{
                sPDFView.PDFSetInk(1);
            }
            mStatus = Status.ink;
            m_modified = true;
            inked = !inked;
        }
    };
    private final View.OnClickListener undoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (sPDFView.PDFCanUndo()){
                sPDFView.PDFUndo();
            }else{
                Toast.makeText(getApplicationContext(),R.string.no_more_undo, Toast.LENGTH_LONG).show();
            }
        }
    };
    private final View.OnClickListener redoClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (sPDFView.PDFCanRedo()) {
                sPDFView.PDFRedo();
            }else{
                Toast.makeText(getApplicationContext(),R.string.no_more_redo, Toast.LENGTH_LONG).show();
            }
        }
    };
    private final View.OnClickListener saveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (sPDFView.PDFCanSave()){
                sPDFView.PDFSetInk(1);
                sPDFView.PDFSave();
                m_modified = false;
                Toast.makeText(getApplicationContext(),R.string.saved_message, Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(),R.string.fail_saved_message, Toast.LENGTH_LONG).show();
            }
        }
    };
    private final View.OnClickListener bookmarkClickListener = v -> {
        int page = m_cur_page;
        BookmarkHandler.BookmarkStatus status = BookmarkHandler.addToBookmarks(sFilePath, page, getString(R.string.bookmark_label, m_cur_page + 1));
        AlertDialog.Builder builder = new AlertDialog.Builder(PDFReaderActivity.this);
        builder.setTitle(R.string.notification_label);
        if (status == BookmarkHandler.BookmarkStatus.SUCCESS) {
            builder.setMessage(R.string.message_add_bookmark_success_label);
        } else if (status == BookmarkHandler.BookmarkStatus.ALREADY_ADDED) {
            builder.setMessage(R.string.message_bookmark_exist_label);
        } else {
            builder.setMessage(R.string.message_bookmark_error_label);
        }
        builder.setPositiveButton(R.string.button_ok_label, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    };
    private final View.OnClickListener commentClickListener = v -> {
        sPDFView.PDFSetFieldEditbox(0);
    };

//    private final View.OnClickListener commentsClickListener = v -> {
//        if (!PDFEditView.inkHashMap.isEmpty()) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
//            builder.setTitle("Comments");
//            ArrayList<String> commentsStringList = new ArrayList<>();
//            for (Ink comment :PDFEditView.inkHashMap.values()) {
//                commentsStringList.add(comment.toString());
//            }
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, commentsStringList);
//            builder.setAdapter(adapter, (dialog, which) -> {
//                Ink selectedComment = PDFEditView.inkHashMap.get(which);
//                sPDFView.PDFSetAnnot(selectedComment, m_cur_page);
//                Toast.makeText(v.getContext(), "Selected: " + selectedComment.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
//            });
//            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
//            AlertDialog alertDialog = builder.create();
//            alertDialog.show();
//        } else {
//            Toast.makeText(v.getContext(), "No comments available", Toast.LENGTH_SHORT).show();
//        }
//    };
    @Override
    public void OnPDFPageModified(int pageno) {
        m_modified = true;
    }

    @Override
    public void OnPDFPageChanged(int pageno) {
        m_cur_page = pageno;
    }
    @Override
    public void OnPDFAnnotTapped(int pno, Page.Annotation annot) {
        if (pno < 0 && annot == null)
        {
            mStatus = Status.none;
        }
    }
    @Override
    public void OnPDFBlankTapped(int pageno) {}
    @Override
    public void OnPDFSelectEnd(){}
    @Override
    public void OnPDFTextSelected(String text, float x, float y) {}
    @Override
    public void OnPDFOpenURI(String uri) {}
    @Override
    public void OnPDFOpenJS(String js) {}
    @Override
    public void OnPDFOpenMovie(String path) {}
    @Override
    public void OnPDFOpenSound(int[] paras, String path) {}
    @Override
    public void OnPDFOpenAttachment(String path) {}
    @Override
    public void OnPDFOpenRendition(String path) {}
    @Override
    public void OnPDFOpen3D(String path) {}
    @Override
    public void OnPDFZoomStart() {}
    @Override
    public void OnPDFZoomEnd() {}
    @Override
    public boolean OnPDFDoubleTapped(float x, float y) {return false;}
    @Override
    public void OnPDFLongPressed(float x, float y) {}
    @Override
    public void OnPDFSearchFinished(boolean found) {}
    @Override
    public void OnPDFPageDisplayed(Canvas canvas, IPDFLayoutView.IVPage vpage){}
    @Override
    public void OnPDFPageRendered(IPDFLayoutView.IVPage vpage) {}
    public void image_annot_text_box(View v) {
        sPDFView.PDFSetEditbox(0);
        mStatus = Status.text_box;
    }
    public void image_annot_ink(View v) {
        sPDFView.PDFSetInk(0);
        mStatus = Status.ink;
    }
    public void image_annot_note(View v) {
        sPDFView.PDFSetNote(0);
        mStatus = Status.note;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mPDFDoc == null)
            mPDFDoc = sPDFView.PDFGetDoc();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        sPDFView.BundleSavePos(savedInstanceState);
        if (need_save_doc && mPDFDoc != null) {
            Document.BundleSave(savedInstanceState, mPDFDoc);//save Document object
            mPDFDoc = null;
        }
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mPDFDoc == null) {
            mPDFDoc = Document.BundleRestore(savedInstanceState);
            sPDFView.PDFOpen(mPDFDoc, this);
            need_save_doc = true;
        }
        sPDFView.BundleRestorePos(savedInstanceState);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pdf_reader_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_vert:
                Global.g_view_mode = 0;
                sPDFView.PDFSetView(0);
                mPDFView.PDFSetView(0);
                break;
            case R.id.action_view_horz:
                Global.g_view_mode = 1;
                sPDFView.PDFSetView(1);
                mPDFView.PDFSetView(1);
                break;
            case R.id.action_view_page:
                Global.g_view_mode = 3;
                sPDFView.PDFSetView(3);
                mPDFView.PDFSetView(3);
                break;
            case R.id.action_view_dual:
                Global.g_view_mode = 6;
                sPDFView.PDFSetView(6);
                mPDFView.PDFSetView(6);
                break;
            case R.id.action_menu:
                CommonUtil.showBothPDFOutlines(sPDFView,mPDFView, this);
                break;
            case R.id.action_book_mark:
                BookmarkHandler.showBookmarks(this, sFilePath, pageno -> {sPDFView.PDFGotoPage(pageno);});
                break;
            case R.id.action_setInProgress:
                RetrofitClient.INSTANCE.updateSubmission(this,submissionID,assessmentID,studentNumber,"In Progress");
                break;
            case R.id.action_setMarked:
                RetrofitClient.INSTANCE.updateSubmission(this,submissionID,assessmentID,studentNumber,"Marked");
                break;
            case R.id.action_setUnmarked:
                RetrofitClient.INSTANCE.updateSubmission(this,submissionID,assessmentID,studentNumber,"Unmarked");
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupDivider() {
        divider.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        int newX = (int) event.getRawX();
                        int totalWidth = rootLayout.getWidth();
                        float memoPercent = (float) newX / totalWidth;
                        float submissionPercent = 1 - memoPercent;
                        if (memoPercent > 0.05 && memoPercent < 0.95) {
                            adjustWidths(memoPercent, submissionPercent);
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }
    private void adjustWidths(float memoPercent, float submissionPercent) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);
        constraintSet.constrainPercentWidth(R.id.pdf_view, submissionPercent);
        constraintSet.constrainPercentWidth(R.id.memo_view, memoPercent);
        constraintSet.applyTo(rootLayout);
    }
    @Override
    public void onBackPressed() {
        if (m_modified && mPDFDoc.CanSave()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.notification_label);
            builder.setMessage(R.string.notification_save_label);
            builder.setPositiveButton(R.string.button_positive_label, (dialog, which) -> {
                if (sPDFView.PDFCanSave())
                    sPDFView.PDFSave();
                dialog.dismiss();
                finish();
            });
            builder.setNegativeButton(R.string.button_negative_label, ((dialog, which) -> {
                dialog.dismiss();
                finish();
            }));
            builder.setNeutralButton(R.string.button_cancel_label, ((dialog, which) -> dialog.dismiss()));
            builder.create().show();
        } else
            super.onBackPressed();
    }
    @Override
    protected void onDestroy() {
        if (mPDFDoc != null) {
            sPDFView.PDFClose();
            mPDFDoc.Close();
            mPDFView.PDFClose();
            mPDFDoc = null;
        }
        Global.RemoveTmp();
        super.onDestroy();
    }
}