package com.cosmicdesigns.quiltkeeper.model;

import java.util.Map;

public class Quilt {

    private String mQuiltName, mFinishDate, mCost, mQuiltOwner, mLength, mWidth, mStiches, mHours, mMinutes, mSeconds,
        mMadeBy, mMachine, mFrame, mDesign, mBatting, mTopThread, mTopThreadColor, mBobbin, mBobbinColor,
        mNeedle, mSPI, mTowa, mTopTension, mNotes, url, filename, quiltKey, mTimeStamp, mCopied;

    public Quilt() {

    }

    public Quilt(String mQuiltName, String mFinishDate, String mCost, String mQuiltOwner, String mLength, String mWidth, String mStiches, String mHours,
                 String mMinutes, String mSeconds, String mMadeBy, String mMachine, String mFrame, String mDesign, String mBatting, String mTopThread, String mTopThreadColor,
                 String mBobbin, String mBobbinColor, String mNeedle, String mSPI, String mTowa, String mTopTension, String mNotes, String url, String filename, String quiltKey,
                 String mTimeStamp, String mCopied) {
        this.mQuiltName = mQuiltName;
        this.mFinishDate = mFinishDate;
        this.mCost = mCost;
        this.mQuiltOwner = mQuiltOwner;
        this.mLength = mLength;
        this.mWidth = mWidth;
        this.mStiches = mStiches;
        this.mHours = mHours;
        this.mMinutes = mMinutes;
        this.mSeconds = mSeconds;
        this.mMadeBy = mMadeBy;
        this.mMachine = mMachine;
        this.mFrame = mFrame;
        this.mDesign = mDesign;
        this.mBatting = mBatting;
        this.mTopThread = mTopThread;
        this.mTopThreadColor = mTopThreadColor;
        this.mBobbin = mBobbin;
        this.mBobbinColor = mBobbinColor;
        this.mNeedle = mNeedle;
        this.mSPI = mSPI;
        this.mTowa = mTowa;
        this.mTopTension = mTopTension;
        this.mNotes = mNotes;
        this.url = url;
        this.filename = filename;
        this.quiltKey = quiltKey;
        this.mTimeStamp = mTimeStamp;
        this.mCopied = mCopied;
    }

    public String getmQuiltName() {
        return mQuiltName;
    }

    public void setmQuiltName(String mQuiltName) {
        this.mQuiltName = mQuiltName;
    }

    public String getmFinishDate() {
        return mFinishDate;
    }

    public void setmFinishDate(String mFinishDate) {
        this.mFinishDate = mFinishDate;
    }

    public String getmCost() {
        return mCost;
    }

    public void setmCost(String mCost) {
        this.mCost = mCost;
    }

    public String getmQuiltOwner() {
        return mQuiltOwner;
    }

    public void setmQuiltOwner(String mQuiltOwner) {
        this.mQuiltOwner = mQuiltOwner;
    }

    public String getmLength() {
        return mLength;
    }

    public void setmLength(String mLength) {
        this.mLength = mLength;
    }

    public String getmWidth() {
        return mWidth;
    }

    public void setmWidth(String mWidth) {
        this.mWidth = mWidth;
    }

    public String getmStiches() {
        return mStiches;
    }

    public void setmStiches(String mStiches) {
        this.mStiches = mStiches;
    }

    public String getmHours() {
        return mHours;
    }

    public void setmHours(String mHours) {
        this.mHours = mHours;
    }

    public String getmMinutes() {
        return mMinutes;
    }

    public void setmMinutes(String mMinutes) {
        this.mMinutes = mMinutes;
    }

    public String getmSeconds() {
        return mSeconds;
    }

    public void setmSeconds(String mSeconds) {
        this.mSeconds = mSeconds;
    }

    public String getmMadeBy() {
        return mMadeBy;
    }

    public void setmMadeBy(String mMadeBy) {
        this.mMadeBy = mMadeBy;
    }

    public String getmMachine() {
        return mMachine;
    }

    public void setmMachine(String mMachine) {
        this.mMachine = mMachine;
    }

    public String getmFrame() {
        return mFrame;
    }

    public void setmFrame(String mFrame) {
        this.mFrame = mFrame;
    }

    public String getmDesign() {
        return mDesign;
    }

    public void setmDesign(String mDesign) {
        this.mDesign = mDesign;
    }

    public String getmBatting() {
        return mBatting;
    }

    public void setmBatting(String mBatting) {
        this.mBatting = mBatting;
    }

    public String getmTopThread() {
        return mTopThread;
    }

    public void setmTopThread(String mTopThread) {
        this.mTopThread = mTopThread;
    }

    public String getmTopThreadColor() {
        return mTopThreadColor;
    }

    public void setmTopThreadColor(String mTopThreadColor) {
        this.mTopThreadColor = mTopThreadColor;
    }

    public String getmBobbin() {
        return mBobbin;
    }

    public void setmBobbin(String mBobbin) {
        this.mBobbin = mBobbin;
    }

    public String getmBobbinColor() {
        return mBobbinColor;
    }

    public void setmBobbinColor(String mBobbinColor) {
        this.mBobbinColor = mBobbinColor;
    }

    public String getmNeedle() {
        return mNeedle;
    }

    public void setmNeedle(String mNeedle) {
        this.mNeedle = mNeedle;
    }

    public String getmSPI() {
        return mSPI;
    }

    public void setmSPI(String mSPI) {
        this.mSPI = mSPI;
    }

    public String getmTowa() {
        return mTowa;
    }

    public void setmTowa(String mTowa) {
        this.mTowa = mTowa;
    }

    public String getmTopTension() {
        return mTopTension;
    }

    public void setmTopTension(String mTopTension) {
        this.mTopTension = mTopTension;
    }

    public String getmNotes() {
        return mNotes;
    }

    public void setmNotes(String mNotes) {
        this.mNotes = mNotes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getQuiltKey() {
        return quiltKey;
    }

    public void setQuiltKey(String quiltKey) {
        this.quiltKey = quiltKey;
    }

    public String getmTimeStamp() {
        return mTimeStamp;
    }

    public void setmTimeStamp(String mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }

    public String getmCopied() {
        return mCopied;
    }

    public void setmCopied(String mCopied) {
        this.mCopied = mCopied;
    }

    @Override
    public String toString() {
        return "Quilt{" +
                "mQuiltName='" + mQuiltName + '\'' +
                ", mFinishDate='" + mFinishDate + '\'' +
                ", mCost='" + mCost + '\'' +
                ", mQuiltOwner='" + mQuiltOwner + '\'' +
                ", mLength='" + mLength + '\'' +
                ", mWidth='" + mWidth + '\'' +
                ", mStiches='" + mStiches + '\'' +
                ", mHours='" + mHours + '\'' +
                ", mMinutes='" + mMinutes + '\'' +
                ", mSeconds='" + mSeconds + '\'' +
                ", mMadeBy='" + mMadeBy + '\'' +
                ", mMachine='" + mMachine + '\'' +
                ", mFrame='" + mFrame + '\'' +
                ", mDesign='" + mDesign + '\'' +
                ", mBatting='" + mBatting + '\'' +
                ", mTopThread='" + mTopThread + '\'' +
                ", mTopThreadColor='" + mTopThreadColor + '\'' +
                ", mBobbin='" + mBobbin + '\'' +
                ", mBobbinColor='" + mBobbinColor + '\'' +
                ", mNeedle='" + mNeedle + '\'' +
                ", mSPI='" + mSPI + '\'' +
                ", mTowa='" + mTowa + '\'' +
                ", mTopTension='" + mTopTension + '\'' +
                ", mNotes='" + mNotes + '\'' +
                ", url='" + url + '\'' +
                ", filename='" + filename + '\'' +
                ", quiltKey='" + quiltKey + '\'' +
                ", mTimeStamp='" + mTimeStamp + '\'' +
                ", mCopied='" + mCopied + '\'' +
                '}';
    }
}
