package com.radaee.modules.fts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * PDFMaster
 * Created by Nermeen on 20/11/2020.
 */
public class FTSDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public FTSDBHelper(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FTSTable.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}