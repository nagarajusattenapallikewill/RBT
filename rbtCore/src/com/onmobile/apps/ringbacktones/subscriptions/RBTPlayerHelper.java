/**
 * This can be used by tone player JSP for doing 
 * any request to RBT system. Currently its handling Access 
 * Count Update and Press * to copy feature. 
 */
package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

public class RBTPlayerHelper{
	
	private static Logger logger = Logger.getLogger(RBTPlayerHelper.class);
	
	public static final String ACCESS = "ACCESS";
	 public static final String POLL = "POLL";
	public static final String COPY = "COPY";
	public static final String RTCOPY = "RTCOPY";
	private static List m_ipList = null;
	private static RBTPlayerHelper m_rbtPlayerHelper = null; 
    private static Object m_initPH = new Object(); 
    /** 
     * Reads from config and populate DB URL and IP List 
     * @throws Exception 
     */ 
    private RBTPlayerHelper() throws Exception {
		try {
			String validServerIP = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "VALID_IP", null);
			if (validServerIP != null) {
				StringTokenizer tokens = new StringTokenizer(validServerIP, ",");
				m_ipList = new ArrayList();
				while (tokens.hasMoreTokens())
					m_ipList.add(tokens.nextToken());
			}
		} catch (Exception exe) {
			throw exe;
		}
	} 

    /**
	 * Returns the singleton instance
	 */ 
    public static RBTPlayerHelper getInstance() {
		if (m_rbtPlayerHelper == null) {
			synchronized (m_initPH) {
				if (m_rbtPlayerHelper == null) {
					try {
						m_rbtPlayerHelper = new RBTPlayerHelper();
					} catch (Exception e) {
						logger.error("", e);
						m_rbtPlayerHelper = null;
					}
				}
			}
		}
		return m_rbtPlayerHelper;
	} 

	
	/**
	 * This API is a generic API that will called from rbt_player_updater.jsp
	 * for inserting into DB or for any processing. Each action should have a
	 * private method for doing the processing.
	 * 
	 * @param action
	 *            can be ACCESS,COPY or add more in future
	 * @param details
	 *            The values player needs to tell RBT
	 * @param extraparams
	 *            Any more params if needed in future
	 * @return sucees or not
	 */
	public static boolean processRecord( String action, String details,
													String extraparams,String IP){
		boolean status = false;
		if(action.equalsIgnoreCase(ACCESS))
			status = updateAccessCount(details,extraparams);
		else if(action.equalsIgnoreCase(COPY))
			status = insertCopyRecord(details,extraparams);
		 else if(action.equalsIgnoreCase(POLL))
              status = insertPollRecord(details,extraparams);
		return status;
	}
	
	private static boolean isValidIP (String IP){
		
		boolean bStatus = false;
		if (m_ipList!=null && m_ipList.size()>0 && m_ipList.contains(IP)){
			bStatus = true; 
		}else{
			logger.info("Valid server IP missing in Configuration");
		}
		return bStatus;
	}
	private static boolean insertPollRecord (String details, String extraparams){
	 	 
         logger.info("Trying to poll " + details);
         try{
                 StringTokenizer tokens = new StringTokenizer(details.trim(),":");
                 String strSubscriberID = processNumber(tokens.nextToken());
                 String strCallerID = processNumber(tokens.nextToken());
                 String strSubscriberWavFile = tokens.nextToken().trim();
                 if(strSubscriberWavFile.endsWith(".wav"))
                         strSubscriberWavFile = strSubscriberWavFile.substring(0,strSubscriberWavFile.length()-4);
                 String pollInput = tokens.nextToken().trim();
                 getDBManager().insertViralSMSTableMap(strSubscriberID, null, POLL,strCallerID, strSubscriberWavFile+":"+pollInput, 0, null,null, null);
                 logger.info("Successfully done "+details);
         }catch(Exception exe){
                 logger.error("", exe);
         }
         return true;
 }

	/**
	 * This function processes all copy request from player
	 * @param details
	 * @param extraparams
	 * @return success or not
	 */
	private static boolean insertCopyRecord (String details, String extraparams){
		
		boolean success = true;
		boolean isShuffle = false;
		String clip = null;
		logger.info("Trying to copy " + details);
		try{
			if (details.indexOf(":RT")!=(-1)){
				return insertRTCopyRecord(details, extraparams);
			}
			StringTokenizer tokens = new StringTokenizer(details.trim(),":");
			String strSubscriberID = processNumber(tokens.nextToken());
			String strCallerID = processNumber(tokens.nextToken());
			String strSubscriberWavFile = tokens.nextToken();
			int iCategoryID = Integer.parseInt(tokens.nextToken());
			String strSelectionStatus = tokens.nextToken();
			if(tokens.hasMoreTokens()&& tokens.nextToken().equalsIgnoreCase("S")){
				isShuffle = true;
			}
			if (iCategoryID == -1){
				if (isDefaultCopyNeeded ())
					getDBManager().insertViralSMSTableMap(strSubscriberID, null,
						COPY, strCallerID, null,0, null, null, null);
			}
			else{
				if (isShuffle){
					clip = strSubscriberWavFile + ":" + "S" + iCategoryID + ":" + strSelectionStatus;
				}
				else
					clip = strSubscriberWavFile + ":" + iCategoryID + ":" + strSelectionStatus;
				getDBManager().insertViralSMSTableMap(strSubscriberID, null, COPY,strCallerID, clip, 0, null,null,null);
			}
			logger.info("Successfully done "+details);
			
		}catch(Exception exe){
			logger.error("", exe);
			return false;
		}
		return success;
	}
	
	private static boolean insertRTCopyRecord (String details, String extraparams){
		
		boolean success = true;
		String clip = null;
		logger.info("Trying to copy " + details);
		try{
			StringTokenizer tokens = new StringTokenizer(details.trim(),":");
			String strSubscriberID = processNumber(tokens.nextToken());
			String strCallerID = processNumber(tokens.nextToken());
			String strSubscriberWavFile = tokens.nextToken();
			int iCategoryID = Integer.parseInt(tokens.nextToken());
			String strSelectionStatus = tokens.nextToken();
			
			if (iCategoryID == -1){
				if (isDefaultCopyNeeded ())
					getDBManager().insertViralSMSTableMap(strSubscriberID, null,
						RTCOPY, strCallerID, null,0, null, null,null);
			}
			else{
				clip = strSubscriberWavFile + ":" + iCategoryID + ":" + strSelectionStatus;
				getDBManager().insertViralSMSTableMap(strSubscriberID, null, RTCOPY,strCallerID, clip, 0, null,null,null);
			}
			logger.info("Successfully done "+details);
			
		}catch(Exception exe){
			logger.error("", exe);
			return false;
		}
		return success;
	}
	private static boolean updateAccessCount (String details, String extraparams){
		
		boolean success = false;
		try{
			success = getDBManager().setAccessDate(processNumber(details.trim()), new Date());
		}
		catch(Exception exe){
			logger.error("", exe);
		}
		return success;
	}	
	
	private static String processNumber (String strSubID){
		return (getDBManager().subID(strSubID));
	}
	
	private static boolean isDefaultCopyNeeded () throws Exception{
		return RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "COPY_DEFAULT_SONG", "FALSE");
	} 
	
	private static RBTDBManager getDBManager(){
		return RBTDBManager.getInstance();
	}
	
	public static void main(String args[])throws Exception{
		//processRecord("COPY","9986026979:9985733333:sub_wav_file:1000:B:S",null,"192.16.7.89");
	}
}