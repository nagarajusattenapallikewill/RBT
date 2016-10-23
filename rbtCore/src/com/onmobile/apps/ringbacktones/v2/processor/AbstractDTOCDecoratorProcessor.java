package com.onmobile.apps.ringbacktones.v2.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedGroups;
import com.onmobile.apps.ringbacktones.rbt2.command.FeatureListRestrictionCommandList;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.daemon.RBTPlayerUpdateDaemonWrapper;
import com.onmobile.apps.ringbacktones.rbt2.db.impl.GroupMembersDBImpl;
import com.onmobile.apps.ringbacktones.rbt2.db.impl.GroupsDBImpl;
import com.onmobile.apps.ringbacktones.rbt2.logger.BasicCDRLogger;
import com.onmobile.apps.ringbacktones.rbt2.logger.dto.LoggerDTO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.exception.RestrictionException;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.service.AssetTypeAdapter;
import com.onmobile.apps.ringbacktones.webservice.RBTProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

public abstract class AbstractDTOCDecoratorProcessor implements RBTProcessor, WebServiceConstants, Constants {
	
	 Logger logger = Logger.getLogger(AbstractDTOCDecoratorProcessor.class);
	protected static ResourceBundle resourceBundle;

	protected AbstractDTOCDecoratorProcessor() {
		
	}
	
	private BasicRBTProcessor processorObj;
	
	protected static AssetTypeAdapter assetTypeAdapter = null;
	
	public void setAssetTypeAdapter(AssetTypeAdapter assetTypeAdapter) {
		this.assetTypeAdapter = assetTypeAdapter;
	}
	
	@Autowired
	private Boolean isSupportDirectActDct = true;

	public void setIsSupportDirectActDct(Boolean isSupportDirectActDct) {
		this.isSupportDirectActDct = isSupportDirectActDct;
	}

	protected BasicRBTProcessor getProcessorObj() {
		return processorObj;
	}

	protected void setProcessorObj(BasicRBTProcessor processorObj) {
		this.processorObj = processorObj;
	}

	@Override
	public String processActivation(WebServiceContext task) {
		String response = processorObj.processActivation(task);
		if(response != null && response.contains("SUCCESS")){
			ISubscriptionProcessor subscriptionProcessor = (ISubscriptionProcessor) ConfigUtil.getBean(BeanConstant.SUBSCRIPTION_PROCESSOR_BEAN);
			subscriptionProcessor.startProcessingProcessACT(task);
		}
		return response;
	}
 
	@Override
	public String updateSubscription(WebServiceContext task) {
		return processorObj.updateSubscription(task);
	}

	@Override
	public String processDeactivation(WebServiceContext task) {
		String response = processorObj.processDeactivation(task);
		if(response != null && response.contains("SUCCESS")){
			ISubscriptionProcessor subscriptionProcessor = (ISubscriptionProcessor) ConfigUtil.getBean(BeanConstant.SUBSCRIPTION_PROCESSOR_BEAN);
			subscriptionProcessor.startProcessingProcessACT(task);
		}
		return response;
	}

	@Override
	public String processRRBTConsentDeactivation(WebServiceContext task) {
		return processorObj.processRRBTConsentDeactivation(task);
	}

	@Override
	public String processSubscriberPromoRequest(WebServiceContext task) {
		return processorObj.processSubscriberPromoRequest(task);
	}

	@Override
	public String processNormalScratchCard(WebServiceContext task) {
		return processorObj.processNormalScratchCard(task);
	}

	@Override
	public String processSelection(WebServiceContext task) {
		String featureRestrictionResponse = featureRestriction(task);
		if(!featureRestrictionResponse.equalsIgnoreCase(Constants.SUCCESS)){
			return featureRestrictionResponse;
		}
		
		return processorObj.processSelection(task);
	}

	@Override
	public String deleteSetting(WebServiceContext task) {
		return processorObj.deleteSetting(task);
	}

	@Override
	public String updateSelection(WebServiceContext task) {
		return processorObj.updateSelection(task);
	}

	@Override
	public String downloadTone(WebServiceContext task) {
		return processorObj.downloadTone(task);
	}

	@Override
	public String deleteTone(WebServiceContext task) {
		return processorObj.deleteTone(task);
	}

	@Override
	public String shuffleDownloads(WebServiceContext task) {
		return processorObj.shuffleDownloads(task);
	}

	@Override
	public String upgradeSelectionPack(WebServiceContext task) {
		return processorObj.upgradeSelectionPack(task);
	}

	@Override
	public String upgradeSpecialSelectionPack(WebServiceContext task) {
		return processorObj.upgradeSpecialSelectionPack(task);
	}

	@Override
	public String deactivateOffer(WebServiceContext task) {
		return processorObj.deactivateOffer(task);
	}

	@Override
	public String deactivatePack(WebServiceContext task) {
		return processorObj.deactivatePack(task);
	}

	@Override
	public String subscribeUser(WebServiceContext task) {
		return processorObj.subscribeUser(task);
	}

	@Override
	public String updateCopyContestInfo(WebServiceContext task) {
		return processorObj.updateCopyContestInfo(task);
	}

	@Override
	public String addBookMark(WebServiceContext task) {
		return processorObj.addBookMark(task);
	}

	@Override
	public String removeBookMark(WebServiceContext task) {
		return processorObj.removeBookMark(task);
	}

	@Override
	public String processGroupRequest(WebServiceContext task) {
		return processorObj.processGroupRequest(task);
	}

	@Override
	public String processGroupMemberRequest(WebServiceContext task) {
		return processorObj.processGroupMemberRequest(task);
	}

	@Override
	public String processGroupMultipleMemberRequest(WebServiceContext task) {
		return processorObj.processGroupMultipleMemberRequest(task);
	}  

	@Override
	public String processAffiliateGroupRequest(WebServiceContext task) {
		return processorObj.processAffiliateGroupRequest(task);
	}

	@Override
	public String processAffiliateGroupMemberRequest(WebServiceContext task) {
		return processorObj.processAffiliateGroupMemberRequest(task);
	}

	@Override
	public String processCopyRequest(WebServiceContext task) {
		return processorObj.processCopyRequest(task);
	}

	@Override
	public String processDirectCopyRequest(WebServiceContext task) {
		return processorObj.processDirectCopyRequest(task);
	}

	@Override
	public String processGiftRequest(WebServiceContext task) {
		return processorObj.processGiftRequest(task);
	}

	@Override
	public String processGiftRejectRequest(WebServiceContext task) {
		return processorObj.processGiftRejectRequest(task);
	}

	@Override
	public String setSubscriberDetails(WebServiceContext task) {
		return processorObj.setSubscriberDetails(task);
	}

	@Override
	public String setApplicationDetails(WebServiceContext task) {
		return processorObj.setApplicationDetails(task);
	}

	@Override
	public String removeApplicationDetails(WebServiceContext task) {
		return processorObj.removeApplicationDetails(task);
	}

	@Override
	public String getApplicationDetails(WebServiceContext task) {
		return processorObj.getApplicationDetails(task);
	}

	@Override
	public String processBulkActivation(WebServiceContext task) {
		return processorObj.processBulkActivation(task);
	}

	@Override
	public String getDownloadOfTheDayEntries(WebServiceContext task) {
		return processorObj.getDownloadOfTheDayEntries(task);
	}

	@Override
	public String processDownloadOfDayInsertion(WebServiceContext task) {
		return processorObj.processDownloadOfDayInsertion(task);
	}

	@Override
	public String processBulkDeactivation(WebServiceContext task) {
		return processorObj.processBulkDeactivation(task);
	}

	@Override
	public String processBulkSelection(WebServiceContext task) {
		return processorObj.processBulkSelection(task);
	}

	@Override
	public String processBulkDeleteSelection(WebServiceContext task) {
		return processorObj.processBulkDeleteSelection(task);
	}

	@Override
	public String processBulkSetSubscriberDetails(WebServiceContext task) {
		return processorObj.processBulkSetSubscriberDetails(task);
	}

	@Override
	public String processBulkGetSubscriberDetails(WebServiceContext task) {
		return processorObj.processBulkGetSubscriberDetails(task);
	}

	@Override
	public String processBulkGetCorporateDetails(WebServiceContext task) {
		return processorObj.processBulkGetCorporateDetails(task);
	}

	@Override
	public String processBulkGetTaskDetails(WebServiceContext task) {
		return processorObj.processBulkGetTaskDetails(task);
	}

	@Override
	public String processBulkUpload(WebServiceContext task) {
		return processorObj.processBulkUpload(task);
	}

	@Override
	public String processBulkTask(WebServiceContext task) {
		return processorObj.processBulkTask(task);
	}

	@Override
	public String uploadNprocessBulkTask(WebServiceContext task) {
		return processorObj.uploadNprocessBulkTask(task);
		
	}

	@Override
	public String editBulkTask(WebServiceContext task) {
		return processorObj.editBulkTask(task);
		
	}

	@Override
	public String editBulkTaskForCorporate(WebServiceContext task) {
		return processorObj.editBulkTaskForCorporate(task);
		
	}

	@Override
	public String checkBulkSubscribersStatus(WebServiceContext task) {
		return processorObj.checkBulkSubscribersStatus(task);
		
	}

	@Override
	public String removeBulkTask(WebServiceContext task) {
		return processorObj.removeBulkTask(task);
		
	}

	@Override
	public String sendSMS(WebServiceContext task) {
		return processorObj.sendSMS(task);
		
	}

	@Override
	public String processHLRRequest(WebServiceContext task) {
		return processorObj.processHLRRequest(task);
		
	}

	@Override
	public String processSuspension(WebServiceContext task) {
		return processorObj.processSuspension(task);
		
	}

	@Override
	public String processThirdPartyRequest(WebServiceContext task) {
		return processorObj.processThirdPartyRequest(task);
		
	}

	@Override
	public String addData(WebServiceContext task) {
		return processorObj.addData(task);
		
	}

	@Override
	public String processData(WebServiceContext task) {
		return processorObj.processData(task);
		
	}

	@Override
	public String updateData(WebServiceContext task) {
		return processorObj.updateData(task);
		
	}

	@Override
	public String removeData(WebServiceContext task) {
		return processorObj.removeData(task);
		
	}

	@Override
	public String processUSSD(WebServiceContext task) {
		return processorObj.processUSSD(task);
		
	}

	@Override
	public String processAnnouncementActivation(WebServiceContext task) {
		return processorObj.processAnnouncementActivation(task);
		
	}

	@Override
	public String processAnnouncementDeactivation(WebServiceContext task) {
		return processorObj.processAnnouncementDeactivation(task);
		
	}

	@Override
	public String processBulkAnnouncementActivation(WebServiceContext task) {
		return processorObj.processBulkAnnouncementActivation(task);
		
	}

	@Override
	public String processBulkAnnouncementDeactivation(WebServiceContext task) {
		return processorObj.processBulkAnnouncementDeactivation(task);
		
	}

	@Override
	public String processBulkUpdateSubscription(WebServiceContext task) {
		return processorObj.processBulkUpdateSubscription(task);
		
	}

	@Override
	public String processSngActivation(WebServiceContext task) {
		return processorObj.processSngActivation(task);
		
	}

	@Override
	public String processSngAllDeativation(WebServiceContext task) {
		return processorObj.processSngAllDeativation(task);
		
	}

	@Override
	public String processSngUserUpdate(WebServiceContext task) {
		return processorObj.processSngUserUpdate(task);
		
	}

	@Override
	public String processSngDeactivation(WebServiceContext task) {
		return processorObj.processSngDeactivation(task);
		
	}

	@Override
	public String processChangeMsisdn(WebServiceContext task) {
		return processorObj.processChangeMsisdn(task);
		
	}

	@Override
	public String processSendChangeMsisdnRequestToSM(WebServiceContext task) {
		return processorObj.processSendChangeMsisdnRequestToSM(task);
		
	}
	
	@Override
	public String processDeleteConsentRecords(WebServiceContext task) {
		return processorObj.processDeleteConsentRecords(task);
		
	}

	@Override
	public String processUpgradeAllSelections(WebServiceContext task) {
		return processorObj.processUpgradeAllSelections(task);
		
	}

	@Override
	public String processBulkSelectionUpgradation(WebServiceContext task) {
		return processorObj.processBulkSelectionUpgradation(task);
		
	}

	@Override
	public String processAddMultipleSelections(WebServiceContext task) {
		return processorObj.processAddMultipleSelections(task);
		
	}

	@Override
	public String processAddMultipleDownloads(WebServiceContext task) {
		return processorObj.processAddMultipleDownloads(task);
		
	}

	@Override
	public String processDeleteMultipleSelections(WebServiceContext task) {
		return processorObj.processDeleteMultipleSelections(task);
		
	}

	@Override
	public String processDeleteMultipleDownloads(WebServiceContext task) {
		return processorObj.processDeleteMultipleDownloads(task);
		
	}

	@Override
	public String disableRandomization(WebServiceContext task) {
		return processorObj.disableRandomization(task);
		
	}

	@Override
	public String reset(WebServiceContext task) {
		return processorObj.reset(task);
	}

	@Override
	public String processUpgradeAllDownloads(WebServiceContext task) {
		return processorObj.processUpgradeAllDownloads(task);
	}

	@Override
	public String processRejectDelayDeactivation(
			WebServiceContext webServiceContext) {
		return processorObj.processRejectDelayDeactivation(webServiceContext);
	}

	@Override
	public String processUDPSelections(WebServiceContext task) {
		return processorObj.processUDPSelections(task);
	}

	@Override
	public String processUDPDeactivation(WebServiceContext task) {
		return processorObj.processUDPDeactivation(task);
	}
	
	@Override
	public String processUpgradeDownload(WebServiceContext task){
		return processorObj.processUpgradeDownload(task);
	}
	
	protected void removeCallerFromBlockedGroup(String subscriberId, String callerId) {
		if (callerId != null && !callerId.isEmpty()
				&& !callerId.startsWith("G")) {
			ExtendedGroups extendedGroup = new ExtendedGroups(-1, "99",
					subscriberId, null, null, null);
			List<ExtendedGroups> extendedGroups = GroupsDBImpl
					.getGroups(extendedGroup);
			List<GroupMember> groupMembers = null;

			if (extendedGroups != null && !extendedGroups.isEmpty()) {
				for (ExtendedGroups group : extendedGroups) {
					GroupMember member = new GroupMember();
					member.setGroupID(group.groupID() + "");
					member.setMemberID(callerId);
					int rowCount = GroupMembersDBImpl
							.getGroupMemberCount(member);
					if (rowCount > 0) {
						if (groupMembers == null)
							groupMembers = new ArrayList<GroupMember>(1);
						groupMembers.add(member);
					}
				}
			}
			try {
				if (groupMembers != null)
					GroupMembersDBImpl.deleteGroupMembers(Integer
							.parseInt(groupMembers.get(0).getGroupID()),
							groupMembers);
			} catch (Exception e) {
				logger.error("Exception Occured: " + e, e);
			}
		}
	}
	
	protected void processClipTransfer(SelectionRequest selectionRequest, Subscriber subscriber) {

		try{
			ISelectionProcessor selectionProcessor = (ISelectionProcessor) ConfigUtil.getBean(BeanConstant.SELECTION_PROCESSOR_BEAN); 
			if(selectionProcessor !=null){
			 WebServiceContext task = new WebServiceContext();
			 task.put(WebServiceConstants.param_clipID, selectionRequest.getClipID());
			 task.put(WebServiceConstants.param_udpId, selectionRequest.getUdpId());
			 task.put(WebServiceConstants.param_categoryID, selectionRequest.getCategoryID());
			 selectionProcessor.startProcessing(task, subscriber);
			}
		}catch(Exception e){
			logger.warn("No such bean exception for :"+BeanConstant.SELECTION_PROCESSOR_BEAN);
		}
	
	}
	
	
	protected boolean clipTransferReqd(Subscriber subscriber) {
		if(isSupportDirectActDct && !RBTDBManager.getInstance().isSubscriberActivationPending(subscriber)) {
			return true;
		}
		return false;
	}
	
	
	protected void writeLoggerForSelectionSuccess(String response, SubscriberStatus subscriberStatus) {
		LoggerDTO loggerDTO = (LoggerDTO) ConfigUtil.getBean(BeanConstant.CDR_LOGGER_DTO_BEAN);
		BasicCDRLogger selectionActLogger = (BasicCDRLogger) ConfigUtil.getBean(BeanConstant.SELECTION_ACT_CDR_LOGGER_BEAN);
		loggerDTO = selectionActLogger.getLoggerDTOForSelACTSuccess(loggerDTO, subscriberStatus);
		loggerDTO.setResponesStatus(response);
	    selectionActLogger.writeCDRLog(loggerDTO);
	}
	
	protected void writeLoggerForSelectionFailure(String subscriberID, SelectionRequest selectionRequest, AssetType type, String response) throws UserException {
		LoggerDTO loggerDTO = (LoggerDTO) ConfigUtil.getBean(BeanConstant.CDR_LOGGER_DTO_BEAN);
		BasicCDRLogger selectionActLogger = (BasicCDRLogger) ConfigUtil.getBean(BeanConstant.SELECTION_ACT_CDR_LOGGER_BEAN);
		loggerDTO = selectionActLogger.getLoggerDTOForSelACTFailure(loggerDTO, subscriberID, selectionRequest, type);
		loggerDTO.setResponesStatus(response);
		selectionActLogger.writeCDRLog(loggerDTO);
	}
	
	protected void postProcessSelectionProcessing(String response, SelectionRequest selectionRequest){
		SubscriberStatus subscriberStatus = getSubscriberLatestSelection(selectionRequest.getSubscriberID(), selectionRequest.getCallerID());
		if ((response.equalsIgnoreCase("SUCCESS") || response
				.equalsIgnoreCase("SUCCESS_DOWNLOAD_EXISTS"))) {
			removeCallerFromBlockedGroup(selectionRequest.getSubscriberID(), selectionRequest.getCallerID());			
			//Tone player code 			
			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(selectionRequest.getSubscriberID());
			if(clipTransferReqd(subscriber)) {
				processClipTransfer(selectionRequest, subscriber);
			}
			writeLoggerForSelectionSuccess(response, subscriberStatus);
		} else {
			try {
				String type = null;
				AssetType assetType = null;
				String udpId = subscriberStatus.udpId();
				if (udpId != null) {
					type = assetTypeAdapter.getAssetType(-1);
				} else {
					type = assetTypeAdapter.getAssetType(subscriberStatus
							.categoryType());
				}

				assetType = AssetType.valueOf(type);
				writeLoggerForSelectionFailure(selectionRequest.getSubscriberID(),
						selectionRequest, assetType, response);
			} catch (Exception e) {
				logger.info("Exception occured while getting asset type.");
			}
		}
	}
	
	protected SubscriberStatus getSubscriberLatestSelection(String subscriberId, String callerId) {
		Map<String, String> whereClauseMap = new HashMap<String, String>(1);
		callerId = RBTDBManager.getInstance().subID(callerId);
		whereClauseMap.put("CALLER_ID", callerId);			
		return ServiceUtil.getSubscriberLatestSelection(subscriberId, whereClauseMap);
	}
	
	protected void postDeleteSettingProcessing(String subscriberID, String refId, String response){
		SubscriberStatus selection = RBTDBManager.getInstance().getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(subscriberID, refId, "REF_ID");
		LoggerDTO loggerDTO = (LoggerDTO) ConfigUtil.getBean(BeanConstant.CDR_LOGGER_DTO_BEAN);
		BasicCDRLogger selectionDctLogger = (BasicCDRLogger) ConfigUtil.getBean(BeanConstant.SELECTION_DCT_CDR_LOGGER_BEAN);
		loggerDTO = selectionDctLogger.getLoggerDTOForSelectionDCT(loggerDTO, selection, subscriberID);
		loggerDTO.setRefId(refId);
		loggerDTO.setResponesStatus(response);
		selectionDctLogger.writeCDRLog(loggerDTO);
		
		if(response.equals("success")){
			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
			if(isSupportDirectActDct && !RBTDBManager.getInstance().isSubscriberActivationPending(subscriber)){
				RBTPlayerUpdateDaemonWrapper.getInstance().removeSelectionsFromTonePlayer(subscriber);
			}
		
		}
	}
	
	protected void writeLoggerForDownloadSuccess(String response, SubscriberDownloads download) {
		LoggerDTO loggerDTO = (LoggerDTO) ConfigUtil.getBean(BeanConstant.CDR_LOGGER_DTO_BEAN);
		BasicCDRLogger downloadActLogger = (BasicCDRLogger) ConfigUtil.getBean(BeanConstant.DOWNLOAD_ACT_CDR_LOGGER_BEAN);
		loggerDTO = downloadActLogger.getLoggerDTOForDownloadACTSuccess(loggerDTO, download);
		loggerDTO.setResponesStatus(response);
		downloadActLogger.writeCDRLog(loggerDTO);
	}
	
	protected void writeLoggerForDownloadFailure(SelectionRequest selectionRequest, String cType,  String response) {
		LoggerDTO loggerDTO = (LoggerDTO) ConfigUtil.getBean(BeanConstant.CDR_LOGGER_DTO_BEAN);
		BasicCDRLogger downloadActLogger = (BasicCDRLogger) ConfigUtil.getBean(BeanConstant.DOWNLOAD_ACT_CDR_LOGGER_BEAN);
		loggerDTO = downloadActLogger.getLoggerDTOForDownloadACTFaliure(loggerDTO,selectionRequest, cType);
		loggerDTO.setResponesStatus(response);
		downloadActLogger.writeCDRLog(loggerDTO);
	}
	
	
	//Added for feature restriction task
	protected String featureRestriction(WebServiceContext task){
		String subscriberID = task.getString(param_subscriberID);
		String clipID=task.getString(param_clipID) ;
		
		// Added for rbt wav file with cut changes
        if(clipID.contains("rbt_")){
        	logger.info("clip id contains wav file name so taking clip id by wav file name..");
        	Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(clipID);
        	if(clip != null){
        		clipID = String.valueOf(clip.getClipId());
        	}
        }
		
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		String subscriptionClass = null;
		if(subscriber != null && subscriber.subscriptionClass() != null){
			subscriptionClass = subscriber.subscriptionClass();
		}
		if(task.containsKey(param_subscriptionClass)){
			subscriptionClass = (String)task.get(param_subscriptionClass);
		}else if(task.containsKey(param_rentalPack)){
			subscriptionClass = (String)task.get(param_rentalPack);
		}
		
		SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
		selectionRequest.prepareRequestParams(task);
		resourceBundle = ResourceBundle.getBundle("config");
		String catid = getValueFromResourceBundle(resourceBundle, "specific.catergory.id", null);
		
		try{
			FeatureListRestrictionCommandList featureListRestrictionCommandList = (FeatureListRestrictionCommandList) ConfigUtil.getBean(BeanConstant.FEATURE_LIST_RESTRICTION_COMMAND_LIST);
			if(featureListRestrictionCommandList != null && subscriptionClass != null) {
				featureListRestrictionCommandList.executeInlineCallCommands(selectionRequest, subscriptionClass,clipID);
				
				//if clip inside that configured cat then task.put(cate_id,configured)
				if(catid == null){
					catid = "103";
				}
				logger.info("****catid"+catid);
				Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(Integer.parseInt(catid));
				if(clips != null && clips.length > 0){	
					for (Clip clip : clips) {
						logger.info(" clip present in category  " + clip.getClipId());
						if (clip.getClipId() == Integer.parseInt(clipID)) {
							task.put(param_categoryID, catid);
						}
					
					}
				}
				return  Constants.SUCCESS;
			}
		}
		catch(RestrictionException e){
			logger.info("Exception occured while executing feature restriction command bean..."+e);
			if(task.containsKey(param_subscriptionClass) || task.containsKey(param_rentalPack)){
				processActivation(task);
				return ACTIVATION_SUPPORTED_SELECTION_NOT_SUPPORTED;
			}
			return e.getResponse();
		}catch(Exception e){
			logger.info("Exception occured while executing feature restriction command bean..."+e);
		}
		return Constants.SUCCESS;
	}
	

	protected  String getValueFromResourceBundle(ResourceBundle resourceBundle, String key, String defaultValue) {
		if(resourceBundle == null) {
			logger.info("ResourceBundle is null, so returning default value: "+defaultValue);
			return defaultValue;
		}
		String value = null;

		try {
			value = resourceBundle.getString(key).trim();
		} catch (MissingResourceException e) {
			logger.error("RBT:: " + e.getMessage());
			value = defaultValue;
		}

		return value;
	}

}
