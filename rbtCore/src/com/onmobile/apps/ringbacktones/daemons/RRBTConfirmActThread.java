package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.TransFileWriter;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class RRBTConfirmActThread extends Thread implements iRBTConstant 
{
	private static Logger logger = Logger.getLogger(RRBTConfirmActThread.class);
	private RBTDaemonManager daemonManager = null;
	private String confirmActStats = "./Trans";
	
	TransFileWriter confirmActStatsWriter = null;
	ParametersCacheManager parametersCacheManager = null;
	RBTDBManager rbtDbManager = null;
	RBTClient rbtClient = null;
	
	int sleepTimeInMin = 5;
	
	public RRBTConfirmActThread(RBTDaemonManager daemonManager)
	{
		this.daemonManager = daemonManager;
		initialise();
	}
	
	public void initialise()
	{
		try
		{
			setName("RRBTConfirmActThread");
			parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
			rbtDbManager = RBTDBManager.getInstance();
			rbtClient = RBTClient.getInstance();
			
			confirmActStats = getParamAsString("DAEMON", "SUBMGR_SDR_WORKING_DIR", ".");
			sleepTimeInMin = Integer.parseInt(getParamAsString("COMMON", "CONFIRM_ACT_THREAD_SLEEP_INTERVAL", "5"));
			
			initConfirmActStats();
		}
		catch(Exception e)
		{
			logger.error("Issue in creating RRBTConfirmActThread", e);
		}
	}
	
	public void run()
	{
		while (daemonManager.isAlive()) 
		{
			ViralSMSTable[] viralContexts = getConfirmActRequests();
			if (viralContexts != null)
			{
				logger.info("No of records to be processed :"+viralContexts.length);
				for (ViralSMSTable viralSMSTable : viralContexts) 
				{
					try
					{
						Subscriber subscriber = rbtDbManager.getSubscriber(viralSMSTable.callerID());
						HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
						
						if (subExtraInfoMap == null || !subExtraInfoMap.containsKey(PLAYER_XML_JINGLE_FLAG))
						{
							writeTrans(viralSMSTable.callerID(), "FAILED:ALREADY CONFIRMED");
							rbtDbManager.removeViralSMS(viralSMSTable.subID(), viralSMSTable.type(), viralSMSTable.sentTime());
							
							continue;
						}
						
						String url = getConfirmChargeUrl(viralSMSTable.callerID(), subscriber);
						String response = confirmSubscription(viralSMSTable.callerID() , url);
						
						if (response.equalsIgnoreCase("SUCCESS"))
						{
							rbtDbManager.removeViralSMS(viralSMSTable.subID(), viralSMSTable.type(), viralSMSTable.sentTime());
						
							subExtraInfoMap.remove(PLAYER_XML_JINGLE_FLAG);
							String extraInfo = DBUtility.getAttributeXMLFromMap(subExtraInfoMap);
							rbtDbManager.updateExtraInfoAndPlayerStatus(viralSMSTable.callerID(), extraInfo, "A");
							
							writeTrans(viralSMSTable.callerID(), response);
						}
						else
						{
							// Retry
						}
					}
					catch(Exception e)
					{
						logger.error("Exception ", e);
					}
				}
			}
			else
			{
				logger.info("No confirm activation requests to process.. sleeping");
			}
			
			try
			{
				Thread.sleep(sleepTimeInMin*60*1000);
			}
			catch(Exception e)
			{
			}
		}
	}
	
	private ViralSMSTable[] getConfirmActRequests() 
	{
		ViralSMSTable[] viralContexts = rbtDbManager.getViralSMSByTypeAndLimit("CONFIRM_ACT", 1000);
		return viralContexts;
	}
	
	private String getConfirmChargeUrl(String subscriberID, Subscriber subscriber)
	{
		String url = getParamAsString("DAEMON", "RENEWAL_URL", null);
		if (url == null)
			return url;
		
		String type = "P";
		if (subscriber.prepaidYes()) 
		{
			type = "P";
		}
		else 
		{
			type = "B";
		}
		String srvKey = "RBT_ACT_"+subscriber.subscriptionClass()+"_RRBT"; 
		
		url = url+"msisdn="+subscriberID+"&srvkey="+srvKey+"&type="+type+"&refid="+subscriber.refID()+"&mode=DAEMON";
		
		String actInfo = "";
		if(subscriber.activationInfo() != null)
		{
			actInfo = subscriber.activationInfo().replaceAll("\\|", "/");
			actInfo = actInfo.replaceAll(":", ";");
		}
		String info = "|CONTENT_ID:actinfo="+actInfo+",cosid:"+subscriber.cosID()+"|cosid:"+subscriber.cosID();
		url = url+"&info="+info;
		
		logger.info("Confirm charge url :"+url);
		return url;
	}

	private String confirmSubscription(String subscriberID, String url) throws Exception
	{
		String response = "FAILED"; 
		HttpParameters httpParameters = new HttpParameters(url);

		HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
		String urlResponse = httpResponse.getResponse();
		logger.info("ConfirmCharge url response for subscriberID :"+subscriberID+" :"+urlResponse);

		if(urlResponse != null)
		{
			urlResponse = urlResponse.trim();
			if(urlResponse.indexOf("SUCCESS") != -1)
				response = "SUCCESS";
		}
		return response;
	}
	
	private void initConfirmActStats() 
	{
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("SUBSCRIBER_ID");
		headers.add("RESPONSE");
		headers.add("CONFIRM_TIME");
		confirmActStatsWriter = new TransFileWriter(confirmActStats, "CONFIRM_ACT_STATS",	headers);
	}
	
	private void writeTrans(String subscriberID, String response)
	{
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("SUBSCRIBER_ID", subscriberID);
		h.put("RESPONSE", response);
		h.put("CONFIRM_TIME", new Date().toString());
		
		if (confirmActStatsWriter != null)
			confirmActStatsWriter.writeTrans(h);
	}

	private String getParamAsString(String type, String param, String defaultValue) 
	{
		try 
		{
			return parametersCacheManager.getParameter(type, param, defaultValue).getValue();
		}
		catch (Exception e) 
		{
			logger.info("Unable to get param ->" + param + "  type ->" + type+ ". Returning defVal > " + defaultValue);
			return defaultValue;
		}
	}

}
