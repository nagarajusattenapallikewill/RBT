package com.onmobile.apps.ringbacktones.daemons;


import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.rbt.Ticket;


public class GNOCURLUtility {
	private static final Logger LOGGER = Logger.getLogger(GNOCURLUtility.class);
	HttpParameters httpParameters = new HttpParameters();
	RBTHttpClient rbtHttpClient = null;
	
//	public static void execute(){
//		GNOCURLUtility gnocUtility = new GNOCURLUtility();
//		gnocUtility.setHTTPParameterValues();
//	}
	
	public void setHTTPParameterValues(){
		boolean useProxy = Boolean.parseBoolean(GNOCPropertyReaderSM.useProxy);
		httpParameters.setUseProxy(useProxy);
		LOGGER.info("use proxy value is " + GNOCPropertyReaderSM.useProxy);
		if (useProxy) {
			httpParameters.setProxyHost(GNOCPropertyReaderSM.proxyHost);
			httpParameters.setProxyPort(Integer.parseInt(GNOCPropertyReaderSM.proxyPort));
			httpParameters.setConnectionTimeout(Integer.parseInt(GNOCPropertyReaderSM.connectionTimeout));
			httpParameters.setSoTimeout(Integer.parseInt(GNOCPropertyReaderSM.soTimeout));
			httpParameters.setMaxTotalConnections(Integer.parseInt(GNOCPropertyReaderSM.maxTotalConnections));
			httpParameters.setMaxHostConnections(Integer.parseInt(GNOCPropertyReaderSM.maxHostConnections));
		}
		
	}
	
	public String getResponseFromURL(String url){
		try {
			LOGGER.info("going to hit " + url);
//			 new GNOCURLUtility().setHTTPParameterValues();
			setHTTPParameterValues();
			LOGGER.info("set HTTP parameters.");
			rbtHttpClient = new RBTHttpClient(httpParameters);
			LOGGER.info("initialised rbthttpclient. going to make request.");
			HttpResponse httpResponse = rbtHttpClient.makeRequestByGet(url, null);
			String response = httpResponse.getResponse();
			if(response != null && !response.equals("")){
				LOGGER.info("response from the url is " + response);
				return response;
			}
		} catch (HttpException e) {
			LOGGER.error("httpexception." ,e);
			//e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("IOException.Server is down or not responding." , e);
			//e.printStackTrace();
		}
		return null;
	}
	
	public List<Ticket> parseJSONResponse(String url){
		GNOCURLUtility gnocUtility = new GNOCURLUtility();
		String JSONStringResponse = gnocUtility.getResponseFromURL(url);
		LOGGER.info("creating a JSON object.");
		JSONObject jsonObj;
		List<Ticket> ticketList = new ArrayList<Ticket>();
		try {
			jsonObj = new JSONObject (JSONStringResponse);
			LOGGER.info("created JSON object " + jsonObj);
			LOGGER.info("name of the title " + jsonObj.names());
			Object insideTicket = jsonObj.get("ticket");
			String finalInside = insideTicket.toString();
			LOGGER.info(insideTicket);
			
			JSONArray jsonObjInside = new JSONArray (finalInside);
			LOGGER.info("created next JSON object " + jsonObjInside);
			String location = null;
			for(int i=0 ; i < jsonObjInside.length(); i++){
				JSONObject element = jsonObjInside.getJSONObject(i);
				LOGGER.info("element is " + element);
				String[] nameArray = JSONObject.getNames(element);
				
				LOGGER.info("name of 1st field is " + nameArray[0]);	
				int id = (Integer)element.get("id");
				LOGGER.info("id is " + id);
				String source = (String)element.get("ip");
				Object jsonLoc = element.get("location");
				// location can be null in the ticket response. In such case JSON is giving object of type JSONObject.NULL which is not visible. 
				if(!jsonLoc.equals(null)){
					location = (String)jsonLoc; 
				}else{
					location = null;
				}
				String name = (String)element.get("name");
				String severity = (String)element.get("severity");
				
				String create_ts = (String)element.get("timestamp");
				
				String summary = (String)element.get("summary");
				String groups= (String)element.get("groups");
				String items= (String)element.get("items");
				Ticket ticket = new Ticket(id,source,location,name,severity,create_ts,summary,groups,items);
				LOGGER.info(ticket.getId());
				ticketList.add(ticket);
				
			}
			return ticketList;
		} catch (JSONException e) {
			LOGGER.info("exception in conversion. " + e.getMessage());
			//e.printStackTrace();
		}catch (NullPointerException e1) {
			LOGGER.info("not able to get the JSON Object. " + e1.getMessage());
			//e1.printStackTrace();
		}
		return null;
	}
}