/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vinayasimha.patil
 *
 */
public class BulkSelectionRequest extends SelectionRequest
{
	private String bulkTaskFile = null;
	private Boolean isShuffleSelection = null;
	private String taskID;
	private String taskName = null;
	private Integer taskStatus = 0;
	private String taskType = null;
	private String taskMode = null;
	private String editNumbersInTask = null; // 'add' or 'delete'
	private String sendSms = null;
	private Boolean isPromoID = null;
	private Boolean isSmsAlias = null;

	public BulkSelectionRequest()
	{
		super(null);
	}

	/**
	 * @param bulkTaskFile
	 */
	public BulkSelectionRequest(String bulkTaskFile)
	{
		super(null);
		this.bulkTaskFile = bulkTaskFile;

	}

	/**
	 * @param bulkTaskFile
	 * @param isPrepaid
	 * @param callerID
	 * @param mode
	 * @param modeInfo
	 * @param categoryID
	 * @param clipID
	 * @param fromTime
	 * @param toTime
	 * @param status
	 * @param interval
	 * @param subscriptionClass
	 * @param chargeClass
	 * @param inLoop
	 * @param profileHours
	 * @param cricketPack
	 * @param ignoreActiveUser
	 * @param removeExistingSetting
	 * @param subscriptionPeriod
	 */
	public BulkSelectionRequest(String bulkTaskFile, Boolean isPrepaid,
			String callerID, String mode, String modeInfo, String categoryID,
			String clipID, Integer fromTime, Integer toTime, Integer status,
			String interval, String subscriptionClass, String chargeClass,
			Boolean inLoop, String profileHours, String cricketPack,
			Boolean ignoreActiveUser, Boolean removeExistingSetting,
			String subscriptionPeriod)
	{
		super(null, isPrepaid, callerID, mode, modeInfo, categoryID, clipID,
				fromTime, toTime, status, interval, subscriptionClass,
				chargeClass, inLoop, profileHours, cricketPack,
				ignoreActiveUser, removeExistingSetting, subscriptionPeriod);
		this.bulkTaskFile = bulkTaskFile;
	}

	/**
	 * @return the bulkTaskFile
	 */
	public String getBulkTaskFile()
	{
		return bulkTaskFile;
	}

	/**
	 * @param bulkTaskFile the bulkTaskFile to set
	 */
	public void setBulkTaskFile(String bulkTaskFile)
	{
		this.bulkTaskFile = bulkTaskFile;
	}

	/**
	 * @return the isShuffleSelection
	 */
	public Boolean getIsShuffleSelection()
	{
		return isShuffleSelection;
	}

	/**
	 * @param isShuffleSelection the isShuffleSelection to set
	 */
	public void setIsShuffleSelection(Boolean isShuffleSelection)
	{
		this.isShuffleSelection = isShuffleSelection;
	}

	/**
	 * @return the taskID
	 */
	public String getTaskID()
	{
		return taskID;
	}

	/**
	 * @param taskID the taskID to set
	 */
	public void setTaskID(String taskID)
	{
		this.taskID = taskID;
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
	 * @return the taskStatus
	 */
	public Integer getTaskStatus()
	{
		return taskStatus;
	}

	/**
	 * @param taskStatus the taskStatus to set
	 */
	public void setTaskStatus(Integer taskStatus)
	{
		this.taskStatus = taskStatus;
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
	 * @return the editNumbersInTask
	 */
	public String getEditNumbersInTask()
	{
		return editNumbersInTask;
	}

	/**
	 * @param editNumbersInTask the editNumbersInTask to set
	 */
	public void setEditNumbersInTask(String editNumbersInTask)
	{
		this.editNumbersInTask = editNumbersInTask;
	}

	/**
	 * @return the sendSms
	 */
	public String getSendSms() {
		return sendSms;
	}

	/**
	 * @param sendSms the sendSms to set
	 */
	public void setSendSms(String sendSms) {
		this.sendSms = sendSms;
	}

	/**
	 * @return the isPromoID
	 */
	public Boolean getIsPromoID() {
		return isPromoID;
	}

	/**
	 * @param isPromoID the isPromoID to set
	 */
	public void setIsPromoID(Boolean isPromoID) {
		this.isPromoID = isPromoID;
	}

	/**
	 * @return the isSmsAlias
	 */
	public Boolean getIsSmsAlias() {
		return isSmsAlias;
	}

	/**
	 * @param isSmsAlias the isSmsAlias to set
	 */
	public void setIsSmsAlias(Boolean isSmsAlias) {
		this.isSmsAlias = isSmsAlias;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.SelectionRequest#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (bulkTaskFile != null) requestParams.put(param_bulkTaskFile, bulkTaskFile);
		if (isShuffleSelection != null) requestParams.put(param_isShuffleSelection, (isShuffleSelection ? YES : NO));
		if (taskID != null) requestParams.put(param_taskID, taskID);
		if (taskStatus != null) requestParams.put(param_taskStatus, String.valueOf(taskStatus));
		if (taskName != null) requestParams.put(param_taskName, taskName);
		if (taskType != null) requestParams.put(param_taskType, taskType);
		if (taskMode != null) requestParams.put(param_taskMode, taskMode);
		if (editNumbersInTask != null) requestParams.put(param_editNumbersInTask, editNumbersInTask);
		if (sendSms != null) requestParams.put(param_sendsms, sendSms);
		if (isPromoID != null) requestParams.put(param_isPromoID, (isPromoID ? YES : NO));
		if (isSmsAlias != null) requestParams.put(param_isSmsAlias, (isSmsAlias ? YES : NO));
		
		return requestParams;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.SelectionRequest#prepareRequestParams()
	 */
	@Override
	public void prepareRequestParams(WebServiceContext task)
	{
		super.prepareRequestParams(task);

		if(task.containsKey(param_bulkTaskFile)){
			bulkTaskFile = task.getString(param_bulkTaskFile);
		}
		if(task.containsKey(param_isShuffleSelection)){
			String shuffleSelection = task.getString(param_isShuffleSelection).trim();
			isShuffleSelection = shuffleSelection.equalsIgnoreCase(YES);
		}
		if(task.containsKey(param_taskID)){
			taskID = task.getString(param_taskID);
		}
		if(task.containsKey(param_taskStatus)){
			taskStatus = Integer.parseInt(task.getString(param_taskStatus).trim());
		}
		if(task.containsKey(param_taskName)){
			taskName = task.getString(param_taskName);
		}
		if(task.containsKey(param_taskType)){
			taskType = task.getString(param_taskType);
		}
		if(task.containsKey(param_taskMode)){
			taskMode = task.getString(param_taskMode);
		}
		if(task.containsKey(param_editNumbersInTask)){
			editNumbersInTask = task.getString(param_editNumbersInTask);
		}
		if(task.containsKey(param_sendsms)){
			sendSms = task.getString(param_sendsms);
		}
		if(task.containsKey(param_isPromoID)){
			String promoID = task.getString(param_isPromoID).trim();
			isPromoID = promoID.equalsIgnoreCase(YES);
		}
		if(task.containsKey(param_isSmsAlias)){
			String smsAlias = task.getString(param_isSmsAlias).trim();
			isSmsAlias = smsAlias.equalsIgnoreCase(YES);
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String superString = super.toString();
		superString = superString.substring(superString.indexOf('[') + 1);

		StringBuilder builder = new StringBuilder();
		builder.append("BulkSelectionRequest[bulkTaskFile = ");
		builder.append(bulkTaskFile);
		builder.append(", editNumbersInTask = ");
		builder.append(editNumbersInTask);
		builder.append(", isShuffleSelection = ");
		builder.append(isShuffleSelection);
		builder.append(", taskID = ");
		builder.append(taskID);
		builder.append(", taskMode = ");
		builder.append(taskMode);
		builder.append(", taskName = ");
		builder.append(taskName);
		builder.append(", taskStatus = ");
		builder.append(taskStatus);
		builder.append(", taskType = ");
		builder.append(taskType);
		builder.append(", isPromoID = ");
		builder.append(isPromoID);
		builder.append(", isSmsAlias = ");
		builder.append(isSmsAlias);
		builder.append(", ");

		builder.append(superString);
		return builder.toString();
	}
}
