package com.onmobile.apps.ringbacktones.genericcache.beans;

public class ChargeClassBean
{
	private String chargeName;
	private String chargeVal;

	/**
	 * 
	 */
	public ChargeClassBean()
	{

	}

	/**
	 * @param chargeName
	 * @param chargeVal
	 */
	public ChargeClassBean(String chargeName, String chargeVal)
	{
		this.chargeName = chargeName;
		this.chargeVal = chargeVal;
	}

	/**
	 * @return the chargeName
	 */
	public String getChargeName()
	{
		return chargeName;
	}

	/**
	 * @param chargeName the chargeName to set
	 */
	public void setChargeName(String chargeName)
	{
		this.chargeName = chargeName;
	}

	/**
	 * @return the chargeVal
	 */
	public String getChargeVal()
	{
		return chargeVal;
	}

	/**
	 * @param chargeVal the chargeVal to set
	 */
	public void setChargeVal(String chargeVal)
	{
		this.chargeVal = chargeVal;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ChargeClassBean[chargeName = ");
		builder.append(chargeName);
		builder.append(", chargeVal = ");
		builder.append(chargeVal);
		builder.append("]");
		return builder.toString();
	}
}
