package com.onmobile.apps.ringbacktones.v2.processor;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedGroups;
import com.onmobile.apps.ringbacktones.rbt2.builder.IAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.impl.GroupsDBImpl;
import com.onmobile.apps.ringbacktones.rbt2.logger.BasicCDRLogger;
import com.onmobile.apps.ringbacktones.rbt2.logger.dto.LoggerDTO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.IUDPDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.dao.impl.UDPDaoImpl.UDPType;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.ISelectionRequest;
import com.onmobile.apps.ringbacktones.v2.service.IUDPService;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

public class DTOCDecorateDownloadProcessor extends AbstractDTOCDecoratorProcessor implements Constants,WebServiceConstants{

	Logger logger = Logger.getLogger(DTOCDecorateDownloadProcessor.class);
	RBTDBManager rbtDBManager = null;
	RBTCacheManager rbtCacheManager = null;
	
	public DTOCDecorateDownloadProcessor(BasicRBTProcessor processorObj){
		super();
		setProcessorObj(processorObj);
		rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
	}
	
	
	@Override
	public String deleteTone(WebServiceContext task) {
		
		String action = task.getString(param_action);
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
		
		
		String browsingLanguage = task.getString(param_browsingLanguage);
		String promoID = null;
		int categoryID;
		
		if (action.equalsIgnoreCase(action_deleteTone)) {

			if (task.containsKey(param_clipID)) {
				int clipID = Integer.parseInt(task.getString(param_clipID));
				Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);
				if (clip == null) {
					logger.info("Clip not in download or wrong clip ID: response : "
							+ FAILED);
					return FAILED;
				}
				promoID = clip.getClipRbtWavFile();
			} else if (task.containsKey(param_rbtFile)) {
				String rbtFile = task.getString(param_rbtFile);
				if (rbtFile.toLowerCase().endsWith(".wav"))
					promoID = rbtFile.substring(0, rbtFile.length() - 4);
				else
					promoID = rbtFile;
			}

			categoryID = (task.containsKey(param_categoryID)) ? Integer.parseInt(task.getString(param_categoryID)) : -1;
			
			Category category = RBTCacheManager.getInstance().getCategory(categoryID);
			
			Map<String, String> whereClauseMap = new HashMap<String, String>();
			
			if(category !=  null && Utility.isShuffleCategory(category.getCategoryTpe())) {
				whereClauseMap.put("CATEGORY_ID", category.getCategoryId()+"");
			}
			else {
				whereClauseMap.put("SUBSCRIBER_WAV_FILE", promoID);
			}
				
			List<SubscriberStatus> subscriberSettings = RBTDBManager.getInstance().getSubscriberActiveSelections(subscriberID, whereClauseMap);
			String response = null;
			if(subscriberSettings != null && !subscriberSettings.isEmpty()) {
				ISelectionRequest selectionRequestResolver = (ISelectionRequest) ConfigUtil.getBean(BeanConstant.SELECTION_REQUEST_RESOLVER);
				for(SubscriberStatus subscriberSetting : subscriberSettings) {					
					try {						
						if(subscriberSetting.udpId() != null) {
							String type = null;
							String toneId = null;
							try {
								if(promoID != null && !promoID.toLowerCase().startsWith("rbt")) {

									IRbtUgcWavfileDao rbtUgcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
									RBTUgcWavfile rbtUgcWavfile = rbtUgcWavfileDao.getUgcWavFile(Long.parseLong(subscriberID), promoID);
									type = "RBTUGC";
									toneId = rbtUgcWavfile.getUgcId()+"";
								}	
								else if(promoID != null){								
									Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(promoID);
									if(clip == null)
										throw new UserException(CLIP_NOT_EXIST);
									type = "SONG";
									toneId = clip.getClipId()+"";
								}

								IUDPService udpService = (IUDPService) ConfigUtil.getBean(BeanConstant.UDP_RBT_SERVICE_IMPL);
								response = (String) udpService.deleteContentFromUDP(subscriberID, subscriberSetting.udpId(), toneId, type);
							}
							catch (Exception e) {
								throw new UserException(e.getMessage());
							}
						} else {
							Map<String,String> responseMap = selectionRequestResolver.deactivateSong(subscriberSetting.refID(), subscriberSetting.subID(), task.getString(param_mode));
							response = responseMap.get("code");
						}
					}
					catch (UserException e) {
						logger.error("Exception Occured: "+e);
						return "deactSelError";
						
					}
				}
			}
			
			if (response == null
					|| response.equalsIgnoreCase(Constants.SUCCESS)) {
				response = super
						.deleteTone(task);

				if (response != null
						&& response.equalsIgnoreCase(Constants.SUCCESS)) {

					String assetType = null;
					try {
						assetType = ServiceUtil.getAssetType(category
								.getCategoryTpe());
					} catch (Exception e) {
						logger.error("Exception Occured: " + e, e);
					}

					if (assetType != null && !assetType.isEmpty()) {

						IAssetUtilBuilder assetUtilBuilder = (IAssetUtilBuilder) ConfigUtil
								.getBean(assetType.trim().toLowerCase());
						long toneId = -1;
						try {
							toneId = assetUtilBuilder.getToneID(subscriberID,
									promoID);
							if (toneId != -1) {
							}
						} catch (UserException e1) {
						}
						IUDPDao udpDao = (IUDPDao) ConfigUtil
								.getBean(BeanConstant.UDP_DAO_IMPL);

						try {
							udpDao.removeContentUDP(subscriberID, toneId,
									UDPType.valueOf(assetType.toUpperCase()));
						} catch (DataAccessException e) {
							logger.error(toneId
									+ " : not found in rbt_udp_clip_map table");
						}

						if (assetType.equalsIgnoreCase(AssetType.RBTUGC
								.toString())) {
							IRbtUgcWavfileDao rbtUgcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil
									.getBean(BeanConstant.UGC_WAV_FILE_DAO);
							long subId = Long.parseLong(subscriberID);
							try {
								rbtUgcWavfileDao.deleteUgcWavfiles(subId,
										promoID);
							} catch (DataAccessException e) {

							}
						}
						postDeleteToneProcessing(response, subscriberID,promoID,category);
					}
				}
			}
			return response;
		}
		return null;
	}
	
	@Override
	public String downloadTone(WebServiceContext task) {
		
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		if (subscriber == null || rbtDBManager.isSubscriberDeactivated(subscriber)) {
			return SUB_DONT_EXIST;
		}
		String downloadTone = super.downloadTone(task);
		postDownloadToneProcessing(subscriberID, task, downloadTone);
		return  downloadTone;
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
			if(category.getCategoryTpe() == iRBTConstant.RECORD){
				clip = new Clip();
				clip.setClipRbtWavFile(task.getString(param_clipID));
			} else {
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
			boolean isEphemeralRBT = task.containsKey(param_selectionInfo + "_PLAYCOUNT") && task.containsKey(param_status) && task.get(param_status).equals("200");
			//RBT-16269 added for profile selection
			boolean notProfileSelection = (!task.containsKey(param_selectionType) || !task.get(param_selectionType).equals("99"));
			if(notProfileSelection && !isEphemeralRBT){
				int rowCount = RBTDBManager.getInstance()
										   .getSubActDwnldsCount(subscriberID,whereClauseMap);
				if (rowCount == 0) {
					return CLIP_NOT_IN_LIBRARY;
				}
				
			}
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
			SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
			selectionRequest.prepareRequestParams(task);
			postProcessSelectionProcessing(processSelection, selectionRequest);
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
	
	
	private void postDownloadToneProcessing(String subscriberID, WebServiceContext task, String downloadTone) {
		SubscriberDownloads[] downloads = rbtDBManager
				.getSubscriberDownloadsWithoutTrack(subscriberID);
		SubscriberDownloads download = null;
		if (downloads != null) {
			download = downloads[downloads.length - 1];
		}
		String cType = "SONG";
		try {
			cType = assetTypeAdapter.getAssetType(download.categoryType());
		} catch (Exception e) {
			logger.info("Exception occured while getting ctype from assetTypeAdapter...");
		}
		SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
		selectionRequest.prepareRequestParams(task);
		if (!downloadTone.equalsIgnoreCase("success")) {
			writeLoggerForDownloadFailure(selectionRequest, cType, downloadTone);
		} else if (!Utility.isConsentRequest(task)) {
			writeLoggerForDownloadSuccess(downloadTone, download);
		}
	}
	
	private void postDeleteToneProcessing(String response, String msisdn,String promoId, Category category){
		SubscriberDownloads activeDownload = null;
		if(category !=  null && Utility.isShuffleCategory(category.getCategoryTpe())) {
			 activeDownload  = rbtDBManager.getActiveSubscriberDownloadByCatIdOrPromoId(msisdn, String.valueOf(category.getCategoryId()), true);
		}
		else {
			 activeDownload  = rbtDBManager.getActiveSubscriberDownloadByCatIdOrPromoId(msisdn, promoId, false);
		}
		LoggerDTO loggerDTO = (LoggerDTO) ConfigUtil.getBean(BeanConstant.CDR_LOGGER_DTO_BEAN);
		BasicCDRLogger downloadDctLogger = (BasicCDRLogger) ConfigUtil.getBean(BeanConstant.DOWNLOAD_DCT_CDR_LOGGER_BEAN);
		loggerDTO = downloadDctLogger.getLoggerDTOForDownloadDCT(loggerDTO, activeDownload, msisdn);
		if(activeDownload == null) {
			Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(promoId);
			loggerDTO.setToneId((long)clip.getClipId());		
		}
		loggerDTO.setResponesStatus(response);			
		downloadDctLogger.writeCDRLog(loggerDTO);
	}
		@Override
	public String processUpgradeDownload(WebServiceContext webServiceContext){
		return getProcessorObj().processUpgradeDownload(webServiceContext);

	}

}
