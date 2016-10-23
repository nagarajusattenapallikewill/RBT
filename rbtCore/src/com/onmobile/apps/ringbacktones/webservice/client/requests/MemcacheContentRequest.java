package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

public class MemcacheContentRequest extends Request
{
	private String info = null;
	private String type = null;
	private String contentId = null;
	private String language = null;
	private String offSet = null;
	private String rowCount = null;
	private String prepaidYes = null;
	private String appName = null;

	/**
	 * 
	 */
	public MemcacheContentRequest()
	{
		super(null);
	}

	/**
	 * @param type
	 */
	public MemcacheContentRequest(String info, String contentId)
	{
		super(null);
		this.info = info;
		this.contentId = contentId;
	}


	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getOffSet() {
		return offSet;
	}

	public void setOffSet(String offSet) {
		this.offSet = offSet;
	}

	public String getRowCount() {
		return rowCount;
	}

	public void setRowCount(String rowCount) {
		this.rowCount = rowCount;
	}

	public String getPrepaidYes() {
		return prepaidYes;
	}

	public void setPrepaidYes(String prepaidYes) {
		this.prepaidYes = prepaidYes;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (info != null) requestParams.put(param_info, info);
		if (type != null) requestParams.put(param_type, type);
		if (contentId != null) requestParams.put(param_contentId, contentId);
		if (language != null) requestParams.put(param_language, language);
		if (offSet != null) requestParams.put(param_offSet, offSet);
		if (rowCount != null) requestParams.put(param_rowCount, rowCount);
		if (prepaidYes != null) requestParams.put(param_isPrepaid, prepaidYes);
		if (appName != null) requestParams.put(param_appName, appName);
		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ApplicationDetailsRequest[type = ");		
		builder.append(type);
		builder.append(", info = ");
		builder.append(info);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", contentId = ");
		builder.append(contentId);
		builder.append(", language = ");
		builder.append(language);
		builder.append(", offSet = ");
		builder.append(offSet);
		builder.append(", rowCount = ");
		builder.append(rowCount);
		builder.append(", browsingLanguage = ");
		builder.append(browsingLanguage);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", prepainYes = ");
		builder.append(prepaidYes);
		builder.append(", appName = ");
		builder.append(appName);
		builder.append("]");
		return builder.toString();
	}

	
}
