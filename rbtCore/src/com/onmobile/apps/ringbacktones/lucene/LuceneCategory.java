package com.onmobile.apps.ringbacktones.lucene;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryInfo;

public class LuceneCategory extends Category{
	
	private static final long serialVersionUID = 176543897604672L;
	private String parentCategoryName;
	private int parentCategoryId;
	

	public LuceneCategory(){
		
	}
	
	public LuceneCategory(Category category,int parentCategoryId, String parentCategoryName){
		this.setCategoryId(category.getCategoryId());
		this.setCategoryName(category.getCategoryName());
		this.setClassType(category.getClassType());
		this.setCategoryNameWavFile(category.getCategoryNameWavFile());
		this.setCategoryPreviewWavFile(category.getCategoryPreviewWavFile());
		this.setCategoryGrammar(category.getCategoryGrammar());
		this.setCategoryTpe(category.getCategoryTpe());
		this.setCategoryAskMobileNumber(category.getCategoryAskMobileNumber());
		this.setCategoryGreeting(category.getCategoryGreeting());
		this.setCategoryStartTime(category.getCategoryStartTime());
		this.setCategoryEndTime(category.getCategoryEndTime());
		this.setCategoryPromoId(category.getCategoryPromoId());
		this.setCategorySmsAlias(category.getCategorySmsAlias());
		this.setMmNumber(category.getMmNumber());
		this.setCategoryInfoSet(category.getCategoryInfoSet());
		this.setCategoryInfoMap(category.getCategoryInfoMap());
		this.setCategoryInfo(category.getCategoryInfo());
		this.setParentCategoryId(parentCategoryId);
		this.setParentCategoryName(parentCategoryName);
		this.setCategoryLanguage(category.getCategoryLanguage());
	}
	
	
	public String getParentCategoryName() {
		return parentCategoryName;
	}
	public void setParentCategoryName(String parentCategoryName) {
		this.parentCategoryName = parentCategoryName;
	}
	public int getParentCategoryId() {
		return parentCategoryId;
	}
	public void setParentCategoryId(int parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof LuceneCategory)) {
			return false;
		}
		LuceneCategory category = (LuceneCategory)obj;
		boolean equals = false;
		equals = this.getCategoryId()==category.getCategoryId();
		return equals;
	}
	
	public int hashCode(){
		int hash = 7;
		hash = 31 * hash + this.getCategoryId();
		//hash = 31 * hash + (null == this.getCategoryName() ? 0 : this.getCategoryName().hashCode());
		return hash;
	}
	
}
