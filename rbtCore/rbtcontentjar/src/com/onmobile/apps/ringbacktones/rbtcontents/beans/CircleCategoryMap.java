package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;

public class CircleCategoryMap implements Serializable {
	
	private static final long serialVersionUID = 2920597487915143262L;
	
	private int categoryId;
	private int categoryIndex;
	private int parentCategoryId;
	private char prepaidYes;
	private String circleId;
	private String categoryLanguage;
	private boolean deleteMap = false;
	
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public int getCategoryIndex() {
		return categoryIndex;
	}
	public void setCategoryIndex(int categoryIndex) {
		this.categoryIndex = categoryIndex;
	}
	public int getParentCategoryId() {
		return parentCategoryId;
	}
	public void setParentCategoryId(int parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}
	public char getPrepaidYes() {
		return prepaidYes;
	}
	public void setPrepaidYes(char prepaidYes) {
		this.prepaidYes = prepaidYes;
	}
	public String getCircleId() {
		return circleId;
	}
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}
	public String getCategoryLanguage() {
		return categoryLanguage;
	}
	public void setCategoryLanguage(String categoryLanguage) {
		this.categoryLanguage = categoryLanguage;
	}
	public boolean isDeleteMap() {
		return deleteMap;
	}
	public void setDeleteMap(boolean deleteMap) {
		this.deleteMap = deleteMap;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CircleCategoryMap [categoryId=");
		builder.append(categoryId);
		builder.append(", categoryIndex=");
		builder.append(categoryIndex);
		builder.append(", parentCategoryId=");
		builder.append(parentCategoryId);
		builder.append(", prepaidYes=");
		builder.append(prepaidYes);
		builder.append(", circleId=");
		builder.append(circleId);
		builder.append(", categoryLanguage=");
		builder.append(categoryLanguage);
		builder.append(", deleteMap=");
		builder.append(deleteMap);
		builder.append("]");
		return builder.toString();
	}
}
