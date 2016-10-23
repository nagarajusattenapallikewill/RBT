package com.onmobile.android.interfaces;

import java.util.Date;
import java.util.List;

import com.onmobile.android.beans.ClipInfoActionBean;
import com.onmobile.android.exceptions.OMAndroidException;

public interface ContentResponse {
	
	
	/**
	 * 
	 * @return String JSON String
	 * @throws OMAndroidException
	 * The return String if JSON impl used should be casted to List<com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip>
	 * The return String if XML impl used should be parsed to com.onmobile.android.jaxb.clips object using JAXB parser
	 */
	public String getPromotionalClips(String browsingLanguage, String appName);
	public String getPromotionalClipsForCategory(String catId, String browsingLanguage, String appName);
	
	/**
	 * Get all the active parent categories to be shown. 
	 * @param subscriberId
	 * @param language
	 * @param browsingLanguage 
	 * @param appName 
	 * @return String JSON String or XML
	 */
	public String getActiveCategories(String subscriberId, String language, String browsingLanguage, String appName,Date modifiedSince,int offset ,int maxResults);	
	
	/**
	 * Get all the clips inside a category. 
	 * @param categoryId
	 * @param offset
	 * @param subscriberId
	 * @param BIIndc
	 * @param appName 
	 * @param maxResults 
	 * @return JSON String or XML 
	 */
	public String getClips(int categoryId,int offset, String subscriberId, boolean BIIndc, String devicetype, String browsingLanguage, String appName, int maxResults);
	public String getClips(ClipInfoActionBean clipInfoActionBean);
	
	public String getClipInfo(String clipId, String browsingLanguage, String appName);
	
	
	public String getNewReleaseClips(int offset, String browsingLanguage, String appName, int maxResults);
	public String getPickOfTheDay(String subscriberId, String browsingLanguage, String appName);
    /*public String getAllsubcategoryclips(int categoryId,int offset,int pageno, String language);*/
    public String like(Integer clipId);
    public String dislike(Integer clipId);
    public String User_Rating(int clipId, int rating);
    
    public String ClipRatingForSingle(Integer clipId);
	public String ClipRatingsForGroup(List<Integer> clipIds);

	public String getSubCategories(String subId, String parentCategoryId, String browsingLanguage, String appName,Date modifiedSince);
	public String getMainCategory(String subId ,String language, String browsingLanguage, String appName);
	public String getHomeCategory(String subId,String language, String browsingLanguage, String appName);
	
	public String getProfileCategories(String subscriberId, String browsingLanguage, String appName);
	/**
	 * Added for RBT-12174 server-side changes
	 * @author rony.gregory
	 * @param subscriberId
	 * @return
	 */
	
	public String getPlaylistsCategories(String subscriberId, String browsingLanguage, String appName);
	/**
	 * Added for RBT-12174 server-side changes
	 * @author rony.gregory
	 * @param subscriberId
	 * @return
	 */
	public String getOtherPlaylistsCategory(String subscriberId, String browsingLanguage, String appName);
	
	/**
	 * 
	 * @param subscriberId
	 * @param offsetInt
	 * @param maxResults 
	 * @return
	 */
	public String getNewReleaseCategories(String subscriberId, int offsetInt, String browsingLanguage, String appName, int maxResults);
	/**
	 * 
	 * @param subId
	 * @return
	 */
	public String getFreemiumCategory(String subId, String browsingLanguage, String appName);
	/**
	 * 
	 * @param subId
	 * @param maxResults 
	 * @return
	 */
	public String getFreemiumClips(String subId, String browsingLanguage, String appName, int maxResults);

}
