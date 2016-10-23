package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;

/**
 * ParametersBean used by hibernate to persist into the RBT_SUBSCRIPTION_CLASS
 * table.
 * 
 * @author manish.shringarpure
 */
public class SubscriptionClass implements Serializable, Cloneable
{

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 26754387765432L;

	private String subscriptionClass;
	private String subscriptionAmount;
	private String renewalAmount;
	private String subscriptionPeriod;
	private String renewalPeriod;
	private String subscriptionRenewal;

	private String smsOnSubscription;
	private String smsOnSubscriptionFailure;
	private String smsAlertBeforeRenewal;
	private String smsRenewalSuccess;
	private String smsDeactivationSuccess;

	private int retryPeriod;
	private String operatorCode1;
	private String operatorCode2;
	private String operatorCode3;
	private String operatorCode4;
	private int gracePeriod;
	private String chargeAfterSubscription;

	private String smsRenewalFailure;
	private String smsAlertRetry;
	private String smsAlertGrace;
	private String smsDeactFailure;

	private int autoDeactivationPeriod;
	private String showOnGui;
	private int freeSelections;
	private String circleID;

	/**
	 * 
	 */
	public SubscriptionClass()
	{

	}

	/**
	 * @param subscriptionClass
	 * @param subscriptionAmount
	 * @param renewalAmount
	 * @param subscriptionPeriod
	 * @param renewalPeriod
	 * @param subscriptionRenewal
	 * @param smsOnSubscription
	 * @param smsOnSubscriptionFailure
	 * @param smsAlertBeforeRenewal
	 * @param smsRenewalSuccess
	 * @param smsDeactivationSuccess
	 * @param retryPeriod
	 * @param operatorCode1
	 * @param operatorCode2
	 * @param operatorCode3
	 * @param operatorCode4
	 * @param gracePeriod
	 * @param chargeAfterSubscription
	 * @param smsRenewalFailure
	 * @param smsAlertRetry
	 * @param smsAlertGrace
	 * @param smsDeactFailure
	 * @param autoDeactivationPeriod
	 * @param showOnGui
	 * @param freeSelections
	 * @param circleID
	 */
	public SubscriptionClass(String subscriptionClass,
			String subscriptionAmount, String renewalAmount,
			String subscriptionPeriod, String renewalPeriod,
			String subscriptionRenewal, String smsOnSubscription,
			String smsOnSubscriptionFailure, String smsAlertBeforeRenewal,
			String smsRenewalSuccess, String smsDeactivationSuccess,
			int retryPeriod, String operatorCode1, String operatorCode2,
			String operatorCode3, String operatorCode4, int gracePeriod,
			String chargeAfterSubscription, String smsRenewalFailure,
			String smsAlertRetry, String smsAlertGrace, String smsDeactFailure,
			int autoDeactivationPeriod, String showOnGui, int freeSelections,
			String circleID) {
		this.subscriptionClass = subscriptionClass;
		this.subscriptionAmount = subscriptionAmount;
		this.renewalAmount = renewalAmount;
		this.subscriptionPeriod = subscriptionPeriod;
		this.renewalPeriod = renewalPeriod;
		this.subscriptionRenewal = subscriptionRenewal;
		this.smsOnSubscription = smsOnSubscription;
		this.smsOnSubscriptionFailure = smsOnSubscriptionFailure;
		this.smsAlertBeforeRenewal = smsAlertBeforeRenewal;
		this.smsRenewalSuccess = smsRenewalSuccess;
		this.smsDeactivationSuccess = smsDeactivationSuccess;
		this.retryPeriod = retryPeriod;
		this.operatorCode1 = operatorCode1;
		this.operatorCode2 = operatorCode2;
		this.operatorCode3 = operatorCode3;
		this.operatorCode4 = operatorCode4;
		this.gracePeriod = gracePeriod;
		this.chargeAfterSubscription = chargeAfterSubscription;
		this.smsRenewalFailure = smsRenewalFailure;
		this.smsAlertRetry = smsAlertRetry;
		this.smsAlertGrace = smsAlertGrace;
		this.smsDeactFailure = smsDeactFailure;
		this.autoDeactivationPeriod = autoDeactivationPeriod;
		this.showOnGui = showOnGui;
		this.freeSelections = freeSelections;
		this.circleID = circleID;
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
	 * @return the subscriptionAmount
	 */
	public String getSubscriptionAmount()
	{
		return subscriptionAmount;
	}

	/**
	 * @param subscriptionAmount the subscriptionAmount to set
	 */
	public void setSubscriptionAmount(String subscriptionAmount)
	{
		this.subscriptionAmount = subscriptionAmount;
	}

	/**
	 * @return the renewalAmount
	 */
	public String getRenewalAmount()
	{
		return renewalAmount;
	}

	/**
	 * @param renewalAmount the renewalAmount to set
	 */
	public void setRenewalAmount(String renewalAmount)
	{
		this.renewalAmount = renewalAmount;
	}

	/**
	 * @return the subscriptionPeriod
	 */
	public String getSubscriptionPeriod()
	{
		return subscriptionPeriod;
	}

	/**
	 * @param subscriptionPeriod the subscriptionPeriod to set
	 */
	public void setSubscriptionPeriod(String subscriptionPeriod)
	{
		this.subscriptionPeriod = subscriptionPeriod;
	}

	/**
	 * @return the renewalPeriod
	 */
	public String getRenewalPeriod()
	{
		return renewalPeriod;
	}

	/**
	 * @param renewalPeriod the renewalPeriod to set
	 */
	public void setRenewalPeriod(String renewalPeriod)
	{
		this.renewalPeriod = renewalPeriod;
	}

	/**
	 * @return the subscriptionRenewal
	 */
	public String getSubscriptionRenewal()
	{
		return subscriptionRenewal;
	}

	/**
	 * @param subscriptionRenewal the subscriptionRenewal to set
	 */
	public void setSubscriptionRenewal(String subscriptionRenewal)
	{
		this.subscriptionRenewal = subscriptionRenewal;
	}

	/**
	 * @return the smsOnSubscription
	 */
	public String getSmsOnSubscription()
	{
		return smsOnSubscription;
	}

	/**
	 * @param smsOnSubscription the smsOnSubscription to set
	 */
	public void setSmsOnSubscription(String smsOnSubscription)
	{
		this.smsOnSubscription = smsOnSubscription;
	}

	/**
	 * @return the smsOnSubscriptionFailure
	 */
	public String getSmsOnSubscriptionFailure()
	{
		return smsOnSubscriptionFailure;
	}

	/**
	 * @param smsOnSubscriptionFailure the smsOnSubscriptionFailure to set
	 */
	public void setSmsOnSubscriptionFailure(String smsOnSubscriptionFailure)
	{
		this.smsOnSubscriptionFailure = smsOnSubscriptionFailure;
	}

	/**
	 * @return the smsAlertBeforeRenewal
	 */
	public String getSmsAlertBeforeRenewal()
	{
		return smsAlertBeforeRenewal;
	}

	/**
	 * @param smsAlertBeforeRenewal the smsAlertBeforeRenewal to set
	 */
	public void setSmsAlertBeforeRenewal(String smsAlertBeforeRenewal)
	{
		this.smsAlertBeforeRenewal = smsAlertBeforeRenewal;
	}

	/**
	 * @return the smsRenewalSuccess
	 */
	public String getSmsRenewalSuccess()
	{
		return smsRenewalSuccess;
	}

	/**
	 * @param smsRenewalSuccess the smsRenewalSuccess to set
	 */
	public void setSmsRenewalSuccess(String smsRenewalSuccess)
	{
		this.smsRenewalSuccess = smsRenewalSuccess;
	}

	/**
	 * @return the retryPeriod
	 */
	public int getRetryPeriod()
	{
		return retryPeriod;
	}

	/**
	 * @param retryPeriod the retryPeriod to set
	 */
	public void setRetryPeriod(int retryPeriod)
	{
		this.retryPeriod = retryPeriod;
	}

	/**
	 * @return the operatorCode1
	 */
	public String getOperatorCode1()
	{
		return operatorCode1;
	}

	/**
	 * @param operatorCode1 the operatorCode1 to set
	 */
	public void setOperatorCode1(String operatorCode1)
	{
		this.operatorCode1 = operatorCode1;
	}

	/**
	 * @return the operatorCode2
	 */
	public String getOperatorCode2()
	{
		return operatorCode2;
	}

	/**
	 * @param operatorCode2 the operatorCode2 to set
	 */
	public void setOperatorCode2(String operatorCode2)
	{
		this.operatorCode2 = operatorCode2;
	}

	/**
	 * @return the operatorCode3
	 */
	public String getOperatorCode3()
	{
		return operatorCode3;
	}

	/**
	 * @param operatorCode3 the operatorCode3 to set
	 */
	public void setOperatorCode3(String operatorCode3)
	{
		this.operatorCode3 = operatorCode3;
	}

	/**
	 * @return the operatorCode4
	 */
	public String getOperatorCode4()
	{
		return operatorCode4;
	}

	/**
	 * @param operatorCode4 the operatorCode4 to set
	 */
	public void setOperatorCode4(String operatorCode4)
	{
		this.operatorCode4 = operatorCode4;
	}

	/**
	 * @return the gracePeriod
	 */
	public int getGracePeriod()
	{
		return gracePeriod;
	}

	/**
	 * @param gracePeriod the gracePeriod to set
	 */
	public void setGracePeriod(int gracePeriod)
	{
		this.gracePeriod = gracePeriod;
	}

	/**
	 * @return the chargeAfterSubscription
	 */
	public String getChargeAfterSubscription()
	{
		return chargeAfterSubscription;
	}

	/**
	 * @param chargeAfterSubscription the chargeAfterSubscription to set
	 */
	public void setChargeAfterSubscription(String chargeAfterSubscription)
	{
		this.chargeAfterSubscription = chargeAfterSubscription;
	}

	/**
	 * @return the smsRenewalFailure
	 */
	public String getSmsRenewalFailure()
	{
		return smsRenewalFailure;
	}

	/**
	 * @param smsRenewalFailure the smsRenewalFailure to set
	 */
	public void setSmsRenewalFailure(String smsRenewalFailure)
	{
		this.smsRenewalFailure = smsRenewalFailure;
	}

	/**
	 * @return the smsAlertRetry
	 */
	public String getSmsAlertRetry()
	{
		return smsAlertRetry;
	}

	/**
	 * @param smsAlertRetry the smsAlertRetry to set
	 */
	public void setSmsAlertRetry(String smsAlertRetry)
	{
		this.smsAlertRetry = smsAlertRetry;
	}

	/**
	 * @return the smsAlertGrace
	 */
	public String getSmsAlertGrace()
	{
		return smsAlertGrace;
	}

	/**
	 * @param smsAlertGrace the smsAlertGrace to set
	 */
	public void setSmsAlertGrace(String smsAlertGrace)
	{
		this.smsAlertGrace = smsAlertGrace;
	}

	/**
	 * @return the smsDeactFailure
	 */
	public String getSmsDeactFailure()
	{
		return smsDeactFailure;
	}

	/**
	 * @param smsDeactFailure the smsDeactFailure to set
	 */
	public void setSmsDeactFailure(String smsDeactFailure)
	{
		this.smsDeactFailure = smsDeactFailure;
	}

	/**
	 * @return the autoDeactivationPeriod
	 */
	public int getAutoDeactivationPeriod()
	{
		return autoDeactivationPeriod;
	}

	/**
	 * @param autoDeactivationPeriod the autoDeactivationPeriod to set
	 */
	public void setAutoDeactivationPeriod(int autoDeactivationPeriod)
	{
		this.autoDeactivationPeriod = autoDeactivationPeriod;
	}

	/**
	 * @return the showOnGui
	 */
	public String getShowOnGui()
	{
		return showOnGui;
	}

	/**
	 * @param showOnGui the showOnGui to set
	 */
	public void setShowOnGui(String showOnGui)
	{
		this.showOnGui = showOnGui;
	}

	/**
	 * @return the freeSelections
	 */
	public int getFreeSelections()
	{
		return freeSelections;
	}

	/**
	 * @param freeSelections the freeSelections to set
	 */
	public void setFreeSelections(int freeSelections)
	{
		this.freeSelections = freeSelections;
	}

	/**
	 * @return the circleID
	 */
	public String getCircleID()
	{
		return circleID;
	}

	/**
	 * @param circleID the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @return the serialversionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}

	public boolean subscriptionRenewal()
	{
		if(subscriptionRenewal != null)
			return subscriptionRenewal.equalsIgnoreCase("y");
		return false;
	}

	public boolean showOnGui()
	{
		if(showOnGui != null)
			return showOnGui.equalsIgnoreCase("y");
		return false;
	}

	public boolean isDeactivationNotAllowed()
	{
		if (chargeAfterSubscription != null)
			return chargeAfterSubscription.equalsIgnoreCase("y");
		return false;
	}


	/**
	 * @return the subscriptionPeriodInDays
	 */
	public int getSubscriptionPeriodInDays()
	{
		if (subscriptionPeriod.startsWith("D"))
			return Integer.parseInt(subscriptionPeriod.substring(1));
		else if (subscriptionPeriod.startsWith("M"))
			return (Integer.parseInt(subscriptionPeriod.substring(1)) * 30);
		return Integer.parseInt(subscriptionPeriod);
	}
	
	/**
	 * @return the smsDeactivationSuccess
	 */
	public String getSmsDeactivationSuccess() {
		return smsDeactivationSuccess;
	}

	/**
	 * @param smsDeactivationSuccess the smsDeactivationSuccess to set
	 */
	public void setSmsDeactivationSuccess(String smsDeactivationSuccess) {
		this.smsDeactivationSuccess = smsDeactivationSuccess;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SubscriptionClass clone() throws CloneNotSupportedException
	{
		return (SubscriptionClass) super.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SubscriptionClass[autoDeactivationPeriod = ");
		builder.append(autoDeactivationPeriod);
		builder.append(", chargeAfterSubscription = ");
		builder.append(chargeAfterSubscription);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", freeSelections = ");
		builder.append(freeSelections);
		builder.append(", gracePeriod = ");
		builder.append(gracePeriod);
		builder.append(", operatorCode1 = ");
		builder.append(operatorCode1);
		builder.append(", operatorCode2 = ");
		builder.append(operatorCode2);
		builder.append(", operatorCode3 = ");
		builder.append(operatorCode3);
		builder.append(", operatorCode4 = ");
		builder.append(operatorCode4);
		builder.append(", renewalAmount = ");
		builder.append(renewalAmount);
		builder.append(", renewalPeriod = ");
		builder.append(renewalPeriod);
		builder.append(", retryPeriod = ");
		builder.append(retryPeriod);
		builder.append(", showOnGui = ");
		builder.append(showOnGui);
		builder.append(", smsAlertBeforeRenewal = ");
		builder.append(smsAlertBeforeRenewal);
		builder.append(", smsAlertGrace = ");
		builder.append(smsAlertGrace);
		builder.append(", smsAlertRetry = ");
		builder.append(smsAlertRetry);
		builder.append(", smsDeactFailure = ");
		builder.append(smsDeactFailure);
		builder.append(", smsOnSubscription = ");
		builder.append(smsOnSubscription);
		builder.append(", smsOnSubscriptionFailure = ");
		builder.append(smsOnSubscriptionFailure);
		builder.append(", smsRenewalFailure = ");
		builder.append(smsRenewalFailure);
		builder.append(", smsRenewalSuccess = ");
		builder.append(smsRenewalSuccess);
		builder.append(", subscriptionAmount = ");
		builder.append(subscriptionAmount);
		builder.append(", subscriptionClass = ");
		builder.append(subscriptionClass);
		builder.append(", subscriptionPeriod = ");
		builder.append(subscriptionPeriod);
		builder.append(", subscriptionRenewal = ");
		builder.append(subscriptionRenewal);
		builder.append("]");
		return builder.toString();
	}
}
