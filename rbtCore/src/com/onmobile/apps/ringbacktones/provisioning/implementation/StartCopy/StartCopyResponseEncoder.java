/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.StartCopy;

import java.util.HashMap;

import org.apache.log4j.Logger;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;

/**
 * A  class which has all the methods to encode response and give proper response. 
 * @author bikash.panda
 *
 */
public class StartCopyResponseEncoder extends ResponseEncoder
{
	protected Logger logger = null; 

	/**
	 * @throws Exception 
	 * 
	 */
	public StartCopyResponseEncoder() throws Exception
	{
		logger = Logger.getLogger(StartCopyResponseEncoder.class);
	}
	/**
	 * @param Task
	 * @return String
	 * Encodes the response for Start Copy request
	 */
	public String encode(Task task)
	{
		String response = null;
        response=task.getString(param_response);  
		logger.debug("RBT:: enocode response: " + response);
		return response;
	}

	
	public String getContentType(HashMap<String, String> requestParams)
	{
		String contentType =null;
		return contentType;
	}

	public String getGenericErrorResponse(HashMap<String, String> requestParams)
	{
		String genericErrorResponse =failureResponse;
		
		return genericErrorResponse;
	}
	
}
