package com.onmobile.android.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.CategoryBean;
import com.onmobile.android.beans.CategoryClipResponseBean;
import com.onmobile.android.beans.ClipInfoActionBean;
import com.onmobile.android.beans.ExtendedClipBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.interfaces.ContentResponse;
import com.onmobile.android.managers.ContentManager;
import com.onmobile.android.utils.CategoryUtils;
import com.onmobile.android.utils.ObjectGsonUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;

public class ContentJSONResponseImpl implements ContentResponse {
	public static Logger logger = Logger
			.getLogger(ContentJSONResponseImpl.class);
	private ContentManager contentManager = new ContentManager();

	public String getActiveCategories(String subscriberId, String language, String browsingLanguage, String appName,Date modifiedSince,int offset,int maxResults) {
		List<CategoryBean> extCategories = contentManager.getActiveCategories(subscriberId, language, browsingLanguage, appName,modifiedSince,offset,maxResults);
		//RBT-14540
		if (modifiedSince != null && (extCategories == null || extCategories.size() == 0)) {
			return null;
		}
		return (ObjectGsonUtils.objectToGson(extCategories));
	}

	@Override
	public String getSubCategories(String subscriberId, String parentCategoryId,String browsingLanguage, String appName,Date modifiedSince) {
		List<CategoryBean> extSubCategories = contentManager.getSubCategories(subscriberId,parentCategoryId, browsingLanguage, appName,modifiedSince);
		//RBT-14540
		if (modifiedSince != null && (extSubCategories == null || extSubCategories.size() == 0)) {
			return null;
		}
		return (ObjectGsonUtils.objectToGson(extSubCategories));
	}

	public String getMainCategory(String subscriberId, String language, String browsingLanguage, String appName){
		String categoryIds = null;
		if (language != null && language.length() != 0 && !language.equalsIgnoreCase("NULL")) {
			logger.info("Language parameter is not null. Using it for retrieving catregoryIds. language: " + language);
			categoryIds = PropertyConfigurator.getMainCategoryId(language);
			logger.info("categoryIds: " + categoryIds);
		}
		if (categoryIds == null) {
			if (PropertyConfigurator.getCircleIdLanguageMap() != null) {
				Subscriber subscriber = RBTClient.getInstance().getSubscriber(new RbtDetailsRequest(subscriberId));
				String circleId = null;
				if (subscriber != null && subscriber.getCircleID() != null) {
					circleId = subscriber.getCircleID();
					logger.debug("circleId obtained from subscriber object and set as: " + circleId);
				} else {
					logger.debug("Default circleId to be obtained.");
					circleId = PropertyConfigurator.getDefaultCircleId();
					if (circleId == null || circleId.length() == 0) {
						circleId ="Default";
						logger.debug("CircleId set as: Default");
					}
				}

				logger.info("Circle Id:" + circleId);
				logger.debug("circleIdLanguage map not null.");
				Map<String, String> circleIdLangMap = PropertyConfigurator.getCircleIdLanguageMap();
				logger.debug("circleIdLangMap: " + circleIdLangMap);
				String circleLang = circleIdLangMap.get(circleId);
				if (circleLang != null) {
					logger.debug("Getting categoryIds for circleLang: " + circleLang + ". The same was configured for the circle: " + circleId);
					categoryIds = PropertyConfigurator.getMainCategoryId(circleLang);
				}
			}
			if (categoryIds == null) {
				logger.debug("CategoryIds null. Retrieving default categoryIds configuration");
				categoryIds = PropertyConfigurator.getMainCategoryId(null);
			}
		}
		logger.info("categoryIds :======>"+categoryIds);

		List<CategoryBean> defaultCatList = new ArrayList<CategoryBean>();
		logger.info("Inside getMainCategory:- categoryIds-->"+categoryIds);
		if(categoryIds != null && categoryIds.contains(",")) {
			String []catIds = categoryIds.split(",");
			logger.info("Inside getMainCategory:- catIds-->"+catIds);
			int catIdLength = catIds.length;
			logger.info("Inside getMainCategory:- catIdLength-->"+catIdLength);
			for(int i=0; i< catIdLength; i++) {
				logger.info("Inside getMainCategory:- catIds[i]-->"+catIds[i]);
				int defaultCatId = Integer.parseInt(catIds[i]);
				logger.info("Inside defaultCatId"+defaultCatId);
				CategoryBean mainCategory = CategoryUtils.getCategoryBean(defaultCatId, browsingLanguage, appName); 
				if(PropertyConfigurator.getTopSongMessage() != null && PropertyConfigurator.getTopSongMessage().length()>0) {
					mainCategory.setTopSongLevel(PropertyConfigurator.getTopSongMessage());
				}
				defaultCatList.add(mainCategory);
			}
		} else if(null != categoryIds && categoryIds.length()>0){
			CategoryBean mainCategory = CategoryUtils.getCategoryBean(Integer.parseInt(categoryIds), browsingLanguage, appName);
			if(PropertyConfigurator.getTopSongMessage() != null && PropertyConfigurator.getTopSongMessage().length()>0) {
				mainCategory.setTopSongLevel(PropertyConfigurator.getTopSongMessage());
			}
			defaultCatList.add(mainCategory);
		}
		return (ObjectGsonUtils.objectToGson(defaultCatList)) ;
	}


	public String getHomeCategory(String subId,String langauge, String browsingLanguage, String appName){
		List<CategoryBean> defaultCatList = contentManager.getHomeCategory(subId,langauge, browsingLanguage, appName);
		return (ObjectGsonUtils.objectToGson(defaultCatList)) ;
	}
	@Override
	public String getProfileCategories(String subscriberId, String browsingLanguage, String appName) {
		String profileParentCat = PropertyConfigurator.getProfileParentCategoryId();
		List<CategoryBean> extSubCategories = contentManager.getSubCategories(subscriberId, profileParentCat, browsingLanguage, appName,null);
		return (ObjectGsonUtils.objectToGson(extSubCategories));
	}

	public String getPromotionalClips(String browsingLanguage, String appName) {
		logger.info("inside getPromotionalClips");
		List<ExtendedClipBean> extendedPromoClips = contentManager.getPromotionalClips(browsingLanguage, appName);
		return (ObjectGsonUtils.objectToGson(extendedPromoClips));
	}

	public String getPromotionalClipsForCategory(String catId, String browsingLanguage, String appName) {
		logger.info("inside getPromotionalClipsForCategory");
		List<ExtendedClipBean> extendedPromoClips = contentManager.getPromotionalClipsForCategory(catId, browsingLanguage, appName);
		return (ObjectGsonUtils.objectToGson(extendedPromoClips));
	}


	public String getClips(int categoryId, int offset, String subscriberId,  boolean BIIndc, String devicetype, String browsingLanguage, String appName, int maxResults) {
		List<ExtendedClipBean> clips = contentManager
				.getExtendedClipsFromCategory(categoryId, offset, subscriberId, BIIndc, devicetype, browsingLanguage, appName, maxResults);
		logger.info("Extended Clips: " + clips);
		String clipsInGson = ObjectGsonUtils.objectToGson(clips);
		return clipsInGson;
	}

	public String getClipInfo(String clipId, String browsingLanguage, String appName) {
		ExtendedClipBean extendedClipBean = contentManager.getExtendedClipByClipId(clipId, browsingLanguage, appName);
		logger.info("extendedClipBean Clips: " + extendedClipBean);
		String clipsInGson = ObjectGsonUtils.objectToGson(extendedClipBean);
		logger.info("extendedClipBean Clips in clipsInGson: " + clipsInGson);
		return clipsInGson;
	}

	public String getNewReleaseClips(int offset, String browsingLanguage, String appName, int maxResults) {
		List<ExtendedClipBean> clips = contentManager.getClipsForNewReleaseCategory(offset, browsingLanguage, appName, maxResults);
		CategoryClipResponseBean responseBean = new CategoryClipResponseBean();
		responseBean.setClip(clips);
		return (ObjectGsonUtils.objectToGson(responseBean));
	}


	public String getPickOfTheDay(String subscriberId, String browsingLanguage, String appName) {
		ExtendedClipBean clip = contentManager.getPickofTheDay(subscriberId, browsingLanguage, appName);
		return (ObjectGsonUtils.objectToGson(clip));
	}

	public String like(Integer clipId) {
		String response = null;
		response = contentManager.likeClip(clipId);
		logger.info("like response "+ response);
		return response;
	}

	public String dislike(Integer clipId) {
		String response = null;
		response = contentManager.dislikeClip(clipId);
		logger.info("dislike response "+ response);
		return response;
	}

	public String User_Rating(int clipId, int rating) {
		String response = null;
		response = contentManager.rateClip(clipId, rating);
		logger.info("rating response "+ response);
		return response;
	}

	public String ClipRatingForSingle(Integer clipId) {
		float rating = 0;
		rating = contentManager.getClipRatingForSingle(clipId);
		return String.valueOf(rating);
	}

	public String ClipRatingsForGroup(List<Integer> clipIds) {
		List<ClipRating> clipratinggrp = new ArrayList<ClipRating>();
		clipratinggrp = contentManager.getClipRatings(clipIds);

		return ObjectGsonUtils.objectToGson(clipratinggrp);
	}

	
	@Override
	public String getPlaylistsCategories(String subscriberId, String browsingLanguage, String appName) {
		List<CategoryBean> playListCategoriesList = contentManager.getPlaylistsCategories(subscriberId, browsingLanguage, appName);
		return (ObjectGsonUtils.objectToGson(playListCategoriesList)) ;
	}

	@Override
	public String getOtherPlaylistsCategory(String subscriberId, String browsingLanguage, String appName) {
		CategoryBean otherPlaylistsCategory = contentManager.getOtherPlaylistsCategory(subscriberId, browsingLanguage, appName);
		return (ObjectGsonUtils.objectToGson(otherPlaylistsCategory)) ;
	}

	
	@Override
	public String getNewReleaseCategories(String subscriberId, int offsetInt, String browsingLanguage, String appName, int maxResults) {
		List<CategoryBean> categories = contentManager.getNewReleaseCategories(subscriberId, offsetInt, browsingLanguage, appName, maxResults);
		CategoryClipResponseBean responseBean = new CategoryClipResponseBean();
		responseBean.setCategory(categories);
		return (ObjectGsonUtils.objectToGson(responseBean));
	}

	@Override
	public String getFreemiumCategory(String subId, String browsingLanguage, String appName) {
		List<CategoryBean> freemiumCategoryList = contentManager.getFreemiumCategory(subId, browsingLanguage, appName);
		return (ObjectGsonUtils.objectToGson(freemiumCategoryList)) ;
	}

	@Override
	public String getFreemiumClips(String subId, String browsingLanguage, String appName, int maxResults) {
		List<ExtendedClipBean> extendedClipList = contentManager.getFreemiumClips(subId, browsingLanguage, appName, maxResults);
		return (ObjectGsonUtils.objectToGson(extendedClipList)) ;
	}
	
	
	public String getClips(ClipInfoActionBean clipInfoActionBean) {
		List<ExtendedClipBean> clips = contentManager
				.getExtendedClipsFromCategory(clipInfoActionBean);
		logger.info("Extended Clips: " + clips);
		String clipsInGson = ObjectGsonUtils.objectToGson(clips);
		return clipsInGson;
	}
}
