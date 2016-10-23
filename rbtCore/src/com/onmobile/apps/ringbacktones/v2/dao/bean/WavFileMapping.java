package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

/**
 * 
 * @author md.alam
 *
 */

@Entity
@Table(name = "rbt_wav_File_Mapping")
public class WavFileMapping implements Serializable {

	private static final long serialVersionUID = 4413996161461149329L;
	@EmbeddedId
	private WavFileCompositeKey wavFileCompositeKey;
	
	@Column(name = "Wav_File_1")
	@Index(name = "index_rbt_wav_file_mapping_wavFileVerOne")
	private String wavFileVerOne;
	
	public WavFileMapping() {
		
	}

	public WavFileCompositeKey getWavFileCompositeKey() {
		return wavFileCompositeKey;
	}

	public void setWavFileCompositeKey(WavFileCompositeKey wavFileCompositeKey) {
		this.wavFileCompositeKey = wavFileCompositeKey;
	}
	
	public String getWavFileVerOne() {
		return wavFileVerOne;
	}

	public void setWavFileVerOne(String wavFileVerOne) {
		this.wavFileVerOne = wavFileVerOne;
	}

	@Embeddable
	public static class WavFileCompositeKey implements Serializable {

		private static final long serialVersionUID = -1227069731778551155L;

		@Column(name = "Operator")
		private String operatorName;
		
		@Column(name = "Wav_File_2")
		private String wavFileVerTwo;

		public WavFileCompositeKey() {

		}

		public String getOperatorName() {
			return operatorName;
		}

		public void setOperatorName(String operatorName) {
			this.operatorName = operatorName.toUpperCase();
		}


		public String getWavFileVerTwo() {
			return wavFileVerTwo;
		}

		public void setWavFileVerTwo(String wavFileVerTwo) {
			this.wavFileVerTwo = wavFileVerTwo;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((operatorName == null) ? 0 : operatorName.hashCode());
			result = prime * result
					+ ((wavFileVerTwo == null) ? 0 : wavFileVerTwo.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WavFileCompositeKey other = (WavFileCompositeKey) obj;
			if (operatorName == null) {
				if (other.operatorName != null)
					return false;
			} else if (!operatorName.equals(other.operatorName))
				return false;
			if (wavFileVerTwo == null) {
				if (other.wavFileVerTwo != null)
					return false;
			} else if (!wavFileVerTwo.equals(other.wavFileVerTwo))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "WavFileCompositeKey [operatorName=" + operatorName
					+ ", wavFileVerTwo=" + wavFileVerTwo + "]";
		}
		
	}

	@Override
	public String toString() {
		return "WavFileMapping [wavFileCompositeKey=" + wavFileCompositeKey
				+ ", wavFileVerOne=" + wavFileVerOne + "]";
	}
	
	

}
