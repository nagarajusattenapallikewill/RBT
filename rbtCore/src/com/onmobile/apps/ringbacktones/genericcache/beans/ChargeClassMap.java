package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;

/**
 * ParametersBean used by hibernate to persist into the RBT_CHARGE_CLASS_MAP
 * table.
 * 
 * @author bikash.panda
 */
public class ChargeClassMap implements Serializable
{

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 26754387765432L;

	private String chargeClass; // Primary key
	private String regexSmsorVoice;
	private String accessMode;
	private String finalClasstype;

	/**
	 * 
	 */
	public ChargeClassMap()
	{

	}

	/**
	 * @param chargeClass
	 * @param regexSmsorVoice
	 * @param accessMode
	 * @param finalClasstype
	 */
	public ChargeClassMap(String chargeClass, String regexSmsorVoice,
			String accessMode, String finalClasstype)
	{
		this.chargeClass = chargeClass;
		this.regexSmsorVoice = regexSmsorVoice;
		this.accessMode = accessMode;
		this.finalClasstype = finalClasstype;
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
	 * @return the regexSmsorVoice
	 */
	public String getRegexSmsorVoice()
	{
		return regexSmsorVoice;
	}

	/**
	 * @param regexSmsorVoice the regexSmsorVoice to set
	 */
	public void setRegexSmsorVoice(String regexSmsorVoice)
	{
		this.regexSmsorVoice = regexSmsorVoice;
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
	 * @return the finalClasstype
	 */
	public String getFinalClasstype()
	{
		return finalClasstype;
	}

	/**
	 * @param finalClasstype the finalClasstype to set
	 */
	public void setFinalClasstype(String finalClasstype)
	{
		this.finalClasstype = finalClasstype;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ChargeClassMap[accessMode = ");
		builder.append(accessMode);
		builder.append(", chargeClass = ");
		builder.append(chargeClass);
		builder.append(", finalClasstype = ");
		builder.append(finalClasstype);
		builder.append(", regexSmsorVoice = ");
		builder.append(regexSmsorVoice);
		builder.append("]");
		return builder.toString();
	}
}
