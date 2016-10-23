package com.onmobile.apps.ringbacktones.lucene;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;

public class LuceneClip extends Clip{
	
	private static final long serialVersionUID = 17654389760654L;
	private String vcode;
	private long parentCategoryId;
	private long subCategoryId;
	private String parentCategoryName;
	private String subCategoryName;
	
	private Clip clip;
	
	public LuceneClip(){
		
	}
	
	public LuceneClip(Clip clip, long parentCategoryId, long subCategoryId, String parentCategoryName, String subCategoryName){
		this.clip = clip;
		String vcode=clip.getClipRbtWavFile();
		vcode=vcode.replaceAll("rbt", "");
		vcode=vcode.replaceAll("_", "");
		this.setVcode(vcode);
		this.setAddToAccessTable(clip.getAddToAccessTable());
		this.setClassType(clip.getClassType());
		this.setClipDemoWavFile(clip.getClipDemoWavFile());
		this.setClipEndTime(clip.getClipEndTime());
		this.setClipStartTime(clip.getClipStartTime());
		this.setSmsStartTime(clip.getSmsStartTime());
		this.setClipGrammar(clip.getClipGrammar());
		this.setClipInfo(clip.getClipInfo());
		this.setClipNameWavFile(clip.getClipNameWavFile());
		this.setClipPreviewWavFile(clip.getClipPreviewWavFile());
		this.setClipPromoId(clip.getClipPromoId());
		this.setClipSmsAlias(clip.getClipSmsAlias());
		this.setLanguage(clip.getLanguage());
		this.setClipRbtWavFile(clip.getClipRbtWavFile());
		this.setClipId(clip.getClipId());
		this.setClipName(clip.getClipName());
		this.setAlbum(clip.getAlbum());
		this.setArtist(clip.getArtist());
		this.setClassType(clip.getClassType());
		this.setParentCategoryName(parentCategoryName);
		this.setSubCategoryName(subCategoryName);
		this.setParentCategoryId(parentCategoryId);
		this.setSubCategoryId(subCategoryId);
		this.setClipInfoMap(clip.getClipInfoMap());
	}
	
	
	public Clip getClip() {
		return clip;
	}

	public void setClip(Clip clip) {
		this.clip = clip;
	}

	public String getVcode() {
		return vcode;
	}
	public void setVcode(String vcode) {
		this.vcode = vcode;
	}
	public String getParentCategoryName() {
		return parentCategoryName;
	}
	public void setParentCategoryName(String parentCategoryName) {
		this.parentCategoryName = parentCategoryName;
	}
	public String getSubCategoryName() {
		return subCategoryName;
	}
	public void setSubCategoryName(String subCategoryName) {
		this.subCategoryName = subCategoryName;
	}
	public long getParentCategoryId() {
		return parentCategoryId;
	}
	public void setParentCategoryId(long parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}
	public long getSubCategoryId() {
		return subCategoryId;
	}
	public void setSubCategoryId(long subCategoryId) {
		this.subCategoryId = subCategoryId;
	}
	
	public boolean equals(Object obj) {
		if (! (obj instanceof LuceneClip)) {
			return false;
		}
		LuceneClip clip = (LuceneClip)obj;
		return this.getClipId()==clip.getClipId();
	}
	
	public int hashCode(){
		int hash = 7;
		hash = 31 * hash + this.getClipId();
		//hash = 31 * hash + (null == this.getClipName() ? 0 : this.getClipName().hashCode());
		return hash;
	}

	
	

}
