package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ParametersBean used by hibernate to persist into the RBT_COS_DETAILS table.
 * 
 * @author bikash.panda
 */
public class CosDetails implements Serializable
{
	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 26754387765432L;

	private String subscriptionClass;
	private String cosId;
	private Timestamp startDate;
	private Timestamp endDate;
	private String circleId;
	private String prepaidYes;
	private String freechargeClass;
	private int validDays;
	private int freeSongs;
	private int freeMusicboxes;
	private String renewalAllowed;
	private String acceptRenewal;
	private int categoryId;
	private String renewalCosid;
	private String activationPrompt;
	private String selectionPrompt;
	private String smspromoClips;
	private int numsubscriptionAllowed;
	private String isDefault;
	private String accessMode;
	private String smsKeyword;
	private String operator;
	private String cosType;
	private String contentTypes;

	/**
	 * 
	 */
	public CosDetails()
	{

	}

	/**
	 * @param subscriptionClass
	 * @param cosId
	 * @param startDate
	 * @param endDate
	 * @param circleId
	 * @param prepaidYes
	 * @param freechargeClass
	 * @param validDays
	 * @param freeSongs
	 * @param freeMusicboxes
	 * @param renewalAllowed
	 * @param acceptRenewal
	 * @param categoryId
	 * @param renewalCosid
	 * @param activationPrompt
	 * @param selectionPrompt
	 * @param smspromoClips
	 * @param numsubscriptionAllowed
	 * @param isDefault
	 * @param accessMode
	 * @param smsKeyword
	 * @param operator
	 * @param cosType
	 */
	public CosDetails(String subscriptionClass, String cosId,
			Timestamp startDate, Timestamp endDate, String circleId,
			String prepaidYes, String freechargeClass, int validDays,
			int freeSongs, int freeMusicboxes, String renewalAllowed,
			String acceptRenewal, int categoryId, String renewalCosid,
			String activationPrompt, String selectionPrompt,
			String smspromoClips, int numsubscriptionAllowed, String isDefault,
			String accessMode, String smsKeyword, String operator,
			String cosType)
	{
		this.subscriptionClass = subscriptionClass;
		this.cosId = cosId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.circleId = circleId;
		this.prepaidYes = prepaidYes;
		this.freechargeClass = freechargeClass;
		this.validDays = validDays;
		this.freeSongs = freeSongs;
		this.freeMusicboxes = freeMusicboxes;
		this.renewalAllowed = renewalAllowed;
		this.acceptRenewal = acceptRenewal;
		this.categoryId = categoryId;
		this.renewalCosid = renewalCosid;
		this.activationPrompt = activationPrompt;
		this.selectionPrompt = selectionPrompt;
		this.smspromoClips = smspromoClips;
		this.numsubscriptionAllowed = numsubscriptionAllowed;
		this.isDefault = isDefault;
		this.accessMode = accessMode;
		this.smsKeyword = smsKeyword;
		this.operator = operator;
		this.cosType = cosType;
	}

	/**
	 * @return the serialVersionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}

	/**
	 * @return the subscriptionClass
	 */
	public String getSubscriptionClass()
	{
		return subscriptionClass;
	}

	/**
	 * @param subscriptionClass the subscriptionClass to set
	 */
	public void setSubscriptionClass(String subscriptionClass)
	{
		this.subscriptionClass = subscriptionClass;
	}

	/**
	 * @return the cosId
	 */
	public String getCosId()
	{
		return cosId;
	}

	/**
	 * @param cosId the cosId to set
	 */
	public void setCosId(String cosId)
	{
		this.cosId = cosId;
	}

	/**
	 * @return the startDate
	 */
	public Timestamp getStartDate()
	{
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Timestamp startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Timestamp getEndDate()
	{
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Timestamp endDate)
	{
		this.endDate = endDate;
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
	 * @return the prepaidYes
	 */
	public String getPrepaidYes()
	{
		return prepaidYes;
	}

	/**
	 * @param prepaidYes the prepaidYes to set
	 */
	public void setPrepaidYes(String prepaidYes)
	{
		this.prepaidYes = prepaidYes;
	}

	/**
	 * @return the freechargeClass
	 */
	public String getFreechargeClass()
	{
		return freechargeClass;
	}

	/**
	 * @param freechargeClass the freechargeClass to set
	 */
	public void setFreechargeClass(String freechargeClass)
	{
		this.freechargeClass = freechargeClass;
	}

	/**
	 * @return the validDays
	 */
	public int getValidDays()
	{
		return validDays;
	}

	/**
	 * @param validDays the validDays to set
	 */
	public void setValidDays(int validDays)
	{
		this.validDays = validDays;
	}

	/**
	 * @return the freeSongs
	 */
	public int getFreeSongs()
	{
		return freeSongs;
	}

	/**
	 * @param freeSongs the freeSongs to set
	 */
	public void setFreeSongs(int freeSongs)
	{
		this.freeSongs = freeSongs;
	}

	/**
	 * @return the freeMusicboxes
	 */
	public int getFreeMusicboxes()
	{
		return freeMusicboxes;
	}

	/**
	 * @param freeMusicboxes the freeMusicboxes to set
	 */
	public void setFreeMusicboxes(int freeMusicboxes)
	{
		this.freeMusicboxes = freeMusicboxes;
	}

	/**
	 * @return the renewalAllowed
	 */
	public String getRenewalAllowed()
	{
		return renewalAllowed;
	}

	/**
	 * @param renewalAllowed the renewalAllowed to set
	 */
	public void setRenewalAllowed(String renewalAllowed)
	{
		this.renewalAllowed = renewalAllowed;
	}

	/**
	 * @return the acceptRenewal
	 */
	public String getAcceptRenewal()
	{
		return acceptRenewal;
	}

	/**
	 * @param acceptRenewal the acceptRenewal to set
	 */
	public void setAcceptRenewal(String acceptRenewal)
	{
		this.acceptRenewal = acceptRenewal;
	}

	/**
	 * @return the categoryId
	 */
	public int getCategoryId()
	{
		return categoryId;
	}

	/**
	 * @param categoryId the categoryId to set
	 */
	public void setCategoryId(int categoryId)
	{
		this.categoryId = categoryId;
	}

	/**
	 * @return the renewalCosid
	 */
	public String getRenewalCosid()
	{
		return renewalCosid;
	}

	/**
	 * @param renewalCosid the renewalCosid to set
	 */
	public void setRenewalCosid(String renewalCosid)
	{
		this.renewalCosid = renewalCosid;
	}

	/**
	 * @return the activationPrompt
	 */
	public String getActivationPrompt()
	{
		return activationPrompt;
	}

	/**
	 * @param activationPrompt the activationPrompt to set
	 */
	public void setActivationPrompt(String activationPrompt)
	{
		this.activationPrompt = activationPrompt;
	}

	/**
	 * @return the selectionPrompt
	 */
	public String getSelectionPrompt()
	{
		return selectionPrompt;
	}

	/**
	 * @param selectionPrompt the selectionPrompt to set
	 */
	public void setSelectionPrompt(String selectionPrompt)
	{
		this.selectionPrompt = selectionPrompt;
	}

	/**
	 * @return the smspromoClips
	 */
	public String getSmspromoClips()
	{
		return smspromoClips;
	}

	/**
	 * @param smspromoClips the smspromoClips to set
	 */
	public void setSmspromoClips(String smspromoClips)
	{
		this.smspromoClips = smspromoClips;
	}

	/**
	 * @return the numsubscriptionAllowed
	 */
	public int getNumsubscriptionAllowed()
	{
		return numsubscriptionAllowed;
	}

	/**
	 * @param numsubscriptionAllowed the numsubscriptionAllowed to set
	 */
	public void setNumsubscriptionAllowed(int numsubscriptionAllowed)
	{
		this.numsubscriptionAllowed = numsubscriptionAllowed;
	}

	/**
	 * @return the isDefault
	 */
	public String getIsDefault()
	{
		return isDefault;
	}

	/**
	 * @param isDefault the isDefault to set
	 */
	public void setIsDefault(String isDefault)
	{
		this.isDefault = isDefault;
	}

	/**
	 * @return the accessMode
	 */
	public String getAccessMode()
	{
		return accessMode;
	}

	/**
	 * @param accessMode the accessMode to set
	 */
	public void setAccessMode(String accessMode)
	{
		this.accessMode = accessMode;
	}

	/**
	 * @return the smsKeyword
	 */
	public String getSmsKeyword()
	{
		return smsKeyword;
	}

	/**
	 * @param smsKeyword the smsKeyword to set
	 */
	public void setSmsKeyword(String smsKeyword)
	{
		this.smsKeyword = smsKeyword;
	}

	/**
	 * @return the operator
	 */
	public String getOperator()
	{
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	/**
	 * @return the cosType
	 */
	public String getCosType()
	{
		return cosType;
	}

	/**
	 * @param cosType the cosType to set
	 */
	public void setCosType(String cosType)
	{
		this.cosType = cosType;
	}

	public boolean prepaidYes()
	{
		return prepaidYes.equalsIgnoreCase("y");
	}

	public boolean renewalAllowed()
	{
		return renewalAllowed.equalsIgnoreCase("y");
	}

	public boolean acceptRenewal()
	{
		return acceptRenewal.equalsIgnoreCase("y");
	}

	public boolean isDefaultCos()
	{
		return isDefault.equalsIgnoreCase("y");
	}

	/**
	 * @param contentTypes the contentTypes to set
	 */
	public void setContentTypes(String contentTypes)
	{
		this.contentTypes = contentTypes;
	}
	
	/**
	 * @return the contentTypes
	 */
	public String getContentTypes() 
	{
		return contentTypes;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CosDetails[acceptRenewal = ");
		builder.append(acceptRenewal);
		builder.append(", accessMode = ");
		builder.append(accessMode);
		builder.append(", activationPrompt = ");
		builder.append(activationPrompt);
		builder.append(", categoryId = ");
		builder.append(categoryId);
		builder.append(", circleId = ");
		builder.append(circleId);
		builder.append(", cosId = ");
		builder.append(cosId);
		builder.append(", cosType = ");
		builder.append(cosType);
		builder.append(", endDate = ");
		builder.append(endDate);
		builder.append(", freeMusicboxes = ");
		builder.append(freeMusicboxes);
		builder.append(", freeSongs = ");
		builder.append(freeSongs);
		builder.append(", freechargeClass = ");
		builder.append(freechargeClass);
		builder.append(", isDefault = ");
		builder.append(isDefault);
		builder.append(", numsubscriptionAllowed = ");
		builder.append(numsubscriptionAllowed);
		builder.append(", operator = ");
		builder.append(operator);
		builder.append(", prepaidYes = ");
		builder.append(prepaidYes);
		builder.append(", renewalAllowed = ");
		builder.append(renewalAllowed);
		builder.append(", renewalCosid = ");
		builder.append(renewalCosid);
		builder.append(", selectionPrompt = ");
		builder.append(selectionPrompt);
		builder.append(", smsKeyword = ");
		builder.append(smsKeyword);
		builder.append(", smspromoClips = ");
		builder.append(smspromoClips);
		builder.append(", startDate = ");
		builder.append(startDate);
		builder.append(", subscriptionClass = ");
		builder.append(subscriptionClass);
		builder.append(", validDays = ");
		builder.append(validDays);
		builder.append(", contentTypes = ");
		builder.append(contentTypes);
		builder.append("]");
		return builder.toString();
	}
}
