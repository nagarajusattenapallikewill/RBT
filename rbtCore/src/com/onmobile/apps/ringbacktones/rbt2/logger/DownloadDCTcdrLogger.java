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
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class DownloadDCTcdrLogger extends BasicCDRLogger {
	
	public DownloadDCTcdrLogger(){
		//loggerName = "downloadDCTLogger";
		setLoggerName("downloadDCTLogger");
	}
	
	public void writeCDRLog(LoggerDTO loggerDTO) {
		loggerDTO.setOperatorName(getOperatorName());
		loggerDTO.setCountryName(getCountryName());
		
		FileAppender loggerFileAppender = getLoggerFileAppender();
		Logger.getLogger("downloadDCTLogger").setLevel(Level.ALL);
		Logger.getLogger("downloadDCTLogger").addAppender(loggerFileAppender);
		
		//timestamp,subscriberId,circleId,toineId,tonename,wavfileName,categoryId,categorytype,refId,operatorName,countryname,subscriberstatus,subscriberSrvKey,downloadSrvKey,responseStatus
		Logger.getLogger("downloadDCTLogger").info(loggerDTO.getSubscriberId() +" | " + 
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
	
	@Override
	public LoggerDTO getLoggerDTOForDownloadDCT(LoggerDTO loggerDTO,SubscriberDownloads download, String subcscriberId){
		/*subscriberId,circleId,toineId,tonename,wavfileName,
		 * categoryId,categorytype,refId,operatorName,countryname,
		 * subscriberstatus,subscriberSrvKey,downloadSrvKey,responseStatus*/
		loggerDTO.setDefaultValues();
		Clip clip = null; 
		Category category = null; 
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subcscriberId);
		if(download != null){
			clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(download.promoId());
			category = RBTCacheManager.getInstance().getCategory(download.categoryID()); 
			loggerDTO.setSubscriberId(Long.parseLong(download.subscriberId()));
			loggerDTO.setWavfileName(download.promoId());
			loggerDTO.setCategoryId((long)download.categoryID());
			loggerDTO.setSongSrvKey(download.classType());
			loggerDTO.setRefId(download.refID());
			if (category != null) {
				String categoryType = Utility.getCategoryType(category.getCategoryTpe());
				loggerDTO.setCategorytype(categoryType);
				try {
					if (categoryType
							.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)) {
						IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil
								.getBean(BeanConstant.UGC_WAV_FILE_DAO);
						RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(
								Long.parseLong(download.subscriberId()),
								download.promoId());
						if (ugcWavFile != null) {
							clip = ServiceUtil.getRBTUGCClip(
									ugcWavFile.getUgcId(), "RBTUGC");
						}
					}
				}catch(Exception e){
				}
		    }
		 }
		if(clip != null){
		 loggerDTO.setToneId((long)clip.getClipId());
		 loggerDTO.setTonename(clip.getClipName());
		}
		
		if(subscriber != null){
			loggerDTO.setSubscriberId(Long.parseLong(subscriber.subID()));
			loggerDTO.setCircleId(subscriber.circleID());
			loggerDTO.setSubscriberSrvKey(subscriber.subscriptionClass());
			loggerDTO.setSubscriberStatus(com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus.getSubscriptionStatus(subscriber.subYes()));
		}
		
		return loggerDTO;
	}

}
