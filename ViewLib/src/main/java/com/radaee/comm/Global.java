package com.radaee.comm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.StrictMode;

import androidx.core.app.ActivityCompat;

import com.radaee.pdf.EditNode;
import com.radaee.viewlib.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * class for Global setting.
 *
 * @author Radaee
 * @version 1.2.32
 */
public class Global {
    //bind to app ID "com.radaee.pdfmaster", can avtive version before "20240918".
    //the version string can be retrieved by Global.getVersion().
    public static String mKey = "ED422803C06B1F11719F7E1FE54FB6355B6BB8850EEF0EEE73E49BEBD07960ACE8259774CC0E6E6AC8D02AEB37E76B1B";

    public static boolean Init(Context ctx) {
        //second para is license type
        //            0 means standard license.
        //            1 means professional license
        //            2 means premium license
        //3rd para is company name string(not package name)
        //4th para is mail
        //5th para is key string
        //the package name got by native C/C++ library, not by pass parameter.
        return Init((ContextWrapper) ctx, mKey);
    }

    /**
     * get version string from library.
     * @return version string, like: "20181101"
     */
    private static native String getVersion();
    private static native void setCMapsPath(String cmaps, String umaps);

    private static native boolean setCMYKICCPath(String path);

    private static native void fontfileListStart();

    private static native void fontfileListAdd(String font_file);

    private static native void fontfileListEnd();

    private static native void loadStdFont(int index, String path);

    private static native int recommandedRenderMode();

    public static native float sqrtf(float v);

    /**
     * map a face name to another name.<br/>
     * invoke after fontfileListEnd and before setDefaultFont.
     *
     * @param map_name mapping name
     * @param name     name in face-list, developer may list all face names by
     *                 getFaceCount and getFaceName
     * @return false if name is not in face-list, or map_name is empty.
     */
    private static native boolean fontfileMapping(String map_name, String name);

    private static native boolean setDefaultFont(String collection,
                                                 String fontname, boolean fixed);

    private static native boolean setAnnotFont(String fontname);

    private static native int getFaceCount();

    private static native String getFaceName(int index);

    public static int mLicenseType = 0;

    /**
     * active license
     *
     * @param context Context object
     * @param serial  serial number you got or buy.
     * @return 0: failed.<br/>
     * 1: standard license actived.
     * 2: professional license actived.
     * 3. premium license actived.
     */
    private static native int active(ContextWrapper context, String serial);

    /**
     * hide all annotations when render pages?
     *
     * @param hide true to hide, false to show.
     */
    private static native void hideAnnots(boolean hide);

    private static native void drawScroll(Bitmap bmp, long dib1, long dib2, int x, int y, int style, int back_side_clr);

    /**
     * not used for developer
     */
    public static void DrawScroll(Bitmap bmp, DIB dib1, DIB dib2, int x, int y, int style, int back_side_clr) {
        drawScroll(bmp, dib1.hand, dib2.hand, x, y, style, back_side_clr);
    }

    private static native void toDIBPoint(long matrix, float[] ppoint,
                                          float[] dpoint);

    private static native void toPDFPoint(long matrix, float[] dpoint,
                                          float[] ppoint);

    private static native void toDIBRect(long matrix, float[] prect,
                                         float[] drect);

    private static native void toPDFRect(long matrix, float[] drect,
                                         float[] prect);

    /**
     * set annotation transparency color.<br/>
     * default value: 0x200040FF
     *
     * @param color formated as 0xAARRGGBB
     */
    public static native void setAnnotTransparency(int color);

    public static boolean g_display_pageno_on_thumbnail = true;
    /**
     * color for ink annotation, change to fully opaque color can better support PDF/A1
     */
    public static int g_ink_color = 0xFFFF0000;//0x80404040;
    /**
     * width for ink lines.
     */
    public static float g_ink_width = 4;
    /**
     * selection color.
     */
    public static int g_sel_color = 0x400000C0;// selection color
    /**
     * Use selection icons (markers).
     */
    public static boolean g_use_sel_icons = true;
    /**
     * Annotation transparency color
     */
    public static int g_annot_transparency = 0x200040FF;
    /**
     * find primary color.
     */
    public static int g_find_primary_color = 0x400000FF;// find primary color
    /**
     * find secondary color.
     */
    public static int g_find_secondary_color = 0x40404040;// find secondary color
    /**
     * max zoom level; valid values: [2, 5]
     */
    public static float g_view_zoom_level = 3;
    public static float g_zoom_step = 1;
    /**
     * can't be neg value. 15 means 15x(2000%) zooming.
     * Starting from version 3.12beta2 (which introduces enhancements in transparency composing and color blending)
     * we recommend using 11 as the max level, higher levels will reduce performance
     */
    public static float g_layout_zoom_level = 11;
    /**
     * can't be neg value. 2 means 2x(200%) zooming. when zoom value greater than this, it clip pages as many small bitmaps.
     */
    public static float g_layout_zoom_clip = 2.5f;
    /**
     * fling distance: 0.5-2
     */
    public static float fling_dis = 1.0f;// 0.5-2
    /**
     * fling speed: 0.1-0.4
     */
    public static float fling_speed = 0.2f;// 0.1 - 0.4
    /**
     * default view:<br/>
     * 0:vertical<br/>
     * 1:horizontal<br/>
     * 2:scroll<br/>
     * 3:single<br/>
     * 4:SingleEx<br/>
     * 5:ReFlow<br/>
     * 6:2 page in landscape
     */
    public static int g_view_mode = 0;
    /**
     * render mode: 0:draft 1:normal 2:best
     */
    public static int g_render_quality = 2;
    /**
     * render as dark mode?
     */
    public static boolean g_dark_mode = false;

    /**
     * temp path, able after Init() invoked
     * debug_mode, show or remove "Avail Mem" watermark
     * save_thumb_in_cache, save pdf first page in cache storage
     */
    public static String tmp_path = null;
    /**
     * Thumb view background color
     */
    public static int g_thumbview_bg_color = 0x40CCCCCC;
    /**
     * Thumb grid view's background color
     */
    public static int thumbGridBgColor = 0xFFCCCCCC;
    /**
     * Thumb view height in dp, i.e. 100 = 100dp
     */
    public static int g_thumbview_height = 100;
    /**
     * Thumb grid view's element height in dp, i.e. 100 = 100dp
     */
    public static int thumbGridElementHeight = 150;
    /**
     * Thumb grid view's element gap (the vertical/horizontal spacing)
     */
    public static int thumbGridElementGap = 10;
    /**
     * Thumb grid view's render mode, 0:full screen 1:justify center
     */
    public static int thumbGridViewMode = 0;
    /**
     * Reader view background color
     */
    public static int g_readerview_bg_color = 0xFFCCCCCC;
    /**
     * navigation mode, 0:thumbnail view 1:seekbar view
     */
    public static int g_navigation_mode = 1;
    public static boolean debug_mode = false;
    public static boolean highlight_annotation = true;
    public static boolean save_thumb_in_cache = true;
    /**
     * enables/disables right to left navigation
     */
    public static boolean g_layout_rtol = false;
    /**
     * is text selection start from right to left in one line?
     */
    public static boolean g_sel_rtol = false;
    /**
     * enables or disable cache during rendering
     */
    public static boolean g_cache_enable = true;
    public static boolean trustAllHttpsHosts = false;
    public static int g_annot_highlight_clr = 0xFFFFFF00;//yellow
    public static int g_annot_underline_clr = 0xFF0000C0;//black blue
    public static int g_annot_strikeout_clr = 0xFFC00000;//black red
    public static int g_annot_squiggle_clr = 0xFF00C000;//black green
    public static String g_annot_def_author; //if valorized, will be used to set the annotation author while its creation
    public static boolean g_annot_lock = true;//if the annotation is locked, disable moving for annotation.
    public static boolean g_annot_readonly = true;//if the annotation is readonly, disable moving for annotation.

    public static boolean g_hand_signature = true;
    public static boolean sFitSignatureToField = true; //if true, the blank space will be trimmed from the signature bitmap
    public static String sSignPadDescr = "Sign Here";

    public static boolean g_exec_js = true;
    /**
     * Annot Rect params, change to fully opaque color can better support PDF/A1
     */
    public static float g_rect_annot_width = 3;
    public static int g_rect_annot_color = 0xFFFF0000;//0x80FF0000;
    public static int g_rect_annot_fill_color = 0xFF0000FF;//0x800000FF;

    /**
     * Annot Ellipse params, change to fully opaque color can better support PDF/A1
     */
    public static float g_oval_annot_width = 3;
    public static int g_oval_annot_color = 0xFFFF0000;//0x80FF0000;
    public static int g_oval_annot_fill_color = 0xFF0000FF;//0x800000FF;

    /**
     * Annot Line params, change to fully opaque color can better support PDF/A1
     */
    public static float g_line_annot_width = 3;
    public static int g_line_annot_style1 = 1;
    public static int g_line_annot_style2 = 0;
    public static int g_line_annot_color = 0xFFFF0000;//0x80FF0000;
    public static int g_line_annot_fill_color = 0xFF0000FF;//0x800000FF;

    public static boolean g_auto_scale = false;
    /**
     * Enables 'whole word' matching
     */
    public static boolean g_match_whole_word = false;
    /**
     * Enables case-sensitive search
     */
    public static boolean g_case_sensitive = false;

    static private void load_file(Resources res, int res_id, File save_file) {
        if (!save_file.exists()) {
            try {
                int read;
                byte[] buf = new byte[4096];
                InputStream src = res.openRawResource(res_id);
                FileOutputStream dst = new FileOutputStream(save_file);
                while ((read = src.read(buf)) > 0)
                    dst.write(buf, 0, read);
                dst.close();
                src.close();
                dst = null;
                src = null;
            } catch (Exception ignored) {
            }
        }
    }

    static private void load_std_font(Resources res, int res_id, int index, File dst) {
        load_file(res, res_id, dst);
        loadStdFont(index, dst.getPath());
    }

    static private void load_truetype_font(Resources res, int res_id, File dst) {
        load_file(res, res_id, dst);
        fontfileListAdd(dst.getPath());
    }

    static private boolean load_cmyk_icc(Resources res, int res_id, File dst) {
        load_file(res, res_id, dst);
        return setCMYKICCPath(dst.getPath());
    }

    static private void load_cmaps(Resources res, int res_cmap, File dst_cmap, int res_umap, File dst_umap) {
        load_file(res, res_cmap, dst_cmap);
        load_file(res, res_umap, dst_umap);
        setCMapsPath(dst_cmap.getPath(), dst_umap.getPath());
    }

    static private void save_font(String path, String out) {
        File file = new File(path);
        if (!file.exists()) return;
        try {
            FileInputStream fi = new FileInputStream(file);
            FileOutputStream fo = new FileOutputStream(out);
            byte[] data = new byte[4096];
            int read;
            while ((read = fi.read(data)) > 0)
                fo.write(data, 0, read);
            fo.close();
            fi.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static private boolean ms_init = false;

    /**
     * global initialize function. it load JNI library and write some data to memory.
     *
     * @param act    Context object must derived from CoontextWrapper, native get package name from this Activity, and then check package name.
     * @param serial
     * @return
     */
    public static boolean Init(ContextWrapper act, String serial) {
        if (ms_init) return true;
        if (act == null) return false;
        // load library
        System.loadLibrary("rddoc");
        // save resource to sand-box for application.
        File files = new File(act.getFilesDir(), "rdres");
        if (!files.exists())// not exist? make it!
            files.mkdir();
        Resources res = act.getResources();
        load_std_font(res, R.raw.rdf013, 13, new File(files, "rdf013"));
        load_cmyk_icc(res, R.raw.cmyk_rgb, new File(files, "cmyk_rgb"));
        load_cmaps(res, R.raw.cmaps, new File(files, "cmaps"), R.raw.umaps, new File(files, "umaps"));

        // create temporary dictionary, to save media or attachment data.
        File sdDir = act.getExternalFilesDir("");
        //File sdDir = Environment.getExternalStorageDirectory();
        File ftmp;
        if (sdDir != null)
            ftmp = new File(sdDir, "rdtmp");
        else
            ftmp = new File(act.getFilesDir(), "rdtmp");
        if (!ftmp.exists())// not exist? make it!
            ftmp.mkdir();
        tmp_path = ftmp.getPath();

        mLicenseType = active(act, serial);
        ms_init = (mLicenseType > 0);
        //this code need to be added, that support share on higher Android version.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }

        // active library, or WaterMark will displayed on each page.
        // boolean succeeded = activeStandard(act, "radaee",
        // "radaee_com@yahoo.cn", "HV8A19-WOT9YC-9ZOU9E-OQ31K2-FADG6Z-XEBCAO");
        // boolean succeeded = activeProfessional( act, "radaee",
        // "radaee_com@yahoo.cn", "Z5A7JV-5WQAJY-9ZOU9E-OQ31K2-FADG6Z-XEBCAO" );
        //boolean succeeded = activePremium(act, "radaee", "radaee_com@yahoo.cn",
        //		"LNJFDN-C89QFX-9ZOU9E-OQ31K2-FADG6Z-XEBCAO");

        // active library, or WaterMark will displayed on each page.
        // these active function is binding to version string "201401".
        //String ver = getVersion();
        // boolean succeeded = activeStandardForVer(act, "Radaee",
        // "radaeepdf@gmail.com", "NP8HLC-Q3M21C-H3CRUZ-WAJQ9H-5R5V9L-KM0Y1L");
        // boolean succeeded = activeProfessionalForVer(act, "Radaee",
        // "radaeepdf@gmail.com", "6D7KV9-FYCVAE-H3CRUZ-WAJQ9H-5R5V9L-KM0Y1L" );
        // boolean succeeded = activePremiumForVer(act, "Radaee", "radaeepdf@gmail.com",
        //		"Q6EL00-BTB1EG-H3CRUZ-WAJQ9H-5R5V9L-KM0Y1L");

        // add external fonts from system and resource
        fontfileListStart();//this method create empty font list
        fontfileListAdd("/system/fonts/DroidSans.ttf");//load from system fonts.
        //save_font("/system/fonts/DroidSans.ttf", "/sdcard/DroidSans.ttf");
        fontfileListAdd("/system/fonts/Roboto-Regular.ttf");
        //save_font("/system/fonts/Roboto-Regular.ttf", "/sdcard/Roboto-Regular.ttf");

        fontfileListAdd("/system/fonts/DroidSansFallback.ttf");
        fontfileListAdd("/system/fonts/DroidSansChinese.ttf");
        //save_font("/system/fonts/DroidSansFallback.ttf", "/sdcard/DroidSansFallback.ttf");
        fontfileListAdd("/system/fonts/NotoSansSC-Regular.otf");
        //save_font("/system/fonts/NotoSansSC-Regular.otf", "/sdcard/NotoSansSC-Regular.otf");
        fontfileListAdd("/system/fonts/NotoSansTC-Regular.otf");
        //save_font("/system/fonts/NotoSansTC-Regular.otf", "/sdcard/NotoSansTC-Regular.otf");
        fontfileListAdd("/system/fonts/NotoSansJP-Regular.otf");
        fontfileListAdd("/system/fonts/NotoSansKR-Regular.otf");
        fontfileListAdd("/system/fonts/NotoSansCJK-Regular.ttc");

        //fontfileListAdd("/system/fonts/NotoSansHebrew-Regular.ttf");
        load_truetype_font(res, R.raw.argbsn00lp, new File(files, "argbsn00lp.ttf"));
        load_truetype_font(res, R.raw.arimo, new File(files, "arimo.ttf"));//load from APP resource
        load_truetype_font(res, R.raw.arimob, new File(files, "arimob.ttf"));
        load_truetype_font(res, R.raw.arimoi, new File(files, "arimoi.ttf"));
        load_truetype_font(res, R.raw.arimobi, new File(files, "arimobi.ttf"));
        load_truetype_font(res, R.raw.tinos, new File(files, "tinos.ttf"));
        load_truetype_font(res, R.raw.tinosb, new File(files, "tinosb.ttf"));
        load_truetype_font(res, R.raw.tinosi, new File(files, "tinosi.ttf"));
        load_truetype_font(res, R.raw.tinosbi, new File(files, "tinosbi.ttf"));
        load_truetype_font(res, R.raw.cousine, new File(files, "cousine.ttf"));
        load_truetype_font(res, R.raw.cousineb, new File(files, "cousineb.ttf"));
        load_truetype_font(res, R.raw.cousinei, new File(files, "cousinei.ttf"));
        load_truetype_font(res, R.raw.cousinebi, new File(files, "cousinebi.ttf"));
        load_truetype_font(res, R.raw.symbol, new File(files, "symbol.ttf"));//Symbol Neu for Powerline
        load_truetype_font(res, R.raw.amiri_regular, new File(files, "amiriRegular.ttf"));//arabic
        fontfileListEnd();//this method parser all added font files, and extract font names, to init font mapping list.
        // using resource fonts to replace type1 fonts.H
        fontfileMapping("Arial", "Arimo");
        fontfileMapping("Arial Bold", "Arimo Bold");
        fontfileMapping("Arial BoldItalic", "Arimo Bold Italic");
        fontfileMapping("Arial Italic", "Arimo Italic");
        fontfileMapping("Arial,Bold", "Arimo Bold");
        fontfileMapping("Arial,BoldItalic", "Arimo Bold Italic");
        fontfileMapping("Arial,Italic", "Arimo Italic");
        fontfileMapping("Arial-Bold", "Arimo Bold");
        fontfileMapping("Arial-BoldItalic", "Arimo Bold Italic");
        fontfileMapping("Arial-Italic", "Arimo Italic");
        fontfileMapping("ArialMT", "Arimo");
        fontfileMapping("Calibri", "Arimo");
        fontfileMapping("Calibri Bold", "Arimo Bold");
        fontfileMapping("Calibri BoldItalic", "Arimo Bold Italic");
        fontfileMapping("Calibri Italic", "Arimo Italic");
        fontfileMapping("Calibri,Bold", "Arimo Bold");
        fontfileMapping("Calibri,BoldItalic", "Arimo Bold Italic");
        fontfileMapping("Calibri,Italic", "Arimo Italic");
        fontfileMapping("Calibri-Bold", "Arimo Bold");
        fontfileMapping("Calibri-BoldItalic", "Arimo Bold Italic");
        fontfileMapping("Calibri-Italic", "Arimo Italic");
        fontfileMapping("Helvetica", "Arimo");
        fontfileMapping("Helvetica Bold", "Arimo Bold");
        fontfileMapping("Helvetica BoldItalic", "Arimo Bold Italic");
        fontfileMapping("Helvetica Italic", "Arimo Italic");
        fontfileMapping("Helvetica,Bold", "Arimo,Bold");
        fontfileMapping("Helvetica,BoldItalic", "Arimo Bold Italic");
        fontfileMapping("Helvetica,Italic", "Arimo Italic");
        fontfileMapping("Helvetica-Bold", "Arimo Bold");
        fontfileMapping("Helvetica-BoldItalic", "Arimo Bold Italic");
        fontfileMapping("Helvetica-Italic", "Arimo Italic");
        fontfileMapping("Garamond", "Tinos");
        fontfileMapping("Garamond,Bold", "Tinos Bold");
        fontfileMapping("Garamond,BoldItalic", "Tinos Bold Italic");
        fontfileMapping("Garamond,Italic", "Tinos Italic");
        fontfileMapping("Garamond-Bold", "Tinos Bold");
        fontfileMapping("Garamond-BoldItalic", "Tinos Bold Italic");
        fontfileMapping("Garamond-Italic", "Tinos Italic");
        fontfileMapping("Times", "Tinos");
        fontfileMapping("Times,Bold", "Tinos Bold");
        fontfileMapping("Times,BoldItalic", "Tinos Bold Italic");
        fontfileMapping("Times,Italic", "Tinos Italic");
        fontfileMapping("Times-Bold", "Tinos Bold");
        fontfileMapping("Times-BoldItalic", "Tinos Bold Italic");
        fontfileMapping("Times-Italic", "Tinos Italic");
        fontfileMapping("Times-Roman", "Tinos");
        fontfileMapping("Times New Roman", "Tinos");
        fontfileMapping("Times New Roman,Bold", "Tinos Bold");
        fontfileMapping("Times New Roman,BoldItalic", "Tinos Bold Italic");
        fontfileMapping("Times New Roman,Italic", "Tinos Italic");
        fontfileMapping("Times New Roman-Bold", "Tinos Bold");
        fontfileMapping("Times New Roman-BoldItalic", "Tinos Bold Italic");
        fontfileMapping("Times New Roman-Italic", "Tinos Italic");
        fontfileMapping("TimesNewRoman", "Tinos");
        fontfileMapping("TimesNewRoman,Bold", "Tinos Bold");
        fontfileMapping("TimesNewRoman,BoldItalic", "Tinos Bold Italic");
        fontfileMapping("TimesNewRoman,Italic", "Tinos Italic");
        fontfileMapping("TimesNewRoman-Bold", "Tinos Bold");
        fontfileMapping("TimesNewRoman-BoldItalic", "Tinos Bold Italic");
        fontfileMapping("TimesNewRoman-Italic", "Tinos Italic");
        fontfileMapping("TimesNewRomanPS", "Tinos");
        fontfileMapping("TimesNewRomanPS,Bold", "Tinos Bold");
        fontfileMapping("TimesNewRomanPS,BoldItalic", "Tinos Bold Italic");
        fontfileMapping("TimesNewRomanPS,Italic", "Tinos Italic");
        fontfileMapping("TimesNewRomanPS-Bold", "Tinos Bold");
        fontfileMapping("TimesNewRomanPS-BoldItalic", "Tinos Bold Italic");
        fontfileMapping("TimesNewRomanPS-Italic", "Tinos Italic");
        fontfileMapping("TimesNewRomanPSMT", "Tinos");
        fontfileMapping("TimesNewRomanPSMT,Bold", "Tinos Bold");
        fontfileMapping("TimesNewRomanPSMT,BoldItalic", "Tinos Bold Italic");
        fontfileMapping("TimesNewRomanPSMT,Italic", "Tinos Italic");
        fontfileMapping("TimesNewRomanPSMT-Bold", "Tinos Bold");
        fontfileMapping("TimesNewRomanPSMT-BoldItalic", "Tinos Bold Italic");
        fontfileMapping("TimesNewRomanPSMT-Italic", "Tinos Italic");
        fontfileMapping("Courier", "Cousine");
        fontfileMapping("Courier Bold", "Cousine Bold");
        fontfileMapping("Courier BoldItalic", "Cousine Bold Italic");
        fontfileMapping("Courier Italic", "Cousine Italic");
        fontfileMapping("Courier,Bold", "Cousine Bold");
        fontfileMapping("Courier,BoldItalic", "Cousine Bold Italic");
        fontfileMapping("Courier,Italic", "Cousine Italic");
        fontfileMapping("Courier-Bold", "Cousine Bold");
        fontfileMapping("Courier-BoldItalic", "Cousine Bold Italic");
        fontfileMapping("Courier-Italic", "Cousine Italic");
        fontfileMapping("Courier New", "Cousine");
        fontfileMapping("Courier New Bold", "Cousine Bold");
        fontfileMapping("Courier New BoldItalic", "Cousine Bold Italic");
        fontfileMapping("Courier New Italic", "Cousine Italic");
        fontfileMapping("Courier New,Bold", "Cousine Bold");
        fontfileMapping("Courier New,BoldItalic", "Cousine Bold Italic");
        fontfileMapping("Courier New,Italic", "Cousine Italic");
        fontfileMapping("Courier New-Bold", "Cousine Bold");
        fontfileMapping("Courier New-BoldItalic", "Cousine Bold Italic");
        fontfileMapping("Courier New-Italic", "Cousine Italic");
        fontfileMapping("CourierNew", "Cousine");
        fontfileMapping("CourierNew Bold", "Cousine Bold");
        fontfileMapping("CourierNew BoldItalic", "Cousine Bold Italic");
        fontfileMapping("CourierNew Italic", "Cousine Italic");
        fontfileMapping("CourierNew,Bold", "Cousine Bold");
        fontfileMapping("CourierNew,BoldItalic", "Cousine Bold Italic");
        fontfileMapping("CourierNew,Italic", "Cousine Italic");
        fontfileMapping("CourierNew-Bold", "Cousine Bold");
        fontfileMapping("CourierNew-BoldItalic", "Cousine Bold Italic");
        fontfileMapping("CourierNew-Italic", "Cousine Italic");
        fontfileMapping("Symbol", "Symbol Neu for Powerline");
        fontfileMapping("Symbol,Bold", "Symbol Neu for Powerline");
        fontfileMapping("Symbol,BoldItalic", "Symbol Neu for Powerline");
        fontfileMapping("Symbol,Italic", "Symbol Neu for Powerline");

        String face_name = null;
        int face_first = 0;
        int face_count = getFaceCount();
        while (face_first < face_count) {
            face_name = getFaceName(face_first);
            //Log.d("------Fonts------", "----face name = " + face_name);
            if (face_name != null) break;
            face_first++;
        }

        // start default font config:
        // all default font applied is only working when font is not embed in PDF file.
        // and non-embed font can't match external font by font name.

        // we using Arimo as default font with empty collection
        // Arimo is better EUR languages than DroidSansFallback.
        // but DroidSansFallback is better than Arimo, when display CJK chars.
        // empty collection also appears on CJK PDF files.
        // to choice between Arimo and DroidSansFallback, depends on APP usage.
        // set default font for fixed width font with empty collection.
        if (!setDefaultFont(null, "Arimo", true) &&
                !setDefaultFont(null, "DroidSansFallback", true) && face_name != null) {
            setDefaultFont(null, face_name, true);
        }
        // set default font for non-fixed width font with empty collection.
        if (!setDefaultFont(null, "Arimo", false) &&
                !setDefaultFont(null, "DroidSansFallback", false) && face_name != null) {
            setDefaultFont(null, face_name, false);
        }

        // for CJK default font setting:
        // first try using DroidSansFallback.
        // if DroidSansFallback not exist, we using NotoSans.
        // NotoSans not good support CJK,
        // it split Chinese Japanese Korean for each. that has some problems.
        // the problem is: Japanese not including some Chinese chars, which is required for Japanese.
        // Korean has the same problem.
        // both DroidSansFallback and NotoSans has risk to use.
        // because some user root the device and replace the font with other fonts.
        // the best CJK support is: add a CJK font to resource, and load from resource.
        // but this make APP large.

        // set default font for Simplified Chinese. 简体
        if (!setDefaultFont("GB1", "AR PL SungtiL GB", true) &&
                !setDefaultFont("GB1", "Noto Sans CJK SC Regular", true) &&
                !setDefaultFont("GB1", "Noto Sans CJK SC", true) &&
                !setDefaultFont("GB1", "DroidSansChinese", true) &&
                !setDefaultFont("GB1", "Noto Sans SC Regular", true) &&
                !setDefaultFont("GB1", "DroidSansFallback", true) && face_name != null)
            setDefaultFont("GB1", face_name, true);
        if (!setDefaultFont("GB1", "AR PL SungtiL GB", false) &&
                !setDefaultFont("GB1", "Noto Sans CJK SC Regular", false) &&
                !setDefaultFont("GB1", "Noto Sans CJK SC", false) &&
                !setDefaultFont("GB1", "Noto Sans SC Regular", false) &&
                !setDefaultFont("GB1", "DroidSansFallback", false) && face_name != null)
            setDefaultFont("GB1", face_name, false);

        // set default font for Traditional Chinese. 繁體
        if (!setDefaultFont("CNS1", "Noto Sans CJK TC Regular", true) &&
                !setDefaultFont("CNS1", "Noto Sans CJK TC", true) &&
                !setDefaultFont("CNS1", "Noto Sans TC Regular", true) &&
                !setDefaultFont("CNS1", "DroidSansFallback", true) && face_name != null)
            setDefaultFont("CNS1", face_name, true);
        if (!setDefaultFont("CNS1", "Noto Sans CJK TC Regular", false) &&
                !setDefaultFont("CNS1", "Noto Sans CJK TC", false) &&
                !setDefaultFont("CNS1", "Noto Sans TC Regular", false) &&
                !setDefaultFont("CNS1", "DroidSansFallback", false) && face_name != null)
            setDefaultFont("CNS1", face_name, false);

        // set default font for Japanese.
        if (!setDefaultFont("Japan1", "Noto Sans CJK JP Regular", true) &&
                !setDefaultFont("Japan1", "Noto Sans CJK JP", true) &&
                !setDefaultFont("Japan1", "Noto Sans JP Regular", true) &&
                !setDefaultFont("Japan1", "DroidSansFallback", true) && face_name != null)
            setDefaultFont("Japan1", face_name, true);
        if (!setDefaultFont("Japan1", "Noto Sans CJK JP Regular", false) &&
                !setDefaultFont("Japan1", "Noto Sans CJK JP", false) &&
                !setDefaultFont("Japan1", "Noto Sans JP Regular", false) &&
                !setDefaultFont("Japan1", "DroidSansFallback", false) && face_name != null)
            setDefaultFont("Japan1", face_name, false);

        // set default font for Korean.
        if (!setDefaultFont("Korea1", "Noto Sans CJK KR Regular", true) &&
                !setDefaultFont("Korea1", "Noto Sans CJK KR", true) &&
                !setDefaultFont("Korea1", "Noto Sans KR Regular", true) &&
                !setDefaultFont("Korea1", "DroidSansFallback", true) && face_name != null)
            setDefaultFont("Korea1", face_name, true);
        if (!setDefaultFont("Korea1", "Noto Sans CJK KR Regular", false) &&
                !setDefaultFont("Korea1", "Noto Sans CJK KR", false) &&
                !setDefaultFont("Korea1", "Noto Sans KR Regular", false) &&
                !setDefaultFont("Korea1", "DroidSansFallback", false) && face_name != null)
            setDefaultFont("Korea1", face_name, false);

        // set text font for edit-box and combo-box editing.
        // first we try using DroidSansFallback, which has large code range include CJK,
        // but not good support france, german and some EUR languages.
        // if DroidSansFallback not exits, we using Arimo, loading from resource, which has good support EUR languages.
        // For arabic support use setAnnotFont("Amiri-Regular")
        if (//!setAnnotFont("DroidSansFallback") &&
                !setAnnotFont("Arimo") && face_name != null) {
            setAnnotFont(face_name);
        }

        EditNode.SetDefFont("Arial");
        EditNode.SetDefCJKFont("AR PL SungtiL GB");
        /*
        File font_file = new File("/system/fonts/NotoSansSC-Regular.otf");
        if (font_file.exists()) EditNode.SetDefCJKFont("NotoSansSC-Regular");
        else {
            font_file = new File("/system/fonts/DroidSansChinese.ttf");
            if (font_file.exists()) EditNode.SetDefCJKFont("DroidSansChinese");
            else {
                font_file = new File("/system/fonts/DroidSansFallback.ttf");
                if (font_file.exists()) EditNode.SetDefCJKFont("DroidSansFallback");
            }
        }
        */
        // set configure to default value
        default_config();

        verifyPermissions((Activity) act);

        return ms_init;
    }

    /**
     * reset to default configure.
     */
    public static void default_config() {
        g_sel_color = 0x400000C0;// selection color
        g_find_primary_color = 0x400000FF;// find primary color
        g_find_secondary_color = 0x40404040;// find secondary color
        fling_dis = 1.0f;// 0.5-2
        fling_speed = 0.1f;// 0.05 - 0.2
        g_view_mode = 0;// 0,1,2,3,4,5,6 0:vertical 1:horizon 2:curl effect 3:single
        // 4:SingleEx 5:Reflow, 6:show 2 page as 1 page in land
        // scape mode
        g_render_quality = recommandedRenderMode();// 0,1,2 0:draft 1:normal 2:best with over print support.
        g_dark_mode = false;// dark mode
        g_view_zoom_level = 3;
        //hideAnnots(true);
        setAnnotTransparency(g_annot_transparency);
    }

    /**
     * map PDF point to DIB point.
     *
     * @param mat    Matrix object defined scale, rotate, tranlate operations.
     * @param ppoint input point in PDF coordinate system. [x, y]
     * @param dpoint output point in DIB coordinate system. [x, y]
     */
    public static void ToDIBPoint(Matrix mat, float[] ppoint, float[] dpoint) {
        toDIBPoint(mat.hand, ppoint, dpoint);
    }

    /**
     * map DIB point to PDF point.
     *
     * @param mat    Matrix object defined scale, rotate, tranlate operations.
     * @param dpoint input point in DIB coordinate system. [x, y]
     * @param ppoint output point in PDF coordinate system. [x, y]
     */
    public static void ToPDFPoint(Matrix mat, float[] dpoint, float[] ppoint) {
        toPDFPoint(mat.hand, dpoint, ppoint);
    }

    /**
     * map PDF rectangle to DIB rectangle.
     *
     * @param mat   Matrix object defined scale, rotate, tranlate operations.
     * @param prect input rect in PDF coordinate system. [left, top, right,
     *              bottom]
     * @param drect output rect in DIB coordinate system. [left, top, right,
     *              bottom]
     */
    public static void ToDIBRect(Matrix mat, float[] prect, float[] drect) {
        toDIBRect(mat.hand, prect, drect);
    }

    /**
     * map DIB rectangle to PDF rectangle.
     *
     * @param mat   Matrix object defined scale, rotate, tranlate operations.
     * @param drect input rect in DIB coordinate system. [left, top, right,
     *              bottom]
     * @param prect output rect in PDF coordinate system. [left, top, right,
     *              bottom]
     */
    public static void ToPDFRect(Matrix mat, float[] drect, float[] prect) {
        toPDFRect(mat.hand, drect, prect);
    }

    /**
     * map PDF point to DIB point.
     *
     * @param ratio  scale value apply to page rendering.
     * @param dib_h  height of render bitmap.
     * @param ppoint input point in PDF coordinate system. [x, y]
     * @param dpoint output point in DIB coordinate system. [x, y]
     */
    public static void ToDIBPoint(float ratio, int dib_h, float[] ppoint,
                                  float[] dpoint) {
        dpoint[0] = ppoint[0] * ratio;
        dpoint[1] = dib_h - ppoint[1] * ratio;
    }

    /**
     * map DIB point to PDF point.
     *
     * @param ratio  scale value apply to page rendering.
     * @param dib_h  height of render bitmap.
     * @param dpoint input point in DIB coordinate system. [x, y]
     * @param ppoint output point in PDF coordinate system. [x, y]
     */
    public static void ToPDFPoint(float ratio, int dib_h, float[] dpoint,
                                  float[] ppoint) {
        ppoint[0] = dpoint[0] / ratio;
        ppoint[1] = (dib_h - dpoint[1]) / ratio;
    }

    /**
     * map PDF rectangle to DIB rectangle.
     *
     * @param ratio scale value apply to page rendering.
     * @param dib_h height of render bitmap.
     * @param prect input rect in PDF coordinate system. [left, top, right,
     *              bottom]
     * @param drect output rect in DIB coordinate system. [left, top, right,
     *              bottom]
     */
    public static void ToDIBRect(float ratio, int dib_h, float[] prect,
                                 float[] drect) {
        drect[0] = prect[0] * ratio;
        drect[1] = dib_h - prect[3] * ratio;
        drect[2] = prect[2] * ratio;
        drect[3] = dib_h - prect[1] * ratio;
    }

    /**
     * map DIB rectangle to PDF rectangle.
     *
     * @param ratio scale value apply to page rendering.
     * @param dib_h height of render bitmap.
     * @param drect input rect in DIB coordinate system. [left, top, right,
     *              bottom]
     * @param prect output rect in PDF coordinate system. [left, top, right,
     *              bottom]
     */
    public static void ToPDFRect(float ratio, int dib_h, float[] drect,
                                 float[] prect) {
        prect[0] = drect[0] / ratio;
        prect[1] = (dib_h - drect[3]) / ratio;
        prect[2] = drect[2] / ratio;
        prect[3] = (dib_h - drect[1]) / ratio;
    }

    /**
     * remove all tmp files that generated when user click multi-media annotations.
     */
    public static void RemoveTmp() {
        try {
            File tmp = new File(tmp_path);
            File[] files = tmp.listFiles();
            if (files != null) {
                for (File file : files) file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private static void verifyPermissions(Activity act) {
        // Check if we have necessary permissions
        int accessNetworkStatePermission = ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_NETWORK_STATE);
        int accessNetworkPermission = ActivityCompat.checkSelfPermission(act, Manifest.permission.INTERNET);
        ArrayList<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            int storagePermissionWrite = ActivityCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int storagePermissionRead = ActivityCompat.checkSelfPermission(act, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (storagePermissionWrite != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (storagePermissionRead != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (accessNetworkStatePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if (accessNetworkPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }

        if (permissions.size() > 0) {
            String[] permissionArray = new String[permissions.size()];
            permissions.toArray(permissionArray);

            ActivityCompat.requestPermissions(act, permissionArray, 1);
        }
    }

    public static boolean isLicenseActivated() { //Nermeen
        return ms_init;
    }
}
