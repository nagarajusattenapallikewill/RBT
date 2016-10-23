package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.smsgateway.accounting.Accounting;

public class StartCopyHelper
{
	private static Logger logger = Logger.getLogger(StartCopyHelper.class);
	
	static String _class = "StartCopyHelper";
	static private Object m_lock = new Object();
	static private String m_dbURL = null;
	static RBTDBManager m_rbtDBManager = null;
	static public StartCopyHelper m_startCopyHelper= null;
	static public Hashtable m_prefixesCircleIdTable = new Hashtable();
	static public Hashtable m_prefixesUrlTable = new Hashtable();
	static public Hashtable m_circleIDURLDetailstable = new Hashtable();
	static public String SITE_URL = "SITE_URL";
	static public String USE_PROXY = "USE_PROXY";
	static public String PROXY_HOST = "PROXY_HOST";
	static public String PROXY_PORT = "PROXY_POR";
	static public String TIME_OUT = "TIME_OUT";
	static public String CONN_TIME_OUT = "CONN_TIME_OUT";
	static public String CIRCLE_ID = "CIRCLE_ID";
	static public String IS_ONMOBILE = "IS_ONMOBILE";
    public static boolean isCGICopyTestOn = true;
    public static ArrayList cGItestNumbers = new ArrayList();
    public static String cGItestNumUrl = null;
	private Hashtable m_prefixesTable = new Hashtable();
	private HashMap m_clips = new HashMap(); 
    private HashMap m_clipsID2Vcode  = new HashMap(); 
    private String m_sdrWorkingDir = "e:/onmobile/sdr"; 
    public static Accounting m_Accounting = null; 

	int m_iConnections = 4; 

    public static StartCopyHelper getInstance()
    {
        String _method = "getInstance";
    	try
        {
	        if (m_startCopyHelper != null)
	            return m_startCopyHelper;
	
	        synchronized (m_lock)
	        {
	            if (m_startCopyHelper != null)
	                return m_startCopyHelper;
	
	            m_startCopyHelper = new StartCopyHelper();
	            m_startCopyHelper.init();
	        }
	    }
        catch(Throwable t)
        {
        	m_startCopyHelper = null;
        	System.out.println(t.getMessage());
        	logger.error("", t);
        }
        return m_startCopyHelper;
    }

    private void init() throws Exception
    {
    	Tools.init("startcopy", false);
    	
        m_dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "START_DB_URL", null);
        if (m_dbURL == null)
            throw new Exception();

		m_iConnections = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "NUM_CONN", 4);
		System.out.println("db url: "+m_dbURL+" : "+m_iConnections);
        m_rbtDBManager = RBTDBManager.init(m_dbURL, m_iConnections);
        makePrefixesCircleIdtable();
        makeCircleIdURLDetailsTable();
        initCache();
        createAccounting();
        isCGICopyTestOn = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "IS_CGI_COPY_TEST_ON", "TRUE");
        cGItestNumbers = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CGI_TEST_NUMBERS", ""), null);
        cGItestNumUrl = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CGI_TEST_NUMBER_URL", "").trim();
    }
    
    private void makePrefixesCircleIdtable()
    {
    	String _method = "makePrefixesCircleIdtable";
    	List<SitePrefix> allPrefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
    	if(allPrefixes == null || allPrefixes.size() <= 0)
    	{
    		logger.info("No prefixes found.");
    		return;
    	}
    	for(int i = 0; i < allPrefixes.size(); i++)
    	{
    		String circleId = allPrefixes.get(i).getCircleID();
			String siteUrl = allPrefixes.get(i).getSiteUrl();
    		String prefixesStr = allPrefixes.get(i).getSitePrefix();
    		if( circleId == null || circleId.length() <= 0 || prefixesStr == null || prefixesStr.length() <= 0)
    			continue;
    		circleId = circleId.trim();
    		StringTokenizer stk = new StringTokenizer(prefixesStr,",");
    		while(stk.hasMoreTokens())
    		{
				String thisToken = stk.nextToken().trim();
				m_prefixesCircleIdTable.put(thisToken, circleId);
				if(siteUrl != null)
					m_prefixesUrlTable.put(thisToken, siteUrl);
				int iPrefixLen = thisToken.length();
				ArrayList prefixAList = null;
				if(iPrefixLen <= 0)
					continue;
				if(m_prefixesTable.containsKey(new Integer(iPrefixLen)) )
					prefixAList = (ArrayList)m_prefixesTable.get(new Integer(iPrefixLen));
				else
					prefixAList = new ArrayList();
				
				prefixAList.add(thisToken);
				m_prefixesTable.put(new Integer(iPrefixLen), prefixAList);
			}
    	}
    	logger.info("m_prefixesCircleIdTable is "+m_prefixesCircleIdTable);
    	logger.info("m_prefixesTable is "+m_prefixesTable);
    }
    
    private void makeCircleIdURLDetailsTable()
    {
    	String _method = "makeCircleIdURLDetailsTable";
    	String urlDetails = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SITE_URL_DETAILS_COPY", null);
    	if(urlDetails == null || urlDetails.length() <= 0)
    	{
    		logger.info("m_rbtCommonConfig.siteURLDetails() is missing/empty ");
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
				logger.info("circle id thistable is "+circleID+":"+thisSiteTable);
                m_circleIDURLDetailstable.put(circleID,thisSiteTable); 
            } 

		}
		logger.info("m_circleIDURLDetailstable is "+m_circleIDURLDetailstable);
    }
    
    public Hashtable getSiteDetails(String subscriberId)
    {
    	String _method = "getSiteDetails";
    	logger.info("subscriberId is "+subscriberId);
    	if(subscriberId == null || subscriberId.length() < 7)
    		return null;
    	logger.info("rbt db manager "+m_rbtDBManager);
    	subscriberId = m_rbtDBManager.subID(subscriberId);
    	logger.info("after subid "+subscriberId);
    	//String prefix = subscriberId.substring(0,4);
		String prefixStr = getPrefix(subscriberId);
		logger.info("prefix "+prefixStr);
    	String circleId = null;
		if(prefixStr != null)
			circleId = (String)m_prefixesCircleIdTable.get(prefixStr);
    	logger.info("circleId is "+circleId);
    	if (circleId != null )
    	{
    		logger.info("URLDetails hashtable  is "+(Hashtable)m_circleIDURLDetailstable.get(circleId));
    		return (Hashtable)m_circleIDURLDetailstable.get(circleId);
    	}
    	return null;
    }
	
	public String getSiteDetailsInternal(String subscriberId)
    {
    	String _method = "getSiteDetailsInternal";
    	logger.info("subscriberId is "+subscriberId);
    	if(subscriberId == null || subscriberId.length() < 7)
    		return null;
    	subscriberId = m_rbtDBManager.subID(subscriberId);
    	int prefixIndex = RBTDBManager.init(m_dbURL, m_iConnections).getPrefixIndex(); 
        String prefix = subscriberId.substring(0,prefixIndex); 
    	logger.info("prefix is "+prefix);
    	if (prefix != null )
    	{
    		logger.info("URL  is "+(String)m_prefixesUrlTable.get(prefix));
    		return (String)m_prefixesUrlTable.get(prefix);
    	}
    	return null;
    }
	
	public String getPrefix(String subscriberID)
	{
		if (subscriberID == null || subscriberID.length() < 7 || subscriberID.length() > 15)
			return null;
		else
		{
			try
			{
				Long.parseLong(m_rbtDBManager.subID(subscriberID));
			}
			catch (Throwable e)
			{
				return null;
			}
		}

		int prefixLength = -1;
		String thisPrefix = null;
		ArrayList prefixArrayListTemp = null;
		Integer prefixKey = null;
		Iterator prefixIteror = m_prefixesTable.keySet().iterator(); 
		while(prefixIteror.hasNext())
		{
			prefixKey = (Integer)prefixIteror.next();
			prefixLength = prefixKey.intValue();
			thisPrefix = subscriberID.substring(0,prefixLength);
			prefixArrayListTemp = (ArrayList)m_prefixesTable.get(prefixKey);
			if(prefixArrayListTemp.contains(thisPrefix)){
				logger.info("RBT:prefix true :"+thisPrefix); 
				return thisPrefix;
			}
		}
		logger.info("RBT:prefix false."); 
		return null;
	}
	
	 private void initCache() 
     { 
         String method = null; 
         com.onmobile.apps.ringbacktones.cache.content.ClipMinimal[] allClips = m_rbtDBManager.getAllActiveClips(); 
         if(allClips != null) 
         { 
                 for(int i=0; i < allClips.length; i++) 
                 { 
                         try 
                         { 
                                 String wavFile = allClips[i].getWavFile(); 
                                 if(wavFile != null) 
                                 { 
                                         if(wavFile.endsWith(".wav")) 
                                                 wavFile = wavFile.substring(0,wavFile.length()-4); 
                                         if(wavFile.startsWith("rbt_")) 
                                                 wavFile = wavFile.substring(4); 
                                         if(wavFile.endsWith("_rbt")) 
                                                 wavFile = wavFile.substring(0,wavFile.length()-4); 
                                         m_clips.put(wavFile, new Integer(allClips[i].getClipId())); 
                                         m_clipsID2Vcode.put(new Integer(allClips[i].getClipId()),wavFile); 
                                 } 
                         } 
                         catch(Exception t) 
                         { 
                                 logger.error("", t); 
                         } 
                 } 
         } 
    }
	 
	public String getClipID(String vcode) {
		Integer clipId = null;
		try {
			clipId = (Integer) m_clips.get(vcode);
		} catch (Exception e) {
		}
		if (clipId != null)
			return "" + clipId.intValue();
		else
			return "MISSING";
	} 

    public String getClipVcode(String clipID) {
		String vcode = null;
		try {
			vcode = (String) m_clipsID2Vcode.get(new Integer(clipID));
		} catch (Exception e) {
		}

		if (vcode != null)
			return vcode.trim();
		else
			return "-1";
	} 
    
    public void addToAccounting(String type, 
            String request, String response, Accounting accObj ) 
    { 
//        String _method = "addToAccounting()"; 
        //logger.info("****** parameters are -- "+type + " 
        // & "+subscriberID + " & "+request+ " & "+response+" & "+ip ); 
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
            logger.error("", e);
        } 
    } 
    public String getSMSType(String keyWord){
    	logger.info("coming"); 
		ArrayList normalCopy=Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "NORMALCOPY_KEY", ""), ",");
		ArrayList starCopy=Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "STARCOPY_KEY", ""), ",");
		ArrayList rtCopy=Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "RTCOPY_KEY", ""), ",");
		logger.info("RBT:: In ServiceProcessor processPlayerHelper() Normalcopy:"+normalCopy+" starcopy: "+starCopy+" rtcopy : "+rtCopy+" keypressed : "+keyWord);
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
		
		logger.info("RBT:: smstype resp : "+response);
		return response;
	}
    public String getKeyPressed(String smsType){
    	String response=null;
    	logger.info("coming"); 
		String key=RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SMSTYPE_KEY", "");
		StringTokenizer stk = new StringTokenizer(key,
		":");
		while (stk.hasMoreTokens())
		{
			String temp=stk.nextToken();
			logger.info(" temp: "+temp.substring(0,temp.indexOf(",")));
			if(temp.substring(0,temp.indexOf(",")).equalsIgnoreCase(smsType)){
				response=temp.substring(temp.indexOf(",")+1);
			    break;
			}
		}
		logger.info("keypressed is "+response); 
    	return response;
    }
    private void createAccounting() 
    { 
//        String _method = "createAccounting()"; 
        //logger.info("****** blank" ); 
        m_Accounting = Accounting.getInstance(m_sdrWorkingDir + File.separator + "startcopy", 1000, 24, "size", true); 
 
        if (m_Accounting == null) 
            logger.info("RBT::Accounting class can not be created"); 
 
    } 


}
