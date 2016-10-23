package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * 
 * @author md.alam
 *
 */

@Entity
@Table(name="rbt_operator_Circle_Mapping",
uniqueConstraints = @UniqueConstraint(columnNames = {"Operator","CircleId"}))
public class OperatorCircleMapping implements Serializable {

	private static final long serialVersionUID = -8016522877220123868L;
	@Id
	@Column(name = "Operator_Circle_Id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Column(name="Operator")
	private String operatorName;
	@Column(name="CircleId")
	private String circleId;

	public OperatorCircleMapping() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getCircleId() {
		return circleId;
	}

	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((circleId == null) ? 0 : circleId.hashCode());
		result = prime * result + id;
		result = prime * result
				+ ((operatorName == null) ? 0 : operatorName.hashCode());
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
		OperatorCircleMapping other = (OperatorCircleMapping) obj;
		if (circleId == null) {
			if (other.circleId != null)
				return false;
		} else if (!circleId.equals(other.circleId))
			return false;
		if (id != other.id)
			return false;
		if (operatorName == null) {
			if (other.operatorName != null)
				return false;
		} else if (!operatorName.equals(other.operatorName))
			return false;
		return true;
	}
	
	
	
}
