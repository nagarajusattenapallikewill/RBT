package com.onmobile.apps.ringbacktones.rbtcontents.bi.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfoAction;
import com.onmobile.apps.ringbacktones.rbtcontents.bi.BIInterface;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.RBTContentUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class BIPersonalizationServerImpl implements BIInterface{

	private static Logger logger = Logger.getLogger(BIPersonalizationServerImpl.class);
	private static Logger biLogger = Logger.getLogger("BiLogger");
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz ");
	private static final String url = RBTContentJarParameters.getInstance().getParameter("BI_PURCHASE_URL");
	private static String userName = RBTContentJarParameters.getInstance().getParameter("BI_USERNAME");
	private static String password = RBTContentJarParameters.getInstance().getParameter("PASSWORD");
	private static String defaultCategoryId = RBTContentJarParameters.getInstance().getParameter("BI_DEFAULT_CATEGORY_ID");
	
	@Override
	public Object[] process(Category category, String subscriberId, String circleId, boolean doReturnActiveClips, String language, String appName,  boolean isFromCategory, ClipInfoAction clipInfoAction){
		// TODO Auto-generated method stub
		Clip[] clips =  null;
		Integer[] clipIdsOrCatIDs = null;
		int length = 0;
		JSONObject jsonObject = null;
		JSONArray jsonArr = null;
		JSONObject tempJsonObj = null;
		Date now = new Date();
		int parentCatId = category.getCategoryId();
		String response = RBTContentUtils.getBIResponse(parentCatId, category.getCategoryTpe(), subscriberId, isFromCategory);
		if(response != null && !response.equalsIgnoreCase("URL_FAILURE")) {
			
			try {
				jsonObject = new JSONObject(response);
				jsonArr = jsonObject.getJSONArray("entry");
				length = jsonArr.length();
				clipIdsOrCatIDs = new Integer[length];
				for(int i = 0; i < length; i++) {
					tempJsonObj = jsonArr.getJSONObject(i);
					clipIdsOrCatIDs[i] = new Integer(tempJsonObj.getString("id"));
				}
			} catch (JSONException e) {
				logger.error("Exception ", e);
			}
			
			
			List<Clip> clipList = new ArrayList<Clip>();
			ArrayList<Category> subCategoryList = new ArrayList<Category>();
			
			if(clipIdsOrCatIDs != null) {
					// if category browsing checking flag and returning sub categories of parent catId
				logger.info("is from category flow :" +isFromCategory);
				logger.info("is from active category flow :" +doReturnActiveClips);
				if(isFromCategory) {
					for(int i=0; i < clipIdsOrCatIDs.length; i++) {
						int cetegoryObj = clipIdsOrCatIDs[i];
						Category subCategories = RBTCacheManager.getInstance().getCategory(cetegoryObj, language);
						logger.info("list of subcategories :" + subCategories);
						if(subCategories == null ) {
							continue;
						}
						if(doReturnActiveClips && (!subCategories.getCategoryStartTime().before(now) || !subCategories.getCategoryEndTime().after(now))) {
							continue;
						}
						subCategoryList.add(subCategories);
						return (Category[]) subCategoryList.toArray(new Category[0]);
					}
				}
				else {
					for(int i=0; i < clipIdsOrCatIDs.length; i++) {
						Clip clip = RBTCacheManager.getInstance().getClip(clipIdsOrCatIDs[i]);
						if(clip == null) {
							continue;
						}
						if(doReturnActiveClips && (!clip.getClipStartTime().before(now) || !clip.getClipEndTime().after(now))) {
							continue;
						}
						clipList.add(clip);
						clips =  clipList.toArray(new Clip[0]);
						if(clips == null || clips.length == 0) {
							int idefaultCategoryId = 0;
							try {
								idefaultCategoryId = Integer.parseInt(defaultCategoryId); 
							}
							catch(Exception e) {}
							if(doReturnActiveClips) {
								clips = RBTCacheManager.getInstance().getActiveClipsInCategory(idefaultCategoryId, language, appName);
							}
							else {
								clips = RBTCacheManager.getInstance().getClipsInCategory(idefaultCategoryId, language); //Only for getActiveClips appName is handled
							}
						}
						
						return clips;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean processHitBIForPurchase(String subscriberId, String refId, String mode, String toneId ) {		
		
		if(url == null)
			return false;
		String tempUrl = url;
		
		tempUrl = tempUrl.replaceAll("<idelitem>", toneId);
		
		Map<String, String> requestParams = new HashMap<String, String>();
		String jsonString = null;
		try {
		
			JSONObject json = new JSONObject();
			json.put("userId", subscriberId);
			json.put("timestamp", sdf.format(new Date()));
			json.put("channel", mode);
			json.put("eventId", refId);	
			jsonString = json.toString();
		}
		catch(JSONException e) {
			logger.error("Exception while making json string ", e);
			return false;
		}
		requestParams.put("BI_POST", jsonString);

		HttpParameters httpParam = new HttpParameters();
		httpParam.setUrl(tempUrl);
		httpParam.setConnectionTimeout(6000);
		
		httpParam.setUsernamePasswordCredentials(new UsernamePasswordCredentials(userName, password));
		
		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParam, requestParams, null);
			String status = "SUCCESS";
			if(httpResponse.getResponseCode() != 200) {
				status = "ERROR";
			}
			biLogger.info(new Date() + "," + subscriberId + "," + tempUrl + "," + jsonString + "," + httpResponse.getResponseCode() + "," + httpResponse.getResponseTime() + "," + httpResponse.getResponse());
			return status.equals("SUCCESS");
		}
		catch(Exception e) {
			logger.error("Exception while hitting url: " +  tempUrl, e);			
		}
		return false;
	
	}
}
