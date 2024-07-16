package com.radaee.modules.fts;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.radaee.viewlib.R;

import java.util.List;

/*
 * PDFMaster
 * Created by Nermeen on 20/11/2020.
 */
public class FTSResultManager {

    private static FTS sCurrentResult;
    private static List<FTS> sFtsResult;
    private static AlertDialog sResultsDialog;
    private static FTSResultListener sFtsListener;

    public interface FTSResultListener {
        void onShowResultItem(FTS clickedItem, int position);
    }

    public static void showSearchResult(Context context, List<FTS> ftsResult, String term, FTSResultListener listener) {
        if(ftsResult == null || ftsResult.size() == 0)
            Toast.makeText(context, context.getString(R.string.no_search_results, term), Toast.LENGTH_SHORT).show();
        else {
            sFtsResult = ftsResult;
            sFtsListener = listener;
            ListView mResultList = new ListView(context);
            FTSResultAdapter ftsAdapter = new FTSResultAdapter(context, ftsResult);
            mResultList.setAdapter(ftsAdapter);

            mResultList.setOnItemClickListener((parent, view, position, id) -> {
                FTS fts = (FTS) parent.getItemAtPosition(position);
                if(fts != null && sFtsListener != null) {
                    sCurrentResult = fts;
                    sFtsListener.onShowResultItem(fts, position);
                }
                sResultsDialog.dismiss();
            });

            sResultsDialog = new AlertDialog.Builder(context)
                    .setTitle("Search Results")
                    .setView(mResultList)
                    .show();
        }
    }

    public static void reShowResults() {
        if(sResultsDialog != null)
            sResultsDialog.show();
    }

    public static void gotoResult(Context context, int direction) {
        if(sFtsResult != null) {
            for (int i = 0; i < sFtsResult.size(); i++) { //find the current item in the results list
                FTS current = sFtsResult.get(i);
                if (current.equals(sCurrentResult)) { //found
                    if (direction > 0 && i + 1 < sFtsResult.size()) {
                        if(sFtsListener != null) {
                            sCurrentResult = sFtsResult.get(i + 1);
                            sFtsListener.onShowResultItem(sCurrentResult, i + 1);
                        }
                        return;
                    }

                    if (direction < 0 && i - 1 >= 0) {  //found
                        if(sFtsListener != null) {
                            sCurrentResult = sFtsResult.get(i - 1);
                            sFtsListener.onShowResultItem(sCurrentResult, i - 1);
                        }
                        return;
                    }
                }
            }
            Toast.makeText(context, R.string.no_more_found, Toast.LENGTH_SHORT).show();
        }
    }

    private static class FTSResultAdapter extends BaseAdapter {

        Context mContext;
        List<FTS> mResults;

        FTSResultAdapter(Context context, List<FTS> results) {
            mContext = context;
            mResults = results;
        }

        @Override
        public int getCount() {
            return mResults != null ? mResults.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return mResults != null ? mResults.get(i) : null;
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        public long getItemId(int i) {
            return mResults != null && getItem(i) != null ? mResults.indexOf(getItem(i)) : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.fts_search_result_row, viewGroup, false);
                holder = new ViewHolder();
                holder.pageNum = convertView.findViewById(R.id.page_num);
                holder.snippet = convertView.findViewById(R.id.line_snippet);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            FTS fts = (FTS) getItem(position);

            if(fts != null) {
                holder.pageNum.setText(Integer.toString(fts.getPageIndex() + 1));
                String lineString = fts.getSnippet();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    holder.snippet.setText(Html.fromHtml(lineString, Html.FROM_HTML_MODE_LEGACY));
                else
                    holder.snippet.setText(Html.fromHtml(lineString));
            }

            return convertView;
        }

        private static class ViewHolder {
            TextView snippet;
            TextView pageNum;
        }
    }
}