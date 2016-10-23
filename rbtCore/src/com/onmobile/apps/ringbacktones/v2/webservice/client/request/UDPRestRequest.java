package com.onmobile.apps.ringbacktones.v2.webservice.client.request;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;

public class UDPRestRequest extends Request {
	
	private String udpName;
    private String extraInfo;
    private String udpId;
    private String clipId;
    private String offSet;
    private String pagesize;
	

	public String getUdpName() {
		return udpName;
	}


	public void setUdpName(String udpName) {
		this.udpName = udpName;
	}


	public String getExtraInfo() {
		return extraInfo;
	}


	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}


	public UDPRestRequest(String subscriberID) {
		super(subscriberID);
	}


	public String getUdpId() {
		return udpId;
	}


	public void setUdpId(String udpId) {
		this.udpId = udpId;
	}

	public String getClipId() {
		return clipId;
	}


	public void setClipId(String clipId) {
		this.clipId = clipId;
	}


	public String getOffSet() {
		return offSet;
	}


	public void setOffSet(String offSet) {
		this.offSet = offSet;
	}


	public String getPagesize() {
		return pagesize;
	}


	public void setPagesize(String pagesize) {
		this.pagesize = pagesize;
	}


	@Override
	public String toString() {
		return "UDPRestRequest [udpName=" + udpName + ", extraInfo="
				+ extraInfo + ", udpId=" + udpId + ", clipId=" + clipId
				+ ", offSet=" + offSet + ", pagesize=" + pagesize + "]";
	}
	
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (udpName != null) requestParams.put(param_name, udpName);
		if (extraInfo != null) requestParams.put(param_extraInfo, extraInfo);
		if (clipId != null) requestParams.put(param_clipID, clipId);
		if (udpId != null) requestParams.put(param_udpId, udpId);
		if (offSet != null) requestParams.put(param_offSet, offSet);
		if (pagesize != null) requestParams.put(param_pageSize, pagesize);
		
		return requestParams;
	}

}
