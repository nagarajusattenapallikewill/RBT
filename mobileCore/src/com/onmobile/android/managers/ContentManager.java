package com.onmobile.android.managers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.CategoryBean;
import com.onmobile.android.beans.ClipInfoActionBean;
import com.onmobile.android.beans.ExtendedClipBean;
import com.onmobile.android.beans.ExtendedSelectionBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.CategoryUtils;
import com.onmobile.android.utils.ClipUtils;
import com.onmobile.android.utils.StringConstants;
import com.onmobile.android.utils.Utility;
import com.onmobile.android.utils.comparator.SelectionEndTimeComparator;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category.CategoryInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PickOfTheDay;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ContentRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;



public class ContentManager implements StringConstants{


	private static Logger logger = Logger.getLogger(ContentManager.class);

	private static RBTCacheManager cacheManager = RBTCacheManager.getInstance();
	private static RBTClient client = RBTClient.getInstance();

	public List<ExtendedClipBean> getPromotionalClips(String browsingLanguage, String appName){
		logger.info("inside getPromotionalClips");
		String clipIdsStr = PropertyConfigurator.getPromotionalClipIds();
		String[] clipIds = null;
		if(clipIdsStr != null){
			clipIds = clipIdsStr.trim().split(",");
		}
		logger.info("clipIds for promotional clips:" + clipIdsStr);
		List<ExtendedClipBean> promoClips = ClipUtils.getExtendedClipListByClipIds(clipIds, null, browsingLanguage, appName);
		return promoClips;
	}

	public List<ExtendedClipBean> getPromotionalClipsForCategory(String catID, String browsingLanguage, String appName){
		logger.info("inside getPromotionalClips");
		String clipIdsStr = PropertyConfigurator.getPromotionalClipIdsForCategory(catID);
		String[] clipIds = null;
		if(clipIdsStr != null){
			clipIds = clipIdsStr.trim().split(",");
		}
		logger.info("clipIds for home page promo category:" + clipIdsStr);
		List<ExtendedClipBean> promoClips = getHomePageExtendedClipListByClipIds(clipIds, browsingLanguage, appName);
		return promoClips;
	}

	public List<CategoryBean> getActiveCategories(String subscriberId, String language, String browsingLanguage, String appName,Date modifiedSince,int offset,int maxResults){
		String circleId = Utility.getCircleId(subscriberId);
		int parentCatId = PropertyConfigurator.getDefaultParentCategoryId(language);
		logger.info("default parentCatId :======>"+parentCatId);
		if (parentCatId == -1)
		{
			parentCatId = PropertyConfigurator.getDefaultParentCategoryId();
			if (PropertyConfigurator.getCircleIdLanguageMap() != null)
			{
				Map<String, String> circleIdLangMap = PropertyConfigurator.getCircleIdLanguageMap();
				String circleLang = circleIdLangMap.get(circleId);
				if (circleLang != null)
					parentCatId = PropertyConfigurator.getDefaultParentCategoryId(circleLang);
			}
		}
		logger.info("parentCatId :======>"+parentCatId);
		logger.info("circle id for cat :======>"+parentCatId);
		Category[] categories = null;
		if (modifiedSince != null) {
			Set<Category> resultCategories = cacheManager
					.getActiveCategoriesModified(circleId, parentCatId, 'b',
							null, browsingLanguage, appName, modifiedSince,offset,maxResults);
			if (resultCategories != null && !resultCategories.isEmpty()) 
				categories = resultCategories.toArray(new Category[resultCategories.size()]);
		} else {
			categories = cacheManager.getActiveCategoriesInCircle(circleId,
					parentCatId, 'b', null, offset, maxResults, browsingLanguage, appName);
		}
		logger.info("categories result :======>"+categories);
		List<String> blockedCategoryIds = PropertyConfigurator.getBlockedCategories();

		List<CategoryBean> extCategories = new ArrayList<CategoryBean>();
		for(int i=0; categories != null && i < categories.length ; i++){
			if(blockedCategoryIds.contains(categories[i].getCategoryId()+"")) {
				continue;
			}
			CategoryBean extCat = CategoryUtils.getCategoryBean(categories[i], browsingLanguage, appName);
			if (extCat != null) {
				extCategories.add(extCat);
			}
		}
		return extCategories;
	}

	public List<CategoryBean> getSubCategories(String subscriberId, String parentCategoryId, String browsingLanguage, String appName,Date modifiedSince) {
		return getSubCategories(subscriberId, parentCategoryId, browsingLanguage, 0, -1, appName,modifiedSince);
	}

	public List<CategoryBean> getSubCategories(String subscriberId, String parentCategoryId) {
		return getSubCategories(subscriberId, parentCategoryId, null, 0, -1, null,null);
	}

	public List<CategoryBean> getSubCategories(String subscriberId, String parentCategoryId, String browsingLanguage, int offset, int rowCount, String appName,Date modifiedSince) {
		List<CategoryBean> subCategories = new ArrayList<CategoryBean>();
		logger.info("parentCat id "+parentCategoryId+" subscriber id "+subscriberId );
		String circleId = Utility.getCircleId(subscriberId);
		char prepaidYes = 'b';

		Category[] subCategories2 = null;
		if (modifiedSince != null) {
			
			Set<Category> resultCategories = cacheManager
					.getActiveCategoriesModified(circleId,
							Integer.parseInt(parentCategoryId), prepaidYes,
							null, browsingLanguage, appName, modifiedSince);
			if (resultCategories != null && !resultCategories.isEmpty()) 
				subCategories2 = resultCategories.toArray(new Category[resultCategories.size()]);
		} else {
			subCategories2 = cacheManager.getActiveCategoriesInCircle(circleId,
					Integer.parseInt(parentCategoryId), prepaidYes, null,
					offset, rowCount, browsingLanguage, appName);
		}
		List<String> blockedCategoryIds = PropertyConfigurator.getBlockedCategories();
		if (subCategories2 != null && subCategories2.length != 0) {
			for (int i = 0; i < subCategories2.length; i++) {
				if(blockedCategoryIds.contains(subCategories2[i].getCategoryId()+"")) {
					continue;
				}
				CategoryBean category = CategoryUtils.getCategoryBean(subCategories2[i], browsingLanguage, appName);
				if (category != null) {
					subCategories.add(category);
				}
			}
		}
		return subCategories;
	}

	public Clip[] getClipsFromCategory(int categoryId, int offset,
			String subscriberId, boolean BIIndc, String devicetype,
			String browsingLanguage, String appName, int maxResults) {
		if (maxResults == -1 ) {
			maxResults = PropertyConfigurator.getClipRowCount();
			if(BIIndc) {
				maxResults = PropertyConfigurator.getRowCountForBI();
				if(devicetype !=null && devicetype.equalsIgnoreCase("blackberry")) {
					maxResults = PropertyConfigurator.getRowCountForBlackberry();
				}
			}
		}

		Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(categoryId, offset, maxResults, browsingLanguage, subscriberId, null, null, appName);
		return clips;
	}

	public List<ExtendedClipBean> getExtendedClipsFromCategory(int categoryId,
			int offset, String subscriberId, boolean BIIndc, String devicetype,
			String browsingLanguage, String appName, int maxResults) {
		Clip[] clips = getClipsFromCategory(categoryId, offset, subscriberId, BIIndc, devicetype, browsingLanguage, appName, maxResults);
		List<ExtendedClipBean> extClips = new ArrayList<ExtendedClipBean>();
		for(int i=0; clips!=null && i < clips.length ; i++){
			ExtendedClipBean extendedClipBean = ClipUtils.getExtendedClipByClipId(String.valueOf(clips[i].getClipId()), browsingLanguage, appName);
			String result = Utility.getChargeClass(subscriberId, String.valueOf(clips[i].getClipId()), 
					String.valueOf(categoryId));
			if(result != null) {
				extendedClipBean.setPeriod(result.split(":")[0]);
				extendedClipBean.setAmount(result.split(":")[1]);
				extendedClipBean.setRenewalPeriod(result.split(":")[2]);
				extendedClipBean.setRenewalAmount(result.split(":")[3]);
			}
			extClips.add(extendedClipBean);
		}
		return extClips;
	}

	/*public static Category[] getCategoriesInCategory(String circle,int categoryId,char prePaidYes){
		Category[] categories = cacheManager.getActiveCategoriesInCircle(circle, categoryId, prePaidYes);
		return ((categories!=null && categories.length!=0)?categories:null);
	}*/

	/*public static HashMap<String,Clip[]> getCategClipMap(Category[] categories){
		HashMap<String, Clip[]> categoryClipMap = new HashMap<String, Clip[]>();
		for (int i = 0; categories!=null && i < categories.length; i++) {
			Clip[] clips = cacheManager.getClipsInCategory(categories[i].getCategoryId());
			categoryClipMap.put(categories[i].getCategoryId()+"", clips);
		}


		return categoryClipMap;
	}*/


	public String likeClip(Integer clipId) {
		ContentRequest contentRequest = new ContentRequest();
		contentRequest.setClipID(clipId);
		client.likeClip(contentRequest);
		return contentRequest.getResponse();
	}

	public String dislikeClip(Integer clipId) {
		ContentRequest contentRequest = new ContentRequest();
		contentRequest.setClipID(clipId);
		client.dislikeClip(contentRequest);
		return contentRequest.getResponse();
	}

	public String rateClip(int clipId, int rating) {
		ContentRequest contentRequest = new ContentRequest();
		contentRequest.setClipID(clipId);
		contentRequest.setRating(rating);
		client.rateClip(contentRequest);
		return contentRequest.getResponse();
	}

	public float getClipRatingForSingle(Integer clipId) {
		ClipRating clipRating = ClipUtils.getClipRating(clipId);
		float rating = ClipUtils.getClipRating(clipRating.getNoOfVotes(), clipRating.getSumOfRatings());
		return rating;
	}

	public List<ClipRating> getClipRatings(List<Integer> clipIds) {
		ContentRequest contentRequest = new ContentRequest();
		Map<Integer, ClipRating> clipRatingsMap = new HashMap<Integer, ClipRating>();
		List<ClipRating> clipRatings = new ArrayList<ClipRating>(); 
		contentRequest.setClipIDs(clipIds);
		clipRatingsMap = client.getClipRatings(contentRequest);
		if(clipRatingsMap!=null && clipRatingsMap.size()!=0){
			for(Integer clipId : clipIds){
				clipRatings.add(clipRatingsMap.get(clipId));
			}
		}
		logger.info("Returning clipRatings List: "+clipRatings);
		return clipRatings;
	}

	public ExtendedClipBean getExtendedClipByClipId(String clipId, String browsingLanguage, String appName) {
		return ClipUtils.getExtendedClipByClipId(clipId, browsingLanguage, appName);
	}

	public List<ExtendedClipBean> getHomePageExtendedClipListByClipIds(String[] clipIds, String browsingLanguage, String appName) {
		RBTCacheManager rbtCacheManager  = RBTCacheManager.getInstance();
		List<ExtendedClipBean> extendedClipList = new ArrayList<ExtendedClipBean>();
		logger.info("rbtCacheManager"+rbtCacheManager);
		Clip[] clips = rbtCacheManager.getClips(clipIds, browsingLanguage, appName);
		logger.info("Clips array returned: "+clips);
		if(clips!=null ){
			logger.info("Clips length "+clips.length);
			for(int i=0; i<clips.length; i++){
				ExtendedClipBean exClip = new ExtendedClipBean(clips[i], ClipUtils.getClipRating(clips[i].getClipId()));
				String clipImgPath = PropertyConfigurator.getCategoryImagePath(clips[i].getClipId()+"");
				exClip.setImageFilePath(clipImgPath);
				extendedClipList.add(exClip);
			}
		}
		logger.info("extended clip list size:" + extendedClipList.size());
		return extendedClipList;
	}

	public List<ExtendedSelectionBean> getExtendedSelectionBeanList(Setting[] settings, String browsingLangauge, String appName) {
		List<ExtendedSelectionBean> extendedSelectionList = new ArrayList<ExtendedSelectionBean>();
		if(settings != null && settings.length > 0){

			logger.info("Settings length "+settings.length);
			for(int i=0; settings!=null && i < settings.length; i++){
				ExtendedClipBean clip = ClipUtils.getExtendedClipByClipId(String.valueOf(settings[i].getToneID()), browsingLangauge, appName);
				String dateformat =  "yyyyMMddHHmmss";
				if(PropertyConfigurator.getDateFormat() != null && PropertyConfigurator.getDateFormat().length()>0) {
					dateformat = PropertyConfigurator.getDateFormat();
				}
				SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
				Date selectionStartTimeDate = settings[i].getStartTime();
				Date selectionEndTimeDate = settings[i].getEndTime();
				String selectionStartTime = (selectionStartTimeDate!=null)?sdf.format(selectionStartTimeDate):null;
				String selectionEndTime = (selectionEndTimeDate!=null)?sdf.format(selectionEndTimeDate):null;
				int status = settings[i].getStatus();
				String selectionStatus = settings[i].getSelectionStatus();
				String interval = settings[i].getSelInterval();
				String fromTime = String.valueOf(settings[i].getFromTime()) + ":" + String.valueOf(settings[i].getFromTimeMinutes());
				String toTime = String.valueOf(settings[i].getToTime()) + ":" + String.valueOf(settings[i].getToTimeMinutes());
				String selectedBy = settings[i].getSelectedBy();
				String nextChargingDate = null;
				if(null != settings[i].getNextBillingDate()) {
					nextChargingDate = sdf.format(settings[i].getNextBillingDate());
				}


				logger.info("startTime:"+selectionStartTime+" selectionEndTIme:"+selectionEndTime + "nextChargingDate:" +nextChargingDate);
				String callerId = settings[i].getCallerID();
				int categoryId = settings[i].getCategoryID();
				String loopStatus = settings[i].getLoopStatus();
				String ugcRbtFile = settings[i].getUgcRbtFile();
				String inLoop = Utility.getInLoop(loopStatus);
				Category cat = cacheManager.getCategory(categoryId, browsingLangauge, appName);
				String catName = null;
				String categoryTypeString = null;
				if (cat != null) {
					int categoryType = cat.getCategoryTpe();
					categoryTypeString = CategoryUtils.getCategoryTypeStringValue(categoryType);
					if (Utility.isShuffleCategory(cat.getCategoryTpe())) {
						catName = cat.getCategoryName();
						String categoryImagePath = PropertyConfigurator.getCategoryImagePath(String.valueOf(categoryId));
						if (categoryImagePath == null) {
							categoryImagePath = cat.getCategoryInfo(CategoryInfoKeys.IMG_URL);
							if (categoryImagePath == null) {
								categoryImagePath = cat.getCategoryInfo(CategoryInfoKeys.IMG);
							}
						}
						if (categoryImagePath == null) {
							logger.debug("categoryImagePath is null. Fetching from first clip.");
							Clip[] clips = cacheManager.getActiveClipsInCategory(categoryId);
							if (clips != null && clips.length > 0) {
								Clip firstClip = clips[0];
								if (firstClip != null) {
									categoryImagePath = firstClip.getClipInfo(Clip.ClipInfoKeys.IMG_URL);
									logger.debug("categoryImagePath: " + categoryImagePath);

								}
							}
						}
						clip.setImageFilePath(categoryImagePath);
					}
				}

				if(clip!=null){
					ExtendedSelectionBean exSel = new ExtendedSelectionBean(clip, selectionStartTime, selectionEndTime, callerId, status, selectionStatus, categoryId, interval, fromTime, toTime, catName, nextChargingDate);
					exSel.setClipPreviewWavFile(clip.getClipPreviewWavFile());
					exSel.setClipPreviewWavFilePath(clip.getClipPreviewWavFilePath());
					exSel.setSelectedBy(selectedBy);
					exSel.setCategoryType(categoryTypeString);
					exSel.setInLoop(inLoop);
					if (null != ugcRbtFile && ugcRbtFile.endsWith(".3gp")) {
						ugcRbtFile = ugcRbtFile.substring(0,
								ugcRbtFile.indexOf(".3gp"));
					}
					if (null != ugcRbtFile && ugcRbtFile.endsWith(".wav")) {
						ugcRbtFile = ugcRbtFile.substring(0,
								ugcRbtFile.indexOf(".wav"));
					}
					exSel.setUgcRbtFile(ugcRbtFile);
					extendedSelectionList.add(exSel);
				}
			}
		}
		logger.info("extended selectionBean list size:" + extendedSelectionList.size());
		return extendedSelectionList;

	}

	public List<ExtendedSelectionBean> getExtendedProfileSelectionBeanList(Setting[] settings) {
		List<ExtendedSelectionBean> extendedSelectionList = new ArrayList<ExtendedSelectionBean>();
		if(settings != null && settings.length > 0){

			logger.info("Settings length "+settings.length);
			for(int i=0; settings!=null && i < settings.length; i++){
				int status = settings[i].getStatus();
				//Filtering only profile selections
				if(status==99){
					Clip clip = ClipUtils.getExtendedClipByClipId(String.valueOf(settings[i].getToneID()), null, null);
					String dateformat =  "yyyyMMddHHmmss";
					if(PropertyConfigurator.getDateFormat() != null && PropertyConfigurator.getDateFormat().length()>0) {
						dateformat = PropertyConfigurator.getDateFormat();
					}
					SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
					Date selectionStartTimeDate = settings[i].getStartTime();
					Date selectionEndTimeDate = settings[i].getEndTime();
					String selectionStartTime = (selectionStartTimeDate!=null)?sdf.format(selectionStartTimeDate):null;
					String selectionEndTime = (selectionEndTimeDate!=null)?sdf.format(selectionEndTimeDate):null;
					logger.info("startTime:"+selectionStartTime+" selectionEndTIme:"+selectionEndTime);
					String selectionStatus = settings[i].getSelectionStatus();
					int categoryId = settings[i].getCategoryID();
					String callerId = settings[i].getCallerID();
					String interval = settings[i].getSelInterval();
					String fromTime = String.valueOf(settings[i].getFromTime()) + ":" + String.valueOf(settings[i].getFromTimeMinutes());
					String toTime = String.valueOf(settings[i].getToTime()) + ":" + String.valueOf(settings[i].getToTimeMinutes());
					String nextChargingDate = null;
					String selectedBy = settings[i].getSelectedBy();
					if(null != settings[i].getNextBillingDate()) {
						nextChargingDate = sdf.format(settings[i].getNextBillingDate());
					}
					ExtendedClipBean exClip = null;
					if (clip!=null && clip.getClipName() != null){
						exClip = new ExtendedClipBean(clip, ClipUtils.getClipRating(clip.getClipId()));
					} else {
						exClip = new ExtendedClipBean();
						exClip.setClipRbtWavFile(settings[i].getRbtFile());
						exClip.setClipName(settings[i].getToneName());
					}

					ExtendedSelectionBean currentSel = new ExtendedSelectionBean(exClip, selectionStartTime, selectionEndTime, callerId, status, selectionStatus, categoryId, interval, fromTime, toTime, null, nextChargingDate);
					currentSel.setSelectedBy(selectedBy);
					//concatenating callerIds
					if(extendedSelectionList.contains(currentSel)){
						int index = extendedSelectionList.indexOf(currentSel);
						ExtendedSelectionBean targetSelBean = extendedSelectionList.get(index);
						String updatedCallerIds = targetSelBean.getCallerId()+","+currentSel.getCallerId();
						targetSelBean.setCallerId(updatedCallerIds);
					}else{
						extendedSelectionList.add(currentSel);							
					}
				}
			}

			// Sorting is not required if there are 1 or 0 selections.
			if (extendedSelectionList.size() > 1)
			{
				Collections.sort(extendedSelectionList, new SelectionEndTimeComparator());
				Set<String> wavFilesSet = new HashSet<String>();
				Iterator<ExtendedSelectionBean> iter = extendedSelectionList.listIterator();
				while (iter.hasNext())
				{
					ExtendedSelectionBean extendedSelectionBean = iter.next();
					String dateformat =  "yyyyMMddHHmmss";
					if(PropertyConfigurator.getDateFormat() != null && PropertyConfigurator.getDateFormat().length()>0) {
						dateformat = PropertyConfigurator.getDateFormat();
					}
					SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
					String endTimeStr = extendedSelectionBean.getSelectionEndTime();
					Date endDate = null;
					try {
						endDate = sdf.parse(endTimeStr);
					} catch (ParseException e) {
						logger.error(e.getMessage(), e);
					}
					if (!wavFilesSet.add(extendedSelectionBean.getClipRbtWavFile()) && endDate.before(new Date()))
					{
						iter.remove();
					}
				}
			}
		}
		logger.info("extended selectionBean list size:" + extendedSelectionList.size());
		return extendedSelectionList;

	}

	public ArrayList<CategoryBean> getHomeCategory(String subscriberId,String language, String browsingLanguage, String appName) {
		//String defaultCatId = PropertyConfigurator.getHomeCategoryId(language);
		/**/
		String defaultCatId = null;
		if (language != null && language.length() != 0 && !language.equalsIgnoreCase("NULL")) {
			logger.info("Language parameter is not null. Using it for retrieving catregoryIds. language: " + language);
			defaultCatId = PropertyConfigurator.getHomeCategoryId(language);
			logger.info("categoryIds: " + defaultCatId);
		}
		if (defaultCatId == null) {
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
					defaultCatId = PropertyConfigurator.getHomeCategoryId(circleLang);
				}
			}
			if (defaultCatId == null) {
				logger.debug("CategoryIds null. Retrieving default categoryIds configuration");
				defaultCatId = PropertyConfigurator.getHomeCategoryId(null);
			}
		}
		logger.info("categoryIds :======>"+defaultCatId);
		/**/
		logger.info("The home categories are "+defaultCatId);
		String[] defaultIds = defaultCatId.split(",");
		List<String> blockedCategoryIds = PropertyConfigurator.getBlockedCategories();
		ArrayList<CategoryBean> activeCategories = new ArrayList<CategoryBean>();
		for(int i=0;i<defaultIds.length;i++) {
			if(blockedCategoryIds.contains(defaultIds[i])) {
				continue;
			}
			CategoryBean defaultCatBean = CategoryUtils.getCategoryBean(Integer.parseInt(defaultIds[i]), browsingLanguage, appName);
			if (defaultCatBean != null) {
				activeCategories.add(defaultCatBean);
			}
		}
		return activeCategories;
	}

	public List<ExtendedClipBean> getClipsForNewReleaseCategory(int offset, String browsingLanguage, String appName, int maxResults){
		logger.info("Inside getClipsForNewReleaseCategory");
		int newReleaseCatId = Integer.parseInt(PropertyConfigurator.getNewReleaseCategoryId());
		logger.info("New release cat id is " + newReleaseCatId);
		Clip[] clips = getClipsFromCategory(newReleaseCatId, offset, null, false, null, browsingLanguage, appName, maxResults);
		List<ExtendedClipBean> extClips = new ArrayList<ExtendedClipBean>();
		for(int i=0; clips!=null && i < clips.length ; i++){
			extClips.add(ClipUtils.getExtendedClipByClipId(String.valueOf(clips[i].getClipId()), browsingLanguage, appName));
		}
		return extClips;
	}

	public ExtendedClipBean getPickofTheDay(String subscriberId, String browsingLanguage, String appName){
		logger.info("inside getPickofTheDay");
		Subscriber subscriber = client.getSubscriber(new RbtDetailsRequest(subscriberId));
		String circleId="Default";
		if(subscriber!=null){
			circleId = subscriber.getCircleID();
		}
		logger.info("Circle Id:"+circleId);
		ApplicationDetailsRequest apr = new ApplicationDetailsRequest();
		apr.setCircleID(circleId);
		PickOfTheDay[] pick = client.getPickOfTheDays(apr);
		ExtendedClipBean clip = null;
		if(pick != null)
			clip = ClipUtils.getExtendedClipByClipId(pick[0].getClipID()+"", browsingLanguage, appName);
		return clip;
	}

	public PickOfTheDay[] getCataforPickofTheDay(String subscriberId){
		logger.info("inside getCataforPickofTheDay");
		Subscriber subscriber = client.getSubscriber(new RbtDetailsRequest(subscriberId));
		String circleId="Default";
		if(subscriber!=null){
			circleId = subscriber.getCircleID();
		}
		logger.info("Circle Id:"+circleId);
		//PickOfTheDay pickOfTheDayObj = null;
		ApplicationDetailsRequest apr = new ApplicationDetailsRequest();
		apr.setCircleID(circleId);
		PickOfTheDay[] pick = client.getPickOfTheDays(apr);
		logger.info("pick Id:"+pick);
		if(pick != null) {
			logger.info("pick Id len:"+pick.length);
		}
		return pick;
	}

	public List<CategoryBean> getPlaylistsCategories(String subscriberId, String browsingLanguage, String appName) {
		logger.info("subscriberId: " + subscriberId);
		String topPlaylistsCategoryId = PropertyConfigurator.getTopPlaylistsCategoryId();
		List<CategoryBean> playlistCategoriesList = null;

		if (!Utility.isStringValid(topPlaylistsCategoryId)) {
			logger.info("Invalid configuration (empty or null) for topPlaylistsCategoryId: " + topPlaylistsCategoryId);
			return null;
		}

		playlistCategoriesList = getSubCategories(subscriberId, topPlaylistsCategoryId, browsingLanguage, appName,null);
		logger.info("subscriberId: " + subscriberId + ", subCategoruies: " + playlistCategoriesList);

		if (playlistCategoriesList == null || playlistCategoriesList.size() == 0) {
			logger.error("No subcategories found for the categoryId: " + topPlaylistsCategoryId);
			return null;
		}

		logger.info("subscriberId: " + subscriberId + ", playlistCategoriesList: " + playlistCategoriesList);
		return playlistCategoriesList;
	}

	public CategoryBean getOtherPlaylistsCategory(String subscriberId, String browsingLanguage, String appName) {
		logger.info("subscriberId: " + subscriberId);
		String otherPlaylistsCategoryId = PropertyConfigurator.getOtherPlaylistsCategoryId();
		if (!Utility.isStringValid(otherPlaylistsCategoryId)) {
			logger.info("Invalid configuration (empty or null) for playlistCategoryIds: " + otherPlaylistsCategoryId);
			return null;
		}
		CategoryBean playlistCategoryBean = null;
		try {
			playlistCategoryBean = CategoryUtils.getCategoryBean(Integer.parseInt(otherPlaylistsCategoryId), browsingLanguage, appName);
		} catch (NumberFormatException e) {
			logger.error("Invalid otherPlaylistsCategoryId (non-number): " + otherPlaylistsCategoryId + ". " + e, e);
		}
		logger.info("subscriberId: " + subscriberId + ", playlistCategoryBean: " + playlistCategoryBean);
		return playlistCategoryBean;
	}


	public List<CategoryBean> getNewReleaseCategories(String subscriberId, int offsetInt, String browsingLanguage, String appName, int maxResults) {
		logger.debug("Inside getNewReleaseCategories. subscriberId: " + subscriberId + ", offsetInt: " + offsetInt);
		if (maxResults == -1) {
			maxResults = PropertyConfigurator.getCategoryRowCount();
		}
		String newReleaseParentCategoryId = PropertyConfigurator.getNewReleaseCategoryId();
		newReleaseParentCategoryId = newReleaseParentCategoryId.toLowerCase().replaceFirst("c", "");
		logger.info("newReleaseParentCategoryId (after removing the prefix c/C): " + newReleaseParentCategoryId);
		List<CategoryBean> categoryList = getSubCategories(subscriberId, newReleaseParentCategoryId, browsingLanguage, offsetInt, maxResults, appName,null);
		logger.debug("newrelease categoryList: " + categoryList);
		return categoryList;
	}

	public List<CategoryBean> getFreemiumCategory(String subscriberId, String browsingLanguage, String appName) {
		logger.debug("Inside getFreemiumCategory. subscriberId: " + subscriberId);
		String freemiumCategoryIds = PropertyConfigurator.getFreemiumCategoryId();
		if (!Utility.isStringValid(freemiumCategoryIds)) {
			logger.error("Configured freemium parentCategoryId null or empty. Returning null.");
			return null;
		}
		List<CategoryBean> categoryList = getSubCategories(subscriberId, freemiumCategoryIds, browsingLanguage, appName,null);
		logger.debug("freemium categoryList: " + categoryList);
		return categoryList;
	}

	public List<ExtendedClipBean> getFreemiumClips(String subscriberId, String browsingLanguage, String appName, int maxResults) {
		logger.debug("Inside getFreemiumClips. subscriberId: " + subscriberId);
		String freemiumCategoryIdForClips = PropertyConfigurator.getFreemiumCategoryIdForClips();
		if (!Utility.isStringValid(freemiumCategoryIdForClips)) {
			logger.error("Configured freemium Category for clips null or empty. Returning null.");
			return null;
		}
		Clip[] clips = getClipsFromCategory(
				Integer.parseInt(freemiumCategoryIdForClips), 0, subscriberId,
				false, null, browsingLanguage, appName, maxResults);
		List<ExtendedClipBean> freemiumClips = new ArrayList<ExtendedClipBean>();
		for(int i=0; clips!=null && i < clips.length ; i++) {
			freemiumClips.add(ClipUtils.getExtendedClipByClipId(String.valueOf(clips[i].getClipId()), browsingLanguage, appName));
		}
		logger.debug("freemiumClips: " + freemiumClips);
		return freemiumClips;
	}
	
	public List<ExtendedClipBean> getExtendedClipsFromCategory(
			ClipInfoActionBean clipInfoActionBean) {
		Clip[] clips = getClipsFromCategory(clipInfoActionBean);
		List<ExtendedClipBean> extClips = new ArrayList<ExtendedClipBean>();
		for (int i = 0; clips != null && i < clips.length; i++) {
			ExtendedClipBean extendedClipBean = ClipUtils
					.getExtendedClipByClipId(
							String.valueOf(clips[i].getClipId()),
							clipInfoActionBean.getBrowsingLanguage(), clipInfoActionBean.getAppName());
			String result = Utility.getChargeClass(clipInfoActionBean.getSubId(),
					String.valueOf(clips[i].getClipId()),
					String.valueOf(clipInfoActionBean.getCatId()));
			if (result != null) {
				extendedClipBean.setPeriod(result.split(":")[0]);
				extendedClipBean.setAmount(result.split(":")[1]);
				extendedClipBean.setRenewalPeriod(result.split(":")[2]);
				extendedClipBean.setRenewalAmount(result.split(":")[3]);
			}
			extClips.add(extendedClipBean);
		}
		return extClips;
	}
	
	
	public Clip[] getClipsFromCategory(ClipInfoActionBean clipInfoActionBean) {
		int maxResults = clipInfoActionBean.getMaxResults();
		boolean BIIndc = clipInfoActionBean.isBIIndc();
		String devicetype = clipInfoActionBean.getDevicetype();
		if (maxResults == -1) {
			maxResults = PropertyConfigurator.getClipRowCount();
			if (BIIndc) {
				maxResults = PropertyConfigurator.getRowCountForBI();
				if (devicetype != null
						&& devicetype.equalsIgnoreCase("blackberry")) {
					maxResults = PropertyConfigurator
							.getRowCountForBlackberry();
				}
			}
		}

		Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(
				clipInfoActionBean.getCatId(), clipInfoActionBean.getOffset(),
				maxResults, clipInfoActionBean.getBrowsingLanguage(),
				clipInfoActionBean.getSubId(), null,
				clipInfoActionBean.getCircleId(),
				clipInfoActionBean.getAppName(),
				clipInfoActionBean.isUserLanguageSelected(),
				clipInfoActionBean.isSubscribed(),
				clipInfoActionBean.getTotalSize(),
				clipInfoActionBean.getSessionID());
		return clips;
	}
	
}