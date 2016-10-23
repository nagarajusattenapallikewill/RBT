package com.onmobile.apps.ringbacktones.provisioning.implementation.StartCopy;

import java.io.File;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.log4j.Logger;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.smsgateway.accounting.Accounting;
/**
 * A  class which has all the methods to be used for processing of Copy Request from BSTL or vice versa 
 * @author bikash.panda
 *
 */
public class StartCopyPreProcessor {

	protected static RBTClient rbtClient = null;
	static public StartCopyPreProcessor startCopyPreProcessor= null;
	protected Logger logger = null; 
	private String sdrWorkingDir = "e:/onmobile/sdr"; 
	public static Accounting copyAccounting = null; 
	static private Object lock = new Object();
	
	static private String countryPrefix = "91";
	public static boolean isCGICopyTestOn = true;
	public static ArrayList cGItestNumbers = new ArrayList();
	public static String cGItestNumUrl = null;
	private Hashtable circleIDURLDetailstable = new Hashtable();
	private Hashtable circleIDURLDetailstableForRdcSelection = new Hashtable();
	private Hashtable circleIDModeBasedURLDetailstableForRdcSelection = new Hashtable();
	static public String SITE_URL = "SITE_URL";
	static public String USE_PROXY = "USE_PROXY";
	static public String PROXY_HOST = "PROXY_HOST";
	static public String PROXY_PORT = "PROXY_POR";
	static public String TIME_OUT = "TIME_OUT";
	static public String CONN_TIME_OUT = "CONN_TIME_OUT";
	static public String CIRCLE_ID = "CIRCLE_ID";
	static public String IS_ONMOBILE = "IS_ONMOBILE";
	private String paramCommonType="COMMON";
	private String paramSmsType="SMS";
	public String interOperatorCopySourceOpr = null;
	public StartCopyPreProcessor() throws Exception {
		super();
		logger = Logger.getLogger(StartCopyPreProcessor.class);
	}

	public static StartCopyPreProcessor getInstance()
	{
		String method = "getInstance";
		try
		{
			if (startCopyPreProcessor!= null)
				return startCopyPreProcessor;

			synchronized (lock)
			{
				if (startCopyPreProcessor!= null)
					return startCopyPreProcessor;

				startCopyPreProcessor = new StartCopyPreProcessor();
				startCopyPreProcessor.init();
			}
		}
		catch(Throwable t)
		{
			startCopyPreProcessor = null;

		}
		return startCopyPreProcessor;
	}
	private void init() throws Exception
	{
		logger.debug(" StartCopyPreProcessor Init ");

		sdrWorkingDir=getParameter(paramSmsType,"SDRWORKING_DIR");
		if(sdrWorkingDir==null)
			sdrWorkingDir="e:/onmobile/sdr";

		if (getParameter(paramCommonType, "COUNTRY_PREFIX")!= null)
			countryPrefix = getParameter(paramCommonType, "COUNTRY_PREFIX");

		makeCircleIdURLDetailsTable("SITE_URL_DETAILS_COPY", circleIDURLDetailstable);
		makeCircleIdURLDetailsTable("SITE_URL_DETAILS_RDC_SELECTION", circleIDURLDetailstableForRdcSelection);
		createAccounting();
		if(getParameter(paramCommonType, "IS_CGI_COPY_TEST_ON")!= null)
			isCGICopyTestOn =getParameter(paramCommonType, "IS_CGI_COPY_TEST_ON").equalsIgnoreCase("true");
		if(getParameter(paramCommonType, "CGI_TEST_NUMBERS")!= null)
			cGItestNumbers = Utility.tokenizeArrayList(getParameter(paramCommonType, "CGI_TEST_NUMBERS"), null);
		if(getParameter(paramCommonType, "CGI_TEST_NUMBER_URL")!= null)
			cGItestNumUrl = getParameter(paramCommonType, "CGI_TEST_NUMBER_URL").trim();
		
		interOperatorCopySourceOpr = getParameter("COPY", "SOURCE_OPERATOR");

	}
	public Hashtable getSiteDetails(String subscriberId) {
		String method = "getSiteDetails ";
		String circleId = null;
		logger.debug(method+" subscriberId is "+subscriberId);
		if(subscriberId == null || subscriberId.length() < 7)
			return null;
		Subscriber subscriber=getSubscriberObj(subscriberId);
		if(subscriber==null)
			return null;
		circleId=subscriber.getCircleID();
		if(circleId == null)
			circleId = "XOP";
		logger.debug(method+" circleId is "+circleId);
		if (circleId != null )
		{
			logger.debug(method+" URLDetails hashtable  is "+(Hashtable)circleIDURLDetailstable.get(circleId));
			return (Hashtable)circleIDURLDetailstable.get(circleId);
		}
		return null;
	}
	public Hashtable getRdcToCgiSelectionSiteDetails(String subscriberId,String mode) {
		String method = "getSiteDetails ";
		String circleId = null;
		logger.debug(method+" subscriberId is "+subscriberId);
		if(subscriberId == null || subscriberId.length() < 7)
			return null;
		Subscriber subscriber=getSubscriberObj(subscriberId);
		if(subscriber==null)
			return null;
		circleId=subscriber.getCircleID();
		if(circleId == null)
			circleId = "XOP";
		logger.debug(method+" circleId is "+circleId);
		
		if (mode != null) {
			if (!circleIDModeBasedURLDetailstableForRdcSelection.containsKey(circleId + "_" + mode)) {
				makeCircleIdModeBasedURLDetailsTable("SITE_URL_DETAILS_RDC_SELECTION_" + mode,
						circleIDModeBasedURLDetailstableForRdcSelection, mode);
				if (circleIDModeBasedURLDetailstableForRdcSelection.containsKey(circleId + "_" + mode)) {
					return (Hashtable) circleIDModeBasedURLDetailstableForRdcSelection.get(circleId
							+ "_" + mode);
				}
			} else {
				return (Hashtable) circleIDModeBasedURLDetailstableForRdcSelection.get(circleId
						+ "_" + mode);
			}
		}
		
		if (circleId != null )
		{
			logger.debug(method+" URLDetails hashtable  is "+(Hashtable)circleIDURLDetailstableForRdcSelection.get(circleId));
			return (Hashtable)circleIDURLDetailstableForRdcSelection.get(circleId);
		}
		
		return null;
	}
	/**
	 * @param 
	 * @return 
	 * Get the all info on URL details for a particular circleID configured in SITE_URL_DETAILS_COPY
	 */
	private void makeCircleIdURLDetailsTable(String param, Hashtable siteDetailsHashTable)
	{
		String method = "makeCircleIdURLDetailsTable";
//		String urlDetails = getParameter(paramCommonType, "SITE_URL_DETAILS_COPY");
		String urlDetails = getParameter(paramCommonType, param);
		if(urlDetails == null || urlDetails.length() <= 0)
		{
			logger.info(method+"m_rbtCommonConfig.siteURLDetails() is missing/empty for " + param);
			return;
		}

		StringTokenizer stkParent=new StringTokenizer(urlDetails,";");
		while(stkParent.hasMoreTokens())
		{
			String thisSite=stkParent.nextToken().trim();
			StringTokenizer stkChild=new StringTokenizer(thisSite,",");
			Hashtable thisSiteTable = new Hashtable();

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(SITE_URL, stkChild.nextToken().trim());

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(USE_PROXY, new Boolean(stkChild.nextToken().trim()));
			else
				thisSiteTable.put(USE_PROXY, new Boolean("FALSE"));

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(PROXY_HOST, stkChild.nextToken().trim());

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(PROXY_PORT, new Integer(stkChild.nextToken().trim()));

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(TIME_OUT, new Integer(stkChild.nextToken().trim()));
			else 
				thisSiteTable.put(TIME_OUT, new Integer(5000));

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(CONN_TIME_OUT, new Integer(stkChild.nextToken().trim()));
			else
				thisSiteTable.put(CONN_TIME_OUT, new Integer(3000));
			if(stkChild.hasMoreTokens()) { 

				thisSiteTable.put(IS_ONMOBILE, new Boolean(stkChild.nextToken().trim())); 
			}
			String circleID = null;
			if(stkChild.hasMoreTokens()) { 
				circleID = stkChild.nextToken().trim(); 
				thisSiteTable.put(CIRCLE_ID, circleID); 
				logger.debug(method+"circle id thistable is "+circleID+":"+thisSiteTable);
//				circleIDURLDetailstable.put(circleID,thisSiteTable);
				siteDetailsHashTable.put(circleID,thisSiteTable);
			} 

		}
//		logger.debug(method+"m_circleIDURLDetailstable is "+circleIDURLDetailstable);
		logger.debug(method+" param: " + param + " m_circleIDURLDetailstable is "+siteDetailsHashTable);
	}
	
	
	private void makeCircleIdModeBasedURLDetailsTable(String param, Hashtable siteDetailsHashTable,String mode)
	{
		String method = "makeCircleIdURLDetailsTable";
//		String urlDetails = getParameter(paramCommonType, "SITE_URL_DETAILS_COPY");
		String urlDetails = getParameter(paramCommonType, param);
		if(urlDetails == null || urlDetails.length() <= 0)
		{
			logger.info(method+"m_rbtCommonConfig.siteURLDetails() is missing/empty for " + param);
			return;
		}

		StringTokenizer stkParent=new StringTokenizer(urlDetails,";");
		while(stkParent.hasMoreTokens())
		{
			String thisSite=stkParent.nextToken().trim();
			StringTokenizer stkChild=new StringTokenizer(thisSite,",");
			Hashtable thisSiteTable = new Hashtable();

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(SITE_URL, stkChild.nextToken().trim());

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(USE_PROXY, new Boolean(stkChild.nextToken().trim()));
			else
				thisSiteTable.put(USE_PROXY, new Boolean("FALSE"));

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(PROXY_HOST, stkChild.nextToken().trim());

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(PROXY_PORT, new Integer(stkChild.nextToken().trim()));

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(TIME_OUT, new Integer(stkChild.nextToken().trim()));
			else 
				thisSiteTable.put(TIME_OUT, new Integer(5000));

			if(stkChild.hasMoreTokens())
				thisSiteTable.put(CONN_TIME_OUT, new Integer(stkChild.nextToken().trim()));
			else
				thisSiteTable.put(CONN_TIME_OUT, new Integer(3000));
			if(stkChild.hasMoreTokens()) { 

				thisSiteTable.put(IS_ONMOBILE, new Boolean(stkChild.nextToken().trim())); 
			}
			String circleID = null;
			if(stkChild.hasMoreTokens()) { 
				circleID = stkChild.nextToken().trim(); 
				thisSiteTable.put(CIRCLE_ID, circleID); 
				logger.debug(method+"circle id thistable is "+circleID+":"+thisSiteTable);
//				circleIDURLDetailstable.put(circleID,thisSiteTable);
				siteDetailsHashTable.put(circleID + "_" + mode , thisSiteTable);
			} 

		}
//		logger.debug(method+"m_circleIDURLDetailstable is "+circleIDURLDetailstable);
		logger.debug(method+" param: " + param + " m_circleIDModeBasedURLDetailstable is "+siteDetailsHashTable);
	}

	/**
	 * @param keyWord
	 * @return smsType
	 * Get the smsType for a keyPressed
	 */
	public String getSMSType(String keyWord){
		String method="getSMSType";
		ArrayList normalCopy=Utility.tokenizeArrayList(getParameter(paramCommonType,"NORMALCOPY_KEY"), ",");
		ArrayList starCopy=Utility.tokenizeArrayList(getParameter(paramCommonType,"STARCOPY_KEY"), ",");
		//ArrayList rtCopy=Utility.tokenizeArrayList(getParameter(paramSmsType,"RTCOPY_KEY"), ",");
		logger.debug(method+"RBT::  Normalcopy : "+normalCopy+" starcopy : "+starCopy+" keypressed : "+keyWord);
		String response=null;
		String key=null;
		boolean isStarcopy=false;

		if(keyWord!=null&&keyWord.length()>=1){
			keyWord=keyWord.toLowerCase();
			for(int i=0;i<normalCopy.size();i++){
				key=(String)normalCopy.get(i).toString().toLowerCase();
				if(keyWord.indexOf(key)!=-1){
					response="COPY";
					isStarcopy=true;
					break;

				}
			}
			/*for(int i=0;i<rtCopy.size();i++){
					 key=(String)rtCopy.get(i).toString().toLowerCase();
					 if(keyWord.indexOf(key)!=-1 || key.equalsIgnoreCase(keyWord)){
						 isStarcopy=true;
						 response="RTCOPY";
		            	 break;

		             }
				}*/

			if(isStarcopy==false){

				for(int i=0;i<starCopy.size();i++){
					key=(String)starCopy.get(i).toString().toLowerCase();
					if(keyWord.indexOf(key)!=-1){
						response="COPYSTAR";
						break;
					}
				}
			}
		}else{
			response="COPY";
		}

		logger.debug(method+"RBT:: smstype resp : "+response);
		return response;
	}
	/**
	 * @param smsType
	 * @return keyPressed
	 * Get the smsType for a keyPressed
	 */
	public String getKeyPressed(String smsType){
		String response=null;
		String method="getKeyPressed";
		String key=getParameter(paramCommonType,"SMSTYPE_KEY");
		StringTokenizer stk = new StringTokenizer(key,":");
		while (stk.hasMoreTokens())
		{
			String temp=stk.nextToken();

			if(temp.substring(0,temp.indexOf(",")).equalsIgnoreCase(smsType)){
				response=temp.substring(temp.indexOf(",")+1);
				break;
			}
		}
		logger.debug(method+" keypressed : "+response);
		return response;
	}
	/**
	 * @param type,request,response,Accounting object
	 * @return 
	 * Add the info to accounting object
	 */
	public void addToAccounting(String type, 
			String request, String response, Accounting accObj ) 
	{ 
		try 
		{ 
			if (accObj != null) 
			{ 
				HashMap acMap = new HashMap(); 
				acMap.put("APP_ID", "RBT"); 
				acMap.put("TYPE", type); 
				acMap.put("SENDER", "NA"); 
				acMap.put("RECIPIENT", "NA"); 
				acMap.put("REQUEST_TS", request); 
				acMap.put("RESPONSE_TIME_IN_MS", response); 
				acMap.put("CALLING_MODE", "NA"); 
				acMap.put("CALLBACK_MODE", "NA"); 
				acMap.put("DATA_VOLUME", "NA"); 
				acMap.put("SMSC_MESSAGE_ID", "NA"); 
				acMap.put("STATUS", (new SimpleDateFormat("yyyyMMddHHmmssms")) 
						.format((new Date(System.currentTimeMillis())))); 
				if (accObj != null) 
				{ 
					accObj.generateSDR("sms", acMap); 
				} 
				acMap = null; 
			} 
		} 
		catch (Exception e) 
		{ 
			logger.info(" AddtoAccounting RBT::Exception caught " + e.getMessage()); 
		} 
	} 
	/**
	 * @param working Directory
	 * @return Accounting Object
	 * Get the Accounting Object
	 */
	private void createAccounting() 
	{ 

		copyAccounting = Accounting.getInstance(sdrWorkingDir + File.separator + "startcopy", 1000, 24, "size", true); 

		if (copyAccounting == null) 
			logger.info(" createAccounting RBT::Accounting class can not be created"); 

	}
	/**
	 * @param SubscriberID
	 * @return Subscriber
	 * Get the subscriber object for a subscriberID
	 */
	private  Subscriber getSubscriberObj(String subscriberID)
	{
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
		Subscriber subscriber=null;
		try {
			subscriber = rbtClient.getInstance().getSubscriber(rbtDetailsRequest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return subscriber;
	}
	/**
	 * @param type,ParamName
	 * @return value
	 */
	protected String getParameter(String type,String paramName) {
		/*ApplicationDetailsRequest smsRequest = new ApplicationDetailsRequest(type, paramName,(String)null);
		Parameter param = rbtClient.getParameter(smsRequest);
		if (param != null){
			String value = param.getValue();
			if (value != null) return value.trim();
		}*/
		Parameters param=CacheManagerUtil.getParametersCacheManager().getParameter(type, paramName);
		if (param != null){
			String value = param.getValue();
			if (value != null) return value.trim();
		}
		return null;
	}
	/**
	 * @param url,statusCode,response,useProxy,proxyHost,proxyPort
	 * @return boolean
	 * Call the given URL 
	 */
	public  boolean callURL(String strURL,StringBuffer response,Integer statusCode,
			boolean useProxy, String proxyHost, int proxyPort)
	{
		String resp="FAILURE";
		//HttpParameters httpParameters = new HttpParameters(strURL);
		logger.info(" callURL RBT:: url to hit: " + strURL);
		HttpParameters httpParameters = new HttpParameters(strURL,useProxy,proxyHost,proxyPort,6000,6000);
		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters,null);
			logger.info(" callURL RBT:: httpResponse: " + httpResponse);
			resp = httpResponse.getResponse();
			
			response.append(resp);
		}
		catch (Exception e) 
		{
			logger.error(" callURL RBT:: " + e.getMessage(), e);
			response.append(resp);
			if (e instanceof ConnectTimeoutException || e instanceof SocketTimeoutException)
				response.append("TimeOutException");
			return false;
		}
		
		return true;
	}


}

