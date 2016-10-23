package com.onmobile.apps.ringbacktones.provisioning.implementation.service.vodafone;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceResponseEncoder;

/**
 * @author sridhar.sindiri
 *
 */
public class VodafoneServiceResponseEncoder extends ServiceResponseEncoder {

	/**
	 * @throws Exception
	 */
	public VodafoneServiceResponseEncoder() throws Exception 
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceResponseEncoder#encode(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String encode(Task task)
	{
		if (task.getString(param_api) != null && task.getString(param_api).equalsIgnoreCase(api_subsats)
				&& (task.getString(param_response).equalsIgnoreCase(ERROR)
						|| task.getString(param_response).equalsIgnoreCase(Resp_invalidParam)
						|| task.getString(param_response).equalsIgnoreCase(Resp_InvalidNumber)))
		{
			String response = task.getString(param_response); 
			return "<Response>" + response + "</Response>";
		}
		
		return super.encode(task);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceResponseEncoder#getContentType(java.util.HashMap)
	 */
	public String getContentType(HashMap<String, String> requestParams)
	{
		String contentType =null;
		if (requestParams.get(param_api).equals(api_subsats))
			contentType = "text/xml; charset=utf-8";

		logger.info("RBT:: contentType: " + contentType);
		return contentType;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceResponseEncoder#getGenericErrorResponse(java.util.HashMap)
	 */
	public String getGenericErrorResponse(HashMap<String, String> requestParams)
	{
		String genericErrorResponse = null;
		
		String api = requestParams.get(param_api);
		if(api.equals(api_copy))
			genericErrorResponse = copy_Resp_Err;
		else if(api.equals(api_cross_copy) || api.equals(api_cross_copy_rdc))
			genericErrorResponse = cross_copy_Resp_Err;
		else if(api.equals(api_UGC))
			genericErrorResponse = ugc_Resp_Fail;
		else if (api.equals(api_subsats))
			genericErrorResponse = "<Response>ERROR</Response>";
		else if (api.equals(api_VodaCTservice))
			genericErrorResponse = response_VODACT_INTERNAL_ERROR;
		
		return genericErrorResponse;
	}
}
