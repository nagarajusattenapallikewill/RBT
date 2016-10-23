package com.onmobile.apps.ringbacktones.webservice.client.requests;


import java.util.HashMap;

/**
 * @author abhinav.anand
 *
 */
public class SngRequest extends Request{
	
	private String userId = null;
	private String sngId = null;
	private String rbtType =null;
	
	public SngRequest(String subscriberID) {
		super(subscriberID);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSngId() {
		return sngId;
	}

	public void setSngId(String sngId) {
		this.sngId = sngId;
	}

	public String getRbtType() {
		return rbtType;
	}

	public void setRbtType(String rbtType) {
		this.rbtType = rbtType;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();
		if (userId != null) requestParams.put(param_userId,userId);
		if (sngId != null) requestParams.put(param_sngId, sngId);
		if (rbtType != null) requestParams.put(param_rbtType, rbtType);

		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SngRequest[userId = ");
		builder.append(userId);
		builder.append(", sngId = ");
		builder.append(sngId);
		builder.append(", rbtType = ");
		builder.append(rbtType);
		builder.append("]");
		return builder.toString();
	}
}
