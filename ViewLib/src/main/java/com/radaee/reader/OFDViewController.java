package com.radaee.reader;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.radaee.comm.Global;
import com.radaee.util.BookmarkHandler;
import com.radaee.view.IOFDLayoutView;
import com.radaee.viewlib.R;

import java.io.File;
import java.text.Bidi;
import java.util.Locale;

public class OFDViewController implements OnClickListener, SeekBar.OnSeekBarChangeListener {
    static public final int BAR_NONE = 0;
    static public final int BAR_CMD = 1;
    static public final int BAR_FIND = 3;
    private int m_bar_status = 0;
    private RelativeLayout m_parent;
    private IOFDLayoutView m_view;
    private RDTopBar m_bar_cmd;
    private RDTopBar m_bar_find;
    private RDBotBar m_bar_seek;
    private RDMenu m_menu_view;
    private RDMenu m_menu_more;
    private ImageView btn_view;
    private ImageView btn_find;
    private ImageView btn_select;
    private ImageView btn_outline;
    private ImageView btn_more;
    private View btn_add_bookmark;
    private View btn_show_bookmarks;
    private View btn_print;
    private ImageView btn_find_back;
    private ImageView btn_find_prev;
    private ImageView btn_find_next;
    private EditText edit_find;
    private SeekBar seek_page;
    private TextView lab_page;
    private View view_vert;
    private View view_horz;
    private View view_single;
    private View view_dual;
    private boolean m_set = false;

    public OFDViewController(RelativeLayout parent, IOFDLayoutView view) {
        m_parent = parent;
        m_view = view;
        m_bar_cmd = new RDTopBar(m_parent, R.layout.bar_cmd_ofd);
        m_bar_find = new RDTopBar(m_parent, R.layout.bar_find);
        m_menu_view = new RDMenu(m_parent, R.layout.pop_view, 160, 180);
        m_menu_more = new RDMenu(m_parent, R.layout.pop_more_ofd, 180, 180);
        RelativeLayout layout = (RelativeLayout) m_bar_cmd.BarGetView();
        btn_view = (ImageView) layout.findViewById(R.id.btn_view);
        btn_find = (ImageView) layout.findViewById(R.id.btn_find);
        btn_select = (ImageView) layout.findViewById(R.id.btn_select);
        btn_outline = (ImageView) layout.findViewById(R.id.btn_outline);
        btn_more = (ImageView) layout.findViewById(R.id.btn_more);
        layout = (RelativeLayout) m_bar_find.BarGetView();
        btn_find_back = (ImageView) layout.findViewById(R.id.btn_back);
        btn_find_prev = (ImageView) layout.findViewById(R.id.btn_left);
        btn_find_next = (ImageView) layout.findViewById(R.id.btn_right);
        edit_find = (EditText) layout.findViewById(R.id.txt_find);
        RelativeLayout layout1 = (RelativeLayout) m_menu_view.MenuGetView();
        view_vert = layout1.findViewById(R.id.view_vert);
        view_horz = layout1.findViewById(R.id.view_horz);
        view_single = layout1.findViewById(R.id.view_single);
        view_dual = layout1.findViewById(R.id.view_dual);
        LinearLayout moreLayout = (LinearLayout) m_menu_more.MenuGetView();
        btn_print = moreLayout.findViewById(R.id.print);
        btn_add_bookmark = moreLayout.findViewById(R.id.add_bookmark);
        btn_show_bookmarks = moreLayout.findViewById(R.id.show_bookmarks);

        btn_view.setOnClickListener(this);
        btn_find.setOnClickListener(this);
        btn_select.setOnClickListener(this);
        btn_outline.setOnClickListener(this);
        btn_more.setOnClickListener(this);
        btn_print.setOnClickListener(this);
        btn_add_bookmark.setOnClickListener(this);
        btn_show_bookmarks.setOnClickListener(this);
        btn_find_back.setOnClickListener(this);
        btn_find_prev.setOnClickListener(this);
        btn_find_next.setOnClickListener(this);
        view_vert.setOnClickListener(this);
        view_horz.setOnClickListener(this);
        view_single.setOnClickListener(this);
        view_dual.setOnClickListener(this);
        SetBtnEnabled(btn_print, true);

        //Nermeen, show/hide buttons based on license type
        if (Global.isLicenseActivated()) {
            if (Global.mLicenseType == 0) {
                btn_select.setVisibility(View.GONE);
            }
        } else {
            btn_find.setVisibility(View.GONE);
            btn_select.setVisibility(View.GONE);
        }
        BookmarkHandler.setDbPath(m_parent.getContext().getFilesDir() + File.separator + "Bookmarks.db");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            btn_print.setVisibility(View.GONE);

        m_bar_seek = new RDBotBar(m_parent, R.layout.bar_seek);
        layout = (RelativeLayout) m_bar_seek.BarGetView();
        lab_page = (TextView) layout.findViewById(R.id.lab_page);
        lab_page.setTextColor(-1);
        seek_page = (SeekBar) layout.findViewById(R.id.seek_page);
        seek_page.setOnSeekBarChangeListener(this);
        seek_page.setMax(m_view.OFDGetDoc().GetPageCount() - 1);

        if (edit_find != null) {
            edit_find.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        activateSearch(1);
                        return true;
                    }
                    return false;
                }
            });
            edit_find.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });
        }

        btn_more.setVisibility(View.VISIBLE);
        btn_print.setVisibility(View.VISIBLE);
        btn_find.setVisibility(View.VISIBLE);
        btn_select.setVisibility(View.VISIBLE);
        btn_view.setVisibility(View.VISIBLE);
        btn_outline.setVisibility(View.VISIBLE);
        btn_add_bookmark.setVisibility(View.VISIBLE);
        btn_show_bookmarks.setVisibility(View.VISIBLE);
    }

    private void SetBtnEnabled(View btn, boolean enable) {
        if (enable) {
            btn.setEnabled(true);
            btn.setBackgroundColor(0);
        } else {
            btn.setEnabled(false);
            btn.setBackgroundColor(0x80888888);
        }
    }

    private void SetBtnChecked(ImageView btn, boolean check) {
        if (check) {
            btn.setBackgroundColor(0x80FF8000);
        } else {
            btn.setBackgroundColor(0);
        }
        m_set = check;
    }

    public void OnBlankTapped() {
        switch (m_bar_status) {
            case BAR_NONE:
                m_bar_cmd.BarShow();
                m_bar_seek.BarShow();
                m_bar_status = BAR_CMD;
                break;
            case BAR_CMD:
                m_menu_view.MenuDismiss();
                m_menu_more.MenuDismiss();
                m_bar_cmd.BarHide();
                m_bar_seek.BarHide();
                m_bar_status = BAR_NONE;
                break;
            case BAR_FIND:
                m_bar_find.BarHide();
                m_bar_status = BAR_NONE;
                ((InputMethodManager) m_parent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(edit_find.getWindowToken(), 0);
                break;
        }
    }

    public void OnSelectEnd() {
        m_view.OFDSetSelect();
        SetBtnChecked(btn_select, false);
        SetBtnEnabled(btn_view, true);
        SetBtnEnabled(btn_find, true);
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        lab_page.setText(String.format(Locale.ENGLISH, "%d", arg0.getProgress() + 1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        m_view.OFDGotoPage(arg0.getProgress());
    }

    public void OnPageChanged(int pageno) {
        lab_page.setText(String.format(Locale.ENGLISH, "%d", pageno + 1));
        seek_page.setProgress(pageno);
    }

    public boolean OnBackPressed() {
        switch (m_bar_status) {
            case BAR_NONE:
                return true;
            case BAR_CMD:
                if (m_set) OnSelectEnd();
                m_menu_view.MenuDismiss();
                m_menu_more.MenuDismiss();
                m_bar_cmd.BarHide();

                m_bar_seek.BarHide();
                m_bar_status = BAR_NONE;
                return false;
            case BAR_FIND:
                m_bar_find.BarHide();
                m_bar_status = BAR_NONE;
                ((InputMethodManager) m_parent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(edit_find.getWindowToken(), 0);
                m_find_str = null;
                m_view.OFDFindEnd();
                return false;
            default:
                return false;
        }
    }

    public void onDestroy() {
    }

    private String m_find_str = null;

    @Override
    public void onClick(View arg0) {
        if (arg0 == btn_view)//popup view list
        {
            m_menu_view.MenuShow(0, m_bar_cmd.BarGetHeight());
        } else if (arg0 == btn_select) {
            if (m_set) OnSelectEnd();
            else {
                m_view.OFDSetSelect();
                SetBtnChecked(btn_select, true);
                SetBtnEnabled(btn_view, false);
                SetBtnEnabled(btn_find, false);
            }
        } else if (arg0 == btn_outline) {
        } else if (arg0 == btn_find) {
            m_bar_cmd.BarSwitch(m_bar_find);
            m_bar_seek.BarHide();
            m_bar_status = BAR_FIND;
        } else if (arg0 == btn_more) {
            m_menu_more.MenuShow(m_parent.getWidth() - m_menu_more.getWidth(), m_bar_cmd.BarGetHeight());
        } else if (arg0 == btn_print) {
            m_menu_more.MenuDismiss();
        } else if (arg0 == btn_find_prev) {
            activateSearch(-1);
        } else if (arg0 == btn_find_next) {
            activateSearch(1);
        } else if (arg0 == btn_find_back) {
            m_bar_find.BarSwitch(m_bar_cmd);

            m_bar_seek.BarShow();
            m_bar_status = BAR_CMD;
            ((InputMethodManager) m_parent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(edit_find.getWindowToken(), 0);
            m_find_str = null;
            m_view.OFDFindEnd();
        } else if (arg0 == view_vert) {
            m_view.OFDSetView(0);
            m_menu_view.MenuDismiss();
        } else if (arg0 == view_horz) {
            m_view.OFDSetView(1);
            m_menu_view.MenuDismiss();
        } else if (arg0 == view_single) {
            m_view.OFDSetView(3);
            m_menu_view.MenuDismiss();
        } else if (arg0 == view_dual) {
            m_view.OFDSetView(6);
            m_menu_view.MenuDismiss();
        }
    }

    private void activateSearch(int direction) {
        String val = edit_find.getText().toString();
        if (!TextUtils.isEmpty(val)) {
            ((InputMethodManager) m_parent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(edit_find.getWindowToken(), 0);
            val = bidiFormatCheck(val);
            if (val.equals(m_find_str))
                m_view.OFDFind(direction);
            else {
                m_find_str = val;
                m_view.OFDFindStart(val, false, true);
                m_view.OFDFind(direction);
            }
        }
    }

    private String bidiFormatCheck(String input) {
        if (Global.g_sel_rtol) { //selection is right to left, check case of mixed text
            Bidi bidi = new Bidi(input, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
            if (bidi.isMixed() || bidi.isLeftToRight()) { //we need to reverse mixed text
                String reversedVal = "", toBeReversed = "";
                int baseLevel = bidi.getBaseLevel();
                for (int i = 0; i < bidi.getLength(); i++) {
                    if (bidi.getLevelAt(i) != baseLevel || bidi.isLeftToRight()) { //mixed char, save it
                        toBeReversed += input.charAt(i);
                        if (i + 1 == bidi.getLength() ||
                                (i + 1 < bidi.getLength() && bidi.getLevelAt(i + 1) == baseLevel && !bidi.isLeftToRight())) { //reverse and append to reversed text
                            reversedVal += new StringBuilder(toBeReversed).reverse().toString();
                            toBeReversed = "";
                        }
                    } else
                        reversedVal += input.charAt(i);

                }
                input = reversedVal;
            }
        }
        return input;
    }

    public String getFindQuery() {
        return m_find_str;
    }
}