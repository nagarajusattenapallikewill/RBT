/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;

/**
 * @author vinayasimha.patil
 *
 */
public class ChargeSMS implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6962003109142773067L;

	private String chargeClass;
	private String classType;
	private String language;
	private String prepaidSuccess;
	private String prepaidFailure;
	private String postpaidSuccess;
	private String postpaidFailure;
	private String prepaidNEFSuccess;
	private String prepaidRenewalSuccess;
	private String prepaidRenewalFailure;
	private String postpaidRenewalSuccess;
	private String postpaidRenewalFailure;

	/**
	 * 
	 */
	public ChargeSMS()
	{

	}

	/**
	 * @param chargeClass
	 * @param classType
	 * @param language
	 * @param prepaidSuccess
	 * @param prepaidFailure
	 * @param postpaidSuccess
	 * @param postpaidFailure
	 * @param prepaidNEFSuccess
	 * @param prepaidRenewalSuccess
	 * @param prepaidRenewalFailure
	 * @param postpaidRenewalSuccess
	 * @param postpaidRenewalFailure
	 */
	public ChargeSMS(String chargeClass, String classType, String language,
			String prepaidSuccess, String prepaidFailure,
			String postpaidSuccess, String postpaidFailure,
			String prepaidNEFSuccess, String prepaidRenewalSuccess,
			String prepaidRenewalFailure, String postpaidRenewalSuccess,
			String postpaidRenewalFailure)
	{
		this.chargeClass = chargeClass;
		this.classType = classType;
		this.language = language;
		this.prepaidSuccess = prepaidSuccess;
		this.prepaidFailure = prepaidFailure;
		this.postpaidSuccess = postpaidSuccess;
		this.postpaidFailure = postpaidFailure;
		this.prepaidNEFSuccess = prepaidNEFSuccess;
		this.prepaidRenewalSuccess = prepaidRenewalSuccess;
		this.prepaidRenewalFailure = prepaidRenewalFailure;
		this.postpaidRenewalSuccess = postpaidRenewalSuccess;
		this.postpaidRenewalFailure = postpaidRenewalFailure;
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
	 * @return the classType
	 */
	public String getClassType()
	{
		return classType;
	}

	/**
	 * @param classType the classType to set
	 */
	public void setClassType(String classType)
	{
		this.classType = classType;
	}

	/**
	 * @return the language
	 */
	public String getLanguage()
	{
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * @return the prepaidSuccess
	 */
	public String getPrepaidSuccess()
	{
		return prepaidSuccess;
	}

	/**
	 * @param prepaidSuccess the prepaidSuccess to set
	 */
	public void setPrepaidSuccess(String prepaidSuccess)
	{
		this.prepaidSuccess = prepaidSuccess;
	}

	/**
	 * @return the prepaidFailure
	 */
	public String getPrepaidFailure()
	{
		return prepaidFailure;
	}

	/**
	 * @param prepaidFailure the prepaidFailure to set
	 */
	public void setPrepaidFailure(String prepaidFailure)
	{
		this.prepaidFailure = prepaidFailure;
	}

	/**
	 * @return the postpaidSuccess
	 */
	public String getPostpaidSuccess()
	{
		return postpaidSuccess;
	}

	/**
	 * @param postpaidSuccess the postpaidSuccess to set
	 */
	public void setPostpaidSuccess(String postpaidSuccess)
	{
		this.postpaidSuccess = postpaidSuccess;
	}

	/**
	 * @return the postpaidFailure
	 */
	public String getPostpaidFailure()
	{
		return postpaidFailure;
	}

	/**
	 * @param postpaidFailure the postpaidFailure to set
	 */
	public void setPostpaidFailure(String postpaidFailure)
	{
		this.postpaidFailure = postpaidFailure;
	}

	/**
	 * @return the prepaidNEFSuccess
	 */
	public String getPrepaidNEFSuccess()
	{
		return prepaidNEFSuccess;
	}

	/**
	 * @param prepaidNEFSuccess the prepaidNEFSuccess to set
	 */
	public void setPrepaidNEFSuccess(String prepaidNEFSuccess)
	{
		this.prepaidNEFSuccess = prepaidNEFSuccess;
	}

	/**
	 * @return the prepaidRenewalSuccess
	 */
	public String getPrepaidRenewalSuccess()
	{
		return prepaidRenewalSuccess;
	}

	/**
	 * @param prepaidRenewalSuccess the prepaidRenewalSuccess to set
	 */
	public void setPrepaidRenewalSuccess(String prepaidRenewalSuccess)
	{
		this.prepaidRenewalSuccess = prepaidRenewalSuccess;
	}

	/**
	 * @return the prepaidRenewalFailure
	 */
	public String getPrepaidRenewalFailure()
	{
		return prepaidRenewalFailure;
	}

	/**
	 * @param prepaidRenewalFailure the prepaidRenewalFailure to set
	 */
	public void setPrepaidRenewalFailure(String prepaidRenewalFailure)
	{
		this.prepaidRenewalFailure = prepaidRenewalFailure;
	}

	/**
	 * @return the postpaidRenewalSuccess
	 */
	public String getPostpaidRenewalSuccess()
	{
		return postpaidRenewalSuccess;
	}

	/**
	 * @param postpaidRenewalSuccess the postpaidRenewalSuccess to set
	 */
	public void setPostpaidRenewalSuccess(String postpaidRenewalSuccess)
	{
		this.postpaidRenewalSuccess = postpaidRenewalSuccess;
	}

	/**
	 * @return the postpaidRenewalFailure
	 */
	public String getPostpaidRenewalFailure()
	{
		return postpaidRenewalFailure;
	}

	/**
	 * @param postpaidRenewalFailure the postpaidRenewalFailure to set
	 */
	public void setPostpaidRenewalFailure(String postpaidRenewalFailure)
	{
		this.postpaidRenewalFailure = postpaidRenewalFailure;
	}

	/**
	 * @return the serialversionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ChargeSMS[chargeClass = ");
		builder.append(chargeClass);
		builder.append(", classType = ");
		builder.append(classType);
		builder.append(", language = ");
		builder.append(language);
		builder.append(", postpaidFailure = ");
		builder.append(postpaidFailure);
		builder.append(", postpaidRenewalFailure = ");
		builder.append(postpaidRenewalFailure);
		builder.append(", postpaidRenewalSuccess = ");
		builder.append(postpaidRenewalSuccess);
		builder.append(", postpaidSuccess = ");
		builder.append(postpaidSuccess);
		builder.append(", prepaidFailure = ");
		builder.append(prepaidFailure);
		builder.append(", prepaidNEFSuccess = ");
		builder.append(prepaidNEFSuccess);
		builder.append(", prepaidRenewalFailure = ");
		builder.append(prepaidRenewalFailure);
		builder.append(", prepaidRenewalSuccess = ");
		builder.append(prepaidRenewalSuccess);
		builder.append(", prepaidSuccess = ");
		builder.append(prepaidSuccess);
		builder.append("]");
		return builder.toString();
	}
}
