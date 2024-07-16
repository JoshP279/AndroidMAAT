package com.radaee.modules.fts;

import android.content.Context;
import android.os.AsyncTask;

import com.radaee.comm.Global;

/*
 * PDFMaster
 *
 * Generates the FTS of the given document (if not done yet)
 *
 * Created by Nermeen on 23/11/2020.
 */
public class FTSGenerationTask extends AsyncTask<Object, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Object... params) {
        Context context = (Context) params[0];

        RadaeeFTSManager.setIndexDB(context, context.getDatabasePath("fts.db").getPath());

        RadaeeFTSManager.addIndex(context, (String) params[1], (String) params[2], Global.g_sel_rtol);

        return null;
    }
}