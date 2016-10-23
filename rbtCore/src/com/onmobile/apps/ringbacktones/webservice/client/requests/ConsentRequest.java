package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;


public class ConsentRequest extends Request{

	private String info = null;
	private Date requestFromTime = null;
	private Date requestToTime = null;
	private String requestType = null;
	private String agentId = null;
	private Logger logger = Logger.getLogger(ConsentRequest.class);
	
	
	@Override 
	public void prepareRequestParams(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext task) {
		super.prepareRequestParams(task);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		info = task.getString(param_info);
		try {
			if (task.containsKey("requestFromTime")) {
				requestFromTime = sdf.parse(task.getString("requestFromTime"));
			}

			if (task.containsKey("requestToTime")) {
				requestToTime = sdf.parse(task.getString("requestToTime"));
			}
		}catch(Exception ex){
			logger.info("Exception while parsing the request FromTime and requestToTime for ConsentRequest"); 
		}
		
		requestType = 	task.getString("requestType");
		
	};
	
	@Override
	public HashMap<String,String> getRequestParamsMap() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		HashMap<String, String> requestParamsMap = super.getRequestParamsMap();
		if(info!=null){
			requestParamsMap.put(param_info, info);
		}
        if(requestFromTime!=null){
        	requestParamsMap.put("requestFromTime",sdf.format(requestFromTime));
        }
        if(requestToTime!=null){
        	requestParamsMap.put("requestToTime" , sdf.format(requestToTime));
        }
        
        if(requestType!=null){
        	requestParamsMap.put("requestType",requestType);
        }
        
        if(agentId!=null){
        	requestParamsMap.put(param_agentId, agentId);
        }
        
        return requestParamsMap;
        
	};
	
	
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Date getRequestFromTime() {
		return requestFromTime;
	}

	public void setRequestFromTime(Date requestFromTime) {
		this.requestFromTime = requestFromTime;
	}

	public Date getRequestToTime() {
		return requestToTime;
	}

	public void setRequestToTime(Date requestToTime) {
		this.requestToTime = requestToTime;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public ConsentRequest(String subscriberID) {
		super(subscriberID);
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	@Override
	public String toString() {
		return "ConsentRequest [info=" + info + ", requestFromTime="
				+ requestFromTime + ", requestToTime=" + requestToTime
				+ ", requestType=" + requestType + ", logger=" + logger + "]";
	}

}
