package com.radaee.modules.fts;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonWriter;
import com.radaee.pdf.Document;
import com.radaee.pdf.Page;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/*
 * PDFMaster
 * Created by Nermeen on 20/11/2020.
 */
public class TextExtractor {

    private static float sHorzGap;
    private static float sVertGap;
    private static float sFontHeightDiff;

    private static float[] sBlockRect;
    private static float[] sCurCharRect;
    private static float[] sNextCharRect;

    public static final String PAGES = "pages";
    private static final String RECT_TOP = "rect_t";
    private static final String RECT_LEFT = "rect_l";
    private static final String RECT_RIGHT = "rect_r";
    private static final String RECT_BOTTOM = "rect_b";

    static class PageBlocks {
        int page;
        List<Block> blocks;
    }

    static class Block {
        String text;
        @SerializedName(RECT_TOP) float rectTop;
        @SerializedName(RECT_LEFT) float rectLeft;
        @SerializedName(RECT_RIGHT) float rectRight;
        @SerializedName(RECT_BOTTOM) float rectBottom;
    }

    /**
     * Extracts all the text from a pdf and puts it into a json file.
     *
     * @param document the input document
     * @return 0: no text, 1:success, -1:failure
     */
    static int extractDocumentText(Document document, String outputPath) {
        try {
            Gson gson = new Gson();
            JsonWriter jsonWriter = null;
            boolean firstEntry = true;
            int pageCount = document.GetPageCount();

            for (int i = 0 ; i < pageCount; i++) {
                Page page = document.GetPage(i);
                if(page != null) {
                    PageBlocks pageBlocks = extractPageText(page, i);
                    if(pageBlocks != null) {
                        if(firstEntry) {
                            jsonWriter = new JsonWriter(new FileWriter(outputPath));
                            jsonWriter.beginObject(); // {
                            jsonWriter.name(PAGES); // "pages":
                            jsonWriter.beginArray(); // [
                            firstEntry = false;
                        }
                        gson.toJson(pageBlocks, PageBlocks.class, jsonWriter);
                    }
                    page.Close();
                }
            }

            if(!firstEntry) {
                jsonWriter.endArray(); // ]
                jsonWriter.endObject(); // }
                jsonWriter.close();
                return 1;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Extracts text from pages, divided into blocks
     *
     * @param page current page object
     * @param pageIndex page index in the document
     * @return page's text into json format
     */
    private static PageBlocks extractPageText(Page page, int pageIndex) {
        try {
            page.ObjsStart();

            int sBlockStartIndex = 0;
            sBlockRect = new float[4];
            sCurCharRect = new float[4];
            sNextCharRect = new float[4];

            page.ObjsGetCharRect(0, sBlockRect);

            List<Block> blocksArray = null;
            int charCount = page.ObjsGetCharCount();
            for(int charIndex = 0; charIndex < charCount; charIndex++) {
                page.ObjsGetCharRect(charIndex, sCurCharRect); //get char's box in PDF coordinate system

                if (charIndex < charCount - 1)
                    page.ObjsGetCharRect(charIndex + 1, sNextCharRect); //get next char's box in PDF coordinate system

                sBlockRect = adjustBlockRect(sCurCharRect, sBlockRect);

                boolean nextBeforeBlock = isNextOutOfBlock();

                if(startNewTextBlock() || nextBeforeBlock || charIndex >= charCount - 1) {
                    if(nextBeforeBlock || charIndex >= charCount - 1)
                        charIndex++;
                    String text = page.ObjsGetString(sBlockStartIndex, charIndex);
                    if(text != null && text.length() > 0)
                        text = text.trim();
                    if(text != null && text.length() > 0) { //add to json
                        text = handleUtf16Chars(text.trim());
                        text = handleSpecialChars(text);
                        Block block = createBlockJson(text);
                        if(block != null) {
                            if(blocksArray == null) blocksArray =  new ArrayList<>();
                            blocksArray.add(block);
                        }
                    }
                    int newIndex = nextBeforeBlock ? charIndex : charIndex + 1;
                    if(newIndex >= charCount)
                        break;
                    page.ObjsGetCharRect(nextBeforeBlock ? charIndex : charIndex + 1, sBlockRect); //reset block rect with next char's rect
                    sBlockStartIndex = nextBeforeBlock ? charIndex : charIndex + 1;
                }
            }

            PageBlocks pageBlocks = null;
            if(blocksArray != null) {
                pageBlocks = new PageBlocks();
                pageBlocks.page = pageIndex;
                pageBlocks.blocks = blocksArray;
            }

            return pageBlocks;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds the passed character rectangle to the whole paragraph rectangle.
     *
     * @param mCharRect current character rectangle
     * @param mRect the block rectangle.
     * @return the modified paragraph rectangle.
     */
    private static float[] adjustBlockRect(float[] mCharRect, float[] mRect) {
        try {
            if(mRect == null)
                mRect = new float[]{mCharRect[0], mCharRect[1], mCharRect[2], mCharRect[3]};
            if(mRect[0] > mCharRect[0]) //left
                mRect[0] = mCharRect[0];
            if(mRect[1] > mCharRect[1]) //top
                mRect[1] = mCharRect[1];
            if(mRect[2] < mCharRect[2]) //right
                mRect[2] = mCharRect[2];
            if(mRect[3] < mCharRect[3]) //bottom
                mRect[3] = mCharRect[3];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mRect;
    }

    /**
     * detect case of next char is actually out of the range of current block
     * @return true if the next character is out of current block, false otherwise
     */
    private static boolean isNextOutOfBlock() {
        boolean sameLine = Math.abs(sNextCharRect[1] - sBlockRect[1]) < 1.5 && Math.abs(sNextCharRect[3] - sBlockRect[3]) < 1.5;
        float gap = (sNextCharRect[3] - sNextCharRect[1]) / 2;
        return (sameLine && sNextCharRect[0] < sBlockRect[0] && sNextCharRect[2] < sBlockRect[0]) ||
                (!sameLine && sNextCharRect[0] - sBlockRect[2] > gap && sNextCharRect[2] - sBlockRect[2] > gap);
    }

    private static boolean startNewTextBlock() {
        sFontHeightDiff = Math.abs((sNextCharRect[3] - sNextCharRect[1]) - (sCurCharRect[3] - sCurCharRect[1]));
        sHorzGap = Math.abs(sNextCharRect[0] - sCurCharRect[2]); //horizontal gap
        sVertGap = sNextCharRect[1] - sCurCharRect[3]; //vertical gap

        boolean sameLine = Math.abs(sCurCharRect[1] - sNextCharRect[1]) < 1.5 && Math.abs(sCurCharRect[3] - sNextCharRect[3]) < 1.5;
        boolean sameColumn = Math.abs(sCurCharRect[0] - sNextCharRect[0]) < 1.5 && Math.abs(sCurCharRect[2] - sNextCharRect[2]) < 1.5;
        boolean sameBlock = Math.abs(sBlockRect[0] - sNextCharRect[0]) < 3 && Math.abs(sBlockRect[2] - sNextCharRect[2]) > 0;

        if((sFontHeightDiff >= 2 && !sameColumn) || (sameLine && sHorzGap >= 85) || (sameBlock && (sVertGap <= -30 || sVertGap >= 20))
                || (!sameLine && !sameBlock && (sVertGap >= 15 || sVertGap <= -43 || sHorzGap >= 800))) { //save current block and start a new one
            //System.out.println("font diff = " + sFontHeightDiff + " ... horz gap = " + sHorzGap + " ... vert gap = " + sVertGap);
            return true;
        }
        return false;
    }

    private static Block createBlockJson(String text) {
        try {
            Block block = new Block();
            block.text = text;
            block.rectTop = sBlockRect[1]; //top
            block.rectLeft = sBlockRect[0]; //left
            block.rectRight = sBlockRect[2]; //right
            block.rectBottom = sBlockRect[3]; //bottom
            return block;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String handleUtf16Chars(String input) {
        input = input.replaceAll("\u0092", "'");
        input = input.replaceAll("\u0095", "•");
        input = input.replaceAll("\u00B0", "°");
        return input;
        //return new String(Charset.forName(StandardCharsets.UTF_8.name()).encode(input).array(), StandardCharsets.UTF_8);
    }

    private static String handleSpecialChars(String input) {
        input = input.replaceAll("’", "'");
        input = input.replaceAll("‘", "'");
        input = input.replaceAll("“", "\"");
        input = input.replaceAll("”", "\"");
        input = input.replaceAll("\r\n", " ");
        //input = new String(input.getBytes(), StandardCharsets.UTF_8);
        return input;
    }
}