package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;

/**
 * ParametersBean used by hibernate to persist into the RBT_CHARGE_CLASS table.
 * 
 * @author bikash.panda
 */
public class ChargeClass implements Serializable, Cloneable
{
	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 26754387765432L;

	private String chargeClass;
	private String amount;
	private String operatorCode;
	private String providerCode;
	private int freeSelection;
	private String selectionType;
	private String selectionPeriod;
	private String operatorCode1;
	private String operatorCode2;
	private String renewalPeriod;
	private String renewalAmount;
	private String smschargeSuccess;
	private String smschargeFailure;
	private String smsrenewalSuccess;
	private String smsrenewalFailure;
	private String showonGui;
	private String circleID;
	private String provisioningFlow;
	/**
	 * 
	 */
	public ChargeClass()
	{

	}

	/**
	 * @param chargeClass
	 * @param amount
	 * @param operatorCode
	 * @param providerCode
	 * @param freeSelection
	 * @param selectionType
	 * @param selectionPeriod
	 * @param operatorCode1
	 * @param operatorCode2
	 * @param renewalPeriod
	 * @param renewalAmount
	 * @param smschargeSuccess
	 * @param smschargeFailure
	 * @param smsrenewalSuccess
	 * @param smsrenewalFailure
	 * @param showonGui
	 * @param circleID
	 */
	public ChargeClass(String chargeClass, String amount, String operatorCode,
			String providerCode, int freeSelection, String selectionType,
			String selectionPeriod, String operatorCode1, String operatorCode2,
			String renewalPeriod, String renewalAmount,
			String smschargeSuccess, String smschargeFailure,
			String smsrenewalSuccess, String smsrenewalFailure,
			String showonGui, String circleID, String provisioningFlow)
	{
		this.chargeClass = chargeClass;
		this.amount = amount;
		this.operatorCode = operatorCode;
		this.providerCode = providerCode;
		this.freeSelection = freeSelection;
		this.selectionType = selectionType;
		this.selectionPeriod = selectionPeriod;
		this.operatorCode1 = operatorCode1;
		this.operatorCode2 = operatorCode2;
		this.renewalPeriod = renewalPeriod;
		this.renewalAmount = renewalAmount;
		this.smschargeSuccess = smschargeSuccess;
		this.smschargeFailure = smschargeFailure;
		this.smsrenewalSuccess = smsrenewalSuccess;
		this.smsrenewalFailure = smsrenewalFailure;
		this.showonGui = showonGui;
		this.circleID = circleID;
		this.provisioningFlow = provisioningFlow;
	}

	/**
	 * @return the serialversionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass()
	{
		return chargeClass;
	}

	/**
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass)
	{
		this.chargeClass = chargeClass;
	}

	/**
	 * @return the amount
	 */
	public String getAmount()
	{
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(String amount)
	{
		this.amount = amount;
	}

	/**
	 * @return the operatorCode
	 */
	public String getOperatorCode()
	{
		return operatorCode;
	}

	/**
	 * @param operatorCode the operatorCode to set
	 */
	public void setOperatorCode(String operatorCode)
	{
		this.operatorCode = operatorCode;
	}

	/**
	 * @return the providerCode
	 */
	public String getProviderCode()
	{
		return providerCode;
	}

	/**
	 * @param providerCode the providerCode to set
	 */
	public void setProviderCode(String providerCode)
	{
		this.providerCode = providerCode;
	}

	/**
	 * @return the freeSelection
	 */
	public int getFreeSelection()
	{
		return freeSelection;
	}

	/**
	 * @param freeSelection the freeSelection to set
	 */
	public void setFreeSelection(int freeSelection)
	{
		this.freeSelection = freeSelection;
	}

	/**
	 * @return the selectionType
	 */
	public String getSelectionType()
	{
		return selectionType;
	}

	/**
	 * @param selectionType the selectionType to set
	 */
	public void setSelectionType(String selectionType)
	{
		this.selectionType = selectionType;
	}

	/**
	 * @return the selectionPeriod
	 */
	public String getSelectionPeriod()
	{
		return selectionPeriod;
	}

	/**
	 * @param selectionPeriod the selectionPeriod to set
	 */
	public void setSelectionPeriod(String selectionPeriod)
	{
		this.selectionPeriod = selectionPeriod;
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
	 * @return the smschargeSuccess
	 */
	public String getSmschargeSuccess()
	{
		return smschargeSuccess;
	}

	/**
	 * @param smschargeSuccess the smschargeSuccess to set
	 */
	public void setSmschargeSuccess(String smschargeSuccess)
	{
		this.smschargeSuccess = smschargeSuccess;
	}

	/**
	 * @return the smschargeFailure
	 */
	public String getSmschargeFailure()
	{
		return smschargeFailure;
	}

	/**
	 * @param smschargeFailure the smschargeFailure to set
	 */
	public void setSmschargeFailure(String smschargeFailure)
	{
		this.smschargeFailure = smschargeFailure;
	}

	/**
	 * @return the smsrenewalSuccess
	 */
	public String getSmsrenewalSuccess()
	{
		return smsrenewalSuccess;
	}

	/**
	 * @param smsrenewalSuccess the smsrenewalSuccess to set
	 */
	public void setSmsrenewalSuccess(String smsrenewalSuccess)
	{
		this.smsrenewalSuccess = smsrenewalSuccess;
	}

	/**
	 * @return the smsrenewalFailure
	 */
	public String getSmsrenewalFailure()
	{
		return smsrenewalFailure;
	}

	/**
	 * @param smsrenewalFailure the smsrenewalFailure to set
	 */
	public void setSmsrenewalFailure(String smsrenewalFailure)
	{
		this.smsrenewalFailure = smsrenewalFailure;
	}

	/**
	 * @return the showonGui
	 */
	public String getShowonGui()
	{
		return showonGui;
	}

	/**
	 * @param showonGui the showonGui to set
	 */
	public void setShowonGui(String showonGui)
	{
		this.showonGui = showonGui;
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

	public boolean showOnGui()
	{
		if(showonGui != null)
			return showonGui.equalsIgnoreCase("y");
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ChargeClass clone() throws CloneNotSupportedException
	{
		return (ChargeClass) super.clone();
	}

	/**
	 * @return the selectionPeriodInDays
	 */
	public int getSelectionPeriodInDays()
	{
		if (selectionPeriod.startsWith("D"))
			return Integer.parseInt(selectionPeriod.substring(1));
		else if (selectionPeriod.startsWith("M"))
			return (Integer.parseInt(selectionPeriod.substring(1)) * 30);
		return Integer.parseInt(selectionPeriod);
	}

	public String getProvisioningFlow() {
		return provisioningFlow;
	}

	public void setProvisioningFlow(String provisioningFlow) {
		this.provisioningFlow = provisioningFlow;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ChargeClass[amount = ");
		builder.append(amount);
		builder.append(", chargeClass = ");
		builder.append(chargeClass);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", freeSelection = ");
		builder.append(freeSelection);
		builder.append(", operatorCode = ");
		builder.append(operatorCode);
		builder.append(", operatorCode1 = ");
		builder.append(operatorCode1);
		builder.append(", operatorCode2 = ");
		builder.append(operatorCode2);
		builder.append(", providerCode = ");
		builder.append(providerCode);
		builder.append(", renewalAmount = ");
		builder.append(renewalAmount);
		builder.append(", renewalPeriod = ");
		builder.append(renewalPeriod);
		builder.append(", selectionPeriod = ");
		builder.append(selectionPeriod);
		builder.append(", selectionType = ");
		builder.append(selectionType);
		builder.append(", showonGui = ");
		builder.append(showonGui);
		builder.append(", smschargeFailure = ");
		builder.append(smschargeFailure);
		builder.append(", smschargeSuccess = ");
		builder.append(smschargeSuccess);
		builder.append(", smsrenewalFailure = ");
		builder.append(smsrenewalFailure);
		builder.append(", smsrenewalSuccess = ");
		builder.append(smsrenewalSuccess);
		builder.append(", provisioningFlow = ");
		builder.append(provisioningFlow);
		builder.append("]");
		return builder.toString();
	}
}
