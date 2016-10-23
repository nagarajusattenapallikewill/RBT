package com.onmobile.apps.ringbacktones.rbt2.logger;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.logger.dto.LoggerDTO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class DownloadACTcdrLogger extends BasicCDRLogger {
	
	
	public DownloadACTcdrLogger(){
		//loggerName = "downloadACTLogger";
		setLoggerName("downloadACTLogger");
	}
	

	public void writeCDRLog(LoggerDTO loggerDTO) {
		loggerDTO.setOperatorName(getOperatorName());
		loggerDTO.setCountryName(getCountryName());
		
		FileAppender loggerFileAppender = getLoggerFileAppender();
		Logger.getLogger("downloadACTLogger").setLevel(Level.ALL);
		Logger.getLogger("downloadACTLogger").addAppender(loggerFileAppender);
		
		//timestamp,subscriberId,circleId,toineId,tonename,wavfileName,categoryId,categorytype,udpId,refId,operatorName,countryname,subscriberstatus,subscriberSrvKey,downloadSrvKey,responseStatus
		Logger.getLogger("downloadACTLogger").info(loggerDTO.getSubscriberId() +" | " + 
				    loggerDTO.getCircleId() +" | " + 
				    loggerDTO.getToneId() +" | " +
				    loggerDTO.getTonename() +" | " +
				    loggerDTO.getWavfileName() +" | " +
				    loggerDTO.getCategoryId()+" | " +
				    loggerDTO.getCategorytype()+" | " +
				    loggerDTO.getRefId()+" | " +
				    loggerDTO.getOperatorName()+" | " +
				    loggerDTO.getCountryName()+" | " +
				    loggerDTO.getSubscriberStatus()+" | " +
				    loggerDTO.getSubscriberSrvKey()+" | " +
				    loggerDTO.getSongSrvKey()+" | " +
				    loggerDTO.getResponesStatus());
	}
	
	public LoggerDTO getLoggerDTOForDownloadACTSuccess(LoggerDTO loggerDTO,SubscriberDownloads download){
		/*subscriberId,circleId,toineId,tonename,wavfileName,
		 * categoryId,categorytype,refId,operatorName,countryname,
		 * subscriberstatus,subscriberSrvKey,downloadSrvKey,responseStatus*/
		loggerDTO.setDefaultValues();
		Clip clip = null;
		Category category = null;
		Subscriber subscriber = null;
		if(download != null){
			String wavFile = download.promoId();
			if(wavFile != null && wavFile.endsWith(".wav"))
				wavFile = wavFile.substring(0, wavFile.lastIndexOf(".wav"));
			clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFile);
			category = RBTCacheManager.getInstance().getCategory(download.categoryID());
			subscriber = RBTDBManager.getInstance().getSubscriber(download.subscriberId());
			loggerDTO.setSubscriberId(Long.parseLong(download.subscriberId()));
			loggerDTO.setWavfileName(download.promoId());
			loggerDTO.setRefId(download.refID());
			loggerDTO.setSongSrvKey(download.classType());
			loggerDTO.setCategoryId((long)download.categoryID());
			
			if(clip != null){
				 loggerDTO.setToneId((long)clip.getClipId());
				 loggerDTO.setTonename(clip.getClipName());
			}
			
			if (category != null) {
				String categoryType = Utility.getCategoryType(category.getCategoryTpe());
				loggerDTO.setCategorytype(categoryType);
				try {
					if (categoryType
							.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)) {
						
						String ugcRbtFile = download.promoId();
						if(ugcRbtFile != null && ugcRbtFile.contains(".wav")){
							ugcRbtFile = ugcRbtFile.replace(".wav", "");
						}
						IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil
								.getBean(BeanConstant.UGC_WAV_FILE_DAO);
						RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(
								Long.parseLong(download.subscriberId()),ugcRbtFile);
						if (ugcWavFile != null) {
							clip = ServiceUtil.getRBTUGCClip(
									ugcWavFile.getUgcId(), "RBTUGC");
							if(clip != null){
							 loggerDTO.setToneId((long)clip.getClipId());
							 loggerDTO.setWavfileName(clip.getClipRbtWavFile());
							}
						}
					}
				}catch(Exception e){
				}
		    }
		}
		
		if(subscriber != null){
			loggerDTO.setCircleId(subscriber.circleID());
			loggerDTO.setSubscriberSrvKey(subscriber.subscriptionClass());
			loggerDTO.setSubscriberStatus(com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus.getSubscriptionStatus(subscriber.subYes()));
		}
		return loggerDTO;
	}
	
	public LoggerDTO getLoggerDTOForDownloadACTFaliure(LoggerDTO loggerDTO,
			SelectionRequest selectionRequest, String type) {
		/*
		 * subscriberId,circleId,toineId,tonename,wavfileName,
		 * categoryId,categorytype,refId,operatorName,countryname,
		 * subscriberstatus,subscriberSrvKey,downloadSrvKey,responseStatus
		 */
		loggerDTO.setDefaultValues();

		Clip clip = null;
		Category category = null;

		if (!(type.equalsIgnoreCase("SONG") && type.equalsIgnoreCase("RBTUGC"))
				&& selectionRequest.getCategoryID() != null) {
			category = RBTCacheManager.getInstance().getCategory(
					Integer.parseInt(selectionRequest.getCategoryID()));
		}

		if (type.equalsIgnoreCase("SONG")) {
			clip = RBTCacheManager.getInstance().getClip(selectionRequest.getClipID());
		}else if(type.equalsIgnoreCase("RBTUGC")){
			try{
			   clip = ServiceUtil.getRBTUGCClip(Long.parseLong(selectionRequest.getClipID()), "RBTUGC");
			}catch(Exception e){}
		}

		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(
				selectionRequest.getSubscriberID());

		loggerDTO.setSubscriberId(Long.parseLong(selectionRequest
				.getSubscriberID()));
		
		if (clip != null) {
			loggerDTO.setToneId((long) clip.getClipId());
			loggerDTO.setTonename(clip.getClipName());
			loggerDTO.setWavfileName(clip.getClipRbtWavFile());
		}

		if (selectionRequest.getRbtFile() != null) {
			loggerDTO.setWavfileName(selectionRequest.getRbtFile());
		}

		if (category != null) {
			loggerDTO.setCategoryId((long) category.getCategoryId());
			loggerDTO.setCategorytype(Utility.getCategoryType(category
					.getCategoryTpe()));
		}

		if (subscriber != null) {
			loggerDTO.setCircleId(subscriber.circleID());
			loggerDTO.setSubscriberSrvKey(subscriber.subscriptionClass());
			loggerDTO
					.setSubscriberStatus(com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus
							.getSubscriptionStatus(subscriber.subYes()));
		}
		return loggerDTO;
	}


}
