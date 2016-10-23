package com.onmobile.android.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.ExtendedClipBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ContentRequest;

public class ClipUtils {
	private static Logger logger = Logger.getLogger(ClipUtils.class);
	
	public static ClipRating getClipRating(Integer clipId){
		ContentRequest contentRequest = new ContentRequest();
		contentRequest.setClipID(clipId);
		ClipRating clipRating = RBTClient.getInstance().getClipRating(contentRequest);
		logger.info("Returning clipRating:"+clipRating);
		return clipRating;
	}
	
	public static float getClipRating(int noOfVotes, int sumOfRatings){
		float rating = 0.0F;
		if(noOfVotes!=0){
			rating = Math.round(2 * ((float)sumOfRatings / (float)noOfVotes)) / 2.0F;
		}
		return rating;
	}
	
	public static List<ExtendedClipBean> getExtendedClipListByClipIds(String[] clipIds, String subId, String browsingLanguage, String appName) {
		RBTCacheManager rbtCacheManager  = RBTCacheManager.getInstance();
		List<ExtendedClipBean> extendedClipList = new ArrayList<ExtendedClipBean>();
		logger.info("rbtCacheManager"+rbtCacheManager);
		Clip[] clips = rbtCacheManager.getClips(clipIds, browsingLanguage, appName);
		logger.info("Clips array returned: "+clips);
		if(clips!=null ){
			logger.info("Clips length "+clips.length);
			for(int i=0; i<clips.length; i++){
				ExtendedClipBean exClip = new ExtendedClipBean(clips[i], getClipRating(clips[i].getClipId()));
				String result = Utility.getChargeClass(subId, String.valueOf(clips[i].getClipId()),
						PropertyConfigurator.getCatIdForChargeClass());
				if(result != null) {
					exClip.setPeriod(result.split(":")[0]);
					exClip.setAmount(result.split(":")[1]);
					exClip.setRenewalPeriod(result.split(":")[2]);
					exClip.setRenewalAmount(result.split(":")[3]);
				}
				extendedClipList.add(exClip);
			}
		}
		logger.info("extended clip list size:" + extendedClipList.size());
		return extendedClipList;
	}
	
	public static ExtendedClipBean getExtendedClipByClipId(String clipId, String browsingLanguage, String appName) {
		RBTCacheManager rbtCacheManager  = RBTCacheManager.getInstance();
		ExtendedClipBean extendedClip = new ExtendedClipBean();
		logger.info("rbtCacheManager"+rbtCacheManager);
		Clip clipDetails = rbtCacheManager.getClip(clipId, browsingLanguage, appName);
		logger.info("Clip Details "+clipDetails);
		if(clipDetails!=null ){
			extendedClip = new ExtendedClipBean(clipDetails, ClipUtils.getClipRating(Integer.parseInt(clipId)));
			extendedClip.setClipPreviewWavFile(clipDetails.getClipPreviewWavFile());
			extendedClip.setClipPreviewWavFilePath(clipDetails.getClipPreviewWavFilePath());
		}
		logger.info("extendedClip Details "+extendedClip);
		return extendedClip;
	}
	
	public static ExtendedClipBean getDummyClipByResponseString(String clipId,String responseString,String clipPromoId, String EndDate ){
		ExtendedClipBean extendedClip = new ExtendedClipBean();
		
		extendedClip.setClipId(Integer.parseInt(clipId));
		extendedClip.setClipName(responseString);
		extendedClip.setClipNameWavFile(responseString);
		extendedClip.setClipPreviewWavFile(responseString);
		extendedClip.setClipPromoId(clipPromoId);
		extendedClip.setClipStartTime(new Timestamp(new Date().getTime()));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp endDate = null;
		try {
			endDate = new Timestamp(dateFormat.parse("2037-12-31 00:00:00").getTime());
		} catch (ParseException e) {
			logger.error(e,e);
		}
		extendedClip.setClipEndTime(endDate);
		return extendedClip; 
	}
}
