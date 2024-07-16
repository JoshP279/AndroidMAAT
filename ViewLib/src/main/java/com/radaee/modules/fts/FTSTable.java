package com.radaee.modules.fts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/*
 * PDFMaster
 * Created by Nermeen on 20/11/2020.
 */
public class FTSTable {

    private static final String SNIPPET = "snippet";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String COMMA_SEP = ",";
    static final String SQL_CREATE_ENTRIES =
            "CREATE VIRTUAL TABLE " + FTSEntry.TABLE_NAME + " USING fts4 (" +
                    FTSEntry.COLUMN_DOC_NAME + TEXT_TYPE + COMMA_SEP +
                    FTSEntry.COLUMN_PAGE_INDEX + INT_TYPE + COMMA_SEP +
                    FTSEntry.COLUMN_RECT_LEFT + DOUBLE_TYPE + COMMA_SEP +
                    FTSEntry.COLUMN_RECT_TOP + DOUBLE_TYPE + COMMA_SEP +
                    FTSEntry.COLUMN_RECT_RIGHT + DOUBLE_TYPE + COMMA_SEP +
                    FTSEntry.COLUMN_RECT_BOTTOM + DOUBLE_TYPE + COMMA_SEP +
                    FTSEntry.COLUMN_TEXT + TEXT_TYPE + ")";
    private static final String[] sProjectionAll = {FTSEntry.COLUMN_DOC_NAME, FTSEntry.COLUMN_TEXT,
            FTSEntry.COLUMN_PAGE_INDEX, FTSEntry.COLUMN_RECT_TOP, FTSEntry.COLUMN_RECT_LEFT, FTSEntry.COLUMN_RECT_RIGHT,
            FTSEntry.COLUMN_RECT_BOTTOM, "snippet(" + FTSEntry.TABLE_NAME + ", '<b>', '</b>', '...', 6, 10) as " + SNIPPET};

    static void insert(SQLiteDatabase mDb, FTS fts) {
        try {
            mDb.insert(FTSEntry.TABLE_NAME, null, buildContentValues(fts));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void delete(SQLiteDatabase mDb, String document) {
        try {
            mDb.delete(FTSEntry.TABLE_NAME, FTSEntry.COLUMN_DOC_NAME + " MATCH ?", new String[]{document});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static boolean doesFTSDocumentExist(SQLiteDatabase db, String document) {
        boolean result = false;
        try {
            Cursor cursor = db.query(FTSEntry.TABLE_NAME, new String[]{FTSEntry.COLUMN_DOC_NAME},
                    FTSEntry.COLUMN_DOC_NAME + " MATCH ?", new String[]{document}, null, null, null);

            if(cursor != null) {
                if(cursor.moveToFirst())
                    result = true;

                cursor.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    static List<FTS> searchInDocument(SQLiteDatabase db, String document, String query) {
        List<FTS> result = null;
        try {
            query = TextUtils.join("* ", TextUtils.split(query, " ")) + "*";
            //query = new String(query.getBytes(), Charset.forName("UTF-8"));
            String orderBy = FTSEntry.COLUMN_PAGE_INDEX + ", " + FTSEntry.COLUMN_RECT_TOP + " DESC, " + FTSEntry.COLUMN_RECT_LEFT;
            Cursor cursor = db.query(FTSEntry.TABLE_NAME, sProjectionAll, FTSEntry.COLUMN_DOC_NAME + " = ? AND " +
                    FTSEntry.COLUMN_TEXT + " MATCH ?", new String[]{document, query}, null, null, orderBy);

            if(cursor != null) {
                if (cursor.moveToFirst()) {
                    result = new ArrayList<>();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        result.add(readFromCursor(cursor));

                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    static List<FTS> searchAllDocuments(SQLiteDatabase db, String query) {
        List<FTS> result = null;
        try {
            query = TextUtils.join("* ", TextUtils.split(query, " ")) + "*";
            String selection = FTSEntry.COLUMN_TEXT + " MATCH ?";
            String[] selectionArgs = new String[]{query};
            String orderBy = FTSEntry.COLUMN_DOC_NAME + ", " + FTSEntry.COLUMN_PAGE_INDEX + ", " + FTSEntry.COLUMN_RECT_TOP + " DESC, " + FTSEntry.COLUMN_RECT_LEFT;
            String projectionFTS = FTSEntry.COLUMN_DOC_NAME + ", " + FTSEntry.COLUMN_TEXT + ", " + FTSEntry.COLUMN_PAGE_INDEX + ", " + FTSEntry.COLUMN_RECT_TOP + ", "
                    + FTSEntry.COLUMN_RECT_LEFT + ", " + FTSEntry.COLUMN_RECT_RIGHT + ", " + FTSEntry.COLUMN_RECT_BOTTOM + ", snippet(" + FTSEntry.TABLE_NAME +
                    ", '<b>', '</b>', '...', 6, 10) as " + SNIPPET;
            String projectionGroup = FTSEntry.COLUMN_DOC_NAME + ", " + FTSEntry.COLUMN_TEXT + ", MIN(" + FTSEntry.COLUMN_PAGE_INDEX
                    + ") AS first_page, COUNT(" + FTSEntry.COLUMN_PAGE_INDEX + ") AS Occurrences, " + FTSEntry.COLUMN_RECT_TOP + ", " + FTSEntry.COLUMN_RECT_LEFT
                    + ", " + FTSEntry.COLUMN_RECT_RIGHT + ", " + FTSEntry.COLUMN_RECT_BOTTOM + ", " + SNIPPET;
            String fullQuery = "SELECT " + projectionGroup + " FROM (SELECT " + projectionFTS + " FROM " + FTSEntry.TABLE_NAME + " WHERE " + selection + " ORDER BY "
                    + orderBy + ") GROUP BY " + FTSEntry.COLUMN_DOC_NAME;

            Cursor cursor = db.rawQuery(fullQuery, selectionArgs);

            if(cursor != null) {
                if (cursor.moveToFirst()) {
                    result = new ArrayList<>();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        FTS fts = readFromCursor(cursor);

                        fts.setPageIndex(cursor.getInt(cursor.getColumnIndex("first_page")));
                        fts.setOccurrences(cursor.getInt(cursor.getColumnIndex("Occurrences")));

                        result.add(fts);

                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private static ContentValues buildContentValues(FTS fts) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(FTSEntry.COLUMN_TEXT, fts.getText()); //text
        contentValues.put(FTSEntry.COLUMN_DOC_NAME, fts.getDocument()); //document
        contentValues.put(FTSEntry.COLUMN_RECT_TOP, fts.getRectTop()); //rect top
        contentValues.put(FTSEntry.COLUMN_RECT_LEFT, fts.getRectLeft()); //rect left
        contentValues.put(FTSEntry.COLUMN_RECT_RIGHT, fts.getRectRight()); //rect right
        contentValues.put(FTSEntry.COLUMN_RECT_BOTTOM, fts.getRectBottom()); //rect bottom
        contentValues.put(FTSEntry.COLUMN_PAGE_INDEX, fts.getPageIndex()); //page index

        return contentValues;
    }

    private static FTS readFromCursor(Cursor cursor) {
        FTS fts = new FTS();

        fts.setSnippet(cursor.getString(cursor.getColumnIndex(SNIPPET)));
        fts.setText(cursor.getString(cursor.getColumnIndex(FTSEntry.COLUMN_TEXT)));
        fts.setRectTop(cursor.getDouble(cursor.getColumnIndex(FTSEntry.COLUMN_RECT_TOP)));
        fts.setDocument(cursor.getString(cursor.getColumnIndex(FTSEntry.COLUMN_DOC_NAME)));
        if(cursor.getColumnIndex(FTSEntry.COLUMN_PAGE_INDEX) > -1)
            fts.setPageIndex(cursor.getInt(cursor.getColumnIndex(FTSEntry.COLUMN_PAGE_INDEX)));
        fts.setRectLeft(cursor.getDouble(cursor.getColumnIndex(FTSEntry.COLUMN_RECT_LEFT)));
        fts.setRectRight(cursor.getDouble(cursor.getColumnIndex(FTSEntry.COLUMN_RECT_RIGHT)));
        fts.setRectBottom(cursor.getDouble(cursor.getColumnIndex(FTSEntry.COLUMN_RECT_BOTTOM)));

        return fts;
    }

    private static final class FTSEntry implements BaseColumns {
        static final String TABLE_NAME = "FTS";
        static final String COLUMN_DOC_NAME = "document";
        static final String COLUMN_PAGE_INDEX = "page";
        static final String COLUMN_RECT_LEFT = "rect_left";
        static final String COLUMN_RECT_TOP = "rect_top";
        static final String COLUMN_RECT_RIGHT = "rect_right";
        static final String COLUMN_RECT_BOTTOM = "rect_bottom";
        static final String COLUMN_TEXT = "text";
    }
}