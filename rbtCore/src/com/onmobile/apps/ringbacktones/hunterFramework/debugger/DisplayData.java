/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

/**
 * A key-value data. DisplayData is used as data holder for the details to be
 * displayed in the Client CLI.
 * 
 * @author vinayasimha.patil
 * @see CLIContext#writeNextRow(java.util.List)
 */
public class DisplayData
{
	/**
	 * Holds the key of the data.
	 */
	private String key = null;

	/**
	 * Holds the value of the data.
	 */
	private String value = null;

	/**
	 * Constructs the DisplayData with <tt>key</tt> and <tt>key</tt>.
	 * 
	 * @param key
	 *            key of the data
	 * @param value
	 *            value of the data
	 */
	public DisplayData(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	/**
	 * Returns the key of the data.
	 * 
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * Sets the key of the data.
	 * 
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key)
	{
		this.key = key;
	}

	/**
	 * Returns the value of the data.
	 * 
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Sets the value of the data.
	 * 
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * Returns the string representation of this class.
	 * 
	 * @return the string representation of this class
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DisplayData[key = ");
		builder.append(key);
		builder.append(", value = ");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}
