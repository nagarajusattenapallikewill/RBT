/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

/**
 * @author vinayasimha.patil
 *
 */
public class WebServiceCopyData
{
	private String result = null;
	private String subscriberID = null;
	private String fromSubscriber = null;
	private int categoryID;
	private int toneID;
	private String toneName;
	private String toneType = null;
	private int status;
	private String previewFile = null;
	private String amount = null;
	private String period = null;
	private String chargeClass = null;
	private boolean isShuffleOrLoop = false;

	/**
	 * 
	 */
	public WebServiceCopyData()
	{

	}

	/**
	 * @param result
	 * @param subscriberID
	 * @param fromSubscriber
	 * @param categoryID
	 * @param toneID
	 * @param toneName
	 * @param toneType
	 * @param status
	 * @param previewFile
	 * @param amount
	 * @param period
	 * @param chargeClass
	 */
	public WebServiceCopyData(String result, String subscriberID,
			String fromSubscriber, int categoryID, int toneID, String toneName,
			String toneType, int status, String previewFile, String amount,
			String period, String chargeClass)
	{
		this.result = result;
		this.subscriberID = subscriberID;
		this.fromSubscriber = fromSubscriber;
		this.categoryID = categoryID;
		this.toneID = toneID;
		this.toneName = toneName;
		this.toneType = toneType;
		this.status = status;
		this.previewFile = previewFile;
		this.amount = amount;
		this.period = period;
		this.chargeClass = chargeClass;
	}

	/**
	 * @return the result
	 */
	public String getResult()
	{
		return result;
	}

	public boolean isShuffleOrLoop() {
		return isShuffleOrLoop;
	}

	public void setShuffleOrLoop(boolean isShuffleOrLoop) {
		this.isShuffleOrLoop = isShuffleOrLoop;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @return the fromSubscriber
	 */
	public String getFromSubscriber()
	{
		return fromSubscriber;
	}

	/**
	 * @return the categoryID
	 */
	public int getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @return the toneID
	 */
	public int getToneID()
	{
		return toneID;
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
	 * @return the status
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * @return the previewFile
	 */
	public String getPreviewFile()
	{
		return previewFile;
	}

	/**
	 * @return the amount
	 */
	public String getAmount()
	{
		return amount;
	}

	/**
	 * @return the period
	 */
	public String getPeriod()
	{
		return period;
	}

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass()
	{
		return chargeClass;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(String result)
	{
		this.result = result;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @param fromSubscriber the fromSubscriber to set
	 */
	public void setFromSubscriber(String fromSubscriber)
	{
		this.fromSubscriber = fromSubscriber;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(int categoryID)
	{
		this.categoryID = categoryID;
	}

	/**
	 * @param toneID the toneID to set
	 */
	public void setToneID(int toneID)
	{
		this.toneID = toneID;
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
	 * @param status the status to set
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}

	/**
	 * @param rbtFile the previewFile to set
	 */
	public void setPreviewFile(String previewFile)
	{
		this.previewFile = previewFile;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(String amount)
	{
		this.amount = amount;
	}

	/**
	 * @param validity the period to set
	 */
	public void setPeriod(String period)
	{
		this.period = period;
	}

	/**
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass)
	{
		this.chargeClass = chargeClass;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceCopyData[amount = ");
		builder.append(amount);
		builder.append(", categoryID = ");
		builder.append(categoryID);
		builder.append(", chargeClass = ");
		builder.append(chargeClass);
		builder.append(", fromSubscriber = ");
		builder.append(fromSubscriber);
		builder.append(", period = ");
		builder.append(period);
		builder.append(", previewFile = ");
		builder.append(previewFile);
		builder.append(", result = ");
		builder.append(result);
		builder.append(", status = ");
		builder.append(status);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", toneID = ");
		builder.append(toneID);
		builder.append(", toneName = ");
		builder.append(toneName);
		builder.append(", toneType = ");
		builder.append(toneType);
		builder.append(", isShuffleOrLoop = ");
		builder.append(isShuffleOrLoop);
		builder.append("]");
		return builder.toString();
	}
}
