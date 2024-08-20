package com.radaee.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.radaee.annotui.UIAnnotMenu;
import com.radaee.comm.Global;
import com.radaee.dataclasses.SubmissionsResponse;
import com.radaee.objects.FileUtil;
import com.radaee.objects.RetrofitClient;
import com.radaee.pdf.Document;
import com.radaee.pdf.Page;
import com.radaee.pdfmaster.R;
import com.radaee.reader.PDFEditLayoutView;
import com.radaee.util.BookmarkHandler;
import com.radaee.util.CommonUtil;
import com.radaee.view.IPDFLayoutView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * PDFReaderActivity displays both the submission and the memo side by side.
 * The user can annotate the submission and save the annotations.
 * This activity is in Java because the RadaeePDFSDK is written in Java (had issues converting to Kotlin).
 */
public class PDFReaderActivity extends AppCompatActivity implements IPDFLayoutView.PDFLayoutListener {
    /**
     * Status enum to keep track of the current annotation type.
     */
    public enum Status {
        none,
        ink,
        note,
        text_box,
    }
    private Status mStatus = Status.none;
    /**
     * the below two static variables are used to pass the submission and memo documents from the SubmissionActivity to the PDFReaderActivity.
     */
    static public Document submission;
    static public Document memo;
    static public List<SubmissionsResponse> filteredSubmissions;
    static public int currentPos;
    private boolean m_modified = false;
    private boolean need_save_doc = false;
    private Document sPDFDoc = null;
    private Document mPDFDoc = null;
    private String mFilePath;
    private String sFilePath;
    private PDFEditLayoutView sPDFView;
    private PDFEditLayoutView mPDFView;
    private ConstraintLayout rootLayout;
    private View divider;
    private int m_cur_page = 0;
    private ImageButton inkButton;
    private boolean inked = false;
    private boolean texted = false;
    private ImageButton undoButton;
    private ImageButton redoButton;
    private ImageButton saveButton;
    private ImageButton bookmarkButton;
    private ImageButton commentButton;
    private TextView studentNum;
    private ImageButton nextSubmissionButton;
    private ImageButton prevSubmissionButton;
    private String studentNumber;
    private int submissionID;
    private int assessmentID;
    private String assessmentName;
    private String moduleCode;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /// Initialize the Global class for the RadaeePDFSDK. This is required to use the SDK.
        Global.Init(this);
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
        prevSubmissionButton = findViewById(R.id.btnPrevSubmission);
        nextSubmissionButton = findViewById(R.id.btnNextSubmission);
        nextSubmissionButton.setOnClickListener(submissionClickListener);
        prevSubmissionButton.setOnClickListener(submissionClickListener);
        prevSubmissionButton.setEnabled(currentPos>0 && currentPos < filteredSubmissions.size());
        nextSubmissionButton.setEnabled(currentPos>=0 && currentPos < filteredSubmissions.size()-1);
        inkButton.setOnClickListener(inkClickListener);
        undoButton.setOnClickListener(undoClickListener);
        redoButton.setOnClickListener(redoClickListener);
        saveButton.setOnClickListener(saveClickListener);
        bookmarkButton.setOnClickListener(bookmarkClickListener);
        commentButton.setOnClickListener(commentsClickListener);
        Log.e("check", currentPos + " " );
        prevSubmissionButton.setVisibility(currentPos == 0 ? View.INVISIBLE : View.VISIBLE);
        nextSubmissionButton.setVisibility(currentPos == filteredSubmissions.size() - 1 ? View.INVISIBLE : View.VISIBLE);
        setupDivider();
        Intent intent = getIntent();
        // Get the student number, submission ID and assessment ID from the intent.
        if (intent != null){
            studentNumber = intent.getStringExtra("studentNum");
            studentNum.setText(studentNumber);
            submissionID = intent.getIntExtra("submissionID", 1);
            assessmentID = intent.getIntExtra("assessmentID", 1);
            assessmentName = intent.getStringExtra("assessmentName");
            moduleCode = intent.getStringExtra("moduleCode");
        }
        if (submission != null && memo != null) {
            mPDFDoc = memo;
            sPDFDoc = submission;
            memo = null;
            submission = null;
            sPDFView.PDFOpen(sPDFDoc, PDFReaderActivity.this);
            sFilePath = sPDFDoc.getDocPath();
            mPDFView.PDFOpen(mPDFDoc, PDFReaderActivity.this);
            mFilePath = mPDFDoc.getDocPath();
        }
    }

    /**
     * submissionClickListener is the OnClickListener for the nextSubmissionButton and prevSubmissionButton.
     * This allows for markers to easily switch between submissions without having to close the activity and go back to the SubmissionActivity.
     * This is applied to both of the buttons, @id/btnNextSubmission and @id/btnPrevSubmission, to allow for easy disabling of the buttons when the user is at the first or last submission.
     */
    private final View.OnClickListener submissionClickListener = v -> {
        int nextPos = -1;
        if (currentPos >= 0 && currentPos < filteredSubmissions.size()) {
            if (currentPos < filteredSubmissions.size())
                nextPos = switch (v.getId()) {
                    case R.id.btnNextSubmission -> currentPos + 1;
                    case R.id.btnPrevSubmission -> currentPos - 1;
                    default -> nextPos;
                };
            if (nextPos != -1) {
                currentPos = nextPos;
                prevSubmissionButton.setEnabled(currentPos > 0 && currentPos < filteredSubmissions.size());
                prevSubmissionButton.setVisibility(currentPos == 0 ? View.INVISIBLE : View.VISIBLE);
                nextSubmissionButton.setEnabled(currentPos >= 0 && currentPos <filteredSubmissions.size() - 1);
                nextSubmissionButton.setVisibility(currentPos == filteredSubmissions.size() - 1 ? View.INVISIBLE : View.VISIBLE);
                SubmissionsResponse submission = filteredSubmissions.get(currentPos);
                openSubmission(submission);
            }
        }
    };
    /**
     * openSubmission is used to open the submission and memo for the current student.
     * @param submission - the current submission with relevant information
     * The submission is opened by checking if the submission and memo PDFs exist locally.
     */
    private void openSubmission(SubmissionsResponse submission) {
        studentNumber = submission.getStudentNumber();
        submissionID = submission.getSubmissionID();
        assessmentID = submission.getAssessmentID();
        studentNum.setText(studentNumber);
        String folderName = assessmentID + "_" + moduleCode + "_" + assessmentName;
        File submissionFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName);
        File memoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName);
        String memoName = "memo_" + assessmentID + ".pdf";
        Log.e("path",submissionID + "_" + submission.getSubmissionFolderName() );
        File sFile = new File(submissionFile, submissionID + "_" + submission.getSubmissionFolderName());
        File mFile = new File(memoFile, memoName);
        checkAndDownloadPDFs(submissionFile, memoFile, folderName, sFile, mFile, submission);
    }

    /**
     * Similarly to the SubmissionActivity, the submission and memo PDFs are downloaded if they do not exist locally.
     * If either do not exist, they are downloaded asynchronously and opened when the download is complete.
     * @param submissionFile - the submission file
     * @param memoFile - the memo file (theoretically, this should never change as it is the same for all submissions. As a sanity check, I still check if it exists to ensure I can proceed)
     * @param folderName - the folder name for the assessment
     * @param sFile - the submission file
     * @param mFile - the memo file
     * @param submission - the current submission with relevant information
     */
    private void checkAndDownloadPDFs(File submissionFile, File memoFile, String folderName, File sFile, File mFile, SubmissionsResponse submission) {
        String submissionName = submission.getSubmissionID() + "_" + submission.getSubmissionFolderName();
        if (FileUtil.INSTANCE.checkSubmissionExists(submissionFile, submissionName, submission.getStudentNumber()) && FileUtil.INSTANCE.checkMemoExists(memoFile, assessmentID)) {
            initPDFReader(sFile.getPath(), mFile.getPath());
        } else if (!FileUtil.INSTANCE.checkSubmissionExists(submissionFile, submissionName,submission.getStudentNumber()) && FileUtil.INSTANCE.checkMemoExists(memoFile, assessmentID)) {
            RetrofitClient.INSTANCE.downloadSubmissionPDF(this, submission.getSubmissionID(), submissionName, folderName, path -> {
                if (path != null) {
                    initPDFReader(path, mFile.getPath());
                } else {
                    Toast.makeText(getApplicationContext(), R.string.pdf_fail_download, Toast.LENGTH_SHORT).show();
                }
                return null;
            });
        } else if (FileUtil.INSTANCE.checkSubmissionExists(submissionFile,submissionName, submission.getStudentNumber()) && !FileUtil.INSTANCE.checkMemoExists(memoFile, assessmentID)) {
            RetrofitClient.INSTANCE.downloadMemoPDF(this, assessmentID, folderName, path -> {
                if (path != null) {
                    initPDFReader(sFile.getPath(), path);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.pdf_fail_download, Toast.LENGTH_SHORT).show();
                }
                return null;
            });
        } else {
            RetrofitClient.INSTANCE.downloadSubmissionPDF(this, submission.getSubmissionID(), submissionName, folderName, sPath -> {
                if (sPath != null) {
                    RetrofitClient.INSTANCE.downloadMemoPDF(this, assessmentID, folderName, mPath -> {
                        if (mPath != null) {
                            initPDFReader(sPath, mPath);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.pdf_fail_download, Toast.LENGTH_SHORT).show();
                        }
                        return null;
                    });
                } else {
                    Toast.makeText(getApplicationContext(), R.string.pdf_fail_download, Toast.LENGTH_SHORT).show();
                }
                return null;
            });
        }
    }

    /**
     * initPDFReader is used to initialize the PDF reader.
     * Document(s) are instantiated again and opened.
     * @param sPath - the path to the submission PDF
     * @param mPath - the path to the memo PDF
     */
    private void initPDFReader(String sPath, String mPath) {
//        mPDFDoc.Close();
        sPDFDoc.Close();
        sPDFDoc = new Document();
//        mPDFDoc = new Document();
        int err1 = sPDFDoc.Open(sPath, null);
//        int err2 = mPDFDoc.Open(mPath, null);
        if (err1 == 0) {
            sPDFView.PDFOpen(sPDFDoc, PDFReaderActivity.this);
            sPDFView.PDFGotoPage(0);
            sFilePath = sPDFDoc.getDocPath();
//            mPDFView.PDFOpen(mPDFDoc, PDFReaderActivity.this);
//            mFilePath = mPDFDoc.getDocPath();
        } else {
            Toast.makeText(getApplicationContext(), R.string.pdf_fail_open, Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * inClickListener is the OnClickListener for the inkButton.
     * When the inkButton is clicked, the ink annotation mode is toggled.
     * PDFSetInk(0) is used to annotate the PDF with ink.
     * PDFSetInk(0) is used to save the annotation to PDF (and go back to scrolling)
     */
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

    /**
     * undoClickListener is the OnClickListener for the undoButton.
     * When the undoButton is clicked, the last annotation is undone (if possible).
     */
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

    /**
     * redoClickListener is the OnClickListener for the redoButton.
     * When the redoButton is clicked, the last undone annotation is redone (if possible).
     */
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

    /**
     * saveClickListener is the OnClickListener for the saveButton.
     * When the saveButton is clicked, the annotations are saved to the PDF and locally to the device.
     * If the annotations are saved successfully, a success message is displayed.
     * If the annotations are not saved successfully, a failure message is displayed.
     */
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

    /**
     * bookmarkClickListener is the OnClickListener for the bookmarkButton.
     * When the bookmarkButton is clicked, the current page is bookmarked.
     * If the bookmark is added successfully, a success message is displayed.
     * If the bookmark already exists, a message is displayed.
     * If the bookmark is not added successfully, an error message is displayed.
     */
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

    /**
     * commentClickListener is the OnClickListener for the commentButton.
     */
    private final View.OnClickListener textClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!texted){
                sPDFView.PDFSetEditbox(0);
            }else{
                sPDFView.PDFSetEditbox(1);
            }
            mStatus = Status.text_box;
            m_modified = true;
            texted = !texted;
        }
    };
    private final View.OnClickListener commentsClickListener = v -> {
        if (!UIAnnotMenu.annotHashSet.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Comments");
            ArrayList<String> commentsStringList = new ArrayList<>();
            for (Page.Annotation annot :UIAnnotMenu.annotHashSet) {
                commentsStringList.add(annot.toString());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, commentsStringList);
            builder.setAdapter(adapter, (dialog, which) -> {
                List<Page.Annotation> annotList = new ArrayList<>(UIAnnotMenu.annotHashSet);
                Page.Annotation selectedAnnot = annotList.get(which);
//                sPDFView.PDFSetAnnot(selectedAnnot, m_cur_page);
            });
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            Toast.makeText(v.getContext(), "No comments available", Toast.LENGTH_SHORT).show();
        }
    };
    /**
     * All the below methods are overridden from the IPDFLayoutView.PDFLayoutListener interface.
     * Many are not used in this activity, but are overridden to avoid compilation errors.
     */
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

    /**
     * onResume is overridden to set the PDF documents if they are null.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mPDFDoc == null)
            mPDFDoc = sPDFView.PDFGetDoc();
        if (sPDFDoc == null) {
            sPDFDoc = mPDFView.PDFGetDoc();
        }
    }
    /**
     * onSaveInstanceState is overridden to save the state of the activity.
     * The state of the PDF documents and the position of the PDF views are saved.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        sPDFView.BundleSavePos(savedInstanceState);
        mPDFView.BundleSavePos(savedInstanceState);
        if (need_save_doc && mPDFDoc != null && sPDFDoc != null) {
            Document.BundleSaveBoth(savedInstanceState, mPDFDoc, sPDFDoc);
            mPDFDoc = null;
            sPDFDoc = null;
        }
    }
    /**
     * onRestoreInstanceState is overridden to restore the state of the activity.
     * The state of the PDF documents and the position of the PDF views are restored.
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mPDFDoc == null && sPDFDoc == null) {
            Document[] docs = Document.BundleRestoreBoth(savedInstanceState);
            if (docs != null) {
                mPDFDoc = docs[0];
                sPDFDoc = docs[1];
            }
            need_save_doc = true;
        }
        mPDFView.BundleRestorePos(savedInstanceState);
        sPDFView.BundleRestorePos(savedInstanceState);
    }

    /**
     * onCreateOptionsMenu is overridden to inflate the menu.
     * The menu contains options to change the view mode, show the menu, show the bookmarks, and set the submission status.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pdf_reader_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * onOptionsItemSelected is overridden to handle the menu item clicks.
     * The menu item clicks are handled based on the item ID.
     */
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
    /**
     * setupDivider is used to set up the divider between the submission and the memo.
     * The divider can be moved to adjust the width of the submission and the memo.
     */
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

    /**
     * adjustWidths is used to adjust the width of the submission and the memo.
     * The width of the submission and the memo are adjusted based on the percentage of the total width.
     */
    private void adjustWidths(float memoPercent, float submissionPercent) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);
        constraintSet.constrainPercentWidth(R.id.pdf_view, submissionPercent);
        constraintSet.constrainPercentWidth(R.id.memo_view, memoPercent);
        constraintSet.applyTo(rootLayout);
    }

    /**
     * onBackPressed is overridden to handle the back button press.
     * If the PDF has been modified and can be saved, the user is prompted to save the PDF.
     * If the PDF has been modified and cannot be saved, the user is prompted to save the PDF.
     * If the PDF has not been modified, the activity is finished.
     */
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

    /**
     * onDestroy is overridden to close the PDF documents and remove the temporary files.
     */
    @Override
    protected void onDestroy() {
        closePDFs();
        super.onDestroy();
    }

    /**
     * closePDFs is used to close the PDF documents.
     */
    private void closePDFs(){
        if (mPDFDoc != null && sPDFDoc != null) {
            sPDFView.PDFClose();
            mPDFDoc.Close();
            mPDFView.PDFClose();
            sPDFDoc.Close();
            mPDFDoc = null;
            sPDFDoc = null;
            Global.RemoveTmp();
        }
    }
}