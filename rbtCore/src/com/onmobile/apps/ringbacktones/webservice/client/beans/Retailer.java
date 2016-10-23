/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

/**
 * @author vinayasimha.patil
 *
 */
public class Retailer
{
	private String retailerID = null;
	private String name = null;
	private String type = null;

	/**
	 * 
	 */
	public Retailer()
	{

	}

	/**
	 * @param retailerID
	 * @param name
	 * @param type
	 */
	public Retailer(String retailerID, String name, String type)
	{
		this.retailerID = retailerID;
		this.name = name;
		this.type = type;
	}

	/**
	 * @return the retailerID
	 */
	public String getRetailerID()
	{
		return retailerID;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param retailerID the retailerID to set
	 */
	public void setRetailerID(String retailerID)
	{
		this.retailerID = retailerID;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((retailerID == null) ? 0 : retailerID.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (!(obj instanceof Retailer))
			return false;
		Retailer other = (Retailer) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (retailerID == null)
		{
			if (other.retailerID != null)
				return false;
		}
		else if (!retailerID.equals(other.retailerID))
			return false;
		if (type == null)
		{
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
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
		builder.append("Retailer[name = ");
		builder.append(name);
		builder.append(", retailerID = ");
		builder.append(retailerID);
		builder.append(", type = ");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}
}
