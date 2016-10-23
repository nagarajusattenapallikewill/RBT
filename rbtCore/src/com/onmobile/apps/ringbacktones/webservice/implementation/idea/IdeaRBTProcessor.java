/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.idea;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author himanshu.goyal
 *
 */
public class IdeaRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(IdeaRBTProcessor.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getCos(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber)
	{
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processActivation(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public String processActivation(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		try
		{
			String subscriberID = webServiceContext.getString(param_subscriberID);
			String categoryID = webServiceContext.getString(param_categoryID);
			Subscriber oldSubscriber = DataUtils.getSubscriber(webServiceContext);

			String udsNotAllowedCosIds = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.COMMON, "UDS_NOT_ALLOWED_COS_IDS", null);
			String udsAllowedCategories = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.COMMON, "UDS_ALLOWED_CATEGORIES", null);
			String cosID = null;
			if(oldSubscriber != null){
				cosID = oldSubscriber.cosID();
			}
			if(categoryID != null && udsAllowedCategories != null && !udsAllowedCategories.isEmpty() && Arrays.asList(udsAllowedCategories.split(",")).contains(categoryID)
					&& cosID !=null && udsNotAllowedCosIds != null && !udsNotAllowedCosIds.isEmpty()) {
				List<String> udsNotAllowedCosId = Arrays.asList(udsNotAllowedCosIds.split(","));
				if(udsNotAllowedCosId.contains(cosID)) {
					logger.info("rejecting request for easytoneUser: "+ cosID );
					return NOT_ALLOWED;
				}
			}
			response = super.processActivation(webServiceContext);

			String activatedBy = getMode(webServiceContext);

			String modesStr = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REACTIVATION_SUPPORTED_MODES", "");
			List<String> supportedModes = Arrays.asList(modesStr.split(","));
			boolean reactUser = oldSubscriber != null
					&& supportedModes.contains(oldSubscriber.activatedBy())
					&& supportedModes.contains(activatedBy);
			if (!reactUser)
			{
				logger.debug("Not supported mode for reactivation");
				return response;
			}

			@SuppressWarnings("null") // subscriber object cant be null here
			String oldSubYes = oldSubscriber.subYes();
			if (response.equalsIgnoreCase(ACTIVE))
			{
				return Utility.upgradeSubscriptionValidity(webServiceContext, oldSubscriber);
			}
			else if (response.equalsIgnoreCase(SUCCESS) && oldSubYes.equals(iRBTConstant.STATE_DEACTIVATED)
					 && !webServiceContext.containsKey(param_isPreConsentBaseRequest))
			{
				String newSelClipID = webServiceContext.getString(param_clipID);
				SubscriberStatus[] settings =  rbtDBManager.getSubscriberRecords(subscriberID);
				if (settings != null)
				{
					// Iterating the selections starting from latest to oldest
					for (int i = (settings.length - 1); i >= 0; i--)
					{
						if (supportedModes.contains(settings[i].selectedBy()))
						{
							Clip clip = rbtCacheManager.getClipByRbtWavFileName(settings[i].subscriberFile());
							if (newSelClipID != null && !String.valueOf(clip.getClipId()).equals(newSelClipID))
							{
								logger.info("New Selection clipID is different from previous deactivated retailer clipID," +
										" so not inserting a new selection");
								break;
							}

							HashMap<String, Object> clipMap = new HashMap<String, Object>();
							clipMap.put("CLIP_CLASS", clip.getClassType());
							clipMap.put("CLIP_END", clip.getClipEndTime());
							clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
							clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
							clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
							clipMap.put("CLIP_NAME", clip.getClipName());

							HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(DBUtility
									.setXMLAttribute(settings[i].extraInfo(), "REACT_REFID", settings[i].refID()));

							String prepaidYes = (settings[i].prepaidYes()) ? "y" : "n";
							char loopStatus = rbtDBManager.getLoopStatusForNewSelection(false,
									subscriberID, settings[i].prepaidYes());
							rbtDBManager.createSubscriberStatus(subscriberID,
									settings[i].callerID(),
									settings[i].categoryID(),
									settings[i].subscriberFile(), null,
									settings[i].startTime(),
									RBTDBManager.m_endDate,
									settings[i].status(),
									settings[i].selectedBy(),
									settings[i].selectionInfo(),
									settings[i].nextChargingDate(), prepaidYes,
									settings[i].classType(), true,
									settings[i].fromTime(),
									settings[i].toTime(), "W", true, clipMap,
									settings[i].categoryType(), false,
									loopStatus, false, 0,
									oldSubscriber.rbtType(),
									settings[i].selInterval(), extraInfoMap,
									null, false, oldSubscriber.circleID());
							
							webServiceContext.put(param_songAlreadyAdded, YES);
							break;
						}
					}
				}
				return response;
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			response = ERROR;
		}

		return response;
	}
	
	@Override
	public String processSelection(WebServiceContext webServiceContext) {
		String response = ERROR;
		try {
			String udsNotAllowedCosIds = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.COMMON, "UDS_NOT_ALLOWED_COS_IDS", null);
			boolean isEasyTone = false;
			boolean isUdsUser = false;
			boolean isCDTNDTUser = false;
			boolean isActiveUDSUser = false;
			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
			String cosID = null;
			if (subscriber != null) {
				cosID = subscriber.cosID();
				HashMap<String,String> extraInfMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
				String isUdsUserOptInTrue = Utility.isUDSUser(extraInfMap, false);
				if(isUdsUserOptInTrue!=null){
					isActiveUDSUser = true;
				}
				if (webServiceContext.containsKey("userInfo_UDS_OPTIN")) {
					isUdsUser = "true".equalsIgnoreCase(webServiceContext.getString("userInfo_UDS_OPTIN"));
				}
				if(Utility.getSubscriberStatus(subscriber).equalsIgnoreCase(ACTIVE)){
					isCDTNDTUser =  com.onmobile.apps.ringbacktones.Gatherer.Utility.isUserCDTNDT(subscriber.cosID());	
				}
			}
			if (cosID != null && udsNotAllowedCosIds != null && !udsNotAllowedCosIds.isEmpty()) {
				List<String> udsNotAllowedCosId = Arrays.asList(udsNotAllowedCosIds.split(","));
				if (udsNotAllowedCosId.contains(cosID)) {
					isEasyTone = true;
				}
			}
			
			if (isUdsUser && isEasyTone) {
				response = NOT_ALLOWED;
				logger.info("rejecting request for easytoneUser: " + cosID);
			} else {
				String isPreConsentBaseSelRequest = webServiceContext.getString(WebServiceConstants.param_isPreConsentBaseSelRequest);
				//Checking Normal / SPL caller
				boolean isAllCaller = true ;
				if (webServiceContext.containsKey(param_callerID)) {
					String callerId = webServiceContext.getString(param_callerID);
					isAllCaller = ((callerId == null || callerId.equalsIgnoreCase(ALL)) ? true : false);
				}
				
				if(isCDTNDTUser && !isActiveUDSUser && isAllCaller && !(isPreConsentBaseSelRequest!=null && isPreConsentBaseSelRequest.equalsIgnoreCase("true"))){
						String activatedBy = getMode(webServiceContext);
						String cdtUsesrNoConsentModeMapStr = parametersCacheManager.getParameterValue(iRBTConstant.WEBSERVICE,"CDT_USER_NO_CONSENT_MODE_MAP", "");
						Map<String,String> cdtUsesrNoConsentModeMap = MapUtils.convertIntoMap(cdtUsesrNoConsentModeMapStr, ";", "=", ",");
						if(cdtUsesrNoConsentModeMap.containsKey(activatedBy) && cdtUsesrNoConsentModeMap.get(activatedBy)!=null)
							webServiceContext.put(param_mode, cdtUsesrNoConsentModeMap.get(activatedBy));
				 }
				response = super.processSelection(webServiceContext);
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		return response;
	}	
	
	@Override
	public String updateSubscription(WebServiceContext webServiceContext) {
		String response = ERROR;
		try {
			String udsNotAllowedCosIds = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.COMMON, "UDS_NOT_ALLOWED_COS_IDS", null);
			boolean isEasyTone = false;
			boolean isUdsUser = false;
			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
			String cosID = null;
			if (subscriber != null) {
				cosID = subscriber.cosID();
				if (webServiceContext.containsKey("userInfo_UDS_OPTIN")) {
					isUdsUser = "true".equalsIgnoreCase(webServiceContext.getString("userInfo_UDS_OPTIN"));
				}
			}

			if (cosID != null && udsNotAllowedCosIds != null && !udsNotAllowedCosIds.isEmpty()) {
				List<String> udsNotAllowedCosId = Arrays.asList(udsNotAllowedCosIds.split(","));
				if (udsNotAllowedCosId.contains(cosID)) {
					isEasyTone = true;
				}
			}
			if (isUdsUser && isEasyTone) {
				response = NOT_ALLOWED;
				logger.info("rejecting request for easytoneUser: " + cosID);
			} else {
				response = super.updateSubscription(webServiceContext);
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		return response;
	}	
}
