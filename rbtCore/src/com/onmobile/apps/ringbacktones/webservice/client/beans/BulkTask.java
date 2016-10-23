package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;
import java.util.HashMap;

public class BulkTask 
{
	private int taskId;
	private String taskName = null;
	private String circleId = null;
	private String activationClass = null;
	private String selectionClass = null;
	private int selectionType;   			
	private String taskType;	 			 
	private String activatedBy = null;
	private String actInfo = null;     		
	private Date uploadTime = null;
	private Date processTime = null;
	private Date endTime = null;
	private int taskStatus = 0;
	private String taskMode = null;
	private HashMap<String, String> taskInfo = null;

	/**
	 * 
	 */
	public BulkTask()
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
	public BulkTask(int taskId, String taskName, String circleId,
			String activationClass, String selectionClass, int selectionType,
			String taskType, String activatedBy, String actInfo,
			Date uploadTime, Date processTime, Date endTime, int taskStatus,
			String taskMode, HashMap<String, String> taskInfo) {
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
	public int getTaskId()
	{
		return taskId;
	}

	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(int taskId)
	{
		this.taskId = taskId;
	}

	/**
	 * @return the taskName
	 */
	public String getTaskName()
	{
		return taskName;
	}

	/**
	 * @param taskName the taskName to set
	 */
	public void setTaskName(String taskName)
	{
		this.taskName = taskName;
	}

	/**
	 * @return the circleId
	 */
	public String getCircleId()
	{
		return circleId;
	}

	/**
	 * @param circleId the circleId to set
	 */
	public void setCircleId(String circleId)
	{
		this.circleId = circleId;
	}

	/**
	 * @return the activationClass
	 */
	public String getActivationClass()
	{
		return activationClass;
	}

	/**
	 * @param activationClass the activationClass to set
	 */
	public void setActivationClass(String activationClass)
	{
		this.activationClass = activationClass;
	}

	/**
	 * @return the selectionClass
	 */
	public String getSelectionClass()
	{
		return selectionClass;
	}

	/**
	 * @param selectionClass the selectionClass to set
	 */
	public void setSelectionClass(String selectionClass)
	{
		this.selectionClass = selectionClass;
	}

	/**
	 * @return the selectionType
	 */
	public int getSelectionType()
	{
		return selectionType;
	}

	/**
	 * @param selectionType the selectionType to set
	 */
	public void setSelectionType(int selectionType)
	{
		this.selectionType = selectionType;
	}

	/**
	 * @return the taskType
	 */
	public String getTaskType()
	{
		return taskType;
	}

	/**
	 * @param taskType the taskType to set
	 */
	public void setTaskType(String taskType)
	{
		this.taskType = taskType;
	}

	/**
	 * @return the activatedBy
	 */
	public String getActivatedBy()
	{
		return activatedBy;
	}

	/**
	 * @param activatedBy the activatedBy to set
	 */
	public void setActivatedBy(String activatedBy)
	{
		this.activatedBy = activatedBy;
	}

	/**
	 * @return the actInfo
	 */
	public String getActInfo()
	{
		return actInfo;
	}

	/**
	 * @param actInfo the actInfo to set
	 */
	public void setActInfo(String actInfo)
	{
		this.actInfo = actInfo;
	}

	/**
	 * @return the uploadTime
	 */
	public Date getUploadTime()
	{
		return uploadTime;
	}

	/**
	 * @param uploadTime the uploadTime to set
	 */
	public void setUploadTime(Date uploadTime)
	{
		this.uploadTime = uploadTime;
	}

	/**
	 * @return the processTime
	 */
	public Date getProcessTime()
	{
		return processTime;
	}

	/**
	 * @param processTime the processTime to set
	 */
	public void setProcessTime(Date processTime)
	{
		this.processTime = processTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime()
	{
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime)
	{
		this.endTime = endTime;
	}

	/**
	 * @return the taskStatus
	 */
	public int getTaskStatus()
	{
		return taskStatus;
	}

	/**
	 * @param taskStatus the taskStatus to set
	 */
	public void setTaskStatus(int taskStatus)
	{
		this.taskStatus = taskStatus;
	}

	/**
	 * @return the taskMode
	 */
	public String getTaskMode()
	{
		return taskMode;
	}

	/**
	 * @param taskMode the taskMode to set
	 */
	public void setTaskMode(String taskMode)
	{
		this.taskMode = taskMode;
	}

	/**
	 * @return the taskInfo
	 */
	public HashMap<String, String> getTaskInfo()
	{
		return taskInfo;
	}

	/**
	 * @param taskInfo the taskInfo to set
	 */
	public void setTaskInfo(HashMap<String, String> taskInfo)
	{
		this.taskInfo = taskInfo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actInfo == null) ? 0 : actInfo.hashCode());
		result = prime * result + ((activatedBy == null) ? 0 : activatedBy.hashCode());
		result = prime * result + ((activationClass == null) ? 0 : activationClass.hashCode());
		result = prime * result + ((circleId == null) ? 0 : circleId.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((processTime == null) ? 0 : processTime.hashCode());
		result = prime * result + ((selectionClass == null) ? 0 : selectionClass.hashCode());
		result = prime * result + selectionType;
		result = prime * result + taskId;
		result = prime * result + ((taskInfo == null) ? 0 : taskInfo.hashCode());
		result = prime * result + ((taskMode == null) ? 0 : taskMode.hashCode());
		result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
		result = prime * result + taskStatus;
		result = prime * result + ((taskType == null) ? 0 : taskType.hashCode());
		result = prime * result + ((uploadTime == null) ? 0 : uploadTime.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BulkTask))
			return false;
		BulkTask other = (BulkTask) obj;
		if (actInfo == null)
		{
			if (other.actInfo != null)
				return false;
		}
		else if (!actInfo.equals(other.actInfo))
			return false;
		if (activatedBy == null)
		{
			if (other.activatedBy != null)
				return false;
		}
		else if (!activatedBy.equals(other.activatedBy))
			return false;
		if (activationClass == null)
		{
			if (other.activationClass != null)
				return false;
		}
		else if (!activationClass.equals(other.activationClass))
			return false;
		if (circleId == null)
		{
			if (other.circleId != null)
				return false;
		}
		else if (!circleId.equals(other.circleId))
			return false;
		if (endTime == null)
		{
			if (other.endTime != null)
				return false;
		}
		else if (!endTime.equals(other.endTime))
			return false;
		if (processTime == null)
		{
			if (other.processTime != null)
				return false;
		}
		else if (!processTime.equals(other.processTime))
			return false;
		if (selectionClass == null)
		{
			if (other.selectionClass != null)
				return false;
		}
		else if (!selectionClass.equals(other.selectionClass))
			return false;
		if (selectionType != other.selectionType)
			return false;
		if (taskId != other.taskId)
			return false;
		if (taskInfo == null)
		{
			if (other.taskInfo != null)
				return false;
		}
		else if (!taskInfo.equals(other.taskInfo))
			return false;
		if (taskMode == null)
		{
			if (other.taskMode != null)
				return false;
		}
		else if (!taskMode.equals(other.taskMode))
			return false;
		if (taskName == null)
		{
			if (other.taskName != null)
				return false;
		}
		else if (!taskName.equals(other.taskName))
			return false;
		if (taskStatus != other.taskStatus)
			return false;
		if (taskType == null)
		{
			if (other.taskType != null)
				return false;
		}
		else if (!taskType.equals(other.taskType))
			return false;
		if (uploadTime == null)
		{
			if (other.uploadTime != null)
				return false;
		}
		else if (!uploadTime.equals(other.uploadTime))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("BulkTask[actInfo = ");
		builder.append(actInfo);
		builder.append(", activatedBy = ");
		builder.append(activatedBy);
		builder.append(", activationClass = ");
		builder.append(activationClass);
		builder.append(", circleId = ");
		builder.append(circleId);
		builder.append(", endTime = ");
		builder.append(endTime);
		builder.append(", processTime = ");
		builder.append(processTime);
		builder.append(", selectionClass = ");
		builder.append(selectionClass);
		builder.append(", selectionType = ");
		builder.append(selectionType);
		builder.append(", taskId = ");
		builder.append(taskId);
		builder.append(", taskInfo = ");
		builder.append(taskInfo);
		builder.append(", taskMode = ");
		builder.append(taskMode);
		builder.append(", taskName = ");
		builder.append(taskName);
		builder.append(", taskStatus = ");
		builder.append(taskStatus);
		builder.append(", taskType = ");
		builder.append(taskType);
		builder.append(", uploadTime = ");
		builder.append(uploadTime);
		builder.append("]");
		return builder.toString();
	}
}
