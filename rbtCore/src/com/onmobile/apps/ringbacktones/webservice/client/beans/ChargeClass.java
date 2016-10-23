/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

/**
 * @author vinayasimha.patil
 *
 */
public class ChargeClass
{
	private String chargeClass = null;
	private String amount = null;
	private String period = null;
	private String renewalAmount = null;
	private String renewalPeriod = null;
	private boolean showOnGUI;
	private String operatorCode1 = null;
	private String subscriptionAmount = null;
	private String subscriptionPeriod = null;

	/**
	 * 
	 */
	public ChargeClass()
	{

	}

	/**
	 * @param chargeClass
	 * @param amount
	 * @param period
	 * @param renewalAmount
	 * @param renewalPeriod
	 * @param showOnGUI
	 * @param operatorCode1
	 */
	public ChargeClass(String chargeClass, String amount, String period,
			String renewalAmount, String renewalPeriod, boolean showOnGUI,
			String operatorCode1)
	{
		this.chargeClass = chargeClass;
		this.amount = amount;
		this.period = period;
		this.renewalAmount = renewalAmount;
		this.renewalPeriod = renewalPeriod;
		this.showOnGUI = showOnGUI;
		this.operatorCode1 = operatorCode1;
	}

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass()
	{
		return chargeClass;
	}

	/**
	 * @return the amount
	 */
	public String getAmount()
	{
		return amount;
	}

	/**
	 * @return the period
	 */
	public String getPeriod()
	{
		return period;
	}

	/**
	 * @return the renewalAmount
	 */
	public String getRenewalAmount()
	{
		return renewalAmount;
	}

	/**
	 * @return the renewalPeriod
	 */
	public String getRenewalPeriod()
	{
		return renewalPeriod;
	}

	/**
	 * @return the showOnGUI
	 */
	public boolean isShowOnGUI()
	{
		return showOnGUI;
	}

	/**
	 * @return the operatorCode1
	 */
	public String getOperatorCode1()
	{
		return operatorCode1;
	}

	/**
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass)
	{
		this.chargeClass = chargeClass;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(String amount)
	{
		this.amount = amount;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(String period)
	{
		this.period = period;
	}

	/**
	 * @param renewalAmount the renewalAmount to set
	 */
	public void setRenewalAmount(String renewalAmount)
	{
		this.renewalAmount = renewalAmount;
	}

	/**
	 * @param renewalPeriod the renewalPeriod to set
	 */
	public void setRenewalPeriod(String renewalPeriod)
	{
		this.renewalPeriod = renewalPeriod;
	}

	/**
	 * @param showOnGUI the showOnGUI to set
	 */
	public void setShowOnGUI(boolean showOnGUI)
	{
		this.showOnGUI = showOnGUI;
	}

	/**
	 * @param operatorCode1 the operatorCode1 to set
	 */
	public void setOperatorCode1(String operatorCode1)
	{
		this.operatorCode1 = operatorCode1;
	}
	public String getSubscriptionAmount() {
		return subscriptionAmount;
	}

	public void setSubscriptionAmount(String subscriptionAmount) {
		this.subscriptionAmount = subscriptionAmount;
	}

	public String getSubscriptionPeriod() {
		return subscriptionPeriod;
	}

	public void setSubscriptionPeriod(String subscriptionPeriod) {
		this.subscriptionPeriod = subscriptionPeriod;
	}
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((chargeClass == null) ? 0 : chargeClass.hashCode());
		result = prime * result + ((operatorCode1 == null) ? 0 : operatorCode1.hashCode());
		result = prime * result + ((period == null) ? 0 : period.hashCode());
		result = prime * result + ((renewalAmount == null) ? 0 : renewalAmount.hashCode());
		result = prime * result + ((renewalPeriod == null) ? 0 : renewalPeriod.hashCode());
		result = prime * result + ((subscriptionAmount == null) ? 0 : subscriptionAmount.hashCode());
		result = prime * result + ((subscriptionPeriod == null) ? 0 : subscriptionPeriod.hashCode());
		result = prime * result + (showOnGUI ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ChargeClass))
			return false;
		ChargeClass other = (ChargeClass) obj;
		if (amount == null)
		{
			if (other.amount != null)
				return false;
		}
		else if (!amount.equals(other.amount))
			return false;
		if (chargeClass == null)
		{
			if (other.chargeClass != null)
				return false;
		}
		else if (!chargeClass.equals(other.chargeClass))
			return false;
		if (operatorCode1 == null)
		{
			if (other.operatorCode1 != null)
				return false;
		}
		else if (!operatorCode1.equals(other.operatorCode1))
			return false;
		if (period == null)
		{
			if (other.period != null)
				return false;
		}
		else if (!period.equals(other.period))
			return false;
		if (renewalAmount == null)
		{
			if (other.renewalAmount != null)
				return false;
		}
		else if (!renewalAmount.equals(other.renewalAmount))
			return false;
		if (renewalPeriod == null)
		{
			if (other.renewalPeriod != null)
				return false;
		}
		else if (!renewalPeriod.equals(other.renewalPeriod))
			return false;
		if (showOnGUI != other.showOnGUI)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ChargeClass[amount = ");
		builder.append(amount);
		builder.append(", chargeClass = ");
		builder.append(chargeClass);
		builder.append(", operatorCode1 = ");
		builder.append(operatorCode1);
		builder.append(", period = ");
		builder.append(period);
		builder.append(", renewalAmount = ");
		builder.append(renewalAmount);
		builder.append(", renewalPeriod = ");
		builder.append(renewalPeriod);
		builder.append(", showOnGUI = ");
		builder.append(showOnGUI);
		builder.append("]");
		return builder.toString();
	}
}
