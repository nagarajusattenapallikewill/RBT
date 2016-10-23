package com.onmobile.apps.ringbacktones.rbt2.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IUDPDao;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class NonRelatedContentDeactivateCommand extends FeatureListRestrictionCommand implements iRBTConstant{

	private static Logger logger = Logger.getLogger(NonRelatedContentDeactivateCommand.class);
	@Override
	public void executeCalback(String msisdn) {

		
		//Get Subscriber object
		//Get cos detils from subscriber cos id
		//Validate download content type validation
		//if content type is not matching, then download to get deactive
		//once download successfully deactivation, then respective song selection to get deactive
		//if same content is present in UDP map table, then to remove the content from UDP map table
		
		logger.info("NonRelatedContentDeactivateCommand execute begins for subscriber: " + msisdn);
		
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
		
		CosDetails cosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.cosID());
		String contentTypes = cosDetail != null ? cosDetail.getContentTypes() : null;
		
		if(contentTypes != null ) {
			
			List<String> contentTypeList = Arrays.asList(contentTypes.split(",")); 
			
			SubscriberDownloads[] subscriberDownloads = RBTDBManager.getInstance().getActiveSubscriberDownloads(msisdn);
			
			if(subscriberDownloads != null && subscriberDownloads.length != 0) {
				Map<String, String> whereClauseMap = new HashMap<String, String>();
				for(SubscriberDownloads subscriberDownload : subscriberDownloads) {
					String wavFile = subscriberDownload.promoId();
					Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFile);
					if(clip == null || contentTypeList.contains(clip.getContentType())) {
						continue;
					}
					
					whereClauseMap.put("SUBSCRIBER_WAV_FILE", wavFile);
					
					SubscriberStatus subscriberSelection = RBTDBManager.getInstance().getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(msisdn, whereClauseMap);
					
					RBTDBManager.getInstance().smUpdateDownloadRenewalCallback(msisdn, wavFile, subscriberDownload.refID(), "FAILURE", false, null, subscriberDownload.classType(), "DAEMON");
					logger.info("Successfully download and selections deactivated for subscriber: " + msisdn + ", wavFileName: " + clip.getClipRbtWavFile());
					
					if(subscriberSelection != null && subscriberSelection.udpId() != null) {
						IUDPDao udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
						try {
							udpDao.removeContentUDP(Integer.parseInt(subscriberSelection.udpId()), clip.getClipId());
							logger.info("Successfully content remove from UDP subscriber: " + msisdn + ", udpId: " + subscriberSelection.udpId() + ", wavFileName: " + clip.getClipRbtWavFile());
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (DataAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					
				}
				whereClauseMap = null;
			}
		}
		
		logger.info("NonRelatedContentDeactivateCommand execute ends for subscriber: " + msisdn);
		
	}
	@Override
	public String executeInlineCall(SelectionRequest selectionRequest, String clipID) {
		return null;
	}

}
