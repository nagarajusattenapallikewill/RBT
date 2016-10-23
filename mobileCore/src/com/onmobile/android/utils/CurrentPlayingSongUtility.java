package com.onmobile.android.utils;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.CategoryBean;
import com.onmobile.android.beans.ExtendedClipBean;
import com.onmobile.android.beans.GetCurrentPlayingSongBean;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;



/**
 * @author koyel.mahata
 *
 */
public class CurrentPlayingSongUtility
{

	private static Logger logger = Logger.getLogger(CurrentPlayingSongUtility.class);
	public static GetCurrentPlayingSongBean getDummyCurrentPlayingResponse(String wavFileName,int categoryId){
		logger.info("getDummyCurrentPlayingResponse");
		GetCurrentPlayingSongBean currentPlaylingSong = new GetCurrentPlayingSongBean();
		
		ExtendedClipBean extendedClip = ClipUtils.getDummyClipByResponseString("-1", wavFileName, "-1", "");
		CategoryBean catBean = CategoryUtils.getDummyCategory(categoryId);
		
		currentPlaylingSong.setClip(extendedClip);
		currentPlaylingSong.setCategory(catBean);
		
		return currentPlaylingSong; 
	}
	
	
	public static GetCurrentPlayingSongBean getCurrentPlayingSongResponse(String wavFileName,String  browsingLanguage, String appName, int categoryId){
		logger.info("getCurrentPlayingSongResponse");
		GetCurrentPlayingSongBean currentPlaylingSong = new GetCurrentPlayingSongBean();
		
		Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFileName);
		
		ExtendedClipBean extendedClip = ClipUtils.getExtendedClipByClipId(String.valueOf(clip.getClipId()),  browsingLanguage, appName);
		CategoryBean catBean = null;
		if(categoryId != -1){
		   catBean = CategoryUtils.getCategoryBean(categoryId, browsingLanguage,appName);
		}else{
			catBean = CategoryUtils.getDummyCategory(categoryId);
		}
		
		currentPlaylingSong.setClip(extendedClip);
		currentPlaylingSong.setCategory(catBean);
		logger.info("currentPlaylingSong: " + currentPlaylingSong);
		return currentPlaylingSong; 
	}
	
	public static ExtendedClipBean getCurrentPlayingSongResponse(
			String wavFileName, String browsingLanguage, String appName) {
		logger.info("getCurrentPlayingSongResponse");
		Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(
				wavFileName);
		ExtendedClipBean extendedClip = new ExtendedClipBean();
		if (clip != null) {
			extendedClip = ClipUtils
					.getExtendedClipByClipId(String.valueOf(clip.getClipId()),
							browsingLanguage, appName);
			logger.info("currentPlaylingSong: " + extendedClip);
			return extendedClip;
		}
		return extendedClip;
	}
}


