package com.radaee.modules.fts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.radaee.comm.Global;
import com.radaee.pdf.Document;
import com.radaee.util.CommonUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/*
 * PDFMaster
 * Created by Nermeen on 20/11/2020.
 */
public class RadaeeFTSManager {

    public static boolean sFTSValid;
    public static final int FTS_QUERY_MIN_LENGTH = 3;
    private static int sSearchType = 1;
    private static String sDbPath;

    /**
     * Set search type
     * @param type 0: standard search, 1: FTS search
     */
    public static void setSearchType(int type) {
        sSearchType = type;
    }

    /**
     * returns the current search type
     * @return 0: standard search, 1: FTS search
     */
    public static int getSearchType() {
        return sSearchType;
    }

    /**
     * creates the FTS db, and FTS table (needed for other FTS methods)
     *
     * @param dbPath the db path (ex:/mnt/sdcard/fts.db)
     * @return error message in case of error
     */
    public static String setIndexDB(Context context, String dbPath) {
        try {
            FTSDBHelper dbHelper = new FTSDBHelper(context, dbPath);
            dbHelper.getReadableDatabase(); //to verify the db creation
            dbHelper.close();
            sDbPath = dbPath;
        } catch (Exception e) {
            return "DB error: " + e.getLocalizedMessage();
        }
        return "DB created successfully";
    }

    /**
     * Extracts the text from the given document and add it to the FTS DB table.
     *
     * @param context to use to open or create the database
     * @param docPath the document's full path
     * @param password password needed to open the document (if any)
     * @param selRtoL if true text selection starts from right to left
     * @return error message in case of error
     */
    public static String addIndex(Context context, String docPath, String password, boolean selRtoL) {
        try {
            if(TextUtils.isEmpty(sDbPath))
                return "Please specify a valid DB by calling FTS_SetIndexDB";
            Document document = new Document();
            int ret = document.Open(docPath, password);
            if(ret == 0) {
                FTSDBHelper dbHelper = new FTSDBHelper(context, sDbPath);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String docId = getDocumentIDMd5(document);

                if(FTSTable.doesFTSDocumentExist(db, docId)) {
                    dbHelper.close();
                    document.Close();
                    return "Warning: Document already added, please call FTS_RemoveFromIndex first";
                }

                //extract text into file
                boolean rtol = Global.g_sel_rtol;
                Global.g_sel_rtol = selRtoL;
                String jsonFile = context.getFilesDir().getAbsolutePath() + File.separator + "fts.json";//"/mnt/sdcard" + File.separator + "fts.json"
                int result = TextExtractor.extractDocumentText(document, jsonFile);
                document.Close();
                Global.g_sel_rtol = rtol;

                if(result == 1) //Text extracted successfully
                    parseFTSJson(jsonFile, docId, db);
                else { //text extraction error
                    dbHelper.close();
                    return result == -1 ? "Extract text error" : "No text to extract";
                }
                dbHelper.close();

                return "SUCCESS: Index added successfully";
            } else
                return processOpenResult(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    /**
     * Removes the given document from the FTS DB table.
     *
     * @param context to use to open or create the database
     * @param docPath the document's full path
     * @param password password needed to open the document (if any)
     * @return error message in case of error
     */
    public static String removeFromIndex(Context context, String docPath, String password) {
        try {
            if(TextUtils.isEmpty(sDbPath))
                return "Please specify a valid DB by calling FTS_SetIndexDB";
            Document document = new Document();
            int ret = document.Open(docPath, password);
            if(ret == 0) {
                FTSDBHelper dbHelper = new FTSDBHelper(context, sDbPath);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String docId = getDocumentIDMd5(document);
                if(!FTSTable.doesFTSDocumentExist(db, docId)) {
                    dbHelper.close();
                    document.Close();
                    return "Error:Document is not added into Index";
                }
                FTSTable.delete(db, docId);
                dbHelper.close();
                document.Close();
                return "Index removed successfully";
            } else
                return processOpenResult(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    /**
     * Applies a FTS search with the given term.
     *
     * @param context to use to open or create the database
     * @param term terms to search (multiple words separated by space will be put in "and")
     * @param docPath if empty: search in whole FTS database and returns the occurrences list grouped by document
     *                pdf path: search in FTS and returns the occurrences of the given document
     * @param password password needed to open the document (if any)
     * @param resultPath empty: returns a JSON with the occurrences
     *                   text file path: create a report file that contains a JSON with the occurrences
     * @return JSON with the occurrences in case of empty resultPath or status message otherwise.
     * JSON struct example:
     * [{
     *      "document" : "", //document id
     *      "text" : "", //snippet
     *      "page_index" : , //page index, 0-index (from 0 to Document.GetPageCount - 1)
     *      "resultCount" : , //occurrences
     *      "rect_t" : , //text block rect top position in PDF coordinates
     *      "rect_r" : , //text block rect right position in PDF coordinates
     *      "rect_l" : , //text block rect left position in PDF coordinates
     *      "rect_b" :  //text block rect bottom position in PDF coordinates
     * }]
     */
    public static String search(Context context, String term, String docPath, String password, String resultPath) {
        try {
            if(TextUtils.isEmpty(sDbPath))
                return "Please specify a valid DB by calling FTS_SetIndexDB";
            if(term.trim().length() < FTS_QUERY_MIN_LENGTH)
                return "Enter at least " + FTS_QUERY_MIN_LENGTH + " characters to start searching";
            String docId = null;

            FTSDBHelper dbHelper = new FTSDBHelper(context, sDbPath);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            if(!TextUtils.isEmpty(docPath)) { //validate document id
                Document document = new Document();
                int ret = document.Open(docPath, password);
                if (ret == 0) {
                    docId = getDocumentIDMd5(document);
                    if (!FTSTable.doesFTSDocumentExist(db, docId)) {
                        dbHelper.close();
                        document.Close();
                        return "Error:Document is not added into Index";
                    }
                    document.Close();
                } else
                    return processOpenResult(ret);
            }

            List<FTS> ftsResult;
            if(!TextUtils.isEmpty(docId))
                ftsResult = FTSTable.searchInDocument(db, docId, term.trim());
            else
                ftsResult = FTSTable.searchAllDocuments(db, term.trim());
            dbHelper.close();

            if(ftsResult == null || ftsResult.size() == 0)
                return "No match Found";
            else
                return exportToJson(ftsResult, resultPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    /**
     * Applies a FTS search with the given term.
     *
     * @param context to use to open or create the database
     * @param term terms to search (multiple words separated by space will be put in "and")
     * @param document search in FTS and returns the occurrences of the given document
     * @return list of the occurrences
     */
    public static List<FTS> search(Context context, String term, Document document) throws Exception {
        sFTSValid = false; //will be true after DB, and document validation
        if(TextUtils.isEmpty(sDbPath))
            throw new Exception("Please specify a valid DB by calling FTS_SetIndexDB");

        FTSDBHelper dbHelper = new FTSDBHelper(context, sDbPath);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String docId = getDocumentIDMd5(document);
        if (!FTSTable.doesFTSDocumentExist(db, docId)) {
            dbHelper.close();
            throw new Exception("Error:Document is not yet added into Index, try again later");
        }
        sFTSValid = true;

        List<FTS> ftsResult = FTSTable.searchInDocument(db, docId, term.trim());
        dbHelper.close();
        return ftsResult;
    }

    private static boolean hasRTL(String textString) {
        for (int i = 0; i < textString.length(); i++) {
            if (String.valueOf(textString.charAt(i)).matches("\\p{InArabic}+")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads the data from the given json file and inserts them into the FTS table.
     *
     * @param jsonPath json containing the extracted text
     * @param documentID the document id
     * @param db database object
     */
    private static void parseFTSJson(String jsonPath, String documentID, SQLiteDatabase db) {
        try {
            Gson gson = new Gson();
            JsonReader jsonReader = new JsonReader(new FileReader(jsonPath));

            db.beginTransaction();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if(name.equals(TextExtractor.PAGES)) {
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        TextExtractor.PageBlocks pageBlocks = gson.fromJson(jsonReader, TextExtractor.PageBlocks.class);
                        for (TextExtractor.Block currentBlock : pageBlocks.blocks) {
                            FTS fts = new FTS();
                            fts.setDocument(documentID);
                            fts.setPageIndex(pageBlocks.page);
                            fts.setText(currentBlock.text);
                            fts.setRectTop(currentBlock.rectTop);
                            fts.setRectLeft(currentBlock.rectLeft);
                            fts.setRectRight(currentBlock.rectRight);
                            fts.setRectBottom(currentBlock.rectBottom);
                            FTSTable.insert(db, fts);
                        }
                    }

                    jsonReader.endArray();
                }
            }
            jsonReader.endObject();
            jsonReader.close();
            db.setTransactionSuccessful();
            new File(jsonPath).delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private static String exportToJson(List<FTS> ftsResult, String resultPath) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        FileWriter fileWriter;
        if(!TextUtils.isEmpty(resultPath)) {
            try {
                fileWriter = new FileWriter(resultPath);
            } catch (IOException e) {
                return "Error:result file error\n" + gson.toJson(ftsResult);
            }
            JsonWriter jsonWriter = new JsonWriter(fileWriter);
            gson.toJson(ftsResult, new TypeToken<List<FTS>>(){}.getType(), jsonWriter);
            try {
                jsonWriter.close();
            } catch (IOException e) {
                return "Error:result file error\n" + gson.toJson(ftsResult);
            }
            return "Search result generated successfully";
        }
        return gson.toJson(ftsResult);
    }

    private static String processOpenResult(int ret) {
        switch( ret ) {
            case -1://need input password
                return "Open Failed: Invalid Password";
            case -2://unknown encryption
                return "Open Failed: Unknown Encryption";
            case -3://damaged or invalid format
                return "Open Failed: Damaged or Invalid PDF file";
            case -10://access denied or invalid file path
                return "Open Failed: Access denied or Invalid path";
            default://unknown error
                return "Open Failed: Unknown Error";
        }
    }

    private static String getDocumentIDMd5(Document document) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if(document.GetID(0) != null)
                outputStream.write(document.GetID(0));
            if(document.GetID(1) != null)
                outputStream.write(document.GetID(1));

            String md5 = CommonUtil.md5(outputStream.toByteArray());

            outputStream.close();

            return md5;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}