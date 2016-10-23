package com.onmobile.apps.ringbacktones.rbt2.logger;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.builder.impl.UGCAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.logger.dto.LoggerDTO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class SelectionACTcdrLogger extends BasicCDRLogger {
	
	public SelectionACTcdrLogger(){
		//loggerName = "selectionACTLogger";
		setLoggerName("selectionACTLogger");
	}
	
	public void writeCDRLog(LoggerDTO loggerDTO) {
		loggerDTO.setOperatorName(getOperatorName());
		loggerDTO.setCountryName(getCountryName());
		
		FileAppender loggerFileAppender = getLoggerFileAppender();
		Logger.getLogger("selectionACTLogger").setLevel(Level.ALL);
		Logger.getLogger("selectionACTLogger").addAppender(loggerFileAppender);
		
		//timestamp,subscriberId,circleId,toineId,tonename,wavfileName,categoryId,categorytype,udpId,status,fromtime,totime,callerId,interval,refId,operatorName,countryname,subscriberStatus,subscriberSrvKey,selectionSrvKey,responesStatus
		Logger.getLogger("selectionACTLogger").info(loggerDTO.getSubscriberId() +" | " + 
				    loggerDTO.getCircleId() +" | " + 
				    loggerDTO.getToneId() +" | " +
				    loggerDTO.getTonename() +" | " +
				    loggerDTO.getWavfileName() +" | " +
				    loggerDTO.getCategoryId()+" | " +
				    loggerDTO.getCategorytype()+" | " +
				    loggerDTO.getUdpId()+" | " +
				    loggerDTO.getStatus()+" | " +
				    loggerDTO.getFromtime()+" | " +
				    loggerDTO.getTotime()+" | " +
				    loggerDTO.getCallerId()+" | " +
				    loggerDTO.getInterval()+" | " +
				    loggerDTO.getRefId()+" | " +
				    loggerDTO.getOperatorName()+" | " +
				    loggerDTO.getCountryName()+" | " +
				    loggerDTO.getSubscriberStatus()+" | " +
				    loggerDTO.getSubscriberSrvKey()+" | " +
				    loggerDTO.getSongSrvKey()+" | " +
				    loggerDTO.getResponesStatus());
	}
	
	
	public LoggerDTO getLoggerDTOForSelACTSuccess(LoggerDTO loggerDTO,
			SubscriberStatus subscriberStatus) {
		/*
		 * subscriberId,circleId,toineId,tonename,
		 * wavfileName,categoryId,categorytype,udpId
		 * status,fromtime,totime,callerId,interval,
		 * refId,operatorName,countryname,subscriberStatus,
		 * subscriberSrvKey,selectionSrvKey,responesStatus
		 */
		loggerDTO.setDefaultValues();

		Clip clip = null;
		Category category = null;
		Subscriber subscriber = null;

		if (subscriberStatus != null) {
			clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(
						subscriberStatus.subscriberFile());
			category = RBTCacheManager.getInstance().getCategory(
						subscriberStatus.categoryID());
			subscriber = RBTDBManager.getInstance().getSubscriber(
					subscriberStatus.subID());
			loggerDTO.setSubscriberId(Long.parseLong(subscriberStatus.subID()));
			loggerDTO.setCircleId(subscriberStatus.circleId());
			loggerDTO.setWavfileName(subscriberStatus.subscriberFile());
			loggerDTO.setCategoryId((long) subscriberStatus.categoryID());
			loggerDTO.setUdpId(subscriberStatus.udpId());
			loggerDTO.setStatus(subscriberStatus.selStatus());
			loggerDTO.setFromtime(subscriberStatus.fromTime());
			loggerDTO.setTotime(subscriberStatus.toTime());
			loggerDTO.setCallerId(subscriberStatus.callerID());
			loggerDTO.setInterval(subscriberStatus.selInterval());
			loggerDTO.setRefId(subscriberStatus.refID());
			loggerDTO.setSongSrvKey(subscriberStatus.classType());
			
			if (category != null) {
				String categoryType = Utility.getCategoryType(category.getCategoryTpe());
				loggerDTO.setCategorytype(categoryType);
				try {
					if (categoryType
							.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)) {
						IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil
								.getBean(BeanConstant.UGC_WAV_FILE_DAO);
						RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(
								Long.parseLong(subscriberStatus.subID()),
								subscriberStatus.subscriberFile());
						if (ugcWavFile != null) {
							clip = ServiceUtil.getRBTUGCClip(
									ugcWavFile.getUgcId(), "RBTUGC");
						}
					}
				}catch(Exception e){
				}
			}
		}

		if (clip != null) {
			loggerDTO.setToneId((long) clip.getClipId());
			loggerDTO.setTonename(clip.getClipName());
		}
		
		if (subscriber != null) {
			loggerDTO.setSubscriberSrvKey(subscriber.subscriptionClass());
			loggerDTO
					.setSubscriberStatus(com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus
							.getSubscriptionStatus(subscriber.subYes()));
		}

		return loggerDTO;
	}
	
	public LoggerDTO getLoggerDTOForSelACTFailure(LoggerDTO loggerDTO,String subscriberID, SelectionRequest selectionRequest, AssetType asseType){
		/*subscriberId,circleId,toineId,tonename,
		 * wavfileName,categoryId,categorytype,udpId
		 * status,fromtime,totime,callerId,interval,
		 * refId,operatorName,countryname,subscriberStatus,
		 * subscriberSrvKey,selectionSrvKey,responesStatus*/
		loggerDTO.setDefaultValues();
		Category category = null;
		Clip clip =  null;
		
		if(!asseType.equals(AssetType.SHUFFLELIST)){
			category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(selectionRequest.getCategoryID()));
		}
		
		if(asseType.equals(AssetType.SONG)){
			clip = RBTCacheManager.getInstance().getClip(selectionRequest.getClipID());
		}else if(asseType.equals(AssetType.RBTUGC)){
			try {
					IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil
							.getBean(BeanConstant.UGC_WAV_FILE_DAO);
					RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(
							Long.parseLong(subscriberID),
							selectionRequest.getClipID());
					if (ugcWavFile != null) {
						clip = ServiceUtil.getRBTUGCClip(
								ugcWavFile.getUgcId(), "RBTUGC");
					}
			}catch(Exception e){}
	     }
		
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		loggerDTO.setSubscriberId(Long.parseLong(subscriberID));
		if(clip != null){
			loggerDTO.setToneId((long)clip.getClipId());
			loggerDTO.setTonename(clip.getClipName());
			loggerDTO.setWavfileName(clip.getClipRbtWavFile());
		}
		
		if(category !=null){
			loggerDTO.setCategoryId(Long.parseLong(selectionRequest.getCategoryID()));
			loggerDTO.setCategorytype(Utility.getCategoryType(category.getCategoryTpe()));
		}
		
		if(selectionRequest.getUdpId() != null){
			loggerDTO.setUdpId(selectionRequest.getUdpId());
		}
		
		if(selectionRequest.getFromTime() != null){
			loggerDTO.setFromtime(selectionRequest.getFromTime());
		}
		if(selectionRequest.getToTime() != null){
			loggerDTO.setTotime(selectionRequest.getToTime());
		}
		if(selectionRequest.getCallerID() != null){
			loggerDTO.setCallerId(selectionRequest.getCallerID());
		}
		
		if(subscriber != null){
			loggerDTO.setSubscriberSrvKey(subscriber.subscriptionClass());
			loggerDTO.setSubscriberStatus(com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus.getSubscriptionStatus(subscriber.subYes()));
			loggerDTO.setCircleId(subscriber.circleID());
			loggerDTO.setSubscriberId(Long.parseLong(subscriberID));
		}
		return loggerDTO;
	}
	

}
