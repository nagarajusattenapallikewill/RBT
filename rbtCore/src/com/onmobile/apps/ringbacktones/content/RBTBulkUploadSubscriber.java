/**
 * 
 */
package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/**
 * @author vasipalli.sreenadh
 *
 */
public class RBTBulkUploadSubscriber 
{
	private int taskId;
	private String subscriberId = null;
	private Character subscriberType ;
	private String circleId = null;
	private String contentId = null;
	private int status = 0;
	private String reason = null;
	private Date processTime = null;
	
	
	public RBTBulkUploadSubscriber()
	{
		
	}


	/**
	 * @param taskId
	 * @param subscriberId
	 * @param prepaidYes
	 * @param circleId
	 * @param contentId
	 * @param status
	 * @param reason
	 * @param processTime
	 */
	public RBTBulkUploadSubscriber(int taskId, String subscriberId,
			Character subscriberType, String circleId, String contentId,
			int status, String reason, Date processTime) {
		super();
		this.taskId = taskId;
		this.subscriberId = subscriberId;
		this.subscriberType = subscriberType;
		this.circleId = circleId;
		this.contentId = contentId;
		this.status = status;
		this.reason = reason;
		this.processTime = processTime;
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
	 * @return the subscriberId
	 */
	public String getSubscriberId() {
		return subscriberId;
	}


	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}


	/**
	 * @return the subscriberType
	 */
	public Character getSubscriberType() {
		return subscriberType;
	}


	/**
	 * @param subscriberType the subscriberType to set
	 */
	public void setSubscriberType(Character subscriberType) {
		this.subscriberType = subscriberType;
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
	 * @return the contentId
	 */
	public String getContentId() {
		return contentId;
	}


	/**
	 * @param contentId the contentId to set
	 */
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}


	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}


	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}


	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
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


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RBTBulkUploadSubscriber[circleId=");
		builder.append(circleId);
		builder.append(", contentId=");
		builder.append(contentId);
		builder.append(", subscriberType=");
		builder.append(subscriberType);
		builder.append(", processTime=");
		builder.append(processTime);
		builder.append(", reason=");
		builder.append(reason);
		builder.append(", status=");
		builder.append(status);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", taskId=");
		builder.append(taskId);
		builder.append("]");
		return builder.toString();
	}
	
	
	
	
}
