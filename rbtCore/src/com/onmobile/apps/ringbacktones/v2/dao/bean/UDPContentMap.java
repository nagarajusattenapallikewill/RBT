package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="RBT_UDP_CLIP_MAP")
public class UDPContentMap implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@EmbeddedId
	private UDPContentKeys contentKeys;	

	public UDPContentKeys getContentKeys() {
		return contentKeys;
	}

	public void setContentKeys(UDPContentKeys contentKeys) {
		this.contentKeys = contentKeys;
	}


	@Embeddable
	public static class UDPContentKeys implements Serializable {

		private static final long serialVersionUID = -1852384400510755755L;
		
		@OneToOne()
		@JoinColumn(name="UDP_ID")
		private UDPBean udpBean;
		
		@Column(name = "CLIP_ID")
		private long clipId;
		
		@Column(name = "Type", columnDefinition = "enum('SONG','RBTUGC')")
		@Enumerated(EnumType.STRING)
		private Type type;

		public UDPBean getUdpBean() {
			return udpBean;
		}
		public void setUdpBean(UDPBean udpBean) {
			this.udpBean = udpBean;
		}
		
		public long getClipId() {
			return clipId;
		}
		public void setClipId(long clipId) {
			this.clipId = clipId;
		}	
		
		public Type getType() {
			return type;
		}
		public void setType(Type type) {
			this.type = type;
		}

		public enum Type {
			SONG,RBTUGC;
		}

	}

}
