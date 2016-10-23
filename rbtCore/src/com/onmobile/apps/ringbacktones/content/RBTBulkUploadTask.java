package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/**
 * @author vasipalli.sreenadh
 *
 */
public class RBTBulkUploadTask 
{
	private int taskId;
	private String taskName = null;
	private String circleId = null;
	private String activationClass = null;
	private String selectionClass = null;
	private int selectionType;   			// VALUES LIKE 0-CORPORATE/90-CRICKET
	private String taskType;	 			// VALUES LIKE ACTIVATION/DEACTIVATION 
	private String activatedBy = null;
	private String actInfo = null;     		// UPLOADED THE FILE AND WHO PROCESSED IT
	private Date uploadTime = null;
	private Date processTime = null;
	private Date endTime = null;			// End time in case of corporate compaign
	private int taskStatus = 0; 			// 0-new/1-success/2-failure/3-processing/4-task edited // -2 is being used to get all processed tasks in CCC
	private String taskMode = null;         // Carporate name
	private String taskInfo;
	
	
	public RBTBulkUploadTask()
	{
		
	}


	/**
	 * @param taskId
	 * @param taskName
	 * @param circleId
	 * @param activationClass
	 * @param selectionClass
	 * @param selectionType
	 * @param taskType
	 * @param activatedBy
	 * @param actInfo
	 * @param uploadTime
	 * @param processTime
	 * @param endTime
	 * @param taskStatus
	 * @param taskMode
	 * @param taskInfo
	 */
	public RBTBulkUploadTask(int taskId, String taskName, String circleId,
			String activationClass, String selectionClass, int selectionType,
			String taskType, String activatedBy, String actInfo,
			Date uploadTime, Date processTime, Date endTime, int taskStatus,
			String taskMode, String taskInfo) {
		super();
		this.taskId = taskId;
		this.taskName = taskName;
		this.circleId = circleId;
		this.activationClass = activationClass;
		this.selectionClass = selectionClass;
		this.selectionType = selectionType;
		this.taskType = taskType;
		this.activatedBy = activatedBy;
		this.actInfo = actInfo;
		this.uploadTime = uploadTime;
		this.processTime = processTime;
		this.endTime = endTime;
		this.taskStatus = taskStatus;
		this.taskMode = taskMode;
		this.taskInfo = taskInfo;
	}


	/**
	 * @return the taskId
	 */
	public int getTaskId() {
		return taskId;
	}


	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}


	/**
	 * @return the taskName
	 */
	public String getTaskName() {
		return taskName;
	}


	/**
	 * @param taskName the taskName to set
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}


	/**
	 * @return the circleId
	 */
	public String getCircleId() {
		return circleId;
	}


	/**
	 * @param circleId the circleId to set
	 */
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}


	/**
	 * @return the activationClass
	 */
	public String getActivationClass() {
		return activationClass;
	}


	/**
	 * @param activationClass the activationClass to set
	 */
	public void setActivationClass(String activationClass) {
		this.activationClass = activationClass;
	}


	/**
	 * @return the selectionClass
	 */
	public String getSelectionClass() {
		return selectionClass;
	}


	/**
	 * @param selectionClass the selectionClass to set
	 */
	public void setSelectionClass(String selectionClass) {
		this.selectionClass = selectionClass;
	}


	/**
	 * @return the selectionType
	 */
	public int getSelectionType() {
		return selectionType;
	}


	/**
	 * @param selectionType the selectionType to set
	 */
	public void setSelectionType(int selectionType) {
		this.selectionType = selectionType;
	}


	/**
	 * @return the taskType
	 */
	public String getTaskType() {
		return taskType;
	}


	/**
	 * @param taskType the taskType to set
	 */
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}


	/**
	 * @return the activatedBy
	 */
	public String getActivatedBy() {
		return activatedBy;
	}


	/**
	 * @param activatedBy the activatedBy to set
	 */
	public void setActivatedBy(String activatedBy) {
		this.activatedBy = activatedBy;
	}


	/**
	 * @return the actInfo
	 */
	public String getActInfo() {
		return actInfo;
	}


	/**
	 * @param actInfo the actInfo to set
	 */
	public void setActInfo(String actInfo) {
		this.actInfo = actInfo;
	}


	/**
	 * @return the uploadTime
	 */
	public Date getUploadTime() {
		return uploadTime;
	}


	/**
	 * @param uploadTime the uploadTime to set
	 */
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}


	/**
	 * @return the processTime
	 */
	public Date getProcessTime() {
		return processTime;
	}


	/**
	 * @param processTime the processTime to set
	 */
	public void setProcessTime(Date processTime) {
		this.processTime = processTime;
	}


	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}


	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}


	/**
	 * @return the taskStatus
	 */
	public int getTaskStatus() {
		return taskStatus;
	}


	/**
	 * @param taskStatus the taskStatus to set
	 */
	public void setTaskStatus(int taskStatus) {
		this.taskStatus = taskStatus;
	}


	/**
	 * @return the taskMode
	 */
	public String getTaskMode() {
		return taskMode;
	}


	/**
	 * @param taskMode the taskMode to set
	 */
	public void setTaskMode(String taskMode) {
		this.taskMode = taskMode;
	}


	/**
	 * @return the taskInfo
	 */
	public String getTaskInfo() {
		return taskInfo;
	}


	/**
	 * @param taskInfo the taskInfo to set
	 */
	public void setTaskInfo(String taskInfo) {
		this.taskInfo = taskInfo;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RBTBulkUploadTask[actInfo=");
		builder.append(actInfo);
		builder.append(", activatedBy=");
		builder.append(activatedBy);
		builder.append(", activationClass=");
		builder.append(activationClass);
		builder.append(", circleId=");
		builder.append(circleId);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", processTime=");
		builder.append(processTime);
		builder.append(", selectionClass=");
		builder.append(selectionClass);
		builder.append(", selectionType=");
		builder.append(selectionType);
		builder.append(", taskId=");
		builder.append(taskId);
		builder.append(", taskInfo=");
		builder.append(taskInfo);
		builder.append(", taskMode=");
		builder.append(taskMode);
		builder.append(", taskName=");
		builder.append(taskName);
		builder.append(", taskStatus=");
		builder.append(taskStatus);
		builder.append(", taskType=");
		builder.append(taskType);
		builder.append(", uploadTime=");
		builder.append(uploadTime);
		builder.append("]");
		return builder.toString();
	}

	


}
