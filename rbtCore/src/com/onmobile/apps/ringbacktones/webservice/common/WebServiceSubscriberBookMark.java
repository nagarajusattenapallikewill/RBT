/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.Arrays;

/**
 * @author vinayasimha.patil
 *
 */
public class WebServiceSubscriberBookMark
{
	private String subscriberID = null;
	private int toneID;
	private int shuffleID;
	private String toneName = null;
	private String toneType = null;
	private String[] previewFiles = null;
	private String[] rbtFiles = null;
	private int categoryID;
    private String clipVcode = null;
	/**
	 * 
	 */
	public WebServiceSubscriberBookMark()
	{

	}


	/**
	 * @param subscriberID
	 * @param toneID
	 * @param shuffleID
	 * @param toneName
	 * @param toneType
	 * @param previewFiles
	 * @param rbtFiles
	 * @param categoryID
	 */
	public WebServiceSubscriberBookMark(String subscriberID, int toneID,
			int shuffleID, String toneName, String toneType,
			String[] previewFiles, String[] rbtFiles, int categoryID)
	{
		this.subscriberID = subscriberID;
		this.toneID = toneID;
		this.shuffleID = shuffleID;
		this.toneName = toneName;
		this.toneType = toneType;
		this.previewFiles = previewFiles;
		this.rbtFiles = rbtFiles;
		this.categoryID = categoryID;
	}


	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @return the toneID
	 */
	public int getToneID()
	{
		return toneID;
	}

	/**
	 * @return the shuffleID
	 */
	public int getShuffleID()
	{
		return shuffleID;
	}

	/**
	 * @return the toneName
	 */
	public String getToneName()
	{
		return toneName;
	}

	/**
	 * @return the toneType
	 */
	public String getToneType()
	{
		return toneType;
	}

	/**
	 * @return the previewFiles
	 */
	public String[] getPreviewFiles()
	{
		return previewFiles;
	}

	/**
	 * @return the rbtFiles
	 */
	public String[] getRbtFiles()
	{
		return rbtFiles;
	}

	/**
	 * @return the categoryID
	 */
	public int getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @param toneID the toneID to set
	 */
	public void setToneID(int toneID)
	{
		this.toneID = toneID;
	}

	/**
	 * @param shuffleID the shuffleID to set
	 */
	public void setShuffleID(int shuffleID)
	{
		this.shuffleID = shuffleID;
	}

	/**
	 * @param toneName the toneName to set
	 */
	public void setToneName(String toneName)
	{
		this.toneName = toneName;
	}

	/**
	 * @param toneType the toneType to set
	 */
	public void setToneType(String toneType)
	{
		this.toneType = toneType;
	}

	/**
	 * @param previewFiles the previewFiles to set
	 */
	public void setPreviewFiles(String[] previewFiles)
	{
		this.previewFiles = previewFiles;
	}

	/**
	 * @param rbtFiles the rbtFiles to set
	 */
	public void setRbtFiles(String[] rbtFiles)
	{
		this.rbtFiles = rbtFiles;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(int categoryID)
	{
		this.categoryID = categoryID;
	}

	public String getClipVcode() {
		return clipVcode;
	}


	public void setClipVcode(String clipVcode) {
		this.clipVcode = clipVcode;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceSubscriberBookMark[categoryID = ");
		builder.append(categoryID);
		builder.append(", previewFiles = ");
		builder.append(Arrays.toString(previewFiles));
		builder.append(", rbtFiles = ");
		builder.append(Arrays.toString(rbtFiles));
		builder.append(", shuffleID = ");
		builder.append(shuffleID);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", toneID = ");
		builder.append(toneID);
		builder.append(", toneName = ");
		builder.append(toneName);
		builder.append(", toneType = ");
		builder.append(toneType);
		builder.append(", clipVcode = ");
		builder.append(clipVcode);
		builder.append("]");
		return builder.toString();
	}
}
