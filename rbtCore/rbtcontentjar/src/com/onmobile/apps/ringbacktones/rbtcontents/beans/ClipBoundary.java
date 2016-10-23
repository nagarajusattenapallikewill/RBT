package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.util.Comparator;

public class ClipBoundary {

	int startIndex = -1;
	int endIndex = -1;
	int count = -1;
	
	public ClipBoundary(int startIndex, int endIndex, int count) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.count = count;
	}
	
	/**
	 * @return Returns the count.
	 */
	public int getCount() {
		return count;
	}
	/**
	 * @param count The count to set.
	 */
	public void setCount(int count) {
		this.count = count;
	}
	/**
	 * @return Returns the endIndex.
	 */
	public int getEndIndex() {
		return endIndex;
	}
	/**
	 * @param endIndex The endIndex to set.
	 */
	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}
	/**
	 * @return Returns the startIndex.
	 */
	public int getStartIndex() {
		return startIndex;
	}
	/**
	 * @param startIndex The startIndex to set.
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	
	public String toString() {
		return "{ " + startIndex + " - " + endIndex + " -> " + count + " }"; 
	}
	
	public static Comparator<ClipBoundary> getClipBoundaryComparator() {
		
		return new Comparator<ClipBoundary>() {
		
			public int compare(ClipBoundary a, ClipBoundary b) {
				int diff = a.getStartIndex() - b.getStartIndex();
				if (diff > 0) {
					return 1;
				} else {
					return -1;
				}
			}
		
		};

	}

}
