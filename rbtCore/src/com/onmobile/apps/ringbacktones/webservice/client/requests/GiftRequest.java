package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class GiftRequest extends Request
{
	private String gifterID = null;
	private String gifteeID = null;
	private String calledNo = null;
	private String toneID = null;
	private String categoryID = null;
	private Date giftSentTime = null;
	private String offerID = null;
	private String subscriptionOfferID = null;
	private String chargeClass = null;
	private String subscriptionClass = null;
	private HashMap<String, String> infoMap = null;
	private Boolean isGifterConfRequired = null;
	private Boolean isGifteeConfRequired = null;
	private Boolean isConsentFlow = null;
	private Boolean isGifterCharged = null;

	/**
	 * 
	 */
	public GiftRequest()
	{
		super(null);
	}

	/**
	 * @param gifterID
	 * @param gifteeID
	 * @param calledNo
	 * @param mode
	 */
	public GiftRequest(String gifterID, String gifteeID, String calledNo, String mode)
	{
		super(null);
		this.gifterID = gifterID;
		this.gifteeID = gifteeID;
		this.calledNo = calledNo;
		this.mode = mode;
	}

	/**
	 * @param gifterID
	 * @param gifteeID
	 * @param calledNo
	 * @param toneID
	 * @param categoryID
	 * @param mode
	 */
	public GiftRequest(String gifterID, String gifteeID, String calledNo, String toneID, String categoryID, String mode)
	{
		super(null);
		this.gifterID = gifterID;
		this.gifteeID = gifteeID;
		this.calledNo = calledNo;
		this.toneID = toneID;
		this.categoryID = categoryID;
		this.mode = mode;
	}

	/**
	 * @param gifterID
	 * @param gifteeID
	 * @param giftSentTime
	 */
	public GiftRequest(String gifterID, String gifteeID, Date giftSentTime)
	{
		super(null);
		this.gifterID = gifterID;
		this.gifteeID = gifteeID;
		this.giftSentTime = giftSentTime;
	}

	/**
	 * @param gifterID
	 * @param gifteeID
	 * @param calledNo
	 * @param toneID
	 * @param categoryID
	 * @param mode
	 * @param giftSentTime
	 */
	public GiftRequest(String gifterID, String gifteeID, String calledNo, String toneID, String categoryID, String mode, Date giftSentTime)
	{
		super(null);
		this.gifterID = gifterID;
		this.gifteeID = gifteeID;
		this.calledNo = calledNo;
		this.toneID = toneID;
		this.categoryID = categoryID;
		this.mode = mode;
		this.giftSentTime = giftSentTime;
	}

	/**
	 * @return the gifterID
	 */
	public String getGifterID()
	{
		return gifterID;
	}

	/**
	 * @param gifterID the gifterID to set
	 */
	public void setGifterID(String gifterID)
	{
		this.gifterID = gifterID;
	}

	/**
	 * @return the gifteeID
	 */
	public String getGifteeID()
	{
		return gifteeID;
	}

	/**
	 * @param gifteeID the gifteeID to set
	 */
	public void setGifteeID(String gifteeID)
	{
		this.gifteeID = gifteeID;
	}

	/**
	 * @return the calledNo
	 */
	public String getCalledNo()
	{
		return calledNo;
	}

	/**
	 * @param calledNo the calledNo to set
	 */
	public void setCalledNo(String calledNo)
	{
		this.calledNo = calledNo;
	}

	/**
	 * @return the toneID
	 */
	public String getToneID()
	{
		return toneID;
	}

	/**
	 * @param toneID the toneID to set
	 */
	public void setToneID(String toneID)
	{
		this.toneID = toneID;
	}

	/**
	 * @return the categoryID
	 */
	public String getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(String categoryID)
	{
		this.categoryID = categoryID;
	}

	/**
	 * @return the giftSentTime
	 */
	public Date getGiftSentTime()
	{
		return giftSentTime;
	}

	/**
	 * @param giftSentTime the giftSentTime to set
	 */
	public void setGiftSentTime(Date giftSentTime)
	{
		this.giftSentTime = giftSentTime;
	}

	/**
	 * @return the offerID
	 */
	public String getOfferID()
	{
		return offerID;
	}

	/**
	 * @param offerID the offerID to set
	 */
	public void setOfferID(String offerID)
	{
		this.offerID = offerID;
	}

	/**
	 * @return the subscriptionOfferID
	 */
	public String getSubscriptionOfferID()
	{
		return subscriptionOfferID;
	}

	/**
	 * @param subscriptionOfferID the subscriptionOfferID to set
	 */
	public void setSubscriptionOfferID(String subscriptionOfferID)
	{
		this.subscriptionOfferID = subscriptionOfferID;
	}

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass()
	{
		return chargeClass;
	}

	/**
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass)
	{
		this.chargeClass = chargeClass;
	}

	/**
	 * @return the subscriptionClass
	 */
	public String getSubscriptionClass()
	{
		return subscriptionClass;
	}

	/**
	 * @param subscriptionClass the subscriptionClass to set
	 */
	public void setSubscriptionClass(String subscriptionClass)
	{
		this.subscriptionClass = subscriptionClass;
	}
	
	/**
	 * @return the infoMap
	 */
	public HashMap<String, String> getInfoMap()
	{
		return infoMap;
	}

	/**
	 * @param infoMap the infoMap to set
	 */
	public void setInfoMap(HashMap<String, String> infoMap)
	{
		this.infoMap = infoMap;
	}

	/**
	 * @return the isGifterConfRequired
	 */
	public Boolean getIsGifterConfRequired()
	{
		return isGifterConfRequired;
	}

	/**
	 * @param isGifterConfRequired the isGifterConfRequired to set
	 */
	public void setIsGifterConfRequired(Boolean isGifterConfRequired)
	{
		this.isGifterConfRequired = isGifterConfRequired;
	}

	/**
	 * @return the isGifteeConfRequired
	 */
	public Boolean getIsGifteeConfRequired()
	{
		return isGifteeConfRequired;
	}

	/**
	 * @param isGifteeConfRequired the isGifteeConfRequired to set
	 */
	public void setIsGifteeConfRequired(Boolean isGifteeConfRequired)
	{
		this.isGifteeConfRequired = isGifteeConfRequired;
	}
	
	
	/**
	 * @return the isConsentFlow
	 */
	public Boolean getIsConsentFlow()
	{
		return isConsentFlow;
	}
	
	/**
	 * @param isConsentFlow the isConsentFlow to set
	 */
	public void setIsConsentFlow(Boolean isConsentFlow)
	{
		this.isConsentFlow = isConsentFlow;
	}

	/**
	 * @param isGifterCharged the isGifterCharged to set
	 */
	public void setIsGifterCharged(Boolean isGifterCharged)
	{
		this.isGifterCharged = isGifterCharged;
	}

	/**
	 * @return the isGifterCharged
	 */
	public Boolean getIsGifterCharged()
	{
		return isGifterCharged;
	}

	
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (gifterID != null) requestParams.put(param_gifterID, gifterID);
		if (gifteeID != null) requestParams.put(param_gifteeID, gifteeID);
		if (calledNo != null) requestParams.put(param_calledNo, calledNo);
		if (toneID != null) requestParams.put(param_toneID, toneID);
		if (categoryID != null) requestParams.put(param_categoryID, categoryID);
		if (giftSentTime != null) requestParams.put(param_giftSentTime, dateFormat.format(giftSentTime));
		if (offerID != null) requestParams.put(param_offerID, offerID);
		if (subscriptionOfferID != null) requestParams.put(param_subscriptionOfferID, subscriptionOfferID);
		if (chargeClass != null) requestParams.put(param_chargeClass, chargeClass);
		if (subscriptionClass != null) requestParams.put(param_subscriptionClass, subscriptionClass);
		if (infoMap != null)
		{
			Set<Entry<String, String>> entryMap = infoMap.entrySet();
			for (Entry<String, String> entry : entryMap)
				requestParams.put(param_info + "_" + entry.getKey(), entry.getValue());
		}
		if (isGifterConfRequired != null) requestParams.put(param_isGifterConfRequired, (isGifterConfRequired ? YES : NO));
		if (isGifteeConfRequired != null) requestParams.put(param_isGifteeConfRequired, (isGifteeConfRequired ? YES : NO));
		if (isConsentFlow != null) requestParams.put(param_isConsentFlow, (isConsentFlow ? YES : NO));
		if (isGifterCharged != null) requestParams.put(param_isGifterCharged, (isGifterCharged ? YES : NO));

		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GiftRequest[calledNo=");
		builder.append(calledNo);
		builder.append(", categoryID=");
		builder.append(categoryID);
		builder.append(", chargeClass=");
		builder.append(chargeClass);
		builder.append(", giftSentTime=");
		builder.append(giftSentTime);
		builder.append(", gifteeID=");
		builder.append(gifteeID);
		builder.append(", gifterID=");
		builder.append(gifterID);
		builder.append(", infoMap=");
		builder.append(infoMap);
		builder.append(", offerID=");
		builder.append(offerID);
		builder.append(", subscriptionClass=");
		builder.append(subscriptionClass);
		builder.append(", subscriptionOfferID=");
		builder.append(subscriptionOfferID);
		builder.append(", toneID=");
		builder.append(toneID);
		builder.append(", isGifterConfRequired=");
		builder.append(isGifterConfRequired);
		builder.append(", isConsentFlow=");
		builder.append(isConsentFlow);
		builder.append(", param_isGifterCharged=");
		builder.append(param_isGifterCharged);
		builder.append("]");
		return builder.toString();
	}
	
}
