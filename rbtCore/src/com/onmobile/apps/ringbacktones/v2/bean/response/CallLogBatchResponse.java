package com.onmobile.apps.ringbacktones.v2.bean.response;

public class CallLogBatchResponse {
	
	private int totalRequest = 0;
	private int successCount = 0;
	private int failureCount = 0;
	
	public int getTotalRequest() {
		return totalRequest;
	}
	public void setTotalRequest(int totalRequest) {
		this.totalRequest = totalRequest;
	}
	public int getSuccessCount() {
		return successCount;
	}
	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}
	public int getFailureCount() {
		return failureCount;
	}
	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}
	
	public CallLogBatchResponse(int totalRequest, int successCount , int failureCount){
		this.totalRequest = totalRequest;
		this.successCount = successCount;
		this.failureCount = failureCount;
		
	}
	
	@Override
	public String toString() {
		
		return "callLogBatchResponse:[ totalRequest: "+totalRequest+", successCount: "+successCount+", failureCount: "+failureCount+" ]";
	}
}
