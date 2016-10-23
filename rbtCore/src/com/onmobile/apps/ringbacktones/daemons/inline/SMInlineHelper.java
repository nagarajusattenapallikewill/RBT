package com.onmobile.apps.ringbacktones.daemons.inline;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.logger.TransLogForSelection;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

@Component("smHelper")
public class SMInlineHelper extends RBTDaemonManager {
	private static Logger logger = Logger.getLogger(SMInlineHelper.class);
	
	private SMInlineHelper() {
		super();
		super.getConfigValues();
	}
	
	public boolean processSelAct(SubscriberStatus ss)  throws OnMobileException {
		ChargeClass chargeClass = m_rbtChargeClassCacheManager.getChargeClass(ss.classType());
		try
		{
			Subscriber subscriber2 = getSubscriber(ss.subID());
			
			boolean isSMHitNotToBeMade = isSMHitNotToBeMade(ss, "ACT");
			/*
			 * If its a zero sel req and charge class amount is zero do not send a request to SM.
			 * Deactivate the old selections and activate the new selections
			 * NOTE: Specifically used in Idea for zero selection charge
			 */
			if(isSMHitNotToBeMade|| (!getParamAsBoolean("SEND_ZERO_SEL_SM", "TRUE")
					&& (chargeClass.getAmount() != null 
							&& Double.parseDouble(chargeClass.getAmount().replace(",", "."))==0
							&& chargeClass.getRenewalAmount() != null 
							&& Double.parseDouble(chargeClass.getRenewalAmount().replace(",", "."))==0))) {
				
				/*
				 * Get old selection information, if loop status is override
				 * Send sms to user, if list is not null and size > 0 
				 */
				List<String> wavFileNameList = new ArrayList<String>();
				
				//Fixed by Sreekar for RBT-7338
				Date startTime = null;
				if (isNavCat(ss.categoryID())) {
					startTime = getStartTime(ss.categoryID());
				}
				else if (ss.startTime().before(new Date())) {
					// don't do anything so that start time will be updated to sysdate
				}
				else {
					startTime = ss.startTime();
				}
//                Subscriber subscriber2 = getSubscriber(ss.subID());
				//RBT-14044	VF ES - MI Playlist functionality for RBT core
				rbtDBManager.addTrackingOfPendingSelections(ss);
				
				boolean success = smURLSelectionActivation(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(),
						ss.toTime(), false, false,  sdf.format(ss.setTime()), ss.loopStatus(), ss.prepaidYes(), 
						startTime, ss.selType(), ss.subscriberFile(), false, ss.refID(), ss.selInterval(), wavFileNameList);
				if (success) {
					if( RBTParametersUtils.getParamAsBoolean("COMMON",
							"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")) {
						rbtDBManager.deactivateOldODAPackOnSuccessCallback(ss.subID(), ss.refID(), ss.callerID(),
								ss.categoryType(), ss, false, null);
					}
					
					//RBT-14044	VF ES - MI Playlist functionality for RBT core
					rbtDBManager.addOldMiplayListSelections(ss);
					if((ss.callerID() == null || ss.callerID().equalsIgnoreCase("all")) && ss.status()==1 && !Utility.isShuffleCategory(ss.categoryType())) {
					  String resp = rbtDBManager.addDownloadForTrackingMiPlaylist(ss.subID(), ss.subscriberFile(),ss.categoryID(),ss.categoryType(), null, ss.classType(), ss.selectedBy(),ss.status(),ss.selType());
					  logger.info("Response of addDownloadForTrackingMiPlaylist in Daemon manager processSelectionsAct: "+resp);
					}
					
					TransLogForSelection.writeTransLogForSelection(
							ss.circleId(), ss.subID(), ss.callerID(),
							ss.selType(), ss.fromTime(), ss.toTime(),
							ss.selInterval(), ss.categoryType(), ss.status(),
							ss.subscriberFile(), ss.categoryID(),
							1,subscriber2.subscriptionClass(),
							new Date(),
							null, ss.loopStatus()
									+ "");
					if (getParamAsBoolean("SUPPORT_IBM_INTEGRATION", "FALSE")) {
						// IBM-Integration
						RBTCallBackEvent.update(RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
								ss.subID(), ss.refID(),
								RBTCallBackEvent.SM_SUCCESS_CALLBACK_RECEIVED, ss.classType());
					}
				}

				if(getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE") && chargeClass.getSmschargeSuccess() != null
						&& !chargeClass.getSmschargeSuccess().equalsIgnoreCase("null")) {
					try {
						String smsText = getSelectionSMS(chargeClass.getSmschargeSuccess(), ss);
						Tools.sendSMS(getSenderNumber(ss.circleId(), getParamAsString("SENDER_NO")), ss.subID(), smsText, getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
					}
					catch (Exception e) {
						logger.error("", e);
					}
				}
				
				/*
	    		 * Sms will not send to subscriber, if sms text is not configured or there is not song for deactivation
	    		 * Sending sms to user for his previous song is also available in his inbox until the expiry period of that song 
	    		 */
				try {
				Subscriber subscriber = rbtDBManager.getSubscriber(ss.subID());
				String smsText = Utility.getSmsTextForDeactivationSelection(subscriber.language(), wavFileNameList);
				if(smsText != null)
					Tools.sendSMS(getSenderNumber(ss.circleId(), getParamAsString("SENDER_NO")), ss.subID(), smsText, getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
				}
				catch(Exception e) {
					logger.error(e);
				}
				
				return true;
			}
			
			HashMap<String, String> selectionExtraInfoMap = null;
			boolean isDelayedDeact = false;
			if (ss.selStatus().equalsIgnoreCase("C"))
			{
				selectionExtraInfoMap = DBUtility.getAttributeMapFromXML(ss.extraInfo());
				// If sel status is C and ExtraInfo contains DELAY_DEACT="TRUE" then it is delayed deactivation request
				isDelayedDeact = (selectionExtraInfoMap != null && "TRUE".equalsIgnoreCase(selectionExtraInfoMap.get("DELAY_DEACT")));
				if (isDelayedDeact)
					logger.info("sel status is C and ExtraInfo contains DELAY_DEACT=TRUE");
			}

			String requestType = isDelayedDeact ? "DCT" : "ACT";

			boolean updateRefID = false;
			/*
			 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
			 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
			 */
			String retryCount = ss.retryCount();
			int noOfRetries = 0;
			if (retryCount != null
					&& ((ss.selStatus().equalsIgnoreCase("C") && retryCount.startsWith("U"))
							|| (ss.selStatus().equalsIgnoreCase("A") && retryCount.startsWith("A"))))
			{
				int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
				noOfRetries = Integer.parseInt(retryCount.substring(1));
				Date nextRetryTime = ss.nextRetryTime();
				if(noOfRetries >= maxRetries){
					boolean success  = rbtDBManager.updateSelStatusBasedOnRefID(ss.subID(), ss.refID(), "E");
					char loopStatus = ss.loopStatus();
					if(loopStatus == 'A'){
						loopStatus = 'l';
					}else if(loopStatus == 'B'){
						loopStatus = 'o';
					}
					if (success) {
						if (rbtDBManager.updateLoopStatus(ss, loopStatus, null)) {
							rbtDBManager.updatePlayerStatus(ss.subID(), "A");
						}
					}

					throw new OnMobileException(WebServiceConstants.MAX_RETRIES_REACHED);
				}else if (nextRetryTime.after(new Date()))
					throw new OnMobileException(WebServiceConstants.RETRIAL);
			}
			HashMap<String, String> resp = null;
			String success = null;
			String refID = null;

			resp = makeSubMgrRequest(null, ss, null, null, requestType , false);
			success = getSMResponseString(resp, RESPONSE);
			
			//RBT-14497 - Tone Status Check
			if (success != null && success.equalsIgnoreCase(CONTENT_EXPIRED)) {
				throw new OnMobileException(WebServiceConstants.CONTENT_EXPIRED);
			}
			refID = getSMResponseString(resp, REFID);

			if(getSMResponseString(resp, REFID_CREATED) != null)
				updateRefID = true;

			logger.info("Selection activation request of subscriber: " + ss.subID() + "  - Response: " + success);
			//RBT-12195 - User block - unblock feature.
			if(success != null && success.equals(Constants.BLOCK_SUB_KEYWORD))
			{
				throw new OnMobileException(WebServiceConstants.SUB_BLOCKED);
			}			
			if(refID == null)
			{
				logger.warn("RBT::Could not get RefID for sub " + ss.subID());
				throw new OnMobileException(WebServiceConstants.NULL_REFID);
			}
			
			if(success != null && success.equals(SM_URL_FAILURE))
			{
				if(updateRefID)
					rbtDBManager.smURLSelectionActivationRetry(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(),
							ss.toTime(), getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), refID);
				else
				{
					long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
					noOfRetries++;
					if (ss.selStatus().equalsIgnoreCase("C"))
						retryCount = "U" + noOfRetries;
					else
						retryCount = "A" + noOfRetries;

					Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
					rbtDBManager.updateRetryCountAndTimeForSelection(ss.subID(), ss.refID(), retryCount, retryTime);
				}
				throw new OnMobileException(WebServiceConstants.SM_FAILURE);
			}
			else if(success != null && success.startsWith("SUCCESS")) {
				String extraInfo = null;
				boolean updateExtraInfo = false;
				boolean successFlag = true;
				// RBT-12842-Unwanted Parameter In Upgrade Request From RBT
				String selMappedStr = getParamAsString(COMMON,
						"SEL_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
				if (selMappedStr != null) {
					selectionExtraInfoMap = DBUtility.getAttributeMapFromXML(ss
							.extraInfo());
				}
				logger.info("Removed SEL_PARAMETERS_MAPPING_FOR_INTEGRATION parameters from extrainfo after getting sm response as success.Now extraInfoMap: "
						+ selectionExtraInfoMap);
				if (selMappedStr != null && selectionExtraInfoMap != null) {
					String str[] = selMappedStr.split(";");
					for (int i = 0; i < str.length; i++) {
						String s[] = str[i].split(",");
						if (s.length == 2
								&& selectionExtraInfoMap.containsKey(s[1])) {
							selectionExtraInfoMap.remove(s[1]);
							updateExtraInfo = true;
						}
						
					}
				}
				logger.info("Removed SEL_PARAMETERS_MAPPING_FOR_INTEGRATION parameters from extrainfo after getting sm response as success.Now extraInfoMap: "
						+ selectionExtraInfoMap);
				if (isDelayedDeact)
				{
					if (selectionExtraInfoMap != null)
					{
						selectionExtraInfoMap.remove("DELAY_DEACT");
						updateExtraInfo = true;
						successFlag = false;
					}
				}
				if (selectionExtraInfoMap != null && updateExtraInfo) {
					extraInfo = DBUtility
							.getAttributeXMLFromMap(selectionExtraInfoMap);
					if (extraInfo == null)
						extraInfo = "NULL";

				}
				smURLSelectionActivation(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(),
						ss.toTime(), successFlag, false,sdf.format(ss.setTime()),ss.loopStatus(),ss.prepaidYes(), 
						getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), updateRefID, refID, ss.selInterval(),extraInfo, null);
				return true;
			}
			else if (success != null && success.startsWith("BLACKLISTED"))
			{
				rbtDBManager.deactivateSubscriber(ss.subID(), "BLACKLISTED", null, true, true, true, true, false);
			}
			else if(success != null && success.startsWith("BASE_DEACTIVE")) {
				String deactivateBy = subscriber2.deactivatedBy();
				if(!rbtDBManager.isSubscriberDeactivated(subscriber2) && !rbtDBManager.isSubscriberDeactivationPending(subscriber2)) {
					rbtDBManager.deactivateSubscriber(ss.subID(), "RECON", null, true, true, true, true, false);
					deactivateBy = "RECON";
				}
				rbtDBManager.smSelectionActivationRenewalFailure(ss.subID(),
						ss.refID(), deactivateBy, ss.prepaidYes()?"p":"n", ss.classType(), LOOP_STATUS_EXPIRED, ss.selType(),
						ss.extraInfo(), null);
				throw new OnMobileException(WebServiceConstants.BASE_DEACTIVATED);
			}
			else if(success != null && success.trim().length() > 0
					&& !success.equalsIgnoreCase(FAILED)) {
				smURLSelectionActivation(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(), 
						ss.toTime(), false, true,sdf.format(ss.setTime()),ss.loopStatus(),ss.prepaidYes(), 
						getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), updateRefID, refID, ss.selInterval(), null);
				return true;
			}
			else if(updateRefID)
				rbtDBManager.smURLSelectionActivationRetry(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(),
						ss.toTime(), getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), refID);
			else
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				if (ss.selStatus().equalsIgnoreCase("C"))
					retryCount = "U" + noOfRetries;
				else
					retryCount = "A" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSelection(ss.subID(), ss.refID(), retryCount, retryTime);
			}
		} catch(OnMobileException oe) {
			throw oe;
		} catch(Exception e) {
			logger.error("", e);
			throw new OnMobileException("Exception: " + e);
		}
		return false;
	}
	
	public boolean processSelDct(SubscriberStatus selectionObj) throws OnMobileException {
		ChargeClass chargeClass = m_rbtChargeClassCacheManager.getChargeClass(selectionObj.classType());
		logger.info("RBT::Processing Selection "
				+ chargeClass.getSelectionPeriod() + " " + chargeClass.getRenewalAmount());
		Subscriber subscriber2 = getSubscriber(selectionObj.subID());
		List<String> actPendingStatusList = Arrays.asList("A,E,N,G".split(","));
		if (subscriber2 != null && actPendingStatusList.contains(subscriber2.subYes())
				&& getParamAsBoolean("COMBINED_CHARGING", "FALSE")) {
        	 logger.info("Subscriber is in act pending state, so not processing the record = "+selectionObj);
        	 throw new OnMobileException(WebServiceConstants.SUB_ACT_PENDING);
         }
		boolean isSMHitNotToBeMade = isSMHitNotToBeMade(selectionObj, "DCT");
		if(isSMHitNotToBeMade || (chargeClass.getChargeClass() != null && chargeClass.getChargeClass().equalsIgnoreCase("TRIAL")
				|| (!getParamAsBoolean("SEND_ZERO_SEL_SM", "TRUE") 
						&& (chargeClass.getAmount() != null && Double.parseDouble(chargeClass.getAmount().replace(",", "."))==0
								&& chargeClass.getRenewalAmount() != null && Double.parseDouble(chargeClass.getRenewalAmount().replace(",", "."))==0)))) {
//				|| (chargeClass.getSelectionPeriod() != null && chargeClass.getSelectionPeriod().equalsIgnoreCase("O"))) {
			boolean success = rbtDBManager.smURLSelectionNotSendSMDeactivation(selectionObj.subID(), selectionObj.callerID(), selectionObj.status(), selectionObj.setTime(),false, false, selectionObj.subscriberFile(), selectionObj.selType(), selectionObj.loopStatus());
			if (success) {
				
				//RBT-14044	VF ES - MI Playlist functionality for RBT core
				 if(!selectionObj.deSelectedBy().equals("SM") && (selectionObj.callerID() == null || selectionObj.callerID().equalsIgnoreCase("all")) && selectionObj.status()==1 && !Utility.isShuffleCategory(selectionObj.categoryType())) {
				  String resp = rbtDBManager.removeMiPlaylistDownloadTrack(selectionObj.subID(),selectionObj.subscriberFile(),
						  selectionObj.categoryID(), selectionObj.categoryType(),selectionObj.callerID(),selectionObj.status());
				  logger.info("Response of removeMiPlaylistDownloadTrack in Daemon manager processSelectionsDct : "+resp);
				 }
				  
				
				TransLogForSelection.writeTransLogForSelection(
						selectionObj.circleId(), selectionObj.subID(),
						selectionObj.callerID(), selectionObj.selType(),
						selectionObj.fromTime(), selectionObj.toTime(),
						selectionObj.selInterval(),
						selectionObj.categoryType(), selectionObj.status(),
						selectionObj.subscriberFile(),
						selectionObj.categoryID(), 2,
						subscriber2.subscriptionClass(), selectionObj.startTime(),
						new Date(), selectionObj.loopStatus() + "");
			}
		}
		else if(chargeClass.getSelectionPeriod() != null) {
			/*
			 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
			 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
			 */
			String retryCount = selectionObj.retryCount();
			int noOfRetries = 0;
			if (retryCount != null
					&& (selectionObj.selStatus().equalsIgnoreCase("D") && retryCount.startsWith("D")))
			{
				int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
				noOfRetries = Integer.parseInt(retryCount.substring(1));
				Date nextRetryTime = selectionObj.nextRetryTime();
				if (noOfRetries >= maxRetries){
					rbtDBManager.updateSelStatusBasedOnRefID(selectionObj.subID(), selectionObj.refID(), "E");
					throw new OnMobileException(WebServiceConstants.MAX_RETRIES_REACHED);
				}else if (nextRetryTime.after(new Date())){
					throw new OnMobileException(WebServiceConstants.RETRIAL);
				}
			}
			HashMap<String, String> resp = null;
			String success = null;
			String refID = null;

			resp = makeSubMgrRequest(null, selectionObj, null, null, "DCT", false);
			success = getSMResponseString(resp, RESPONSE);
			refID = getSMResponseString(resp, REFID);

			logger.info("Selection deactivation request of subscriber: " + selectionObj.subID() + "  - Response: " + success);
			
			if(success != null && success.equals(SM_URL_FAILURE))
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "D" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSelection(selectionObj.subID(), selectionObj.refID(), retryCount, retryTime);
				throw new OnMobileException(WebServiceConstants.SM_FAILURE);
			}
			else if(success != null && success.trim().length() > 0 && success.startsWith("SUCCESS")){
				// the subscriber sel_statius is set to P, end date to deactivation date and next charging date to null
			boolean check = smURLSelectionDeactivation(selectionObj.subID(), refID, true, false, selectionObj.selType());
				if (check) {
					TransLogForSelection.writeTransLogForSelection(
							selectionObj.circleId(), selectionObj.subID(),
							selectionObj.callerID(), selectionObj.selType(),
							selectionObj.fromTime(), selectionObj.toTime(),
							selectionObj.selInterval(),
							selectionObj.categoryType(), selectionObj.status(),
							selectionObj.subscriberFile(),
							selectionObj.categoryID(),
							2, subscriber2.subscriptionClass(),
							selectionObj.startTime(), new Date(),
							selectionObj.loopStatus() + "");
				}
				return true;
			}
			else if(success != null && success.trim().length() > 0
					&& success.equalsIgnoreCase("ALREADY_DEACTIVE")){

				if(selectionObj.refID() == null) 
				{
					Connection conn = rbtDBManager.getConnection(); 
					
					try
					{
						rbtDBManager.updateRefIDSelectionOldLogic(conn, selectionObj.subID(), selectionObj.callerID(), selectionObj.status(), sdf.format(selectionObj.setTime()),
							selectionObj.fromTime(), selectionObj.toTime(), selectionObj.subscriberFile(), refID);
					}
					catch(Throwable e)
					{
						logger.error("Exception before release connection", e);
					}
					finally
					{
						rbtDBManager.releaseConnection(conn);
					}
				}
				// set sel_status to X
				rbtDBManager.smSelectionDeactivationSuccess(selectionObj.subID(), refID, LOOP_STATUS_EXPIRED_INIT, selectionObj.selType(),null);
				return true;
			}
			else if(success != null && success.startsWith("BASE_DEACTIVE")) {
				String deactivateBy = subscriber2.deactivatedBy();
				if(!rbtDBManager.isSubscriberDeactivated(subscriber2) && !rbtDBManager.isSubscriberDeactivationPending(subscriber2)) {
					rbtDBManager.deactivateSubscriber(selectionObj.subID(), "RECON", null, true, true, true, true, false);
					deactivateBy = "RECON";
				}
				rbtDBManager.smSelectionActivationRenewalFailure(selectionObj.subID(),
						selectionObj.refID(), deactivateBy, selectionObj.prepaidYes()?"p":"n", selectionObj.classType(), LOOP_STATUS_EXPIRED, selectionObj.selType(),
								selectionObj.extraInfo(), null);
			}
			else if(success != null && success.trim().length() > 0
					&& !success.equalsIgnoreCase(FAILED)){
				// set sel_status to F
				smURLSelectionDeactivation(selectionObj.subID(), refID, false, true, selectionObj.selType());
				return true;
			}
			else
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "D" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSelection(selectionObj.subID(), selectionObj.refID(), retryCount, retryTime);
			}
		}
		return false;
	}
}
