package com.onmobile.apps.ringbacktones.v2.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.IClipStatusMappingDAO;
import com.onmobile.apps.ringbacktones.rbt2.thread.ProcessingClipTransfer;
import com.onmobile.apps.ringbacktones.rbt2.thread.ThreadExecutor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.dao.bean.ClipStatusMapping;
import com.onmobile.apps.ringbacktones.v2.dao.bean.OperatorCircleMapping;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;
import com.onmobile.apps.ringbacktones.v2.dao.bean.ClipStatusMapping.CompositeKey;

public class ClipUtilService implements IClipUtils, Constants {
	private static Logger logger = Logger.getLogger(ClipUtilService.class);
	private int DEFAULT_CATEGORY = 3;
	private int DEFAULT_CATEGORY_TYPE = 7;

	@Override
	public Map<String, String> updateClipToTPIfNotExists(String subscriberID,
			String rbtWavFile, String categoryId, String circleId)
			throws UserException {
		boolean canProcessClipStatus = RBTParametersUtils.getParamAsBoolean(
				"COMMON", "CHECK_CLIP_STATUS_AND_PROCESS", "FALSE");
		logger.info("updateClipToTPIfNotExists Request reached: subscriberId: "
				+ subscriberID + ", rbtWavFile: " + rbtWavFile
				+ ",categoryId:  " + categoryId + " ,circleId: " + circleId);
		boolean clipUpdated = false;
		Map<String, String> map = new HashMap<String, String>();
		if (canProcessClipStatus) {
			logger.info("checkIfClipExistsAndUpdate subscriberID:"
					+ ", rbtWavFile: " + rbtWavFile + ", categoryId: "
					+ categoryId);
			int categoryID = DEFAULT_CATEGORY;
			int categoryType = DEFAULT_CATEGORY_TYPE;
			if (null != categoryId && !categoryId.isEmpty()) {
				try {
					categoryID = Integer.parseInt(categoryId);
					Category category = RBTCacheManager.getInstance()
							.getCategory(categoryID);
					if (null != category) {
						categoryType = category.getCategoryTpe();
					}
				} catch (Exception e) {
					logger.info("passed category id is not an number format");
				}
			}
			if (circleId != null && !circleId.isEmpty()) {
				clipUpdated = checkIfClipExistsAndUpdate(rbtWavFile,
						categoryType, categoryID, circleId);
			} else if (subscriberID != null && !subscriberID.isEmpty()) {
				clipUpdated = checkIfClipExistsAndUpdate(subscriberID,
						rbtWavFile, categoryType, categoryID);
			}
		}
		map.put("status", (clipUpdated == true) ? "success" : "failure");
		return map;
	}

	public boolean checkIfClipExistsAndUpdate(String subscriberID,
			String rbtWavFile, int categoryType, int categoryId) {
		boolean clipUpdated = false;
		try {
			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(
					subscriberID);
			String circleID = subscriber.circleID();
			clipUpdated = checkIfClipExistsAndUpdate(rbtWavFile, categoryType,
					categoryId, circleID);
		} catch (Exception e) {
			logger.error(
					"Exception adding clips to clip status table. TP might noto play song for rbtWavFile="
							+ rbtWavFile + ", categoryId=" + categoryId, e);
		}
		return clipUpdated;
	}

	private boolean checkIfClipExistsAndUpdate(String rbtWavFile,
			int categoryType, int categoryId, String circleID) {
		boolean clipUpdated = false;
		try {
			String catType = Utility.getCategoryType(categoryType);
			if (catType.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)
					|| catType
							.equalsIgnoreCase(WebServiceConstants.CATEGORY_KARAOKE))
				return false;
			Clip[] clips = null;
			if (Utility.isShuffleCategory(categoryType)) {
				// RBT-14894
				String isCategoryToBeInsertedInClipStatusTable = RBTContentJarParameters
						.getInstance()
						.getParameter(
								"is_category_entry_to_be_inserted_in_clip_status_table");
				if (isCategoryToBeInsertedInClipStatusTable != null
						&& isCategoryToBeInsertedInClipStatusTable
								.equalsIgnoreCase("true")) {
					clipUpdated = RBTDBManager.getInstance()
							.checkAndInsertClipWithStatus("cat_" + categoryId,
									circleID, -1);
				}
				Category category = RBTCacheManager.getInstance().getCategory(
						categoryId);
				if (category != null)
					clips = RBTCacheManager.getInstance().getClipsInCategory(
							categoryId);
			}
			// 100 is just a sanity check so that we do not flood clip status
			// table
			if (clips != null
					&& clips.length > 0
					&& clips.length < RBTParametersUtils.getParamAsInt(
							"DAEMON", "MAX_CLIPS_PER_SHUFFLE_CATEGORY", 100)) {
				for (Clip clip : clips)
					clipUpdated = RBTDBManager.getInstance()
							.checkAndInsertClipWithStatus(
									clip.getClipRbtWavFile(), circleID, 0);
			} else {
				clipUpdated = RBTDBManager.getInstance()
						.checkAndInsertClipWithStatus(rbtWavFile, circleID, 0);
			}
		} catch (Exception e) {
			logger.error(
					"Exception adding clips to clip status table. TP might noto play song for rbtWavFile="
							+ rbtWavFile + ", categoryId=" + categoryId, e);
		}
		return clipUpdated;
	}
	
	public boolean checkIfClipExistsAndUpdateMapping(Subscriber sub, String rbtWavFile, int categoryType, int categoryId, Integer operatorId) {
		boolean clipUpdated = false;
		RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
		
		try {
			IClipStatusMappingDAO statusMappingDAO = (IClipStatusMappingDAO) ConfigUtil.getBean(BeanConstant.CLIP_STATUS_MAPPING_DAO);
			if(statusMappingDAO == null)
				throw new OnMobileException("Status Mapping DAO is null, D2C clip transfer is not configured...");

			String catType = Utility.getCategoryType(categoryType);
			if (catType.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)
					|| catType.equalsIgnoreCase(WebServiceConstants.CATEGORY_KARAOKE))
				return false;
			Clip[] clips = null;
			if (Utility.isShuffleCategory(categoryType)) {
				Category category = rbtCacheManager.getCategory(categoryId);
				if (category != null)
					clips = rbtCacheManager.getClipsInCategory(categoryId);
			}
			// 100 is just a sanity check so that we do not flood clip status
			// table
			if (clips != null
					&& clips.length > 0
					&& clips.length < RBTParametersUtils.getParamAsInt(
							"DAEMON", "MAX_CLIPS_PER_SHUFFLE_CATEGORY", 100)) {
				for (Clip clip : clips) {
					checkClipStatusAndSendClip(sub, operatorId, clip, statusMappingDAO);
				}
			} else {
				Clip clip = rbtCacheManager.getClipByRbtWavFileName(rbtWavFile);
				if(clip == null) 
					throw new OnMobileException("Clip is null for rbtWavFile" + rbtWavFile);
				checkClipStatusAndSendClip(sub, operatorId, clip, statusMappingDAO);
			}
		} catch (Throwable t) {
			logger.error(
					"Exception adding clips to clip status mapping table. TP might noto play song for rbtWavFile="
							+ rbtWavFile + ", categoryId=" + categoryId, t);
		}
		return clipUpdated;
	}
	
	private void checkClipStatusAndSendClip(Subscriber sub, Integer operatorId, Clip clip, IClipStatusMappingDAO statusMappingDAO) {
		int clipId = clip.getClipId();
		try {
			
			ClipStatusMapping statusMapping = statusMappingDAO.getClipStatusMappingByOperatorId(operatorId, clipId);
			OperatorCircleMapping circleMapping = ProcessingClipTransfer.getOperatorcirclemapbyid().get(operatorId);
			
			if(statusMapping == null) {
				ClipStatusMapping clipMapping = new ClipStatusMapping();
				CompositeKey key = new CompositeKey();
				key.setOperatorCircleMapping(circleMapping);
				key.setClipId(clipId);
				clipMapping.setCompositeKey(key);
				statusMappingDAO.saveClipStatusMapping(clipMapping);
				statusMapping = clipMapping;
			}
			
			if(statusMapping.getStatus() != 0) {
				if(logger.isDebugEnabled()) {
					logger.debug("Clip " + clipId+ " is already transferred");
				}
				return;
			}
			
			if(!RBTParametersUtils.getParamAsBoolean(iRBTConstant.PROVISIONING, WebServiceConstants.INLINE_PARAMETERS, "false"))
				return;
			
			ThreadExecutor executor = (ThreadExecutor) ConfigUtil.getBean(BeanConstant.THREAD_EXECUTOR);
			ProcessingClipTransfer clipTransferThread = new ProcessingClipTransfer(statusMapping, circleMapping, clip);
//			clipTransferThread.setSubscriber(sub);
//			clipTransferThread.setClipId(String.valueOf(clipId));
			executor.getExecutor().execute(clipTransferThread);
		} catch(Throwable t) {
			logger.error("Error while clip transfer for clipId " + clipId + " operatorId " + operatorId + " continuing with other clips...");
		}
	}
}
