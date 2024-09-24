package com.radaee.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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

import com.radaee.annotui.UIAnnotMenu;
import com.radaee.comm.Global;
import com.radaee.dataclasses.SubmissionsResponse;
import com.radaee.objects.FileUtil;
import com.radaee.objects.RetrofitClient;
import com.radaee.objects.SharedPref;
import com.radaee.objects.SnackbarUtil;
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
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private long lastClickTime = 0;
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
    private ImageButton keyboardButton;
    private TextView studentNum;
    private ImageButton nextSubmissionButton;
    private ImageButton prevSubmissionButton;
    private TextView pdfHelper;
    private String studentNumber;
    private int submissionID;
    private int assessmentID;
    private String assessmentName;
    private String moduleCode;
    private String submissionFolderName;
    private int totalMarks;
    private String markingStyle;
    private boolean isButtonPressed = false;
    private boolean isSpenButtonPressed = false;

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
        pdfHelper = findViewById(R.id.pdfHelper);
        pdfHelper.setOnClickListener(v -> displayHelperDialog());

//        commentButton = findViewById(R.id.commentButton);
//        bookmarkButton = findViewById(R.id.bookMarkButton);
        keyboardButton = findViewById(R.id.typedAnnotButton);
        prevSubmissionButton = findViewById(R.id.btnPrevSubmission);
        nextSubmissionButton = findViewById(R.id.btnNextSubmission);
        nextSubmissionButton.setOnClickListener(submissionClickListener);
        prevSubmissionButton.setOnClickListener(submissionClickListener);
        keyboardButton.setOnClickListener(textClickListener);
        updatePrevAndNextButtons();
        inkButton.setOnClickListener(inkClickListener);
        undoButton.setOnClickListener(undoClickListener);
        redoButton.setOnClickListener(redoClickListener);
        saveButton.setOnClickListener(saveClickListener);
//        bookmarkButton.setOnClickListener(bookmarkClickListener);
//        commentButton.setOnClickListener(commentsClickListener);
        prevSubmissionButton.setEnabled(currentPos == 0);
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
            submissionFolderName = intent.getStringExtra("submissionFolderName");
            totalMarks = intent.getIntExtra("totalMarks", 1);
            markingStyle = SharedPref.INSTANCE.getString(this,"marking_style", getString(R.string.marking_style1));
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
        InitLongClickListeners();
        updatePrevAndNextButtons();
        UpdateImageButtons();
    }

    private final View.OnClickListener inkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!inked){
                inkButton.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                sPDFView.PDFSetInk(0);
            }else{
                inkButton.clearColorFilter();
                sPDFView.PDFSetInk(1);
            }
            UpdateImageButtons();
            mStatus = Status.ink;
            m_modified = true;
            inked = !inked;
        }
    };
    private void displayHelperDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PDFReaderActivity.this);
        builder.setTitle(R.string.helperHeading);
        builder.setMessage(R.string.pdfhelper_message);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void UpdateImageButtons() {
        if (sPDFView.PDFCanUndo()){
            undoButton.setAlpha(1f);
        }else{
            undoButton.setAlpha(0.2f);
        }
        if (sPDFView.PDFCanRedo()){
            redoButton.setAlpha(1f);
        }else{
            redoButton.setAlpha(0.2f);
        }
    }
    private void InitLongClickListeners() {
        undoButton.setOnLongClickListener(v -> {
            showAlertDialog(getString(R.string.undo_explainer));
            return true;
        });

        redoButton.setOnLongClickListener(v -> {
            showAlertDialog(getString(R.string.redo_explainer));
            return true;
        });

        saveButton.setOnLongClickListener(v -> {
            showAlertDialog(getString(R.string.save_explainer));
            return true;
        });

        inkButton.setOnLongClickListener(v -> {
            showAlertDialog(getString(R.string.ink_explainer));
            return true;
        });

        keyboardButton.setOnLongClickListener(v -> {
            showAlertDialog(getString(R.string.text_explainer));
            return true;
        });
    }

    private void showAlertDialog(String message) {
        new AlertDialog.Builder(PDFReaderActivity.this)
                .setTitle(getString(R.string.button_explainer))
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()) // Action on "OK"
                .show();
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
                OnPDFBlankTapped(m_cur_page); //if an annotation is in focus, remove focus and open next PDF
                currentPos = nextPos;
                updatePrevAndNextButtons();
                SubmissionsResponse submission = filteredSubmissions.get(currentPos);
                openSubmission(submission);
            }
        }
    };

    private void updatePrevAndNextButtons(){
        if (currentPos > 0 && currentPos < filteredSubmissions.size()){
            prevSubmissionButton.setEnabled(true);
            prevSubmissionButton.setAlpha(1f);
        }else{
            prevSubmissionButton.setEnabled(false);
            prevSubmissionButton.setAlpha(0.5f);
        }

        if (currentPos >= 0 && currentPos <filteredSubmissions.size() - 1){
            nextSubmissionButton.setEnabled(true);
            nextSubmissionButton.setAlpha(1f);
        }
        else{
            nextSubmissionButton.setEnabled(false);
            nextSubmissionButton.setAlpha(0.5f);
        }
    }
    /**
     * openSubmission is used to open the submission and memo for the current student.
     * @param submission - the current submission with relevant information
     * The submission is opened by checking if the submission and memo PDFs exist locally.
     */
    private void openSubmission(SubmissionsResponse submission) {
        mStatus = Status.none;
        studentNumber = submission.getStudentNumber();
        submissionID = submission.getSubmissionID();
        assessmentID = submission.getAssessmentID();
        submissionFolderName = submission.getSubmissionFolderName();
        studentNum.setText(studentNumber);
        String folderName = assessmentID + "_" + moduleCode + "_" + assessmentName;
        File submissionFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName);
        File memoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName);
        String memoName = "memo_" + assessmentID + ".pdf";
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
            RetrofitClient.INSTANCE.downloadSubmissionPDF(this, findViewById(android.R.id.content), submission.getSubmissionID(), submissionName, folderName, true, path -> {
                if (path != null) {
                    initPDFReader(path, mFile.getPath());
                } else {
                    SnackbarUtil.INSTANCE.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_fail_download), this);
                }
                return null;
            });
        } else if (FileUtil.INSTANCE.checkSubmissionExists(submissionFile,submissionName, submission.getStudentNumber()) && !FileUtil.INSTANCE.checkMemoExists(memoFile, assessmentID)) {
            RetrofitClient.INSTANCE.downloadMemoPDF(this, findViewById(android.R.id.content), assessmentID, folderName, true, path -> {
                if (path != null) {
                    initPDFReader(sFile.getPath(), path);
                } else {
                    SnackbarUtil.INSTANCE.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_fail_download), this);
                }
                return null;
            });
        } else {
            RetrofitClient.INSTANCE.downloadSubmissionPDF(this, findViewById(android.R.id.content), submission.getSubmissionID(), submissionName, folderName, true, sPath -> {
                if (sPath != null) {
                    RetrofitClient.INSTANCE.downloadMemoPDF(this,findViewById(android.R.id.content), assessmentID, folderName, true,  mPath -> {
                        if (mPath != null) {
                            initPDFReader(sPath, mPath);
                        } else {
                            SnackbarUtil.INSTANCE.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_fail_download), this);
                        }
                        return null;
                    });
                } else {
                    SnackbarUtil.INSTANCE.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_fail_download), this);
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
//        mPDFDoc.Close();\
        OnPDFAnnotTapped(-1, null);
        if (sPDFDoc.GetPage(m_cur_page) != null) {
            sPDFDoc.GetPage(m_cur_page).Close();
        }
        sPDFDoc.Close();
        sPDFDoc = new Document();
//        mPDFDoc = new Document();
        if (!sPath.contains(".pdf")) {
            sPath = sPath + ".pdf";
        }
        int err1 = sPDFDoc.Open(sPath, null);
//        int err2 = mPDFDoc.Open(mPath, null);
        if (err1 == 0) {
            sPDFView.PDFOpen(sPDFDoc, PDFReaderActivity.this);
            if (m_cur_page >= 0 && m_cur_page <= sPDFView.PDFGetDoc().GetPageCount()) {
                sPDFView.PDFGotoPage(m_cur_page);
            }
            sFilePath = sPDFDoc.getDocPath();
//            mPDFView.PDFOpen(mPDFDoc, PDFReaderActivity.this);
//            mFilePath = mPDFDoc.getDocPath();
        } else {
            SnackbarUtil.INSTANCE.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_fail_open), this);
        }
    }
    /**
     * inClickListener is the OnClickListener for the inkButton.
     * When the inkButton is clicked, the ink annotation mode is toggled.
     * PDFSetInk(0) is used to annotate the PDF with ink.
     * PDFSetInk(0) is used to save the annotation to PDF (and go back to scrolling)
     */


    /**
     * undoClickListener is the OnClickListener for the undoButton.
     * When the undoButton is clicked, the last annotation is undone (if possible).
     */
    private final View.OnClickListener undoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (m_modified){
                sPDFView.PDFSetInk(1);
                sPDFView.PDFSetEditbox(1);
                sPDFView.PDFSetInk(0);
                UpdateImageButtons();
            }
            if (sPDFView.PDFCanUndo()){
                sPDFView.PDFUndo();
            }else{
                SnackbarUtil.INSTANCE.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.no_more_undo), PDFReaderActivity.this);
            }
            UpdateImageButtons();
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
                SnackbarUtil.INSTANCE.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.no_more_redo), PDFReaderActivity.this);
            }
            UpdateImageButtons();
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
                SnackbarUtil.INSTANCE.showSuccessSnackBar(findViewById(android.R.id.content), getString(R.string.saved_message), PDFReaderActivity.this);
            }else{
                saveButton.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                SnackbarUtil.INSTANCE.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.fail_saved_message), PDFReaderActivity.this);
            }
            UpdateImageButtons();
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
     * textClickListener is the OnClickListener for the textButton.
     * Allows a user to input typed annotations, rather than stylus input
     */
    private final View.OnClickListener textClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!texted){
                keyboardButton.setAlpha(0.2f);
                sPDFView.PDFSetEditbox(0);
            }else{
                sPDFView.PDFSetEditbox(1);
            }
            mStatus = Status.text_box;
            m_modified = true;
            texted = !texted;
        }
    };
    /**
     * commentsClickListener is the OnClickListener for the commentsButton
     * Allows a user to reuse common annotations across assessments
     */
    private final View.OnClickListener commentsClickListener = v -> {
        if (!UIAnnotMenu.annotHashSet.isEmpty()) {
            // Create a list to hold the annotations as strings
            List<String> annotationsList = new ArrayList<>();
            List<UIAnnotMenu.IMemnuCallback> callbackList = new ArrayList<>(UIAnnotMenu.annotHashSet);

            // Iterate over the HashSet and format the annotations
            for (UIAnnotMenu.IMemnuCallback callback : callbackList) {
                // Assuming each callback has a unique identifier or description
                String annotationInfo = callback.toString(); // Replace with appropriate method to get annotation details
                annotationsList.add(annotationInfo);
            }

            // Convert the list to an array
            String[] annotationsArray = annotationsList.toArray(new String[0]);

            // Build and display the AlertDialog
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Annotations")
                    .setItems(annotationsArray, (dialog, which) -> {
                        UIAnnotMenu.IMemnuCallback selectedCallback = callbackList.get(which);
                        sPDFView.PDFSetAnnot(null, m_cur_page);
                    })
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            // Show a message if the HashSet is empty
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Annotations")
                    .setMessage("No annotations found.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    };



    /**
     * All the below methods are overridden from the IPDFLayoutView.PDFLayoutListener interface.
     * Many are not used in this activity, but are overridden to avoid compilation errors.
     */
    @Override
    public void OnPDFPageModified(int pageno) {
        m_modified = true;
        UpdateImageButtons();
    }

    @Override
    public void OnPDFPageChanged(int pageno) {
        m_cur_page = pageno;
        keyboardButton.setAlpha(1f);
        UpdateImageButtons();
    }
    @Override
    public void OnPDFAnnotTapped(int pno, Page.Annotation annot) {
        Log.e("annot", annot==null?"null":"not null");
        Log.e("annot", pno+"");
        if (pno < 0 && annot == null) {
            mStatus = Status.none;
            keyboardButton.setAlpha(1f);
            UpdateImageButtons();
        }
    }
    @Override
    public void OnPDFBlankTapped(int pageno) {
        if (mStatus != Status.none)
            return;
    }
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
    public void OnPDFLongPressed(float x, float y) {
        Toast.makeText(this, "deez", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void OnPDFSearchFinished(boolean found) {}
    @Override
    public void OnPDFPageDisplayed(Canvas canvas, IPDFLayoutView.IVPage vpage){}
    @Override
    public void OnPDFPageRendered(IPDFLayoutView.IVPage vpage) {}
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
//            case R.id.action_book_mark:
//                BookmarkHandler.showBookmarks(this, sFilePath, pageno -> {sPDFView.PDFGotoPage(pageno);});
//                break;
            case R.id.action_setInProgress:
                RetrofitClient.INSTANCE.updateSubmission(this,findViewById(android.R.id.content), submissionID,assessmentID,totalMarks,"In Progress", submissionFolderName, markingStyle);
                break;
            case R.id.action_setMarked:
                RetrofitClient.INSTANCE.updateSubmission(this,findViewById(android.R.id.content), submissionID,assessmentID,totalMarks,"Marked", submissionFolderName, markingStyle);
                break;
            case R.id.action_setUnmarked:
                RetrofitClient.INSTANCE.updateSubmission(this,findViewById(android.R.id.content), submissionID,assessmentID,totalMarks,"Unmarked", submissionFolderName, markingStyle);
            case android.R.id.home:
                if (handleBackPressed()) {
                    return true;
                }
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
                sPDFView.PDFSetInk(1);
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
        divider.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                sPDFView.PDFSetInk(1);
                adjustWidths(0.5f, 0.5f);
            }
            lastClickTime = clickTime;
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
        if (!handleBackPressed()) {
            super.onBackPressed();
        }
    }

    private boolean handleBackPressed() {
        if (m_modified && mPDFDoc.CanSave()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.notification_label);
            builder.setMessage(R.string.notification_save_label);
            builder.setPositiveButton(R.string.button_positive_label, (dialog, which) -> {
                if (sPDFView.PDFCanSave()) {
                    sPDFView.PDFSetInk(1);
                    sPDFView.PDFSetEditbox(1);
                    sPDFView.PDFSave();
                }
                finish();
            });
            builder.setNegativeButton(R.string.button_negative_label, (dialog, which) -> {
                dialog.dismiss();
                finish();
            });
            builder.setNeutralButton(R.string.button_cancel_label, (dialog, which) -> {
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        }
        return false;
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