/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client;

import java.io.File;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 * @author abhinav.anand
 */
public interface Connector extends WebServiceConstants
{
	public Parser makeWebServiceRequest(Request request, String api, String action);
	public File makeWebServiceBulkRequest(Request request, String api, String action);
	public String redirectWebServiceRequest(HashMap<String,String> requestParams, String api);
	//public File redirectWebServiceBulkRequest(HashMap<String,String> requestParams, String api);
	public String makeRestRequest(Request restrequest, String api,String action) throws Exception;
	public Parser makeWebServiceRequest(ConnectorHandler connectorHandler, Request request, String api, String action);
	
}
