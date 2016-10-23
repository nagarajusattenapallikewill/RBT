package com.onmobile.apps.ringbacktones.timer;

import java.io.Serializable;
import java.util.Date;


public class BatchUpdateActivity implements Serializable {

    private Date startTime;
    private Date endTime;
    private long batchSize;
    private String activatedBy;
    private boolean prePaidYes;
    private String subscriberFile;
    private String wavFile;
    private boolean isClipWavFile;
    private boolean wavFileGiven;
    private boolean firstSel;
    
    
   
    /**
     * @return Returns the firstSel.
     */
    public boolean isFirstSel() {
        return this.firstSel;
    }
    /**
     * @param firstSel The firstSel to set.
     */
    public void setFirstSel(boolean firstSel) {
        this.firstSel = firstSel;
    }
    /**
     * @return Returns the subscriberFile.
     */
    public String getSubscriberFile() {
        return this.subscriberFile;
    }
    /**
     * @param subscriberFile The subscriberFile to set.
     */
    public void setSubscriberFile(String subscriberFile) {
        this.subscriberFile = subscriberFile;
    }
    /**
     * @return Returns the batchSize.
     */
    public long getBatchSize() {
        return this.batchSize;
    }
    /**
     * @param batchSize The batchSize to set.
     */
    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }
    /**
     * @return Returns the endTime.
     */
    public Date getEndTime() {
        return this.endTime;
    }
    /**
     * @param endTime The endTime to set.
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    /**
     * @return Returns the startTime.
     */
    public Date getStartTime() {
        return this.startTime;
    }
    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    /**
     * @return Returns the activatedBy.
     */
    public String getActivatedBy() {
        return this.activatedBy;
    }
    /**
     * @param activatedBy The activatedBy to set.
     */
    public void setActivatedBy(String activatedBy) {
        this.activatedBy = activatedBy;
    }
    /**
     * @return Returns the prePaidYes.
     */
    public boolean isPrePaidYes() {
        return this.prePaidYes;
    }
    /**
     * @param prePaidYes The prePaidYes to set.
     */
    public void setPrePaidYes(boolean prePaidYes) {
        this.prePaidYes = prePaidYes;
    }
    /**
     * @return Returns the isClipWavFile.
     */
    public boolean isClipWavFile() {
        return this.isClipWavFile;
    }
    /**
     * @param isClipWavFile The isClipWavFile to set.
     */
    public void setClipWavFile(boolean isClipWavFile) {
        this.isClipWavFile = isClipWavFile;
    }
    /**
     * @return Returns the wavFile.
     */
    public String getWavFile() {
        return this.wavFile;
    }
    /**
     * @param wavFile The wavFile to set.
     */
    public void setWavFile(String wavFile) {
        this.wavFile = wavFile;
    }
    /**
     * @return Returns the wavFileGiven.
     */
    public boolean isWavFileGiven() {
        return this.wavFileGiven;
    }
    /**
     * @param wavFileGiven The wavFileGiven to set.
     */
    public void setWavFileGiven(boolean wavFileGiven) {
        this.wavFileGiven = wavFileGiven;
    }
   }