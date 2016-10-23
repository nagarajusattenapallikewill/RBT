/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.webservice.client.Connector;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 * @author abhinav.anand
 *
 */
public abstract class Request implements WebServiceConstants
{
	protected String subscriberID = null;
	protected String operatroID=null;
	protected String circleID = null;
	protected String mode = null;
	protected String modeInfo = null;
	protected String browsingLanguage = null;
	protected Boolean onlyResponse = null;
	protected String response = null;
	protected Class<? extends Connector> connectorClass = null;
	protected Boolean isFollowSameRbtResponse = false;
	protected Boolean isPostMethod = false;
	protected Boolean isDtoCRequest = false;
	protected Boolean isActiveUser = false;

	public Boolean getIsActiveUser() {
		return isActiveUser;
	}

	public void setIsActiveUser(Boolean isActiveUser) {
		this.isActiveUser = isActiveUser;
	}

	/**
	 * @param subscriberID
	 */
	public Request(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @param subscriberID
	 * @param circleID
	 */
	public Request(String subscriberID, String circleID)
	{
		this.subscriberID = subscriberID;
		this.circleID = circleID;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @return the circleID
	 */
	public String getCircleID()
	{
		return circleID;
	}

	/**
	 * @param circleID the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @return the mode
	 */
	public String getMode()
	{
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode)
	{
		this.mode = mode;
	}

	/**
	 * @return the modeInfo
	 */
	public String getModeInfo()
	{
		return modeInfo;
	}

	/**
	 * @param modeInfo the modeInfo to set
	 */
	public void setModeInfo(String modeInfo)
	{
		this.modeInfo = modeInfo;
	}

	/**
	 * @return the browsingLanguage
	 */
	public String getBrowsingLanguage()
	{
		return browsingLanguage;
	}

	/**
	 * @param browsingLanguage the browsingLanguage to set
	 */
	public void setBrowsingLanguage(String browsingLanguage)
	{
		this.browsingLanguage = browsingLanguage;
	}

	/**
	 * @return the onlyResponse
	 */
	public Boolean getOnlyResponse()
	{
		return onlyResponse;
	}

	/**
	 * @param onlyResponse the onlyResponse to set
	 */
	public void setOnlyResponse(Boolean onlyResponse)
	{
		this.onlyResponse = onlyResponse;
	}
	
	/**
	 * @return the isFollowSameRbtResponse
	 */
	public Boolean getIsFollowSameRbtResponse() {
		return isFollowSameRbtResponse;
	}

	/**
	 * @param isFollowSameRbtResponse the isFollowSameRbtResponse to set
	 */
	public void setIsFollowSameRbtResponse(Boolean isFollowSameRbtResponse) {
		this.isFollowSameRbtResponse = isFollowSameRbtResponse;
	}

	/**
	 * 
	 * @return isPostMethod
	 */
	public Boolean getIsPostMethod() {
		return isPostMethod;
	}

	/**
	 * 
	 * @param isPostMethod
	 */
	public void setIsPostMethod(Boolean isPostMethod) {
		this.isPostMethod = isPostMethod;
	}

	/**
	 * @return the response
	 */
	public String getResponse()
	{
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(String response)
	{
		this.response = response;
	}
	
	/**
	 * @return the operatroID
	 */
	public String getOperatroID() {
		return operatroID;
	}

	/**
	 * @param operatroID the operatroID to set
	 */

	public void setOperatroID(String operatroID) {
		this.operatroID = operatroID;
	}

	/**
	 * @return the connectorClass
	 */
	public Class<? extends Connector> getConnectorClass()
	{
		return connectorClass;
	}

	/**
	 * @param connectorClass the connectorClass to set
	 */
	public void setConnectorClass(Class<? extends Connector> connectorClass)
	{
		this.connectorClass = connectorClass;
	}
	
	/**
	 * @return the isDtoCRequest
	 */
	public Boolean getIsDtoCRequest() {
		return isDtoCRequest;
	}

	/**
	 * @param isDtoCRequest the isDtoCRequest to set
	 */
	public void setIsDtoCRequest(Boolean isDtoCRequest) {
		this.isDtoCRequest = isDtoCRequest;
	}

	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = new HashMap<String, String>();

		if (subscriberID != null) requestParams.put(param_subscriberID, subscriberID);
		if (circleID != null) requestParams.put(param_circleID, circleID);
		if (operatroID != null) requestParams.put(param_operatorID, operatroID);
		if (mode != null) requestParams.put(param_mode, mode);
		if (modeInfo != null) requestParams.put(param_modeInfo, modeInfo);
		if (browsingLanguage != null) requestParams.put(param_browsingLanguage, browsingLanguage);
		if (onlyResponse != null) requestParams.put(param_onlyResponse, (onlyResponse ? YES : NO));
		if (isFollowSameRbtResponse != null) requestParams.put(param_useSameResForConsent, (isFollowSameRbtResponse ? YES : NO));
		if (isPostMethod != null) requestParams.put(param_isPostMethod, (isPostMethod ? YES : NO));
		if (isDtoCRequest != null) requestParams.put(param_dtocRequest, (isDtoCRequest ? YES : NO));

		return requestParams;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#prepareRequestParams()
	 */
	public void prepareRequestParams(WebServiceContext task){
		if (task.containsKey(param_subscriberID)) {
			subscriberID = task.getString(param_subscriberID);
		}
		if (task.containsKey(param_circleID)) {
			circleID = task.getString(param_circleID);
		}
		if (task.containsKey(param_operatorID)) {
			operatroID = task.getString(param_operatorID);
		}
		if (task.containsKey(param_mode)) {
			mode = task.getString(param_mode);
		}
		if (task.containsKey(param_modeInfo)) {
			modeInfo = task.getString(param_modeInfo);
		}
		if (task.containsKey(param_browsingLanguage)) {
			browsingLanguage = task.getString(param_browsingLanguage);
		}
		if (task.containsKey(param_onlyResponse)) {
			onlyResponse = task.getString(param_onlyResponse).equalsIgnoreCase(
					YES);
		}
		if (task.containsKey(param_useSameResForConsent)) {
			isFollowSameRbtResponse = task.getString(param_useSameResForConsent).equalsIgnoreCase(
					YES);
		}
		if (task.containsKey(param_isPostMethod)) {
			isPostMethod = task.getString(param_isPostMethod).equalsIgnoreCase(
					YES);
		}
		if (task.containsKey(param_dtocRequest)) {
			isDtoCRequest = task.getString(param_dtocRequest).equalsIgnoreCase(
					YES);
		}
	}

}
