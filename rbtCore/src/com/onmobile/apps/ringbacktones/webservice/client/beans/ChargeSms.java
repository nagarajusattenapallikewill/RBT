/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

/**
 * @author vinayasimha.patil
 *
 */
public class ChargeSms
{
	private String className = null;
	private String classType = null;
	private String prepaidSuccessSms = null;
	private String prepaidFailureSms = null;
	private String postpaidSuccessSms = null;
	private String postpaidFailureSms = null;
	private String prepaidNEFSuccessSms = null;
	private String prepaidRenewalSuccessSms = null;
	private String prepaidRenewalFailureSms = null;
	private String postpaidRenewalSuccessSms = null;
	private String postpaidRenewalFailureSms = null;

	/**
	 * 
	 */
	public ChargeSms()
	{

	}

	/**
	 * @param className
	 * @param classType
	 * @param prepaidSuccessSms
	 * @param prepaidFailureSms
	 * @param postpaidSuccessSms
	 * @param postpaidFailureSms
	 * @param prepaidNEFSuccessSms
	 * @param prepaidRenewalSuccessSms
	 * @param prepaidRenewalFailureSms
	 * @param postpaidRenewalSuccessSms
	 * @param postpaidRenewalFailureSms
	 */
	public ChargeSms(String className, String classType,
			String prepaidSuccessSms, String prepaidFailureSms,
			String postpaidSuccessSms, String postpaidFailureSms,
			String prepaidNEFSuccessSms, String prepaidRenewalSuccessSms,
			String prepaidRenewalFailureSms, String postpaidRenewalSuccessSms,
			String postpaidRenewalFailureSms)
	{
		this.className = className;
		this.classType = classType;
		this.prepaidSuccessSms = prepaidSuccessSms;
		this.prepaidFailureSms = prepaidFailureSms;
		this.postpaidSuccessSms = postpaidSuccessSms;
		this.postpaidFailureSms = postpaidFailureSms;
		this.prepaidNEFSuccessSms = prepaidNEFSuccessSms;
		this.prepaidRenewalSuccessSms = prepaidRenewalSuccessSms;
		this.prepaidRenewalFailureSms = prepaidRenewalFailureSms;
		this.postpaidRenewalSuccessSms = postpaidRenewalSuccessSms;
		this.postpaidRenewalFailureSms = postpaidRenewalFailureSms;
	}

	/**
	 * @return the className
	 */
	public String getClassName()
	{
		return className;
	}

	/**
	 * @return the classType
	 */
	public String getClassType()
	{
		return classType;
	}

	/**
	 * @return the prepaidSuccessSms
	 */
	public String getPrepaidSuccessSms()
	{
		return prepaidSuccessSms;
	}

	/**
	 * @return the prepaidFailureSms
	 */
	public String getPrepaidFailureSms()
	{
		return prepaidFailureSms;
	}

	/**
	 * @return the postpaidSuccessSms
	 */
	public String getPostpaidSuccessSms()
	{
		return postpaidSuccessSms;
	}

	/**
	 * @return the postpaidFailureSms
	 */
	public String getPostpaidFailureSms()
	{
		return postpaidFailureSms;
	}

	/**
	 * @return the prepaidNEFSuccessSms
	 */
	public String getPrepaidNEFSuccessSms()
	{
		return prepaidNEFSuccessSms;
	}

	/**
	 * @return the prepaidRenewalSuccessSms
	 */
	public String getPrepaidRenewalSuccessSms()
	{
		return prepaidRenewalSuccessSms;
	}

	/**
	 * @return the prepaidRenewalFailureSms
	 */
	public String getPrepaidRenewalFailureSms()
	{
		return prepaidRenewalFailureSms;
	}

	/**
	 * @return the postpaidRenewalSuccessSms
	 */
	public String getPostpaidRenewalSuccessSms()
	{
		return postpaidRenewalSuccessSms;
	}

	/**
	 * @return the postpaidRenewalFailureSms
	 */
	public String getPostpaidRenewalFailureSms()
	{
		return postpaidRenewalFailureSms;
	}

	/**
	 * @param className the className to set
	 */
	public void setClassName(String className)
	{
		this.className = className;
	}

	/**
	 * @param classType the classType to set
	 */
	public void setClassType(String classType)
	{
		this.classType = classType;
	}

	/**
	 * @param prepaidSuccessSms the prepaidSuccessSms to set
	 */
	public void setPrepaidSuccessSms(String prepaidSuccessSms)
	{
		this.prepaidSuccessSms = prepaidSuccessSms;
	}

	/**
	 * @param prepaidFailureSms the prepaidFailureSms to set
	 */
	public void setPrepaidFailureSms(String prepaidFailureSms)
	{
		this.prepaidFailureSms = prepaidFailureSms;
	}

	/**
	 * @param postpaidSuccessSms the postpaidSuccessSms to set
	 */
	public void setPostpaidSuccessSms(String postpaidSuccessSms)
	{
		this.postpaidSuccessSms = postpaidSuccessSms;
	}

	/**
	 * @param postpaidFailureSms the postpaidFailureSms to set
	 */
	public void setPostpaidFailureSms(String postpaidFailureSms)
	{
		this.postpaidFailureSms = postpaidFailureSms;
	}

	/**
	 * @param prepaidNEFSuccessSms the prepaidNEFSuccessSms to set
	 */
	public void setPrepaidNEFSuccessSms(String prepaidNEFSuccessSms)
	{
		this.prepaidNEFSuccessSms = prepaidNEFSuccessSms;
	}

	/**
	 * @param prepaidRenewalSuccessSms the prepaidRenewalSuccessSms to set
	 */
	public void setPrepaidRenewalSuccessSms(String prepaidRenewalSuccessSms)
	{
		this.prepaidRenewalSuccessSms = prepaidRenewalSuccessSms;
	}

	/**
	 * @param prepaidRenewalFailureSms the prepaidRenewalFailureSms to set
	 */
	public void setPrepaidRenewalFailureSms(String prepaidRenewalFailureSms)
	{
		this.prepaidRenewalFailureSms = prepaidRenewalFailureSms;
	}

	/**
	 * @param postpaidRenewalSuccessSms the postpaidRenewalSuccessSms to set
	 */
	public void setPostpaidRenewalSuccessSms(String postpaidRenewalSuccessSms)
	{
		this.postpaidRenewalSuccessSms = postpaidRenewalSuccessSms;
	}

	/**
	 * @param postpaidRenewalFailureSms the postpaidRenewalFailureSms to set
	 */
	public void setPostpaidRenewalFailureSms(String postpaidRenewalFailureSms)
	{
		this.postpaidRenewalFailureSms = postpaidRenewalFailureSms;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((classType == null) ? 0 : classType.hashCode());
		result = prime * result + ((postpaidFailureSms == null) ? 0 : postpaidFailureSms.hashCode());
		result = prime * result + ((postpaidRenewalFailureSms == null) ? 0 : postpaidRenewalFailureSms.hashCode());
		result = prime * result + ((postpaidRenewalSuccessSms == null) ? 0 : postpaidRenewalSuccessSms.hashCode());
		result = prime * result + ((postpaidSuccessSms == null) ? 0 : postpaidSuccessSms.hashCode());
		result = prime * result + ((prepaidFailureSms == null) ? 0 : prepaidFailureSms.hashCode());
		result = prime * result + ((prepaidNEFSuccessSms == null) ? 0 : prepaidNEFSuccessSms.hashCode());
		result = prime * result + ((prepaidRenewalFailureSms == null) ? 0 : prepaidRenewalFailureSms.hashCode());
		result = prime * result + ((prepaidRenewalSuccessSms == null) ? 0 : prepaidRenewalSuccessSms.hashCode());
		result = prime * result + ((prepaidSuccessSms == null) ? 0 : prepaidSuccessSms.hashCode());
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
		if (!(obj instanceof ChargeSms))
			return false;
		ChargeSms other = (ChargeSms) obj;
		if (className == null)
		{
			if (other.className != null)
				return false;
		}
		else if (!className.equals(other.className))
			return false;
		if (classType == null)
		{
			if (other.classType != null)
				return false;
		}
		else if (!classType.equals(other.classType))
			return false;
		if (postpaidFailureSms == null)
		{
			if (other.postpaidFailureSms != null)
				return false;
		}
		else if (!postpaidFailureSms.equals(other.postpaidFailureSms))
			return false;
		if (postpaidRenewalFailureSms == null)
		{
			if (other.postpaidRenewalFailureSms != null)
				return false;
		}
		else if (!postpaidRenewalFailureSms
				.equals(other.postpaidRenewalFailureSms))
			return false;
		if (postpaidRenewalSuccessSms == null)
		{
			if (other.postpaidRenewalSuccessSms != null)
				return false;
		}
		else if (!postpaidRenewalSuccessSms
				.equals(other.postpaidRenewalSuccessSms))
			return false;
		if (postpaidSuccessSms == null)
		{
			if (other.postpaidSuccessSms != null)
				return false;
		}
		else if (!postpaidSuccessSms.equals(other.postpaidSuccessSms))
			return false;
		if (prepaidFailureSms == null)
		{
			if (other.prepaidFailureSms != null)
				return false;
		}
		else if (!prepaidFailureSms.equals(other.prepaidFailureSms))
			return false;
		if (prepaidNEFSuccessSms == null)
		{
			if (other.prepaidNEFSuccessSms != null)
				return false;
		}
		else if (!prepaidNEFSuccessSms.equals(other.prepaidNEFSuccessSms))
			return false;
		if (prepaidRenewalFailureSms == null)
		{
			if (other.prepaidRenewalFailureSms != null)
				return false;
		}
		else if (!prepaidRenewalFailureSms
				.equals(other.prepaidRenewalFailureSms))
			return false;
		if (prepaidRenewalSuccessSms == null)
		{
			if (other.prepaidRenewalSuccessSms != null)
				return false;
		}
		else if (!prepaidRenewalSuccessSms
				.equals(other.prepaidRenewalSuccessSms))
			return false;
		if (prepaidSuccessSms == null)
		{
			if (other.prepaidSuccessSms != null)
				return false;
		}
		else if (!prepaidSuccessSms.equals(other.prepaidSuccessSms))
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
		builder.append("ChargeSms[className = ");
		builder.append(className);
		builder.append(", classType = ");
		builder.append(classType);
		builder.append(", postpaidFailureSms = ");
		builder.append(postpaidFailureSms);
		builder.append(", postpaidRenewalFailureSms = ");
		builder.append(postpaidRenewalFailureSms);
		builder.append(", postpaidRenewalSuccessSms = ");
		builder.append(postpaidRenewalSuccessSms);
		builder.append(", postpaidSuccessSms = ");
		builder.append(postpaidSuccessSms);
		builder.append(", prepaidFailureSms = ");
		builder.append(prepaidFailureSms);
		builder.append(", prepaidNEFSuccessSms = ");
		builder.append(prepaidNEFSuccessSms);
		builder.append(", prepaidRenewalFailureSms = ");
		builder.append(prepaidRenewalFailureSms);
		builder.append(", prepaidRenewalSuccessSms = ");
		builder.append(prepaidRenewalSuccessSms);
		builder.append(", prepaidSuccessSms = ");
		builder.append(prepaidSuccessSms);
		builder.append("]");
		return builder.toString();
	}
}
