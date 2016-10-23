/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

/**
 * @author vinayasimha.patil
 *
 */
public class SubscriptionClass
{
	String subscriptionClass = null;
	String amount = null;
	String period = null;
	String renewalAmount = null;
	String renewalPeriod = null;
	boolean showOnGUI;

	/**
	 * 
	 */
	public SubscriptionClass()
	{

	}

	/**
	 * @param subscriptionClass
	 * @param amount
	 * @param period
	 * @param renewalAmount
	 * @param renewalPeriod
	 * @param showOnGUI
	 */
	public SubscriptionClass(String subscriptionClass, String amount,
			String period, String renewalAmount, String renewalPeriod,
			boolean showOnGUI)
	{
		this.subscriptionClass = subscriptionClass;
		this.amount = amount;
		this.period = period;
		this.renewalAmount = renewalAmount;
		this.renewalPeriod = renewalPeriod;
		this.showOnGUI = showOnGUI;
	}

	/**
	 * @return the subscriptionClass
	 */
	public String getSubscriptionClass()
	{
		return subscriptionClass;
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
	 * @param subscriptionClass the subscriptionClass to set
	 */
	public void setSubscriptionClass(String subscriptionClass)
	{
		this.subscriptionClass = subscriptionClass;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((period == null) ? 0 : period.hashCode());
		result = prime * result + ((renewalAmount == null) ? 0 : renewalAmount.hashCode());
		result = prime * result + ((renewalPeriod == null) ? 0 : renewalPeriod.hashCode());
		result = prime * result + (showOnGUI ? 1231 : 1237);
		result = prime * result + ((subscriptionClass == null) ? 0 : subscriptionClass.hashCode());
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
		if (!(obj instanceof SubscriptionClass))
			return false;
		SubscriptionClass other = (SubscriptionClass) obj;
		if (amount == null)
		{
			if (other.amount != null)
				return false;
		}
		else if (!amount.equals(other.amount))
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
		if (subscriptionClass == null)
		{
			if (other.subscriptionClass != null)
				return false;
		}
		else if (!subscriptionClass.equals(other.subscriptionClass))
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
		builder.append("SubscriptionClass[amount = ");
		builder.append(amount);
		builder.append(", period = ");
		builder.append(period);
		builder.append(", renewalAmount = ");
		builder.append(renewalAmount);
		builder.append(", renewalPeriod = ");
		builder.append(renewalPeriod);
		builder.append(", showOnGUI = ");
		builder.append(showOnGUI);
		builder.append(", subscriptionClass = ");
		builder.append(subscriptionClass);
		builder.append("]");
		return builder.toString();
	}
}
