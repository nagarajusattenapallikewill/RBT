package com.onmobile.apps.ringbacktones.lucene.solr;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author laxmankumar
 *
 */
public class SearchResult {
	
	@Field("clipid")
	String clipId = null;
	@Field("subcatid")
	String subCategoryId = null;
	@Field("parentcatid")
	String parentCategoryId = null;
	/**
	 * @return the clipId
	 */
	public String getClipId() {
		return clipId;
	}
	/**
	 * @param clipId the clipId to set
	 */
	@Field("clipid")
	public void setClipId(String clipId) {
		this.clipId = clipId;
	}
	/**
	 * @return the subCategoryId
	 */
	public String getSubCategoryId() {
		return subCategoryId;
	}
	/**
	 * @param subCategoryId the subCategoryId to set
	 */
	@Field("subcatid")
	public void setSubCategoryId(String subCategoryId) {
		this.subCategoryId = subCategoryId;
	}
	/**
	 * @return the parentCategoryId
	 */
	public String getParentCategoryId() {
		return parentCategoryId;
	}
	/**
	 * @param parentCategoryId the parentCategoryId to set
	 */
	@Field("parentcatid")
	public void setParentCategoryId(String parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchResult [clipId=");
		builder.append(clipId);
		builder.append(", parentCategoryId=");
		builder.append(parentCategoryId);
		builder.append(", subCategoryId=");
		builder.append(subCategoryId);
		builder.append("]");
		return builder.toString();
	}
	
}

