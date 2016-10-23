package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;


public class RbtSocialContentPublisher implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3757957561500731470L;

	private long sequenceId;
	
	private short className;
	
	private String classData;
	
	
	private short status;

	public RbtSocialContentPublisher() {
	}

	public RbtSocialContentPublisher(short className, String classData,
			short status) {
		this.className = className;
		this.classData = classData;
		this.status = status;
	}

	public long getSequenceId() {
		return this.sequenceId;
	}

	public void setSequenceId(long sequenceId) {
		this.sequenceId = sequenceId;
	}

	public short getClassName() {
		return this.className;
	}

	public void setClassName(short className) {
		this.className = className;
	}

	public String getClassData() {
		return this.classData;
	}

	public void setClassData(String classData) {
		this.classData = classData;
	}

	public short getStatus() {
		return this.status;
	}

	public void setStatus(short status) {
		this.status = status;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "RbtSocialContentPublisher [classData=" + classData
				+ ", className=" + className + ", sequenceId=" + sequenceId
				+ ", status=" + status + "]";
	}
	

}
