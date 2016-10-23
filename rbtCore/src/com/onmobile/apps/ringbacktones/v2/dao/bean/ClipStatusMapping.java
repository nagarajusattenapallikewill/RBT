package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * 
 * @author md.alam
 *
 */
@Entity
@Table(name="rbt_clip_Status_Mapping")
public class ClipStatusMapping implements Serializable {


	private static final long serialVersionUID = -4885626081256000489L;


	@EmbeddedId
	private CompositeKey compositeKey;

	@Column(name="Status")
	private int status;

	public ClipStatusMapping() {

	}

	public CompositeKey getCompositeKey() {
		return compositeKey;
	}

	public void setCompositeKey(CompositeKey compositeKey) {
		this.compositeKey = compositeKey;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Embeddable
	public static class CompositeKey implements Serializable {


		private static final long serialVersionUID = 7868149972758221785L;

		@OneToOne
		@JoinColumn(name="Operator_Circle_Id")
		private OperatorCircleMapping operatorCircleMapping;

		@Column(name="Clip_Id")
		private int clipId;

		public CompositeKey() {

		}


		public OperatorCircleMapping getOperatorCircleMapping() {
			return operatorCircleMapping;
		}

		public void setOperatorCircleMapping(OperatorCircleMapping operatorCircleMapping) {
			this.operatorCircleMapping = operatorCircleMapping;
		}

		public int getClipId() {
			return clipId;
		}

		public void setClipId(int clipId) {
			this.clipId = clipId;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + clipId;
			result = prime
					* result
					+ ((operatorCircleMapping == null) ? 0
							: operatorCircleMapping.hashCode());
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
			CompositeKey other = (CompositeKey) obj;
			if (clipId != other.clipId)
				return false;
			if (operatorCircleMapping == null) {
				if (other.operatorCircleMapping != null)
					return false;
			} else if (!operatorCircleMapping
					.equals(other.operatorCircleMapping))
				return false;
			return true;
		}

	}
}
