package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;

import com.onmobile.apps.ringbacktones.rbtcontents.utils.RBTContentUtils;

public class CategoryInfo implements Serializable{
	
	private static final long serialVersionUID = -1278276354852731129L;
	
	private int categoryId;
	private String name;
	private String value;
	
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = RBTContentUtils.ignoreJunkCharacters(name);
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = RBTContentUtils.ignoreJunkCharacters(value);
	}
	
		@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("CategoryInfo[categoryId = ");
		builder.append(categoryId);
		builder.append(", name = ");
		builder.append(name);
		builder.append(", value = ");
		builder.append(value);
		builder.append(" ]");
		return builder.toString();
	}
	
	

}
