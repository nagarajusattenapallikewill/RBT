package com.onmobile.apps.ringbacktones.cache.content;

import java.util.Comparator;

/**
 * 
 * @author Sreekar
 * @date 26/08/2008
 */
public class ClipMap {
	private int _clipID;
	private int _categoryID;
	private int _index;
	private char _clipInList;
	private int _playTime;
	
	public ClipMap(int clipID, int categoryID, int index, char clipInList, int playTime) {
		_clipID = clipID;
		_categoryID = categoryID;
		_index = index;
		_clipInList = clipInList;
		_playTime = playTime;
	}
	
	public int getClipID() {
		return _clipID;
	}
	
	public int getCategoryID() {
		return _categoryID;
	}
	
	public int getIndex() {
		return _index;
	}
	
	public char getClipInList() {
		return _clipInList;
	}
	
	public int getPlayTime() {
		return _playTime;
	}
	
	public static final Comparator<ClipMap> INDEX_COMPARATOR = new Comparator<ClipMap>() {
		public int compare(ClipMap clip1, ClipMap clip2) {
			return clip1.getIndex() - (clip2.getIndex());
		}
	};
}