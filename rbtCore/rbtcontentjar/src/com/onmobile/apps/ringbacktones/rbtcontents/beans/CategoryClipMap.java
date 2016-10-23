package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;

public class CategoryClipMap implements Serializable {

	private static final long serialVersionUID = -821877516526498342L;

	private int categoryId;
	private int clipId;
	private char clipInList;
	private int clipIndex;
	private String playTime;
	private boolean deleteMap = false;
	
	private Date fromTime;
	private Date toTime;
	
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public int getClipId() {
		return clipId;
	}
	public void setClipId(int clipId) {
		this.clipId = clipId;
	}
	public char getClipInList() {
		return clipInList;
	}
	public void setClipInList(char clipInList) {
		this.clipInList = clipInList;
	}
	public int getClipIndex() {
		return clipIndex;
	}
	public void setClipIndex(int clipIndex) {
		this.clipIndex = clipIndex;
	}
	public String getPlayTime() {
		return playTime;
	}
	public void setPlayTime(String playTime) {
		this.playTime = playTime;
	}
	public boolean isDeleteMap() {
		return deleteMap;
	}
	public void setDeleteMap(boolean deleteMap) {
		this.deleteMap = deleteMap;
	}
	public Date getFromTime() {
		return fromTime;
	}
	public void setFromTime(Date fromTime) {
		this.fromTime = fromTime;
	}
	public Date getToTime() {
		return toTime;
	}
	public void setToTime(Date toTime) {
		this.toTime = toTime;
	}

	//	public static int[] getClipIdArray(List<CategoryClipMap> clipsInCategory) {
	//		int []clipIdArray = new int[clipsInCategory.size()];
	//		
	//		for(int i=0; i<clipsInCategory.size(); i++) {
	//			CategoryClipMap clipInCategory = clipsInCategory.get(i);
	//			clipIdArray[i] = clipInCategory.getClipId();
	//			
	//		}
	//		return clipIdArray;
	//	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CategoryClipMap [categoryId=");
		builder.append(categoryId);
		builder.append(", clipId=");
		builder.append(clipId);
		builder.append(", clipInList=");
		builder.append(clipInList);
		builder.append(", clipIndex=");
		builder.append(clipIndex);
		builder.append(", playTime=");
		builder.append(playTime);
		builder.append(", deleteMap=");
		builder.append(deleteMap);
		builder.append(", fromTime=");
		builder.append(fromTime);
		builder.append(", toTime=");
		builder.append(toTime);
		builder.append("]");
		return builder.toString();
	}
	public static String[] getClipIdsArray(List<CategoryClipMap> clipsInCategory) {
		//		String []clipIdArray = new String[clipsInCategory.size()];

		List<String> result = new ArrayList<String>(clipsInCategory.size()); 
		for(int i=0; i<clipsInCategory.size(); i++) {
			CategoryClipMap clipInCategory = clipsInCategory.get(i);
			String key = RBTCacheKey.getClipIdCacheKey(clipInCategory.getClipId());
			Clip clip = (Clip)RBTCache.getMemCachedClient().get(key);
			//			Clip clip = RBTCacheManager.getInstance().getClip(clipInCategory.getClipId());
			if(null != clip){
				//				clipIdArray[i] = RBTCacheKey.getClipIdCacheKey(clipInCategory.getClipId());
				result.add(key);
			}
		}
		//		return clipIdArray;
		return result.toArray(new String[0]);
	}

	public static String[] getActiveClipIdsArray(List<CategoryClipMap> clipsInCategory) {

		List<String> result = new ArrayList<String>(clipsInCategory.size());
		Date now = new Date();
		for(int i=0; i<clipsInCategory.size(); i++) {
			CategoryClipMap clipInCategory = clipsInCategory.get(i);
			String key = RBTCacheKey.getClipIdCacheKey(clipInCategory.getClipId());
			//			Clip clip = RBTCacheManager.getInstance().getClip(clipInCategory.getClipId());
			Clip clip = (Clip)RBTCache.getMemCachedClient().get(key);
			if(null != clip && clip.getClipEndTime().after(now)) {
				result.add(key);
			}
		}
		return result.toArray(new String[0]);
	}
}
