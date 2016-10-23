package com.onmobile.apps.ringbacktones.cache.content;

import java.util.Comparator;
import java.util.Date;

import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.ClipsImpl;
/**
 * @author Sreekar
 * @date 20/06/2008
 */
public class ClipMinimal {
	private int clipId;
	private String startingLetter;
	private String clipName;
	private String wavFile;
	private String nameFile;
	private String previewFile;
	private String demoFile;
	private String album;
	private String classType;
	private Date smsTime;
	private Date endTime;
	private String promoID;
	private String grammar;
	private String language;
	private String artist;
	private String smsAlias;
	private String clipInfo;

	public ClipMinimal(Clips clip) {
		this.clipId = clip.id();
		this.promoID = clip.promoID();
		this.startingLetter = clip.name().trim().substring(0, 1).toUpperCase();
		this.clipName = clip.name();
		this.wavFile = clip.wavFile();
		this.nameFile = clip.nameFile();
		this.previewFile = clip.previewFile();
		this.demoFile = clip.demoFile();
		this.classType = clip.classType();
		this.smsTime = clip.smsTime();
		this.endTime = clip.endTime();
		this.album = clip.album();
		this.grammar = clip.grammar();
		this.language = clip.lang();
		this.artist = clip.artist();
		this.smsAlias = clip.alias();
		this.clipInfo = clip.clipInfo();
	}

	public ClipMinimal(int id, String promo, String name, String wavFile, String nameFile,
			String previewFile, String demoFile, String grammar, String classType, Date smsTime,
			Date endTime, String album, String lang, String artist, String smsAlias, String clipInfo) {
		this.clipId = id;
		this.promoID = promo;
		this.startingLetter = name.trim().substring(0, 1).toUpperCase();
		this.clipName = name;
		this.wavFile = wavFile;
		this.nameFile = nameFile;
		this.previewFile = previewFile;
		this.demoFile = demoFile;
		this.grammar = grammar;
		this.classType = classType;
		this.smsTime = smsTime;
		this.endTime = endTime;
		this.album = album;
		this.language = lang;
		this.artist = artist;
		this.smsAlias = smsAlias;
		this.clipInfo = clipInfo;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getLanguage() {
		return language;
	}

	public String getClipName() {
		return clipName;
	}

	public void setClipName(String clipName) {
		this.clipName = clipName;
	}

	public String getStartingLetter() {
		return startingLetter;
	}

	public void setStartingLetter(String startingLetter) {
		this.startingLetter = startingLetter;
	}

	public String getWavFile() {
		return wavFile;
	}

	public void setWavFile(String wavFile) {
		this.wavFile = wavFile;
	}

	public String getNameFile() {
		return nameFile;
	}

	public void setNameFile(String nameFile) {
		this.nameFile = nameFile;
	}

	public String getPreviewFile() {
		return previewFile;
	}

	public void setPreviewFile(String previewFile) {
		this.previewFile = previewFile;
	}

	public String getDemoFile() {
		return demoFile;
	}

	public void setDemoFile(String demoFile) {
		this.demoFile = demoFile;
	}

	public int getClipId() {
		return clipId;
	}

	public void setClipId(int clipId) {
		this.clipId = clipId;
	}

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getSmsTime() {
		return smsTime;
	}

	public void setSmsTime(Date smsTime) {
		this.smsTime = smsTime;
	}

	public String getPromoID() {
		return promoID;
	}

	public void setPromoID(String promoID) {
		this.promoID = promoID;
	}

	public String getGrammar() {
		return grammar;
	}
	
	public String getSMSAlias() {
		return smsAlias;
	}
	
	public String getClipInfo(){
		return clipInfo;
	}
	
	public ClipsImpl getClipsObj() {
		return new ClipsImpl(clipId, clipName, nameFile, previewFile, wavFile, grammar, smsAlias,
				"y", promoID, classType, new Date(), endTime, smsTime, album, language, demoFile,
				artist, clipInfo);
	}
	
	public static final Comparator<ClipMinimal> NAME_COMPARATOR = new Comparator<ClipMinimal>() {
		public int compare(ClipMinimal clip1, ClipMinimal clip2) {
			return clip1.getClipName().compareToIgnoreCase(clip2.getClipName());
		}
	};
}