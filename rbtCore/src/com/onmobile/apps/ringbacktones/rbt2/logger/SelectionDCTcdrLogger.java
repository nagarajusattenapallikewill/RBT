package com.onmobile.apps.ringbacktones.rbt2.logger;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
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

public class SelectionDCTcdrLogger extends BasicCDRLogger {
	
	public SelectionDCTcdrLogger(){
		//loggerName = "selectionDCTLogger";
		setLoggerName("selectionDCTLogger");
	}
	
	public void writeCDRLog(LoggerDTO loggerDTO) {
		loggerDTO.setOperatorName(getOperatorName());
		loggerDTO.setCountryName(getCountryName());
		
		FileAppender loggerFileAppender = getLoggerFileAppender();
		Logger.getLogger("selectionDCTLogger").setLevel(Level.ALL);
		Logger.getLogger("selectionDCTLogger").addAppender(loggerFileAppender);
		
		//timestamp,subscriberId,circleId,toineId,tonename,wavfileName,categoryId,categorytype,udpId,status,fromtime,totime,callerId,interval,refId,operatorName,countryname,subscriberStatus,subscriberSrvKey,selectionSrvKey,responesStatus
		Logger.getLogger("selectionDCTLogger").info(loggerDTO.getSubscriberId() +" | " + 
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
	

	public LoggerDTO getLoggerDTOForSelectionDCT(LoggerDTO loggerDTO, SubscriberStatus subscriberStatus, String subscriberId) {
		/*subscriberId,circleId,toineId,tonename,
		 * wavfileName,categoryId,categorytype,udpId
		 * status,fromtime,totime,callerId,interval,
		 * refId,operatorName,countryname,subscriberStatus,
		 * subscriberSrvKey,selectionSrvKey,responesStatus*/
		loggerDTO.setDefaultValues();
		Clip clip = null;
		Category category = null;
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId);

		if(subscriberStatus != null){
			clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(subscriberStatus.subscriberFile());
			category = RBTCacheManager.getInstance().getCategory(subscriberStatus.categoryID());
			
			loggerDTO.setSubscriberId(Long.parseLong(subscriberStatus.subID()));
			loggerDTO.setCircleId(subscriberStatus.circleId());
			loggerDTO.setWavfileName(subscriberStatus.subscriberFile());
			loggerDTO.setStatus(subscriberStatus.selStatus());
			loggerDTO.setFromtime(subscriberStatus.fromTime());
			loggerDTO.setTotime(subscriberStatus.toTime());
			loggerDTO.setCallerId(subscriberStatus.callerID());
			loggerDTO.setInterval(subscriberStatus.selInterval());
			loggerDTO.setRefId(subscriberStatus.refID());
			loggerDTO.setSongSrvKey(subscriberStatus.classType());
			loggerDTO.setCategoryId((long)subscriberStatus.categoryID());

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

		if(clip != null){
			loggerDTO.setToneId((long)clip.getClipId());
			loggerDTO.setTonename(clip.getClipName());
		}

		if(subscriber != null){
			loggerDTO.setSubscriberSrvKey(subscriber.subscriptionClass());
			loggerDTO.setSubscriberStatus(com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus.getSubscriptionStatus(subscriber.subYes()));
			loggerDTO.setSubscriberId(Long.parseLong(subscriberId));
			loggerDTO.setCircleId(subscriber.circleID());
		}

		return loggerDTO;
	}

}
