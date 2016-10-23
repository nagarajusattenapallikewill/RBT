package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.onmobile.apps.ringbacktones.rbtCommon.XmlField;
import com.onmobile.apps.ringbacktones.rbtCommon.XmlType;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.RBTContentUtils;

@XmlType
public class Clip implements Serializable {
	
	private static final long serialVersionUID = 7033621518567765513L;

	public static final Date DEFAULT_CLIP_END_TIME = new Date();

	
	@XmlField
	private int clipId;
	@XmlField
	private String clipName;
	@XmlField
	private String clipNameWavFile;
	@XmlField
	private String clipPreviewWavFile;
	@XmlField
	private String clipRbtWavFile;
	@XmlField
	private String clipGrammar;
	@XmlField
	private String clipSmsAlias;
	@XmlField
	private char addToAccessTable;
	@XmlField
	private String clipPromoId;
	@XmlField
	private String classType;
	@XmlField
	private Date clipStartTime;
	@XmlField
	private Date clipEndTime;
	@XmlField
	private Date smsStartTime;
	@XmlField
	private String album;
	@XmlField
	private String language;
	@XmlField
	private String clipDemoWavFile;
	@XmlField
	private String artist;
	@XmlField
	private String clipInfo;
	@XmlField
	private String contentType;
	@XmlField
	private String clipVcode;
	private Date lastModifiedTime;
	
	public Date getLastModifiedTime() {
		return lastModifiedTime;
	}
	public void setLastModifiedTime(Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	private Set<ClipInfo> clipInfoSet;
	private Map<String, String> clipInfoMap;
	
	private String clipLanguage;
	private String shortLanguage;
	private String clipPreviewWavFilePath;
	
	public int getClipId() {
		return clipId;
	}
	public void setClipId(int clipId) {
		this.clipId = clipId;
	}
	public String getClipInfo(Enum key){
		if(clipInfoMap!=null&&clipInfoMap.size()>0){
			String origpath = clipInfoMap.get(key.toString());
			return origpath;	
			}
		
		return null;	
		}
	
	public String getClipName() {
		return clipName;
	}
	public void setClipName(String clipName) {
		this.clipName = RBTContentUtils.ignoreJunkCharacters(clipName);
	}
	public String getClipName(String language) {
		if(clipInfoMap==null || language==null)
			return null;
		else
			return clipInfoMap.get("CLIP_NAME_" + language.toUpperCase());
	}
	
	public Map<String, String> getClipInfoMap(){
		return this.clipInfoMap;
	}
	
	public void setClipInfoMap(Map<String, String> clipInfoMap){
		this.clipInfoMap = clipInfoMap;
	}
	
	public String getClipNameWavFile() {
		return clipNameWavFile;
	}
	public void setClipNameWavFile(String clipNameWavFile) {
		this.clipNameWavFile = RBTContentUtils.ignoreJunkCharacters(clipNameWavFile);
	}
	public String getClipPreviewWavFile() {
		return clipPreviewWavFile;
	}
	public String getClipPreviewWavFilePath() {
		return clipPreviewWavFilePath;
	}
	public void setClipPreviewWavFilePath(String clipPreviewWavFilePath) {
		this.clipPreviewWavFilePath = clipPreviewWavFilePath;
	}
	public void setClipPreviewWavFile(String clipPreviewWavFile) {
		this.clipPreviewWavFile = RBTContentUtils.ignoreJunkCharacters(clipPreviewWavFile);
		setClipPreviewWavFilePath(RBTContentUtils.getClipPreviewFolderPath(String.valueOf(this.clipId)));
	}
	public String getClipRbtWavFile() {
		return clipRbtWavFile;
	}
	public void setClipRbtWavFile(String clipRbtWavFile) {
		this.clipRbtWavFile = RBTContentUtils.ignoreJunkCharacters(clipRbtWavFile);
	}
	public String getClipGrammar() {
		return clipGrammar;
	}
	public void setClipGrammar(String clipGrammar) {
		this.clipGrammar = RBTContentUtils.ignoreJunkCharacters(clipGrammar);
	}
	public String getClipGrammar(String language) {
		if(clipInfoMap==null || language==null)
			return null;
		else
			return clipInfoMap.get("CLIP_GRAMMAR_" + language.toUpperCase());
	}
	public String getClipSmsAlias() {
		return clipSmsAlias;
	}
	public void setClipSmsAlias(String clipSmsAlias) {
		this.clipSmsAlias = RBTContentUtils.ignoreJunkCharacters(clipSmsAlias);
	}
	public char getAddToAccessTable() {
		return addToAccessTable;
	}
	public void setAddToAccessTable(char addToAccessTable) {
		this.addToAccessTable = addToAccessTable;
	}
	public String getClipPromoId() {
		return clipPromoId;
	}
	public void setClipPromoId(String clipPromoId) {
		this.clipPromoId = RBTContentUtils.ignoreJunkCharacters(clipPromoId);
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = RBTContentUtils.ignoreJunkCharacters(classType);
	}
	public Date getClipStartTime() {
		return clipStartTime;
	}
	public void setClipStartTime(Date clipStartTime) {
		this.clipStartTime = clipStartTime;
	}
	public Date getClipEndTime() {
		return clipEndTime;
	}
	public void setClipEndTime(Date clipEndTime) {
		this.clipEndTime = clipEndTime;
	}
	public Date getSmsStartTime() {
		return smsStartTime;
	}
	public void setSmsStartTime(Date smsStartTime) {
		this.smsStartTime = smsStartTime;
	}
	public String getAlbum() {
		return album;
	}
	
	public void setAlbum(String album) {
		this.album = RBTContentUtils.ignoreJunkCharacters(album);
	}
	public String getAlbum(String language) {
		if(clipInfoMap==null || language==null)
			return null;
		else
			return clipInfoMap.get("ALBUM_" + language.toUpperCase());
	}

	public String getImage(String language) {
		if(clipInfoMap==null || language==null)
			return null;
		else
			return clipInfoMap.get("IMG_" + language.toUpperCase());
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = RBTContentUtils.ignoreJunkCharacters(language);
		setShortLanguage(RBTContentUtils.getShortCodeLanguage(this.language));
	}
	public String getLanguage(String language) {
		if(clipInfoMap==null || language==null)
			return null;
		else
			return clipInfoMap.get("LANGUAGE_" + language.toUpperCase());
	}
	public String getClipDemoWavFile() {
		return clipDemoWavFile;
	}
	public void setClipDemoWavFile(String clipDemoWavFile) {
		this.clipDemoWavFile = RBTContentUtils.ignoreJunkCharacters(clipDemoWavFile);
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = RBTContentUtils.ignoreJunkCharacters(artist);
	}
	public String getArtist(String language) {
		if(clipInfoMap==null || language==null)
			return null;
		else
			return clipInfoMap.get("ARTIST_" + language.toUpperCase());
	}
	public String getClipInfo() {
		return clipInfo;
	}
	public void setClipInfo(String clipInfo) {
		this.clipInfo = RBTContentUtils.ignoreJunkCharacters(clipInfo);
	}
	public String getClipInfo(String language) {
		if(clipInfoMap==null || language==null)
			return null;
		else
			return clipInfoMap.get("CLIP_INFO_" + language.toUpperCase());
	}

	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = RBTContentUtils.ignoreJunkCharacters(contentType);
	}
	
	public Set<ClipInfo> getClipInfoSet() {
		return clipInfoSet;
	}
	public void setClipInfoSet(Set<ClipInfo> clipInfoSet) {
		this.clipInfoSet = clipInfoSet;
	}
	public void setClipLanguage(String clipLanguage) {
		this.clipLanguage = clipLanguage;
	}
	public String getClipLanguage(){
		return clipLanguage;
	}
	public String getShortLanguage() {
		return shortLanguage;
	}
	public void setShortLanguage(String shortLanguage) {
		this.shortLanguage = shortLanguage;		
	}
	public String getShortLanguage(String language) {
		return RBTContentUtils.getShortCodeLanguage(clipInfoMap.get("LANGUAGE_" + language.toUpperCase()));
	}
	public String getClipVcode() {
		return clipVcode;
	}
	public void setClipVcode(String clipVcode) {
		this.clipVcode = clipVcode;
	}
	@Override
	public String toString() {
		return "Clip [clipId=" + clipId + ", clipName=" + clipName + ", clipNameWavFile=" + clipNameWavFile + ", clipPreviewWavFile="
				+ clipPreviewWavFile + ", clipRbtWavFile=" + clipRbtWavFile + ", clipGrammar=" + clipGrammar + ", clipSmsAlias="
				+ clipSmsAlias + ", addToAccessTable=" + addToAccessTable + ", clipPromoId=" + clipPromoId + ", classType=" + classType
				+ ", clipStartTime=" + clipStartTime + ", clipEndTime=" + clipEndTime + ", smsStartTime=" + smsStartTime + ", album="
				+ album + ", language=" + language + ", clipDemoWavFile=" + clipDemoWavFile + ", artist=" + artist + ", clipInfo="
				+ clipInfo + ", contentType=" + contentType + ", clipInfoSet=" + clipInfoSet + ", clipInfoMap=" + clipInfoMap
				+ ", clipLanguage=" + clipLanguage + ", shortLanguage=" + shortLanguage + ", clipPreviewWavFilePath="
				+ clipPreviewWavFilePath + ", lastModifiedTime=" + lastModifiedTime+ "]";
	}
	public enum ClipInfoKeys {
		
		IMG_URL,
		CLIP_FULL_TRACK,
		IMG;
	}
}
