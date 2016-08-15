package com.thespeakers_studio.thespeakersstudioapp.model;

import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 7/18/2016.
 */
public class OutlineItem {
    /*
    private ArrayList<OutlineItem> mSubItems;
    private String mText;
    private long mDuration;
    private int mOrder;
    */
    private String mId;
    private String mParentId;
    private int mOrder;
    private String mText;
    private String mAnswerId;
    private boolean mFromDB;
    private long mDuration;
    private String mPresentationId;
    private long mTimedDuration;

    public static final String INTRO = "intro";
    public static final String CONCLUSION = "conclusion";
    public static final String NO_PARENT = "no_parent";

    public OutlineItem (String id, String parent, int order, String text, String answer, boolean fromDB, long duration, String presentation) {
        mId = id;
        mParentId = parent;
        mOrder = order;
        mText = text;
        mAnswerId = answer;
        mFromDB = fromDB;
        mDuration = duration;
        mPresentationId = presentation;
        mTimedDuration = 0;
    }

    // for the intro / conclusion items
    public OutlineItem (String id, int order, String text, String presentation) {
        this(id, NO_PARENT, order, text, "", false, 0, presentation);
    }

    // for manually setting everything up
    public OutlineItem () {
    }

    public void setIsFromDB() {
        mFromDB = true;
    }
    public boolean getIsFromDB() {
        return mFromDB;
    }

    public String getId() {
        return mId;
    }
    public void setId(String id) {
        mId = id;
    }

    public String getAnswerId() {
        return mAnswerId;
    }
    public void setAnswerId(String answer) {
        mAnswerId = answer;
    }

    public long getDuration() {
        return mDuration;
    }
    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getParentId () {
        return mParentId;
    }
    public void setParentId(String parent) {
        mParentId = parent;
    }

    public int getOrder () {
        return mOrder;
    }
    public void setOrder(int order) {
        mOrder = order;
    }

    public String getPresentationId() {
        return mPresentationId;
    }
    public void setPresentationId(String presentation) {
        mPresentationId = presentation;
    }

    public String getText() {
        return mText;
    }
    public void setText(String text) {
        mText = text;
    }

    public long getTimedDuration() {
        return mTimedDuration;
    }
    public void setTimedDuration(Long duration) {
        mTimedDuration = duration;
    }

    public static OutlineItem createDurationItem (OutlineItem item) {
        OutlineItem durationItem = new OutlineItem();
        durationItem.setPresentationId(item.getPresentationId());
        durationItem.setAnswerId(item.getAnswerId());
        durationItem.setParentId(item.getParentId());
        durationItem.setDuration(item.getTimedDuration());
        durationItem.setIsFromDB();

        return durationItem;
    }
}
