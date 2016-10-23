/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;

/**
 * @author vinayasimha.patil
 *
 */
public abstract class ResponseEncoder implements Constants
{
	public String encode(Task task)
	{
		String response = task.getString(param_response);
		return (response == null) ? "FAILURE" : response ; 
	}
	public String getContentType(HashMap<String, String> requestParams){return null;}
	public String getGenericErrorResponse(HashMap<String, String> requestParams){return null;}
}
