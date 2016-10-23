package com.onmobile.android.beans;

import java.util.List;

public class CategoryClipResponseBean {
	private List<CategoryBean> category;
	private List<ExtendedClipBean> clip;
	public List<CategoryBean> getCategory() {
		return category;
	}
	public void setCategory(List<CategoryBean> category) {
		this.category = category;
	}
	public List<ExtendedClipBean> getClip() {
		return clip;
	}
	public void setClip(List<ExtendedClipBean> clip) {
		this.clip = clip;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CategoryClipResponseBean [category=");
		builder.append(category);
		builder.append(", clip=");
		builder.append(clip);
		builder.append("]");
		return builder.toString();
	}
}
