package com.onmobile.apps.ringbacktones.webservice.implementation.egypt;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author sridhar.sindiri
 */
public class EgyptRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(EgyptRBTProcessor.class);

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor
	 * #getChargeClass(com.onmobile.apps.ringbacktones.webservice.common.Task,
	 * com.onmobile.apps.ringbacktones.content.Subscriber,
	 * com.onmobile.apps.ringbacktones.rbtcontents.beans.Category,
	 * com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip)
	 */
	@Override
	protected String getChargeClass(WebServiceContext task, Subscriber subscriber,
			Category category, Clip clip)
	{
		String chargeClass = null;
		String action = task.getString(param_action);

		if (action.equalsIgnoreCase(action_acceptGift))
			return super.getChargeClass(task, subscriber, category, clip);

		String clipChargeClass = (clip == null ? null : clip.getClassType());
		if (clipChargeClass != null
				&& !clipChargeClass.equalsIgnoreCase("DEFAULT"))
		{
			chargeClass = clipChargeClass;
		}
		else if (task.containsKey(param_chargeClass)
				&& ((task.containsKey(param_useUIChargeClass) && task
						.getString(param_useUIChargeClass)
						.equalsIgnoreCase(YES)) || task
						.containsKey(param_offerID)))
		{
			chargeClass = task.getString(param_chargeClass);
		}
		else {
			return super.getChargeClass(task, subscriber, category, clip);
		}
//		else if (task.containsKey(param_activatedNow)
//				&& task.getString(param_activatedNow).equalsIgnoreCase(YES))
//		{
//			chargeClass = "FIRST";
//		}
//		else if (subscriber != null && subscriber.maxSelections() == 0)
//		{
//			chargeClass = "FIRST";
//		}	
//		else
//			chargeClass = "DEFAULT";
//
//		
		task.put(param_useUIChargeClass, YES);
		logger.info("RBT:: response: "
				+ chargeClass);
		return chargeClass;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processSelection(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processSelection(WebServiceContext task)
	{
		String response = null;		
		try {
			String clearTNBFlag = task.getString(param_clearTNBFlag);
			if(clearTNBFlag != null && clearTNBFlag.equalsIgnoreCase(YES)) {
				String subscriberID = task.getString(param_subscriberID);
				String mode = getMode(task);
				Subscriber subscriber = DataUtils.getSubscriber(task);			
				Map<String,String> userInfoMap = new HashMap<String, String>(); 
				if(!rbtDBManager.isSubscriberDeactivated(subscriber)) {
					String userInfo = subscriber.extraInfo();
					userInfoMap = DBUtility.getAttributeMapFromXML(userInfo);
				}
				
				if (userInfoMap != null
						&& userInfoMap.containsKey(iRBTConstant.TNB_USER)
						&& userInfoMap.get(iRBTConstant.TNB_USER).equalsIgnoreCase("TRUE")) {
					logger.debug("userInfoMap contains TNB_USER");
					userInfoMap.remove(iRBTConstant.TNB_USER);
					String extraInfoXML = DBUtility.getAttributeXMLFromMap(userInfoMap);
					rbtDBManager.updateExtraInfo(subscriberID, extraInfoXML);
					subscriber.setExtraInfo(extraInfoXML);
					int tnbClipId = -1; 
					Parameters parameter = parametersCacheManager.getParameter(iRBTConstant.COMMON, "TNB_DEFAULT_CLIP_ID", null);
					if(parameter != null) {
						tnbClipId = Integer.parseInt(parameter.getValue());
					}
					
					Clip clip = rbtCacheManager.getClip(tnbClipId);
					
					if(clip != null) {
						SubscriberDownloads subscriberDownload = rbtDBManager.getActiveSubscriberDownload(subscriberID, clip.getClipRbtWavFile());
						logger.debug("subscriberDownload presents: " + subscriberDownload);
						if(subscriberDownload != null) {
							rbtDBManager.expireSubscriberDownload(subscriberID,
									clip.getClipRbtWavFile(),
									subscriberDownload.categoryID(),
									subscriberDownload.categoryType(), mode, null, false);
							logger.debug("subscriberDownload deactivated");
						}
						
						parameter =  parametersCacheManager.getParameter(iRBTConstant.COMMON, "TNB_CHARGE_CLASS", null);
						if(parameter != null) {
							task.put(param_chargeClass, parameter.getValue());
							task.put(param_useUIChargeClass, YES);
							logger.debug("parameter chargeclass: " + task.getString(param_chargeClass));
						}						
					}
					
					task.put(param_modifiedSubscriber, YES);
				}
			}
			response = super.processSelection(task);			
		}
		catch(Exception e) {
			logger.error("", e);
			response = ERROR;
		}
		return response;		
	}
	
	@Override
	public String disableRandomization(WebServiceContext task) {
		logger.debug("Inside method disable Randomization.....");
		String response = FAILED;
		String subscriberId = task.getString(param_subscriberID);
		SubscriberStatus subscriberSettings[] = rbtDBManager.getAllActiveSubscriberSettings(subscriberId);
		if (subscriberSettings != null) {
			Map<String, SubscriberStatus> callerIDSettingsMap = new HashMap<String, SubscriberStatus>();
			String deactivateBy = "UNRANDOMIZATION";
			
			Map<String, SubscriberStatus> allSettingsMap = new HashMap<String, SubscriberStatus>();
			
			// Latest one not deactivating
			for (int i = 0; i < subscriberSettings.length; i++) {
				String callerID = subscriberSettings[i].callerID();
				callerIDSettingsMap.put(callerID, subscriberSettings[i]);
				if(callerID == null) {
					allSettingsMap.put(subscriberSettings[i].subscriberFile(), subscriberSettings[i]);
				}
			}
			
			//Added for Vf-Egypt where latest download to be considered for all selections. RBT-6380
			SubscriberDownloads[] subscriberDownloads  = rbtDBManager.getActiveSubscriberDownloads(subscriberId);
			if(subscriberDownloads != null) {
				for(int i = subscriberDownloads.length-1; i >=0 ; i--) {
					SubscriberDownloads download = subscriberDownloads[i];
					if(allSettingsMap.containsKey(download.promoId())) {
						callerIDSettingsMap.put(null, allSettingsMap.get(download.promoId()));
						break;
					}
				}
			}
			
			for (int i = 0; i < subscriberSettings.length; i++) {
				String callerID = subscriberSettings[i].callerID();
				String refID = subscriberSettings[i].refID();
				SubscriberStatus subStatus = callerIDSettingsMap.get(callerID);
				if (!refID.equalsIgnoreCase(subStatus.refID()) && subscriberSettings[i].selType() != 2) {
					rbtDBManager.deactivateSubscriberRecordsByRefId(subscriberId, deactivateBy, refID);
				}
			}
		}

		HashMap<String, String> attributeMap = null;// new HashMap<String,String>();
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
		String extraInfo = subscriber.extraInfo();
		attributeMap = DBUtility.getAttributeMapFromXML(extraInfo);
		if (attributeMap != null && attributeMap.containsKey("UDS_OPTIN")) {
			attributeMap.remove("UDS_OPTIN");
		}
		extraInfo = DBUtility.getAttributeXMLFromMap(attributeMap);
		if (attributeMap == null) {
			attributeMap = new HashMap<String, String>();
		}
		attributeMap.put("EXTRA_INFO", extraInfo);
		response = rbtDBManager.updateSubscriber(subscriberId, attributeMap);
		logger.info("Response from DisableRandomization===" + response);
		return response;
	}
}
