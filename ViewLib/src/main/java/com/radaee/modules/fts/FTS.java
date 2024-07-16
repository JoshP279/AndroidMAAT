package com.radaee.modules.fts;

import com.google.gson.annotations.SerializedName;

/*
 * PDFMaster
 * Created by Nermeen on 20/11/2020.
 */
public class FTS {

    @SerializedName("page_index") private int pageIndex;
    @SerializedName("resultCount") private int occurrences;

    @SerializedName("rect_t") private double rectTop;
    @SerializedName("rect_l") private double rectLeft;
    @SerializedName("rect_r") private double rectRight;
    @SerializedName("rect_b") private double rectBottom;

    private transient String text;
    @SerializedName("text") private String snippet;
    private String document;

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public double getRectTop() {
        return rectTop;
    }

    public void setRectTop(double rectTop) {
        this.rectTop = rectTop;
    }

    public double getRectLeft() {
        return rectLeft;
    }

    public void setRectLeft(double rectLeft) {
        this.rectLeft = rectLeft;
    }

    public double getRectRight() {
        return rectRight;
    }

    public void setRectRight(double rectRight) {
        this.rectRight = rectRight;
    }

    public double getRectBottom() {
        return rectBottom;
    }

    public void setRectBottom(double rectBottom) {
        this.rectBottom = rectBottom;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }
}