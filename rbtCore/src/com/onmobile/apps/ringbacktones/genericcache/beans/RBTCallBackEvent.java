package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.dao.DomainObject;

public class RBTCallBackEvent extends DomainObject
{
	private static Logger logger = Logger.getLogger(RBTCallBackEvent.class);
	
	private long sequenceID;
	private String subscriberID = null;
	private int moduleID;
	private int eventType;
	private int clipID;
	private String classType = null;
	private String selectedBy = null;
	private String selectionInfo = null;
	private String message = null;
	
	public static final Integer MODULE_ID_AD2C = 10;
	public static final Integer AD2C_PENDING_CALLBACK = 0;
	public static final Integer AD2C_TO_BE_SENT = 1;
	
	public static final Integer MODULE_ID_IBM_INTEGRATION = 11;
	public static final Integer SM_CALLBACK_PENDING    = 0;
	public static final Integer SM_SUCCESS_CALLBACK_RECEIVED   = 1;
	public static final Integer SM_FAILURE_CALLBACK_RECEIVED   = 2;
//	public static final Integer SUCCESS_STATUS_UPDATED = 3;
//	public static final Integer FAILURE_STATUS_UPDATED = 4;
	
	
	public RBTCallBackEvent() 
	{
		
	}	
	/**
	 * @param subscriberID
	 * @param moduleID
	 * @param eventType
	 * @param clipID
	 * @param classType
	 * @param selectedBy
	 * @param selectionInfo
	 * @param message
	 */
	public RBTCallBackEvent(String subscriberID, int moduleID, int eventType,
			int clipID, String classType, String selectedBy,
			String selectionInfo, String message) {
		this.subscriberID = subscriberID;
		this.moduleID = moduleID;
		this.eventType = eventType;
		this.clipID = clipID;
		this.classType = classType;
		this.selectedBy = selectedBy;
		this.selectionInfo = selectionInfo;
		this.message = message;
	}



	/**
	 * @return the sequenceID
	 */
	public long getSequenceID() {
		return sequenceID;
	}
	/**
	 * @param sequenceID the sequenceID to set
	 */
	public void setSequenceID(long sequenceID) {
		this.sequenceID = sequenceID;
	}
	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID() {
		return subscriberID;
	}
	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}
	/**
	 * @return the moduleID
	 */
	public int getModuleID() {
		return moduleID;
	}
	/**
	 * @param moduleID the moduleID to set
	 */
	public void setModuleID(int moduleID) {
		this.moduleID = moduleID;
	}
	/**
	 * @return the eventType
	 */
	public int getEventType() {
		return eventType;
	}
	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	/**
	 * @return the clipID
	 */
	public int getClipID() {
		return clipID;
	}
	/**
	 * @param clipID the clipID to set
	 */
	public void setClipID(int clipID) {
		this.clipID = clipID;
	}
	/**
	 * @return the classType
	 */
	public String getClassType() {
		return classType;
	}
	/**
	 * @param classType the classType to set
	 */
	public void setClassType(String classType) {
		this.classType = classType;
	}
	/**
	 * @return the selectedBy
	 */
	public String getSelectedBy() {
		return selectedBy;
	}
	/**
	 * @param selectedBy the selectedBy to set
	 */
	public void setSelectedBy(String selectedBy) {
		this.selectedBy = selectedBy;
	}
	/**
	 * @return the selectionInfo
	 */
	public String getSelectionInfo() {
		return selectionInfo;
	}
	/**
	 * @param selectionInfo the selectionInfo to set
	 */
	public void setSelectionInfo(String selectionInfo) {
		this.selectionInfo = selectionInfo;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RBTCallBackEvent[classType=");
		builder.append(classType);
		builder.append(", clipID=");
		builder.append(clipID);
		builder.append(", eventType=");
		builder.append(eventType);
		builder.append(", message=");
		builder.append(message);
		builder.append(", moduleID=");
		builder.append(moduleID);
		builder.append(", selectedBy=");
		builder.append(selectedBy);
		builder.append(", selectionInfo=");
		builder.append(selectionInfo);
		builder.append(", sequenceID=");
		builder.append(sequenceID);
		builder.append(", subscriberID=");
		builder.append(subscriberID);
		builder.append("]");
		return builder.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classType == null) ? 0 : classType.hashCode());
		result = prime * result + clipID;
		result = prime * result + eventType;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + moduleID;
		result = prime * result
				+ ((selectedBy == null) ? 0 : selectedBy.hashCode());
		result = prime * result
				+ ((selectionInfo == null) ? 0 : selectionInfo.hashCode());
		result = prime * result + (int) (sequenceID ^ (sequenceID >>> 32));
		result = prime * result
				+ ((subscriberID == null) ? 0 : subscriberID.hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	//@Override
	public boolean equals1(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RBTCallBackEvent other = (RBTCallBackEvent) obj;
		if (classType == null) {
			if (other.classType != null)
				return false;
		} else if (!classType.equals(other.classType))
			return false;
		if (clipID != other.clipID)
			return false;
		if (eventType != other.eventType)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (moduleID != other.moduleID)
			return false;
		if (selectedBy == null) {
			if (other.selectedBy != null)
				return false;
		} else if (!selectedBy.equals(other.selectedBy))
			return false;
		if (selectionInfo == null) {
			if (other.selectionInfo != null)
				return false;
		} else if (!selectionInfo.equals(other.selectionInfo))
			return false;
		if (sequenceID != other.sequenceID)
			return false;
		if (subscriberID == null) {
			if (other.subscriberID != null)
				return false;
		} else if (!subscriberID.equals(other.subscriberID))
			return false;
		return true;
	}
	
	
	@Override
	public Long getId()
	{
		return sequenceID;
	}
	@Override
	public void setId(Long id) 
	{
		sequenceID = id;
	}
	
	public void createCallbackEvent(RBTCallBackEvent rbtCallBackEvent)
	{
		try
		{
			rbtCallBackEvent.insert();
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}
	
	public void deleteCallbackEvent(RBTCallBackEvent rbtCallBackEvent)
	{
		rbtCallBackEvent.delete();
	}
	
	public RBTCallBackEvent getCallbackEvent(long sequenceID)
	{
		String sql = "SELECT * FROM RBT_CALL_BACK_EVENT WHERE SEQUENCE_ID="+sequenceID;
		List<RBTCallBackEvent> list = load(RBTCallBackEvent.class, sql);
		
		return (list == null ? null : list.get(0));
		
	}
	
	public List<RBTCallBackEvent> getCallbackEventsOfModule(int moduleID, int eventType)
	{
		String sql = "SELECT * FROM RBT_CALL_BACK_EVENT WHERE MODULE_ID="+moduleID +" AND EVENT_TYPE ="+eventType ;
		List<RBTCallBackEvent> list = load(RBTCallBackEvent.class, sql);
		return list;
		
	}
	
	/**
	 * Method to update a record in RBT_CALL_BACK_EVENT based on selectionInfo (refId), moduleId and the subscriberId. Done for RBT-10520.
	 * @author rony.gregory
	 * @param moduleId
	 * @param subscriberId
	 * @param selectionInfo refId is passed in this variable
	 * @param eventType
	 * @param classType
	 * @return the number of records updated.
	 */
	public static boolean update (Integer moduleId, String subscriberId, String selectionInfo, Integer eventType, String classType) {
		String sql = "SELECT * FROM RBT_CALL_BACK_EVENT WHERE MODULE_ID='"
				+ moduleId + "' AND SELECTION_INFO='" + selectionInfo
				+ "' AND SUBSCRIBER_ID='" + subscriberId + "'";
		RBTCallBackEvent rbtCallBackEvent = loadSingle(RBTCallBackEvent.class, sql);
		boolean success = false;
		if (rbtCallBackEvent != null) {
			rbtCallBackEvent.setEventType(eventType);
			rbtCallBackEvent.setClassType(classType);
			rbtCallBackEvent.update();
			logger.info("Record updated in RBT_CALL_BACK_EVENT for selectionInfo = '"
					+ selectionInfo
					+ "', moduleId = '"
					+ moduleId
					+ "' and  subscriberId = '"
					+ subscriberId
					+ "' with eventType = '"
					+ eventType
					+ "' and classType = '"
					+ classType + "'");
			success = true;
		} else {
			logger.info("No records peresent in RBT_CALL_BACK_EVENT for selectionInfo = "
					+ selectionInfo
					+ ", moduleId = "
					+ moduleId
					+ ", subscriberId = " + subscriberId);
		}
		return success;
	}
	
	public static boolean update (String subscriberId, String oldSelectionInfo, String newSelectionInfo) {
		String sql = "SELECT * FROM RBT_CALL_BACK_EVENT WHERE SELECTION_INFO='" + oldSelectionInfo
				+ "' AND SUBSCRIBER_ID='" + subscriberId + "'";
		RBTCallBackEvent rbtCallBackEvent = loadSingle(RBTCallBackEvent.class, sql);
		boolean success = false;
		if (rbtCallBackEvent != null) {
			rbtCallBackEvent.setSelectionInfo(newSelectionInfo);
			rbtCallBackEvent.update();
			logger.info("Record updated in RBT_CALL_BACK_EVENT for selectionInfo = '"
					+ oldSelectionInfo
					+ "' and  subscriberId = '"
					+ subscriberId
					+ "' with selectionInfo = '"
					+ newSelectionInfo + "'");
			success = true;
		} else {
			logger.info("No records peresent in RBT_CALL_BACK_EVENT for selectionInfo = "
					+ oldSelectionInfo
					+ ", subscriberId = " + subscriberId);
		}
		return success;
	}

	public static boolean updateMessage (String subscriberId, String refId, String msg) {
		String sql = "SELECT * FROM RBT_CALL_BACK_EVENT WHERE SELECTION_INFO='" + refId
				+ "' AND SUBSCRIBER_ID='" + subscriberId + "'";
		RBTCallBackEvent rbtCallBackEvent = loadSingle(RBTCallBackEvent.class, sql);
		boolean success = false;
		if (rbtCallBackEvent != null) {
			rbtCallBackEvent.setMessage(msg);
			rbtCallBackEvent.update();
			logger.info("Record updated in RBT_CALL_BACK_EVENT for selectionInfo = '"
					+ refId
					+ "' and  subscriberId = '"
					+ subscriberId
					+ "' with Message = '"
					+ msg + "'");
			success = true;
		} else {
			logger.info("No records peresent in RBT_CALL_BACK_EVENT for selectionInfo = "
					+ refId
					+ ", subscriberId = " + subscriberId);
		}
		return success;
	}
	
	public static boolean insert(String subscriberId, String refId, String msg, int eventType,
			int moduleId, int clipId, String mode) {
		RBTCallBackEvent rbtCallBackEvent = new RBTCallBackEvent();
		rbtCallBackEvent.setClipID(clipId);
		rbtCallBackEvent.setEventType(eventType);
		rbtCallBackEvent.setMessage(msg);
		rbtCallBackEvent.setModuleID(moduleId);
		rbtCallBackEvent.setSelectedBy(mode);
		rbtCallBackEvent.setSelectionInfo(refId);
		rbtCallBackEvent.setSubscriberID(subscriberId);
		rbtCallBackEvent.insert();
		logger.info("Record inserted in RBT_CALL_BACK_EVENT for RBTCallBackEvent = "
				+ rbtCallBackEvent);
		return true;
	}

	/*public static void main(String args[]) {
		update(RBTCallBackEvent.AD2C_TO_BE_SENT, 
				"price=10.50", "ce06e079-7d7b-4bc4-937b-7c6796e72ad0", RBTCallBackEvent.MODULE_ID_AD2C, "9845123963");
	}*/
}