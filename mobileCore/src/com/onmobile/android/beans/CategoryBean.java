package com.onmobile.android.beans;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;

public class CategoryBean{
	private int categoryId;
	private String categoryName;
	private int clipCount;
	private String categoryPath;
	private int categoryType;
	private String topSongLevel;
	private String description;
	private String categoryLanguage;

	public CategoryBean(Category category, int clipCount) {
		this.categoryId = category.getCategoryId();
		this.categoryName = category.getCategoryName();
		this.setClipCount(clipCount);
	}
	
	public CategoryBean(Category category, String clipPath) {
		this.categoryId = category.getCategoryId();
		this.categoryName = category.getCategoryName();
		this.setCategoryPath(clipPath);
	}
	
	public String getCategoryPath() {
		return categoryPath;
	}
	public void setCategoryPath(String categoryPath) {
		this.categoryPath = categoryPath;
	}
	
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public void setClipCount(int clipCount) {
		this.clipCount = clipCount;
	}
	public int getClipCount() {
		return clipCount;
	}

	public int getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(int categoryType) {
		this.categoryType = categoryType;
	}
	
	public String getTopSongLevel() {
		return topSongLevel;
	}

	public void setTopSongLevel(String topSongLevel) {
		this.topSongLevel = topSongLevel;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategoryLanguage() {
		return categoryLanguage;
	}

	public void setCategoryLanguage(String categoryLanguage) {
		this.categoryLanguage = categoryLanguage;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CategoryBean)) {
			return false;
		}
		CategoryBean catBean = (CategoryBean)obj;
		if(catBean.getCategoryId()==this.getCategoryId() && catBean.getCategoryName().equals(this.getCategoryName())){
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + this.getCategoryId();
		hash = 31 * hash + (null == this.getCategoryName() ? 0 : this.getCategoryName().hashCode());
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CategoryBean [categoryId=");
		builder.append(categoryId);
		builder.append(", categoryName=");
		builder.append(categoryName);
		builder.append(", clipCount=");
		builder.append(clipCount);
		builder.append(", categoryPath=");
		builder.append(categoryPath);
		builder.append(", categoryType=");
		builder.append(categoryType);
		builder.append(", topSongLevel=");
		builder.append(topSongLevel);
		builder.append(", description=");
		builder.append(description);
		builder.append(", categoryLanguage=");
		builder.append(categoryLanguage);
		builder.append("]");
		return builder.toString();
	}
}
