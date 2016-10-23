package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.FeedStatus;
import com.onmobile.apps.ringbacktones.content.StatusType;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class RBTFeedHelper
{
	private static Logger logger = Logger.getLogger(RBTFeedHelper.class);
	
    private String _class = "RBTFeedHelper";
    boolean m_cricketPass = false;
    String m_filePathDefault = null;
    String m_filePath = m_filePathDefault;
    String m_countryPrefix = "0";
    RBTDBManager m_DBManager;
    String m_clipDIR = null;
//    private HttpParameters m_httpParams;
    private static String playerContentPage = null;
    public static final String COMMON = "COMMON";
    public static final String PLAYER_CONTENT_URL_PAGE = "PLAYER_CONTENT_URL_PAGE";
    public static final String TYPE = "TYPE";
    public static final String STATUS = "FEED_STATUS";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
private String m_ivr_cricket_path=null;
    public RBTFeedHelper() throws Exception{
        Tools.init("RBT_WAR", false);
        m_filePath = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
        m_clipDIR = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CLIPS_DIR", null);
        m_cricketPass = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CRICKET_PASS", "FALSE");
        m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
        m_ivr_cricket_path = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "IVR_CRICKET_PATH", null);

        m_DBManager = RBTDBManager.getInstance();
        Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, PLAYER_CONTENT_URL_PAGE);
        if(param != null)
        	playerContentPage = param.getValue();
    }
    
    /**
	 * If proxy exists use the following format in RBT_PARAMETERS table for PLAYER_CONTENT_URL_DETAILS
	 * 		http://localhost:8080/rbtplayer/rbt_feed_download_file.jsp;6000;6000;TRUE;192.16.2.11;8000
	 * If proxy not needed use the follwoing format below
	 * 		http://localhost:8080/rbtplayer/rbt_feed_download_file.jsp;6000;6000;FALSE
	 * 		http://localhost:8080/rbtplayer/rbt_feed_download_file.jsp;6000;6000
	 * Format Used
	 * 		URL;ConnectionTimeout;DataTimeout;ProxyNeeded;ProxyIP;ProxyPort
	 * 
	 */
    /*private void initilizePlayerContentURL() throws Exception {
    	
    	Parameters pp = m_DBManager.getParameter(COMMON,PLAYER_CONTENT_URL_PAGE);
    	
    	String strPlayerContentURL = null;
    	if(pp!= null&& (strPlayerContentURL = pp.value())!= null ){
    		StringTokenizer tokens = new StringTokenizer(strPlayerContentURL,";");
    		m_httpParams = new HttpParameters();
    		if (tokens.hasMoreTokens())
    			m_httpParams.setUrl(tokens.nextToken());
    		try{
    			if (tokens.hasMoreTokens())
    				m_httpParams.setConnectionTimeout(Integer.parseInt(tokens.nextToken()));
    			if (tokens.hasMoreTokens())
    				m_httpParams.setDataTimeout(Integer.parseInt(tokens.nextToken()));
    			if (tokens.hasMoreTokens())
    				m_httpParams.setHasProxy(Boolean.getBoolean(tokens.nextToken()));
    			if (tokens.hasMoreTokens())
    				m_httpParams.setProxyHost(tokens.nextToken());
    			if (tokens.hasMoreTokens())
    				m_httpParams.setProxyPort(Integer.parseInt(tokens.nextToken()));
    			//this is to send the request parameters as parts
    			m_httpParams.setParamsAsParts(true);
    		}catch(Exception exe){
    			logger.error("", exe);
    		}
        }
    }*/

    public boolean addFeed(String strName, String strStatus)
    {
    	boolean check=false;
        logger.info("RBT::adding feed " + strName);
        FeedStatus feedStatus = null;
        if (strName != null)
        {
            feedStatus = m_DBManager.getFeedStatus(strName.toUpperCase());
//            String file = null;
            if (feedStatus != null)
            {
//                file = feedStatus.file();
                 
                check=m_DBManager.setStatus(strName.toUpperCase(), strStatus);

				logger.info("result=="+check);
				return check;
            }
        }
        return (feedStatus != null);
    }

    public boolean updateFeed(String strName, String strStatus)
    {
        logger.info("RBT::updating feed " + strName
                + " with status " + strStatus);
        boolean success = false;
        if (strName != null)
        {
            int status = 90;
            StatusType[] statusType = m_DBManager.getStatusTypes();
            if (statusType != null)
            {
                for (int i = 0; i < statusType.length; i++)
                {
                    if (statusType[i].desc().toLowerCase()
                            .indexOf(strName.toLowerCase()) != -1)
                    {
                        status = statusType[i].code();
                        break;
                    }
                }
            }
            
            //update Player if it memcache exists
            if(playerContentPage != null) { //if(m_httpParams!=null) {
            	try{
            		success = doHttpFeedUpdate (strName,strStatus);
            	}catch(Exception exe){
            		logger.error("", exe);
            	}finally{
            		if(!success)
            			return success;
            	}
            }
            
            success = m_DBManager.setStatus(strName.toUpperCase(), strStatus);
//            RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix)
  //                  .updateSubscriberFeedStatus(null, status);
            
            if (m_cricketPass)
            {
            	m_DBManager.setFeedScheduleEndTime(strName.toUpperCase(), null);
            }
        }
        return success;
    }

    public String updateFile(String strName, String strFile)
    {
        try
        {
            FeedStatus feedStatus = m_DBManager.getFeedStatus(strName.toUpperCase());
            if(feedStatus == null || feedStatus.status().equalsIgnoreCase("OFF"))
				return "FAILURE";
			
			if(strName!=null && playerContentPage != null){ // m_httpParams!=null){
            	//new code for uploading wav file into into memcache player code.
            	return doHttpFeedProcessing(strName, strFile);
            }
			else if (strName != null && m_clipDIR != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat(
                        "yyyyMMdd-HHmmss");
                String fileName = strName.toUpperCase()
                        + "-"
                        + timeFormat
                                .format(new Date(System.currentTimeMillis()))
                        + ".wav";
                File feedFile = new File(m_filePath + File.separator + strFile);
                if (!feedFile.exists() || feedFile.length() <= 0)
                {
                    logger.info("RBT::file missing or file size is zero "
                                            + strFile);
                    return "FAILURE";
                }
                
                //old code for updating player
                StringTokenizer tokens = new StringTokenizer(m_clipDIR, ",");
            	while (tokens.hasMoreTokens())
            	{
            		String directory = tokens.nextToken();
            		logger.info("RBT::copying "
            				+ feedFile.getName() + " to " + directory + " as "
            				+ fileName);
            		Tools.moveFile(directory, feedFile);
            		new File(directory + File.separator + feedFile.getName())
            		.renameTo(new File(directory + File.separator
            				+ fileName));
            	}
                feedFile.delete();
                logger.info("RBT::deleting "
                        + feedFile.getName());

//                int status = 90;
                if (feedStatus != null)
                {
                    String file = feedStatus.file();
                    if (file == null)
                    {
                        file = fileName;
                    }
                    else
                    {
                        StringTokenizer st = new StringTokenizer(file, ",");
                        String first = null;
                        if (st.hasMoreTokens())
                        {
                            first = st.nextToken();
                            file = first + "," + fileName;
                        }
                        if (st.hasMoreTokens())
                        {
                            tokens = new StringTokenizer(m_clipDIR, ",");
                            while (tokens.hasMoreTokens())
                            {
                                String directory = tokens.nextToken();
                                if (new File(directory + File.separator + first)
                                        .delete())
                                {
                                    logger.info("RBT::deleted " + first
                                                            + " from "
                                                            + directory);
                                }
                            }
                            file = st.nextToken() + "," + fileName;
                        }
                        /*StatusType[] statusType = m_DBManager.getStatusTypes();
                        if (statusType != null)
                        {
                            for (int i = 0; i < statusType.length; i++)
                            {
                                if (statusType[i].desc().toLowerCase()
                                        .indexOf(strName.toLowerCase()) != -1)
                                {
                                    status = statusType[i].code();
                                    break;
                                }
                            }
                        }*/
                    }
                    boolean success = m_DBManager.setFile(strName.toUpperCase(), file);
//                    RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix)
  //                          .updateSubscriberFeedStatus(fileName, status);

                    if (success)
                        return "SUCCESS";
                }
            }
        }
        catch (Exception e){
            logger.info("RBT::Exception caught "
                    + e.getMessage());
            e.printStackTrace();
        }
        return "FAILURE";
    }
    
    /**
     * For uploading the feed file instead of moving to all tone player folders
     * If needed the same can copy to ivr path (For Airtel.)
     * @param strName
     * @param strFile
     * @return
     * @throws Exception
     */
    private boolean checkAndUploadToVoiceportal(String strFile){
    	String uploadCricketToVoicePortal=null;
    	String destCricketVoicePath=null;
    	boolean uploadToVoicePortalDone=true;
    	File feedFile = new File(m_filePath + File.separator + strFile);
    	if(m_ivr_cricket_path!=null && feedFile.exists() && (feedFile.length()>0)){
    		StringTokenizer st=new StringTokenizer(m_ivr_cricket_path,";");
    		/*if(st.hasMoreTokens()){
    			uploadCricketToVoicePortal=st.nextToken();
    		}*/
    		//if(st.hasMoreTokens() && uploadCricketToVoicePortal.equalsIgnoreCase("true")&& feedFile.exists() && (feedFile.length()>0)){
    		while(st.hasMoreTokens() ){	
    			destCricketVoicePath=st.nextToken();
    			try {
    				//uploadToVoicePortalDone=true;
					copyFile(destCricketVoicePath, strFile, m_filePath);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					uploadToVoicePortalDone=false;
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					uploadToVoicePortalDone=false;
					e.printStackTrace();
				}catch (Exception e){
					uploadToVoicePortalDone=false;
					e.printStackTrace();
				}
    		}
    	}
    	return uploadToVoicePortalDone;
    }
    private String doHttpFeedProcessing (String strName, String strFile) {
    	String strStatus = FAILURE;
    	File feedFile = new File(m_filePath + File.separator + strFile);
    	if (!feedFile.exists() || feedFile.length() <= 0){
    		logger.info("RBT::file missing or file size is zero " + strFile);
    		return strStatus;
    	}
    	HashMap map = new HashMap();
    	map.put(TYPE,strName);
    	map.put(STATUS,"ON");
    	File [] file = new File[] {feedFile};
    	logger.info("RBT:: starting the ");
    	// Not considering the result to ignore the player response 
    	boolean result = new MakeHttpRequestToAllPlayers(map, file).updatePlayer();
    	boolean uploadToVoicePortalDone=false;
    	uploadToVoicePortalDone=checkAndUploadToVoiceportal(strFile);
    	//copy to IVR folder config name is IVR_CRICKET_PATH=TRUE;e:/onmobile/apps/prompts/rbt_prompts
    	feedFile.delete();
    	boolean success = m_DBManager.setFile(strName.toUpperCase(), strFile);
    	if(success && uploadToVoicePortalDone)
    		strStatus = SUCCESS;
    	return strStatus;
    }
    private void copyFile(String destinationPath, String fileName, String localDir)
	throws FileNotFoundException, IOException {
		Tools.copyFile(localDir + File.separator + fileName, destinationPath + File.separator
				+ fileName);
	}
    /**
     * For updaing the player when the feed stops
     * @param strName
     * @param strStatus
     * @throws Exception
     */
    private boolean doHttpFeedUpdate (String strName, String strStatus) {
    	HashMap map = new HashMap();
    	map.put(TYPE,strName);
    	map.put(STATUS,strStatus);
    	return new MakeHttpRequestToAllPlayers(map, null).updatePlayer();
    }
    
    
    
    /**
     * 
     * @author Sreekar
     * This class makes httpRequests to all players
     * 				(configured in rbt_site_prefix table)
     */
    class MakeHttpRequestToAllPlayers
    {
    	private String _class = "MakeHttpRequestToAllPlayers";
        private ArrayList allPlayerIPConfig = null;
    	HashMap params;
    	File[] files = null;
    	
    	MakeHttpRequestToAllPlayers(HashMap params, File[] files) {
    		this.params = params;
    		this.files = files;
    		initPlayerIP();
    	}
    	
    	private void initPlayerIP() {
    		allPlayerIPConfig = m_DBManager.getLocalPlayerIP();
    	}
    	
    	public boolean updatePlayer()
    	{
        	if(allPlayerIPConfig == null)
        	{
        		logger.info("RBT::no player URL's to update");
        		return false;
        	}
        	else
        		logger.info("RBT::total player url's to update = " + allPlayerIPConfig.size());
        	boolean result = true;
        	String url = null;
        	for(int i = 0;i < allPlayerIPConfig.size(); i++)
        	{
        		HttpParameters httpParams = Tools.getHttpParamsForURL((String)allPlayerIPConfig.get(i), playerContentPage);
        		url = httpParams.getUrl();
        		httpParams.setParamsAsParts(true);
            	logger.info("RBT::posting feed file to " + httpParams.getUrl());
            	String response = null;
				try 
				{
					response = RBTHTTPProcessing.postFile(httpParams, params, files);
					if (response != null && response.toUpperCase().indexOf("SUCCESS") != -1)
					{
						result = result && true;
					}
				} 
				catch (Exception e) 
				{
					logger.info("RBT::Error invoking URL->" + url + System.getProperty("line.separator") + Tools.getStackTrace(e));
					result = result && false;
				}
            	logger.info("RBT::response for posted feed file to " +httpParams.getUrl() + " is " + response);
        	}
        	return result;
    	}
    }
}