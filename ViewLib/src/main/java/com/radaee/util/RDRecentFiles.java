package com.radaee.util;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class RDRecentFiles {
    protected static int MAX_RECENT_COUNT = 30;

    public static void loadAndUpdate(Context ctx, PDFGridThread thread, ArrayList<PDFGridAdt.SnatchItem> items) {
        File dir = ctx.getExternalFilesDir(null);
        File rfiles = new File(dir, "recent.json");
        try {
            FileInputStream fis = new FileInputStream(rfiles);
            byte[] bdata = new byte[fis.available()];
            fis.read(bdata);
            String sret = new String(bdata, "utf-8");
            fis.close();

            JSONObject obj = new JSONObject(sret);
            JSONArray list = obj.getJSONArray("list");
            JSONArray nlist = new JSONArray();
            int cnt = list.length();
            int ncur = 0;
            for (int cur = 0; cur < cnt; cur++) {
                JSONObject jitem = list.getJSONObject(cur);
                PDFGridAdt.SnatchItem item = new PDFGridAdt.SnatchItem();
                String file_path = jitem.getString("fpath");
                File file = new File(file_path);
                if (!file.exists()) {
                    continue;
                }
                nlist.put(ncur++, jitem);
                item.m_name = jitem.getString("fname");
                item.m_path = jitem.getString("fpath");
                item.m_item = new PDFGridItem(ctx, null);
                item.m_item.set_file(thread, item.m_name, item.m_path);
                items.add(item);
            }
            obj.put("list", nlist);
            String sval = obj.toString();

            FileOutputStream fos = new FileOutputStream(rfiles);
            fos.write(sval.getBytes());
            fos.close();
        } catch (Exception ex) {
        }
    }

    private static int findDocument(JSONArray jlist, String path) {
        if (jlist == null) return -1;
        int cnt = jlist.length();
        try {
            for (int cur = 0; cur < cnt; cur++) {
                JSONObject jitem = jlist.getJSONObject(cur);
                PDFGridAdt.SnatchItem item = new PDFGridAdt.SnatchItem();
                String file_path = jitem.getString("fpath");
                if (path.equals(file_path)) return cur;
            }
        } catch (Exception ex) {
        }
        return -1;
    }

    public static boolean insertDocument(Context ctx, String name, String path) {
        File dir = ctx.getExternalFilesDir(null);
        File rfiles = new File(dir, "recent.json");
        JSONObject jobj = null;
        JSONArray jlist = null;
        try {
            FileInputStream fis = new FileInputStream(rfiles);
            byte[] bdata = new byte[fis.available()];
            fis.read(bdata);
            String sret = new String(bdata, "utf-8");
            fis.close();

            jobj = new JSONObject(sret);
            JSONArray list = jobj.getJSONArray("list");
            jlist = new JSONArray();
            int cnt = list.length();
            int ncur = 0;
            for (int cur = 0; cur < cnt; cur++) {
                JSONObject jitem = list.getJSONObject(cur);
                PDFGridAdt.SnatchItem item = new PDFGridAdt.SnatchItem();
                String file_path = jitem.getString("fpath");
                File file = new File(file_path);
                if (!file.exists()) {
                    continue;
                }
                jlist.put(ncur++, jitem);
            }
            jobj.put("list", jlist);
        } catch (Exception ex) {
        }
        try {
            if (jobj == null) {
                jobj = new JSONObject();
                jlist = new JSONArray();
                jobj.put("list", jlist);
            }
            int icur = findDocument(jlist, path);
            if (icur >= 0)//already exists in list?
            {
                if (icur == 0) return false;
                //need to reorder to first item.
                Object fobj = jlist.get(icur);
                while (icur > 0) {
                    jlist.put(icur, jlist.get(icur - 1));
                    icur--;
                }
                jlist.put(0, fobj);
            } else//not exists in list
            {
                JSONObject nobj = new JSONObject();
                nobj.put("fpath", path);
                nobj.put("fname", name);
                icur = jlist.length();
                if (jlist.length() >= MAX_RECENT_COUNT) {
                    jlist.remove(icur - 1);
                    icur--;
                }
                while (icur > 0) {
                    jlist.put(icur, jlist.get(icur - 1));
                    icur--;
                }
                jlist.put(0, nobj);
            }
            FileOutputStream fos = new FileOutputStream(rfiles);
            String sval = jobj.toString();
            fos.write(sval.getBytes());
            fos.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
