/**
 * 
 */
package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;

/**
 * @author vinayasimha.patil
 *
 */
public class CategoryGrammar implements Serializable {

	private static final long serialVersionUID = 9130654228425856753L;

	private int categoryId;
	private String language;
	private String grammar;
	
	public int getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	} 

	public String getGrammar() {
		return grammar;
	}
	
	public void setGrammar(String grammar) {
		this.grammar = grammar;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CategoryGrammar[categoryId = ");
		builder.append(categoryId);
		builder.append(", grammar = ");
		builder.append(grammar);
		builder.append(", language = ");
		builder.append(language);
		builder.append("]");
		return builder.toString();
	}
}
