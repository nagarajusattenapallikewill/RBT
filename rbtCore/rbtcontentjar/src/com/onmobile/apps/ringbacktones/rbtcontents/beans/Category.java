package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.onmobile.apps.ringbacktones.rbtCommon.XmlField;
import com.onmobile.apps.ringbacktones.rbtCommon.XmlType;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.RBTContentUtils;

@XmlType
public class Category implements Serializable {

	private static final long serialVersionUID = -5250749288654150328L;
	
	@XmlField
	private int categoryId;
	@XmlField
	private String categoryName;
	@XmlField
	private String categoryNameWavFile;
	@XmlField
	private String categoryPreviewWavFile;
	@XmlField
	private String categoryGrammar;
	@XmlField
	private int categoryTpe;
	@XmlField
	private char categoryAskMobileNumber;
	@XmlField
	private String categoryGreeting;
	@XmlField
	private Date categoryStartTime;
	@XmlField
	private Date categoryEndTime;
	@XmlField
	private String classType;
	@XmlField
	private String categoryPromoId;
	@XmlField
	private String categorySmsAlias;
	@XmlField
	private String mmNumber;
	private Set<CategoryInfo> categoryInfoSet;
	private Map<String, String> categoryInfoMap;
	@XmlField
	private String categoryInfo;
	
	private String categoryLanguage;
	private Date lastModifiedTime;
	
	
	public Date getLastModifiedTime() {
		return lastModifiedTime;
	}
	public void setLastModifiedTime(Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	
	public String getCategoryInfo(CategoryInfoKeys key){
		if(categoryInfoMap!=null&&categoryInfoMap.size()>0){
			String origpath = categoryInfoMap.get(key.toString());
			return origpath;
		}
		return null;
	}
	
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = RBTContentUtils.ignoreJunkCharacters(categoryName);
	}
	public String getCategoryName(String language) {
		if(categoryInfoMap==null || language==null)
			return null;
		else
			return categoryInfoMap.get("CATEGORY_NAME_" + language.toUpperCase());
	}
	public String getCategoryNameWavFile() {
		return categoryNameWavFile;
	}
	public void setCategoryNameWavFile(String categoryNameWavFile) {
		this.categoryNameWavFile = RBTContentUtils.ignoreJunkCharacters(categoryNameWavFile);
	}
	public String getCategoryPreviewWavFile() {
		return categoryPreviewWavFile;
	}
	public void setCategoryPreviewWavFile(String categoryPreviewWavFile) {
		this.categoryPreviewWavFile = RBTContentUtils.ignoreJunkCharacters(categoryPreviewWavFile);
	}
	public String getCategoryGrammar() {
		return categoryGrammar;
	}
	public void setCategoryGrammar(String categoryGrammar) {
		this.categoryGrammar = RBTContentUtils.ignoreJunkCharacters(categoryGrammar);
	}
	public String getCategoryGrammar(String language) {
		if(categoryInfoMap==null || language==null)
			return null;
		else
			return categoryInfoMap.get("CATEGORY_GRAMMAR_" + language.toUpperCase());
	}
	public int getCategoryTpe() {
		return categoryTpe;
	}
	public void setCategoryTpe(int categoryTpe) {
		this.categoryTpe = categoryTpe;
	}
	public char getCategoryAskMobileNumber() {
		return categoryAskMobileNumber;
	}
	public void setCategoryAskMobileNumber(char categoryAskMobileNumber) {
		this.categoryAskMobileNumber = categoryAskMobileNumber;
	}
	public String getCategoryGreeting() {
		return categoryGreeting;
	}
	public void setCategoryGreeting(String categoryGreeting) {
		this.categoryGreeting = RBTContentUtils.ignoreJunkCharacters(categoryGreeting);
	}
	public Date getCategoryStartTime() {
		return categoryStartTime;
	}
	public void setCategoryStartTime(Date categoryStartTime) {
		this.categoryStartTime = categoryStartTime;
	}
	public Date getCategoryEndTime() {
		return categoryEndTime;
	}
	public void setCategoryEndTime(Date categoryEndTime) {
		this.categoryEndTime = categoryEndTime;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = RBTContentUtils.ignoreJunkCharacters(classType);
	}
	public String getCategoryPromoId() {
		return categoryPromoId;
	}
	public void setCategoryPromoId(String categoryPromoId) {
		this.categoryPromoId = RBTContentUtils.ignoreJunkCharacters(categoryPromoId);
	}
	public String getCategorySmsAlias() {
		return categorySmsAlias;
	}
	public void setCategorySmsAlias(String categorySmsAlias) {
		this.categorySmsAlias = RBTContentUtils.ignoreJunkCharacters(categorySmsAlias);
	}
	public String getMmNumber() {
		return mmNumber;
	}
	public void setMmNumber(String mmNumber) {
		this.mmNumber = RBTContentUtils.ignoreJunkCharacters(mmNumber);
	}
	
	
	public Set<CategoryInfo> getCategoryInfoSet() {
		return categoryInfoSet;
	}
	public void setCategoryInfoSet(Set<CategoryInfo> categoryInfoSet) {
		this.categoryInfoSet = categoryInfoSet;
	}
	public Map<String, String> getCategoryInfoMap() {
		return categoryInfoMap;
	}
	public void setCategoryInfoMap(Map<String, String> categoryInfoMap) {
		this.categoryInfoMap = categoryInfoMap;
	}

	public String getCategoryInfo() {
		return categoryInfo;
	}
	public void setCategoryInfo(String categoryInfo) {
		this.categoryInfo = RBTContentUtils.ignoreJunkCharacters(categoryInfo);
	}
	public String getCategoryLanguage() {
		return categoryLanguage;
	}
	public void setCategoryLanguage(String categoryLanguage) {
		this.categoryLanguage = categoryLanguage;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Category[categoryId = ");
		builder.append(categoryId);
		builder.append(", categoryName = ");
		builder.append(categoryName);
		builder.append(", categoryAskMobileNumber = ");
		builder.append(categoryAskMobileNumber);
		builder.append(", categoryEndTime = ");
		builder.append(categoryEndTime);
		builder.append(", categoryGrammar = ");
		builder.append(categoryGrammar);
		builder.append(", categoryGreeting = ");
		builder.append(categoryGreeting);
		builder.append(", categoryNameWavFile = ");
		builder.append(categoryNameWavFile);
		builder.append(", categoryPreviewWavFile = ");
		builder.append(categoryPreviewWavFile);
		builder.append(", categoryPromoId = ");
		builder.append(categoryPromoId);
		builder.append(", categorySmsAlias = ");
		builder.append(categorySmsAlias);
		builder.append(", categoryStartTime = ");
		builder.append(categoryStartTime);
		builder.append(", categoryTpe = ");
		builder.append(categoryTpe);
		builder.append(", classType = ");
		builder.append(classType);
		builder.append(", mmNumber = ");
		builder.append(mmNumber);
		builder.append(", categoryInfo = ");
		builder.append(categoryInfo);
		builder.append(", categoryLanguage = ");
		builder.append(categoryLanguage);
		builder.append(", categoryInfoMap = ");
		builder.append(categoryInfoMap);
		builder.append(", lastModifiedTime = ");
		builder.append(lastModifiedTime);
		builder.append("]");
		return builder.toString();
	}
	public enum CategoryInfoKeys {
		
		CONTENT_TYPE,
		IMG_URL,
		IMG,
		CAT_DESC;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + categoryId;
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (categoryId != other.categoryId)
			return false;
		return true;
	}
	
}
