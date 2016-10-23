/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.service;

import java.util.HashMap;

import org.apache.log4j.Logger;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;

/**
 * @author vinayasimha.patil
 *
 */
public class ServiceResponseEncoder extends ResponseEncoder
{
	protected Logger logger = null; 

	/**
	 * @throws Exception 
	 * 
	 */
	public ServiceResponseEncoder() throws Exception
	{
		logger = Logger.getLogger(ServiceResponseEncoder.class);
	}

	public String encode(Task task)
	{
		String response = null;
        response=task.getString(param_response);  
		logger.info("RBT:: response: " + response);
		return response;
	}

	
	public String getContentType(HashMap<String, String> requestParams)
	{
		String contentType =null;

		//TODO: put proper contentType
        
		if(requestParams.get(param_api).equals(api_copy))
			contentType=null;
		else if(requestParams.get(param_api).equals(api_UGC))
			contentType=null;
		logger.info("RBT:: contentType: " + contentType);
		return contentType;
	}

	public String getGenericErrorResponse(HashMap<String, String> requestParams)
	{
		String genericErrorResponse = null;
		if(requestParams.get(param_api).equals(api_copy))
			genericErrorResponse=copy_Resp_Err;
		else if(requestParams.get(param_api).equals(api_cross_copy)||requestParams.get(param_api).equals(api_cross_copy_rdc))
			genericErrorResponse=cross_copy_Resp_Err;
		else if(requestParams.get(param_api).equals(api_UGC))
			genericErrorResponse=ugc_Resp_Fail;
		return genericErrorResponse;
	}
	
}
