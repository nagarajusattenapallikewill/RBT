/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;

/**
 * @author vinayasimha.patil
 * @author abhinav.anand
 */
public class RestDirectConnector implements Connector
{
	

	private Configurations configurations = null;

	private DocumentBuilder documentBuilder = null;

	
	@Override
	public String makeRestRequest(Request restrequest, String api,
			String action) throws Exception{
		throw new Exception("Method is not implemented");
	}


	@Override
	public Parser makeWebServiceRequest(Request request, String api,
			String action) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public File makeWebServiceBulkRequest(Request request, String api,
			String action) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String redirectWebServiceRequest(
			HashMap<String, String> requestParams, String api) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Parser makeWebServiceRequest(ConnectorHandler connectorHandler,
			Request request, String api, String action) {
		// TODO Auto-generated method stub
		return null;
	}

}
