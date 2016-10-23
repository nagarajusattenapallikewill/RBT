package com.onmobile.apps.ringbacktones.provisioning.bean;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "RBT_END_TIME_MODIFIED_CONTENTS")
public class RBTUpdatedClips implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ID")
	@GeneratedValue(strategy=GenerationType.AUTO) 
	private String id;
	
	@Column(name="END_TIME")
	private Timestamp endTime;

	@Column(name="clip_id")
	private int clipId;

	@Column(name="CLIP_NAME")
	private String clipName;

	@Column(name="LAST_MODIFIED_TIME")
	private Timestamp lastModifiedTime;

	@Column(name="CLIP_WAV_FILE")
	private String clipWavFile;
	
	@Column(name="CATEGORY_ID")
	private int categoryId;

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public int getClipId() {
		return clipId;
	}

	public void setClipId(int clipId) {
		this.clipId = clipId;
	}

	public String getClipName() {
		return clipName;
	}

	public void setClipName(String clipName) {
		this.clipName = clipName;
	}

	public Timestamp getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(Timestamp lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	
	public String getClipWavFile() {
		return clipWavFile;
	}
	
	public void setClipWavFile(String clipWavFile) {
		this.clipWavFile = clipWavFile;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	@Override
	public String toString() {
		return "RBTUpdatedClips [endTime=" + endTime + ", clipId="
				+ clipId + ", clipName=" + clipName + ", lastModifiedTime="
				+ lastModifiedTime + ", clipWavFile=" + clipWavFile
				+ ", categoryId=" + categoryId + "]";
	}
	
	}
