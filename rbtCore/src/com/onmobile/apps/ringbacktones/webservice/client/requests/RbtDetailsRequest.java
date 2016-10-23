package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.jcraft.jsch.Logger;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
/**
 * @author senthil.raja
 *
 */
public class RbtDetailsRequest extends Request
{
	private String calledNo = null;
	private String mmContext = null;
	private String info = null;
	private String clipName = null;
	private Date startDate = null;
	private Date endDate = null;
	private String status = null;
	private Boolean isPrepaid = null;
	private String type = null;
	private String language = null;
	private String offerID = null;
	private String offerType = null;
	private String classType = null;
	private String clipID = null;
	private String nextBillDate = null;
	private boolean consentInd = false;
	private boolean redirectionRequired = false;
	private String protocolNo = null;
	private Boolean chrgDetailsReq = null;  
	private String selstatus=null;

	/**
	 * Added by Sreekar for sending offer parameters
	 */
	private HashMap<String, String> extraInfoMap = new HashMap<String, String>();

	/**
	 * @param subscriberID
	 */
	public RbtDetailsRequest(String subscriberID)
	{
		super(subscriberID);
	}

	/**
	 * @param subscriberID
	 * @param calledNo
	 */
	public RbtDetailsRequest(String subscriberID, String calledNo)
	{
		super(subscriberID);
		this.calledNo = calledNo;
	}

	/**
	 * @param subscriberID
	 * @param calledNo
	 * @param mode
	 */
	public RbtDetailsRequest(String subscriberID, String calledNo, String mode)
	{
		super(subscriberID);
		this.calledNo = calledNo;
		this.mode = mode;
	}

	
	public RbtDetailsRequest(String subscriberID, String type, String id,String status)
	{
		super(subscriberID);
		this.type=type;
		this.calledNo=id;
		setSelstatus(status);
	}
	/**
	 * @param subscriberID
	 * @param calledNo
	 * @param mode
	 * @param mmContext
	 * @param info
	 */
	public RbtDetailsRequest(String subscriberID, String calledNo, String mode, String mmContext, String info)
	{
		super(subscriberID);
		this.calledNo = calledNo;
		this.mode = mode;
		this.mmContext = mmContext;
		this.info = info;
	}

	/**
	 * @param subscriberID
	 * @param clipName
	 * @param startDate
	 * @param endDate
	 */
	public RbtDetailsRequest(String subscriberID, String clipName, Date startDate, Date endDate)
	{
		super(subscriberID);
		this.clipName = clipName;
		this.startDate = startDate;
		this.endDate = endDate;
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
	 * @return the mmContext
	 */
	public String getMmContext()
	{
		return mmContext;
	}

	/**
	 * @param mmContext the mmContext to set
	 */
	public void setMmContext(String mmContext)
	{
		this.mmContext = mmContext;
	}

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info)
	{
		this.info = info;
	}

	/**
	 * @return the clipName
	 */
	public String getClipName()
	{
		return clipName;
	}

	/**
	 * @param clipName the clipName to set
	 */
	public void setClipName(String clipName)
	{
		this.clipName = clipName;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate()
	{
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * @return the isPrepaid
	 */
	public Boolean getIsPrepaid()
	{
		return isPrepaid;
	}

	/**
	 * @param isPrepaid the isPrepaid to set
	 */
	public void setIsPrepaid(Boolean isPrepaid)
	{
		this.isPrepaid = isPrepaid;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the language
	 */
	public String getLanguage()
	{
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * @return the extraInfoMap
	 */
	public HashMap<String, String> getExtraInfoMap()
	{
		return extraInfoMap;
	}

	/**
	 * @return the offerID
	 */
	public String getOfferID()
	{
		return offerID;
	}

	/**
	 * @param offerID to set
	 */
	public void setOfferID(String offerID)
	{
		this.offerID = offerID;
	}
	
	/**
	 * @return the offerType
	 */
	public String getOfferType()
	{
		return offerType;
	}
	
	/**
	 * @param offerType the offerType to set
	 */
	public void setOfferType(String offerType)
	{
		this.offerType = offerType;
	}

	/**
	 * @param extraInfoMap the extraInfoMap to set
	 */
	public void setExtraInfoMap(HashMap<String, String> extraInfoMap)
	{
		this.extraInfoMap = extraInfoMap;
	}
	

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}
	
	public String getClipID() {
		return clipID;
	}

	public void setClipID(String clipID) {
		this.clipID = clipID;
	}

	public String getNextBillDate() {
		return nextBillDate;
	}

	public void setNextBillDate(String nextBillDate) {
		this.nextBillDate = nextBillDate;
	}
	
	public boolean isConsentInd() {
		return consentInd;
	}

	public void setConsentInd(boolean consentInd) {
		this.consentInd = consentInd;
	}

	public boolean isRedirectionRequired() {
		return redirectionRequired;
	}

	public String getProtocolNo() {
		return protocolNo;
	}

	public void setProtocolNo(String protocolNo) {
		this.protocolNo = protocolNo;
	}

	public void setRedirectionRequired(boolean redirectionRequired) {
		this.redirectionRequired = redirectionRequired;
	}

	public Boolean getChrgDetailsReq() {
		return chrgDetailsReq;
	}

	public void setChrgDetailsReq(Boolean chrgDetailsReq) {
		this.chrgDetailsReq = chrgDetailsReq;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (calledNo != null) requestParams.put(param_calledNo, calledNo);
		if (mmContext != null) requestParams.put(param_mmContext, mmContext);
		if (info != null) requestParams.put(param_info, info);
		if (clipName != null) requestParams.put(param_clipName, clipName);
		if (startDate != null) requestParams.put(param_startDate, dateFormat.format(startDate));
		if (endDate != null) requestParams.put(param_endDate, dateFormat.format(endDate));
		if (status != null) requestParams.put(param_status, status);
		if (isPrepaid != null) requestParams.put(param_isPrepaid, (isPrepaid ? YES : NO));
		if (type != null) requestParams.put(param_type, type);
		if (language != null) requestParams.put(param_language, language);
		if (offerID != null) requestParams.put(param_offerID, offerID);
		if (offerType != null) requestParams.put(param_offerType, offerType);
		if (classType != null) requestParams.put(param_classType, classType);
		if (clipID != null) requestParams.put(param_clipID, clipID);
		if (nextBillDate != null) requestParams.put(param_showNextBillDate, nextBillDate);
		if (consentInd) requestParams.put(param_preConsent, "true");
		if (redirectionRequired) requestParams.put(param_redirectionRequired, "true");
		if (protocolNo != null) requestParams.put(param_protocolNo, protocolNo);
		if (chrgDetailsReq != null) requestParams.put(param_chrgDetailsReq, chrgDetailsReq?"TRUE":"FALSE");
		if (selstatus != null) requestParams.put(param_selstatus,selstatus);
			
			
			
		if (extraInfoMap != null)
		{
			Set<Entry<String, String>> entrySet = extraInfoMap.entrySet();
			for (Entry<String, String> entry : entrySet)
				requestParams.put(param_extraInfo + "_" + entry.getKey(), entry.getValue());
		}
		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("RbtDetailsRequest[browsingLanguage = ");
		builder.append(browsingLanguage);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", modeInfo = ");
		builder.append(modeInfo);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", calledNo = ");
		builder.append(calledNo);
		builder.append(", clipName = ");
		builder.append(clipName);
		builder.append(", endDate = ");
		builder.append(endDate);
		builder.append(", extraInfoMap = ");
		builder.append(extraInfoMap);
		builder.append(", info = ");
		builder.append(info);
		builder.append(", isPrepaid = ");
		builder.append(isPrepaid);
		builder.append(", language = ");
		builder.append(language);
		builder.append(", mmContext = ");
		builder.append(mmContext);
		builder.append(", offerID = ");
		builder.append(offerID);
		builder.append(", offerType = ");
		builder.append(offerType);
		builder.append(", startDate = ");
		builder.append(startDate);
		builder.append(", status = ");
		builder.append(status);
		builder.append(", type = ");
		builder.append(type);
		builder.append(", classType = ");
		builder.append(classType);
		builder.append(", clipID = ");
		builder.append(clipID);
		builder.append(", consentInd = ");
		builder.append(consentInd);
		builder.append(", redirectionRequired = ");
		builder.append(redirectionRequired);
		builder.append(", protocolNo = ");
		builder.append(protocolNo);
		builder.append(", chrgDetailsReq = ");
		builder.append(chrgDetailsReq);
		builder.append(", selstatus = ");
		builder.append(selstatus);
		builder.append("]");
		return builder.toString();
	}

	public String getSelstatus() {
		return selstatus;
	}

	public void setSelstatus(String selstatus) {
		this.selstatus = selstatus;
	}
}