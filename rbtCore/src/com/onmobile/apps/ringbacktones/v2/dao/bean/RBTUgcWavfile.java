package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "rbt_ugc_wav_file")
public class RBTUgcWavfile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="UGC_ID")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long ugcId;

	@Column(name="SUBSCRIBER_ID")
	@Index(name = "IND_RBT_UGC_WAV_FILE_SUBID")
	private long subscriberId;

	@Column(name="UGC_WAV_FILE")
	private String ugcWavFile;

	@Column(name="UPLOAD_STATUS")
	@Index(name = "IND_RBT_UGC_WAV_FILE_STATUS")
	private int uploadStatus;

	@Column(name="MODE", updatable=false)
	private String mode;

	@Column(name="NEXT_RETRY_TIME",columnDefinition="TIMESTAMP NULL DEFAULT NULL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date nextRetryTime;
	
	@Column(name="RETRY_COUNT")
	private int retryCount;

	public long getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(long subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getUgcWavFile() {
		return ugcWavFile;
	}

	public void setUgcWavFile(String ugcWavFile) {
		this.ugcWavFile = ugcWavFile;
	}

	public UgcFileUploadStatus getUploadStatus(){
		return UgcFileUploadStatus.getUgcFileUploadStatus(uploadStatus);
	}

	public void setUploadStatus(UgcFileUploadStatus downloadStatus) {
		this.uploadStatus = downloadStatus.getUgcDownloadState();
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Date getNextRetryTime() {
		return nextRetryTime;
	}

	public void setNextRetryTime(Date nextRetryTime) {
		this.nextRetryTime = nextRetryTime;
	}

	public long getUgcId() {
		return ugcId;
	}

	public void setUgcId(long ugcId) {
		this.ugcId = ugcId;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	@Override
	public String toString() {
		return "RBTUgcWavfile [ugcId=" + ugcId + ", subscriberId="
				+ subscriberId + ", ugcWavFile=" + ugcWavFile
				+ ", uploadStatus=" + UgcFileUploadStatus.getUgcFileUploadStatus(uploadStatus) + ", mode=" + mode
				+ ", nextRetryTime=" + nextRetryTime + "]";
	}
	
	
	public static enum UgcFileUploadStatus{
		TO_BE_PROCESS_STATE(0), SUCCESS_STATE(1), FAILED_STATE(2);
		
		private int state;
		private static Map<Integer, UgcFileUploadStatus> uploadStatusMap = null;
		
		UgcFileUploadStatus(int state) {
			this.state = state;
		}
		
		public int getUgcDownloadState() {
			return state;
		}
		
		public static UgcFileUploadStatus getUgcFileUploadStatus(int uploadStatus){
			if(uploadStatusMap == null) {
				synchronized (UgcFileUploadStatus.class) {
					if(uploadStatusMap == null) {
						UgcFileUploadStatus[] arr = UgcFileUploadStatus.values();
						uploadStatusMap = new HashMap<Integer, RBTUgcWavfile.UgcFileUploadStatus>(arr.length);
						for(UgcFileUploadStatus obj : arr) {
							uploadStatusMap.put(obj.getUgcDownloadState(), obj);							
						}						
					}
				}
			}
//			UgcFileUploadStatus[] arr = UgcFileUploadStatus.values();
//			for(UgcFileUploadStatus obj : arr) {
//				if(obj.getUgcDownloadState() == i) {
//					return obj;
//				}
//			}
			
			return uploadStatusMap.get(uploadStatus);
		}
	}

}
