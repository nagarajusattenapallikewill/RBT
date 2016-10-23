package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

public class CallLogRestRequest extends Request {

	public CallLogRestRequest(String subscriberID) {
		super(subscriberID);
	}

	private String callType = null;
	private Boolean restRequest = false;
	private int offSet = 0;
	private int pageSize = 0;

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public Boolean getRestRequest() {
		return restRequest;
	}

	public void setRestRequest(Boolean restRequest) {
		this.restRequest = restRequest;
	}

	public int getOffSet() {
		return offSet;
	}

	public void setOffSet(int offSet) {
		this.offSet = offSet;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public HashMap<String, String> getRequestParamsMap() {

		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (callType != null)
			requestParams.put(param_callType, callType + "");
		requestParams.put(param_offSet, offSet + "");
		requestParams.put(param_pageSize, pageSize + "");
		if (restRequest != null)
			requestParams.put(param_restRequest, restRequest + "");
		return requestParams;
	}
}
