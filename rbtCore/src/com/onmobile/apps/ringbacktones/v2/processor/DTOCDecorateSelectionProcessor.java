package com.onmobile.apps.ringbacktones.v2.processor;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedGroups;
import com.onmobile.apps.ringbacktones.rbt2.db.impl.GroupsDBImpl;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

public class DTOCDecorateSelectionProcessor extends AbstractDTOCDecoratorProcessor implements Constants,WebServiceConstants{

	Logger logger = Logger.getLogger(DTOCDecorateSelectionProcessor.class);
	RBTDBManager rbtDBManager = null;
	RBTCacheManager rbtCacheManager = null;
	
	public DTOCDecorateSelectionProcessor(BasicRBTProcessor processorObj){
		super();
		setProcessorObj(processorObj);
		rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
	}
	
	
	@Override
	public String deleteTone(WebServiceContext task) {
		
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		else {
			subscriber = rbtDBManager.getSubscriber(subscriberID);
			if(subscriber != null) {
				task.put(param_subscriber, subscriber);
			}
		}
		if(subscriber == null || rbtDBManager.isSubscriberDeactivated(subscriber)) {
			logger.debug("Subscriber not found returning error resonse " + SUB_DONT_EXIST);
			return SUB_DONT_EXIST;
		}
		return FEATURE_NOT_SUPPORTED;
	}
	
	@Override
	public String downloadTone(WebServiceContext task) {
		
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		if (subscriber == null || rbtDBManager.isSubscriberDeactivated(subscriber)) {
			return SUB_DONT_EXIST;
		}	
		return  FEATURE_NOT_SUPPORTED;
	}

	@Override
	public String processSelection(WebServiceContext task) {
		
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		if (!task.containsKey(param_subscriptionClass) && (subscriber == null || rbtDBManager.isSubscriberDeactivated(subscriber))) {
			return SUB_DONT_EXIST;
		}		
		
		String promoID = null;
		int categoryID = (task.containsKey(param_categoryID)) ? Integer
				.parseInt(task.getString(param_categoryID)) : -1;

		Category category = RBTCacheManager.getInstance().getCategory(
				categoryID);
		
		if (category == null)
			return CATEGORY_NOT_EXIST;
		
		if (task.containsKey(param_clipID)) {
			Clip clip = null;
			if(category.getCategoryTpe() == iRBTConstant.RECORD || ((String)task.get(param_clipID)).contains("_cut_")){
				clip = new Clip();
				clip.setClipRbtWavFile(task.getString(param_clipID));
			}else {
				 int clipID = Integer.parseInt(task.getString(param_clipID));
				 clip = rbtCacheManager.getClip(clipID);
			 }
			
				if (clip == null) {
					logger.info("Clip not in download or wrong clip ID: response : "
							+ FAILED);
					return CLIP_NOT_EXISTS;
				}
				promoID = clip.getClipRbtWavFile();
			} else if (task.containsKey(param_rbtFile)) {
				String rbtFile = task.getString(param_rbtFile);
				if (rbtFile.toLowerCase().endsWith(".wav"))
					promoID = rbtFile.substring(0, rbtFile.length() - 4);
				else
					promoID = rbtFile;
			}

		
			
			
			//CallerId checking
			String callerId = (!task.containsKey(param_callerID) || task
					.getString(param_callerID).equalsIgnoreCase(ALL)) ? null
					: task.getString(param_callerID);
			
			
			
			// Added for valid group check
			if(callerId != null && callerId.startsWith("G")){
				ExtendedGroups extendedgroup = new ExtendedGroups(Integer.parseInt(callerId.substring(1)), null, subscriberID, null, null, null);
				List<ExtendedGroups> groups = GroupsDBImpl.getGroups(extendedgroup );
				if(groups == null || groups.size() <= 0){
					return INVALID_GROUP_ID;
				}
			}
			
			

			Map<String, String> whereClauseMap = new HashMap<String, String>();

			if (category != null && Utility.isShuffleCategory(category.getCategoryTpe())) {
				
				whereClauseMap.put("CATEGORY_ID", category.getCategoryId() + "");
			} else {
				
				whereClauseMap.put("SUBSCRIBER_WAV_FILE", promoID);
			}
			
			whereClauseMap.put("CALLER_ID", callerId);
			
			int fromHrs = 0;
			int toHrs = 23;
			int fromMinutes = 0;
			int toMinutes = 59;
			
			if (task.containsKey(param_fromTime))
				fromHrs = Integer.parseInt(task.getString(param_fromTime));
			
			if (task.containsKey(param_toTime))
				toHrs = Integer.parseInt(task.getString(param_toTime));
			
			if (task.containsKey(param_toTimeMinutes))
				toMinutes = Integer.parseInt(task.getString(param_toTimeMinutes));
			
			if (task.containsKey(param_fromTimeMinutes))
				fromMinutes = Integer.parseInt(task.getString(param_fromTimeMinutes));
			
			String fromTime = ServiceUtil.getTime(fromHrs, fromMinutes);
			String toTime = ServiceUtil.getTime(toHrs, toMinutes);
			
			whereClauseMap.put("FROM_TIME", fromTime);
			whereClauseMap.put("TO_TIME", toTime);
			
			if(task.containsKey(param_status)) {
				whereClauseMap.put("STATUS", task.getString(param_status));
			}
			
			SubscriberStatus subscriberSetting = RBTDBManager.getInstance().getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subscriberID, whereClauseMap);
			if(subscriberSetting != null) {
				return SELECTION_ALREADY_ACTIVE;
			}

			// Added for ephemeral
			/*boolean isEphemeralRBT = task.containsKey(param_selectionInfo + "_PLAYCOUNT") && task.containsKey(param_status) && task.get(param_status).equals("200");
			//RBT-16269 added for profile selection
			boolean notProfileSelection = (!task.containsKey(param_selectionType) || !task.get(param_selectionType).equals("99"));
			if(notProfileSelection && !isEphemeralRBT){
				int rowCount = RBTDBManager.getInstance()
										   .getSubActDwnldsCount(subscriberID,whereClauseMap);
				if (rowCount == 0) {
					return CLIP_NOT_IN_LIBRARY;
				}
				
			}*/
			boolean isDirectActivation = task.containsKey(param_selDirectActivation) && task.get(param_selDirectActivation).equals(YES);
			
			if(!task.containsKey(param_udpId) && isDirectActivation){
				// Added to check default setting				
				Map<String, String> whereClause = new HashMap<String, String>();
				if (category != null && Utility.isShuffleCategory(category.getCategoryTpe())) {
					whereClause.put("CATEGORY_ID", category.getCategoryId() + "");
				} else {
					whereClause.put("SUBSCRIBER_WAV_FILE", promoID);
				}
				whereClause.put("CALLER_ID", null);
				SubscriberStatus subscriberDefaultSetting = RBTDBManager.getInstance().getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subscriberID, whereClause);
				if(subscriberDefaultSetting != null) {
					return SELECTION_ALREADY_ACTIVE;
				}
			}
			
			// Changed for RBT 2.0
			String processSelection = super.processSelection(task);
			if(processSelection.equalsIgnoreCase("SUCCESS") && isDirectActivation){
				
				if(!task.containsKey(param_udpId)) {
						
					//Changed for batch issue
					SubscriberStatus latestActiveSelection = ServiceUtil.getSubscriberLatestSelection(subscriberID, null);
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					if(latestActiveSelection != null){
						rbtDBManager.smDeactivateOldSelection(latestActiveSelection.subID(),
							latestActiveSelection.callerID(),latestActiveSelection.status(),sdf.format(latestActiveSelection.setTime()),latestActiveSelection.fromTime(),
							latestActiveSelection.toTime(),latestActiveSelection.selType(),latestActiveSelection.selInterval(),latestActiveSelection.refID(),true);
					}		
				}

			}
			
			//execute below lines if inlineFlag is null or enable for isDirectActivation but isDirectActivation use case in BasicRBTProcessor?
			//In successful SM callback (Inline flag + User is active + Not downloads model) create clip status column, prepare selection req like below
			if(isDirectActivation) {
			SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
			selectionRequest.prepareRequestParams(task);
			postProcessSelectionProcessing(processSelection, selectionRequest);
			}
			return processSelection;
	}
	
	
	@Override
	public String deleteSetting(WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		String refId = task.getString(param_refID);
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		if (subscriber == null || rbtDBManager.isSubscriberDeactivated(subscriber)) {
			return SUB_DONT_EXIST;
		}
		String deleteSetting = super.deleteSetting(task);
		postDeleteSettingProcessing(subscriberID,refId,deleteSetting);
		return deleteSetting;
	}
	
	@Override
	public String processUDPSelections(WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		if (subscriber == null || rbtDBManager.isSubscriberDeactivated(subscriber)) {
			return SUB_DONT_EXIST;
		}
		
		// Added for valid group check
		String callerId = (!task.containsKey(param_callerID) || task
				.getString(param_callerID).equalsIgnoreCase(ALL)) ? null
				: task.getString(param_callerID);
		
		if(callerId != null && callerId.startsWith("G")){
			ExtendedGroups extendedgroup = new ExtendedGroups(Integer.parseInt(callerId.substring(1)), null, subscriberID, null, null, null);
			List<ExtendedGroups> groups = GroupsDBImpl.getGroups(extendedgroup );
			if(groups == null || groups.size() <= 0){
				return INVALID_GROUP_ID;
			}
		}
		String processUDPSelection = super.processUDPSelections(task);
		return processUDPSelection;
	}
	
	@Override
	public String processUDPDeactivation(WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		if (subscriber == null || rbtDBManager.isSubscriberDeactivated(subscriber)) {
			return SUB_DONT_EXIST;
		}
		
		String processUDPDeactivation = super.processUDPDeactivation(task);
		return processUDPDeactivation;
	}
}
