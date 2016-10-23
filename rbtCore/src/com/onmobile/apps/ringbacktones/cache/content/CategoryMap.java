package com.onmobile.apps.ringbacktones.cache.content;
/**
 * @author Sreekar
 * @date 07/08/2008
 */
public class CategoryMap {
	private int _id;
	private int _index;
	private int _parentCatID;
	private char _prepaidYes;
	private String _language;
	private String _circleID;
	
	public CategoryMap(int id, int index, int parentCatID, char prepaidYes, String language,
			String circleID) {
		_id = id;
		_index = index;
		_parentCatID = parentCatID;
		_prepaidYes = prepaidYes;
		_language = language;
		_circleID = circleID;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getIndex() {
		return _index;
	}
	
	public int getParentCategoryID() {
		return _parentCatID;
	}
	
	public char getPrepaidYes() {
		return _prepaidYes;
	}
	
	public String getLanguage() {
		return _language;
	}
	
	public String getCircleID() {
		return _circleID;
	}
}